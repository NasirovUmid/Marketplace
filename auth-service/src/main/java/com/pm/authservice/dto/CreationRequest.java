package com.pm.authservice.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;

public record CreationRequest(
        @Email @NotNull
        String email,
        @NotNull @Size(min = 8, max = 45)
        String password,
        @NotNull @Past
        Instant birthDate,
        @NotNull @Pattern(regexp = "^(\\\\+?998)?\\\\d{9}$", message = "A number has to be in the format of (+)998XXXXXXXXX")
        String phoneNumber
) {
}
