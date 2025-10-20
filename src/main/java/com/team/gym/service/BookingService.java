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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.team.gym.model.BookingStatus;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "class_not_found"));

        if (bookingRepo.existsByUserIdAndClassSessionId(uid, classId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "already_booked");
        }

        long taken = bookingRepo.countByClassSessionId(classId);
        if (taken >= cs.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "class_full");
        }

        // time overlap
        List<Booking> mine = bookingRepo.findByUserIdOrderByClassSessionStartAtAsc(uid);
        Instant start = cs.getStartAt(), end = cs.getEndAt();
        boolean conflict = mine.stream().anyMatch(b ->
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

    @Transactional
    public void cancel(Long userId, Long classId) {
        ClassSession cs = classRepo.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "class_not_found"));

        // 2-hour cutoff (keep if needed)
        Instant now = Instant.now();
        if (cs.getStartAt() != null && now.isAfter(cs.getStartAt().minus(Duration.ofHours(2)))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "too_late_to_cancel");
        }

        long deleted = bookingRepo.deleteByUserIdAndClassSessionId(userId, classId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not_found");
        }
    }
}
