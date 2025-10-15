package com.team.gym.controller;

import com.team.gym.dto.BookingResponse;
import com.team.gym.errors.Unauthorized;
import com.team.gym.repository.BookingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class BookingController {
    private final BookingRepository bookings;
    public BookingController(BookingRepository bookings) { this.bookings = bookings; }

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
