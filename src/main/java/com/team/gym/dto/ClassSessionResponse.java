package com.team.gym.dto;

import java.time.Instant;

public record ClassSessionResponse( 
    Long id,
    String type,
    String teacher,
    int capacity,
    int freeSeats,
    Instant startAt,
    Instant endAt
) {}
