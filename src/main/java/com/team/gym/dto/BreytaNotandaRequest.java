package com.team.gym.dto;

import jakarta.validation.constraints.*;

/**
 * request hlutur sem notaður eru til að uppfæra upplýsingar um notanda.
 *
 * @param ssn      Kennitala. Nákvæmlega 10 tölustafir.
 * @param email    Tölvupóstfang.
 * @param password Nýtt lykilorð. Lengd á milli 4 til 255 stafa.
 */
public record BreytaNotandaRequest(
        @Size(min = 10, max = 10) String ssn,
        @Email String email,
        @Size(min=4, max=255) String password
) {}
