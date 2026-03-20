package com.pm.catalogservice.dto;

import java.time.Instant;

public record UpdateRequestDto(
        String title,
        String description,
        Double price,
        Instant dateOfEvent
        ) {
}
