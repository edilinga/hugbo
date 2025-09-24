package com.team.gym.dto;

import jakarta.validation.constraints.*;

public record InnskraningRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}
