package com.team.gym.controller;

import com.team.gym.dto.BookingResponse;
import com.team.gym.dto.PagedResponse;
import com.team.gym.errors.Unauthorized;
import com.team.gym.model.Booking;
import com.team.gym.repository.BookingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class UserBookingsController {
    private final BookingRepository bookings;

    public UserBookingsController(BookingRepository bookings) {
        this.bookings = bookings;
    }

    // UC11
    // Dæmi um notkun:
    // /users/123/bookings?days=7
    // /users/123/bookings?from=2025-11-01T00:00:00Z&to=2025-11-30T23:59:59Z&page=0&size=10&sort=startAt&dir=asc
    @GetMapping("/users/{userId}/bookings")
    public PagedResponse<BookingResponse> userBookings(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startAt") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null)
            throw new Unauthorized();
        if (!uid.equals(userId))
            throw new IllegalArgumentException("forbidden"); // 403

        if (page < 0)
            throw new IllegalArgumentException("bad_page");
        if (size < 1 || size > 100)
            throw new IllegalArgumentException("bad_size");

        Map<String, String> allowedSorts = Map.of(
                "startAt", "classSession.startAt",
                "type", "classSession.type");
        String sortProp = allowedSorts.get(sort);
        if (sortProp == null)
            throw new IllegalArgumentException("bad_sort");

        if (!"asc".equalsIgnoreCase(dir) && !"desc".equalsIgnoreCase(dir))
            throw new IllegalArgumentException("bad_dir");
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(new Sort.Order(direction, sortProp), new Sort.Order(direction, "id")));

        Instant now = Instant.now();
        Instant fromInst, toInst;
        if (from != null || to != null) {
            try {
                fromInst = (from != null) ? Instant.parse(from) : now;
                toInst = (to != null) ? Instant.parse(to) : now.plus(Duration.ofDays(365));
            } catch (Exception e) {
                throw new IllegalArgumentException("bad_datetime");
            }
        } else {
            int d = (days == null) ? 7 : days;
            if (d < 1 || d > 365)
                throw new IllegalArgumentException("bad_days");
            fromInst = now;
            toInst = now.plus(Duration.ofDays(d));
        }
        if (!toInst.isAfter(fromInst))
            throw new IllegalArgumentException("bad_range");

        Page<Booking> pageResult = bookings.findByUserIdAndClassSessionStartAtBetween(userId, fromInst, toInst,
                pageable);

        List<BookingResponse> items = pageResult.getContent().stream().map(b -> new BookingResponse(
                b.getId(),
                b.getClassSession().getId(),
                b.getClassSession().getType(),
                b.getClassSession().getStartAt(),
                b.getClassSession().getEndAt(),
                b.getStatus())).toList();

        return new PagedResponse<>(
                items,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages());
    }
}
