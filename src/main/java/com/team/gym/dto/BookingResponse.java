package com.team.gym.dto;

import com.team.gym.model.BookingStatus;

import java.time.Instant;

public record BookingResponse(
    Long bookingId,
    Long classSessionId,
    String type,
    Instant startAt,
    Instant endAt,
    BookingStatus status
) {}