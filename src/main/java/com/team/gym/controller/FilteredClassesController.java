package com.team.gym.controller;

import com.team.gym.dto.ClassSessionResponse;
import com.team.gym.dto.PagedResponse;
import com.team.gym.model.ClassSession;
import com.team.gym.repository.BookingRepository;
import com.team.gym.repository.ClassSessionRepository;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping
public class FilteredClassesController {
    private final ClassSessionRepository classes;
    private final BookingRepository bookings;

    public FilteredClassesController(ClassSessionRepository classes, BookingRepository bookings) {
        this.classes = classes;
        this.bookings = bookings;
    }

    //UC10 - Sækja síuð gögn með query params
    //Dæmi um notkun:
    // /classes?days=7
    // /classes?from=2025-11-01T00:00:00Z&to=2025-11-30T23:59:59Z&page=0&size=20&sort=startAt&dir=asc

    @GetMapping("/classes")
    public PagedResponse<ClassSessionResponse> listFiltered(
        @RequestParam(required = false) Integer days,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "startAt") String sort,
        @RequestParam(defaultValue = "asc") String dir
    ) {
        if (page < 0) throw new IllegalArgumentException("bad_page");
        if (size < 1 || size > 100) throw new IllegalArgumentException("bad_size");

        Map<String, String> allowedSorts = Map.of(
            "startAt", "startAt",
            "type", "type",
            "teacher", "teacher",
            "capacity", "capacity"
        );
        String sortProp = allowedSorts.getOrDefault(sort, null);
        if (sortProp == null) throw new IllegalArgumentException("bad_sort");
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProp));

        Instant now = Instant.now();
        Instant fromInst;
        Instant toInst;

        if(from != null || to != null){
            try {
                fromInst = (from != null) ? Instant.parse(from) : now;
                toInst = (to != null) ? Instant.parse(to) : now.plus(Duration.ofDays(365));
            } catch (Exception e) {
                throw new IllegalArgumentException("bad_datetime");
            }
        } else {
            int d = (days == null) ? 7 : days;
            if (d < 1 || d > 365) throw new IllegalArgumentException("bad_days");
            fromInst = now;
            toInst = now.plus(Duration.ofDays(d));
        }
        if (!toInst.isAfter(fromInst)) throw new IllegalArgumentException("bad_range");

        Page<ClassSession> pageResult = classes.findByStartAtBetween(fromInst, toInst, pageable);
        List<ClassSessionResponse> items = pageResult.getContent().stream().map(cs -> {
            long taken = bookings.countByClassSessionId(cs.getId());
            int free = Math.max(0, cs.getCapacity() - (int) taken);
            return new ClassSessionResponse(
                cs.getId(),
                cs.getType(),
                cs.getTeacher(),
                cs.getCapacity(),
                free,
                cs.getStartAt(),
                cs.getEndAt()
            );
        }).toList();

        return new PagedResponse<>(
            items,
            pageResult.getNumber(),
            pageResult.getSize(),
            pageResult.getTotalElements(),
            pageResult.getTotalPages()
        );
    }
}
