package com.team.gym.dto;

import jakarta.validation.constraints.*;

public record NyskraningRequest (
    @NotBlank @Size(min=10, max=10) String ssn,
    @NotBlank @Email String email,
    @NotBlank @Size(min=4, max=255) String password
) {}

