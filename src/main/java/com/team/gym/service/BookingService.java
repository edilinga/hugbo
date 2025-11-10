package com.team.gym.service;

import com.team.gym.dto.BookingResponse;
import com.team.gym.errors.Unauthorized;
import com.team.gym.model.*;
import com.team.gym.repository.BookingRepository;
import com.team.gym.repository.ClassSessionRepository;
import com.team.gym.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final ClassSessionRepository classRepo;
    private final UserRepository userRepo;

    public BookingService(BookingRepository bookingRepo,
                          ClassSessionRepository classRepo,
                          UserRepository userRepo) {
        this.bookingRepo = bookingRepo;
        this.classRepo = classRepo;
        this.userRepo = userRepo;
    }

    /**
     * Sækir auðkennisnúmer notanda úr núverandi session
     *
     * @param session núverandi HTTP session
     * @return auðkenni notanda sem geymt er í session
     * @throws Unauthorized ef ekkert auðkenni finnst í session
     */
    private Long requireUid(HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) throw new Unauthorized();
        return uid;
    }

    /**
     * UC1 - bóka tíma (staðfest bókun, ekki biðlisti)
     *
     * @param classId auðkenni tíma sem á að bóka
     * @param session núverandi HTTP session sem inniheldur innskráðan notanda
     * @return hlekkur {@link BookingResponse} sem innheldur upplýsingar um bókun
     * @throws ResponseStatusException með status 404 ef tími eða notandi finnst ekki
     * @throws ResponseStatusException með status 409 ef notandi hefur nú þegar bókað, tíminn er fullur, eða tími stangast á
     */
    @Transactional
    public BookingResponse book(Long classId, HttpSession session) {
        Long uid = requireUid(session);

        ClassSession cs = classRepo.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "class_not_found"));

        if (bookingRepo.existsByUserIdAndClassSessionId(uid, classId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "already_booked");
        }

        // Teljum bara CONFIRMED bókanir, ekki WAITLISTED/CANCELLED
        long confirmed = bookingRepo.countByClassSessionIdAndStatus(classId, BookingStatus.CONFIRMED);
        if (confirmed >= cs.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "class_full");
        }

        // time overlap: aðeins confirmed tímar eiga að blokka
        List<Booking> mine = bookingRepo.findByUserIdOrderByClassSessionStartAtAsc(uid);
        Instant start = cs.getStartAt(), end = cs.getEndAt();
        boolean conflict = mine.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .anyMatch(b ->
                        b.getClassSession().getStartAt().isBefore(end) &&
                                b.getClassSession().getEndAt().isAfter(start)
                );
        if (conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "time_conflict");
        }

        User user = userRepo.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user_not_found"));

        Booking b = new Booking();
        b.setUser(user);
        b.setClassSession(cs);
        b.setStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepo.save(b);

        return new BookingResponse(
                saved.getId(),
                cs.getId(),
                cs.getType(),
                cs.getStartAt(),
                cs.getEndAt()
        );
    }

    /**
     * UC3 - Skrá á biðlista
     *
     * @param classId auðkenni tíma sem á að skrá á biðlista fyrir
     * @param session núverandi HTTP session sem inniheldur innskráðan notanda
     * @return hlekkur {@link BookingResponse} sem inniheldur upplýsingar um biðlista
     * @throws ResponseStatusException með status 404 ef tími eða notandi finnst ekki
     * @throws ResponseStatusException með status 409 ef notandi hefur nú þegar bókað eða tíminn er ekki fullur
     */
    @Transactional
    public BookingResponse joinWaitlist(Long classId, HttpSession session) {
        Long uid = requireUid(session);

        ClassSession cs = classRepo.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "class_not_found"));

        // má ekki vera þegar bókaður eða á biðlista í þessum tíma
        if (bookingRepo.existsByUserIdAndClassSessionId(uid, classId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "already_booked_or_waitlisted");
        }

        // aðeins leyfa biðlista ef tíminn er virkilega fullur
        long confirmed = bookingRepo.countByClassSessionIdAndStatus(classId, BookingStatus.CONFIRMED);
        if (confirmed < cs.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "class_not_full");
        }

        User user = userRepo.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user_not_found"));

        Booking b = new Booking();
        b.setUser(user);
        b.setClassSession(cs);
        b.setStatus(BookingStatus.WAITLISTED);

        Booking saved = bookingRepo.save(b);

        return new BookingResponse(
                saved.getId(),
                cs.getId(),
                cs.getType(),
                cs.getStartAt(),
                cs.getEndAt()
        );
    }

    /**
     * UC2 - afbóka tíma (og hækka af biðlista ef sæti losnar)
     *
     * @param userId auðkenni notanda sem ætlar sér að afbóka
     * @param classId auðkenni tímans sem notandi ætlar sér að afbóka
     * @throws ResponseStatusException með status 404 ef engin bókun á gefnum tíma finnst fyrir gefin notanda
     * @throws ResponseStatusException með status 409 ef það er of seint að afbóka
     */
    @Transactional
    public void cancel(Long userId, Long classId) {
        Booking booking = bookingRepo.findByUserIdAndClassSessionId(userId, classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not_found"));

        ClassSession cs = booking.getClassSession();

        // 2 klst cutoff bara ef bókun er CONFIRMED
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            Instant now = Instant.now();
            if (cs.getStartAt() != null &&
                    now.isAfter(cs.getStartAt().minus(Duration.ofHours(2)))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "too_late_to_cancel");
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        bookingRepo.save(booking);

        promoteFromWaitlistIfSeatFree(cs);
    }

    /**
     * Færir af biðlista í tíma
     *
     * @param cs tími sem athuga skal hvort sé tómur og þá flytja af biðlista í tíma
     */
    private void promoteFromWaitlistIfSeatFree(ClassSession cs) {
        long confirmed = bookingRepo.countByClassSessionIdAndStatus(cs.getId(), BookingStatus.CONFIRMED);
        if (confirmed >= cs.getCapacity()) {
            return; // ekkert sæti laust
        }

        List<Booking> waitlisted = bookingRepo
                .findByClassSessionIdAndStatusOrderByCreatedAtAsc(cs.getId(), BookingStatus.WAITLISTED);

        if (waitlisted.isEmpty()) {
            return;
        }

        Booking next = waitlisted.get(0);
        next.setStatus(BookingStatus.CONFIRMED);
        next.setCancelledAt(null);
        bookingRepo.save(next);

    }
}
