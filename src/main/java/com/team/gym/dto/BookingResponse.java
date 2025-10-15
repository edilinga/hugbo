package com.team.gym.dto;

import java.time.Instant;

public record BookingResponse(
    Long bookingId,
    Long classSessionId,
    String type,
    Instant startAt,
    Instant endAt
) {}