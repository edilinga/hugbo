package com.team.gym.controller;

import com.team.gym.dto.ClassSessionResponse;
import com.team.gym.repository.BookingRepository;
import com.team.gym.repository.ClassSessionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping
public class TimeTableController {
    private final ClassSessionRepository classes;
    private final BookingRepository bookings;

    public TimeTableController(ClassSessionRepository classes, BookingRepository bookings) {
        this.classes = classes; this.bookings = bookings;
    }

    @GetMapping("/timetable")
    public List<ClassSessionResponse> timetable() {
        return classes.findAllByOrderByStartAtAsc().stream().map(cs ->{
            long taken = bookings.countByClassSessionId(cs.getId());
            int free = Math.max(0, cs.getCapacity() - (int)taken);
            return new ClassSessionResponse(
                cs.getId(), cs.getType(), cs.getTeacher(),
                cs.getCapacity(), free, cs.getStartAt(), cs.getEndAt()
            );
        }).toList();
    }
}
