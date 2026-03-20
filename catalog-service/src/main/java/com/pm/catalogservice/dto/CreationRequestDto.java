package com.pm.catalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public record CreationRequestDto(
        @NotNull @NotBlank() @Size(min = 5, max = 25) @Schema(example = "Ielts english language course")
        String title,

        @Schema(example = "Teaches language efficiently")
        String description,

        @NotNull @Digits(integer = 10, fraction = 2, message = " price has to be at max 10 digits and 2 figures after comma")
        @Positive @Schema(example = "1500.0")
        Double price,

        @NotNull @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID creatorId,

        @NotNull @Max(50) @Positive @Schema(example = "30")
        Long numberOfTickets,

        @Future @Schema(example = "2026-01-15T10:00:00Z")
        Instant dateOfEvents
) {
}
