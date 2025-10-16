package com.team.gym.service;

import com.team.gym.dto.BookingResponse;
import com.team.gym.errors.Unauthorized;
import com.team.gym.model.Booking;
import com.team.gym.model.ClassSession;
import com.team.gym.model.User;
import com.team.gym.repository.BookingRepository;
import com.team.gym.repository.ClassSessionRepository;
import com.team.gym.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private Long requireUid(HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) throw new Unauthorized(); // your project already has this
        return uid;
    }

    @Transactional
    public BookingResponse book(Long classId, HttpSession session) {
        Long uid = requireUid(session);

        ClassSession cs = classRepo.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("not_found"));

        // 1) already booked the same class?
        if (bookingRepo.existsByUserIdAndClassSessionId(uid, classId)) {
            throw new IllegalStateException("already_booked");
        }

        // 2) capacity check
        long taken = bookingRepo.countByClassSessionId(classId);
        if (taken >= cs.getCapacity()) {
            throw new IllegalStateException("class_full");
        }

        // 3) time conflict with any existing booking of this user (overlap)
        List<Booking> mine = bookingRepo.findByUserIdOrderByClassSessionStartAtAsc(uid);
        Instant start = cs.getStartAt();
        Instant end   = cs.getEndAt();
        boolean conflict = mine.stream().anyMatch(b ->
                b.getClassSession().getStartAt().isBefore(end) &&
                        b.getClassSession().getEndAt().isAfter(start));
        if (conflict) {
            throw new IllegalStateException("time_conflict");
        }

        // 4) create booking
        User user = userRepo.findById(uid).orElseThrow(() -> new IllegalArgumentException("not_found"));
        Booking b = new Booking();
        b.setUser(user);
        b.setClassSession(cs);
        Booking saved = bookingRepo.save(b);

        return new BookingResponse(
                saved.getId(),
                cs.getId(),
                cs.getType(),
                cs.getStartAt(),
                cs.getEndAt()
        );
    }

    @Transactional
    public void cancel(Long classId, HttpSession session) {
        Long uid = requireUid(session);
        // find the specific booking of this user for this class
        List<Booking> mine = bookingRepo.findByUserIdOrderByClassSessionStartAtAsc(uid);
        Booking toDelete = mine.stream()
                .filter(b -> b.getClassSession().getId().equals(classId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("not_found")); // no booking found

        bookingRepo.delete(toDelete);
    }
}
