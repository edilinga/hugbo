package com.team.gym.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RequestMapping
public class BookingController {
    @GetMapping
    public ResponseEntity<List<?>> listBookings() {
        return ResponseEntity.ok(Collections.emptyList());
        // Einfalt GET sem myndi skila tómum lista viljandi(í bili)
    }
}
