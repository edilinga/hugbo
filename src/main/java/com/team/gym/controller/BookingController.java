package com.team.gym.controller;

import com.team.gym.dto.BookingResponse;
import com.team.gym.service.BookingService;
import com.team.gym.errors.Unauthorized;
import com.team.gym.repository.BookingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping
public class BookingController {
    private final BookingRepository bookings;

    private final BookingService bookingService;
    public BookingController(BookingRepository bookings, BookingService bookingService) {
        this.bookings = bookings;
        this.bookingService = bookingService;
    }

    // UC1 - bóka tíma
    @PostMapping("/bookings/{classId}")
    public BookingResponse book(@PathVariable Long classId, HttpSession session) {
        return bookingService.book(classId, session);
    }

    // UC2 — afbóka tíma
    @DeleteMapping("/bookings/{classId}")
    public ResponseEntity<Void> cancel(@PathVariable Long classId, HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) throw new Unauthorized();
        bookingService.cancel(uid, classId);
        return ResponseEntity.noContent().build(); // 204
    }
    //UC5: mínar bókanir
    @GetMapping("/minar-bokanir")
    public List<BookingResponse> myBookings(HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) throw new Unauthorized();

        return bookings.findByUserIdOrderByClassSessionStartAtAsc(uid).stream()
        .map(b -> new BookingResponse(
            b.getId(),
            b.getClassSession().getId(),
            b.getClassSession().getType(),
            b.getClassSession().getStartAt(),
            b.getClassSession().getEndAt()
        )).toList();
    }
}
