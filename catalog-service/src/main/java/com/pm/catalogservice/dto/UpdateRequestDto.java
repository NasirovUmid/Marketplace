package com.pm.catalogservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.time.Instant;

@Builder
public record UpdateRequestDto(
        String title,
        String description,
        @Positive
        Double price,
        @Future
        Instant dateOfEvent
) {

}
