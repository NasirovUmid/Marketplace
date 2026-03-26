package com.pm.catalogservice.dto;

import com.pm.catalogservice.entity.Catalog;

import java.time.Instant;
import java.util.UUID;

public record CatalogPageResponseDto(
        UUID id,
        String title,
        String description,
        UUID creatorId,
        Double price,
        String status,
        Instant dateOfEvent,
        Instant createdAt
) {
    public static CatalogPageResponseDto from(Catalog catalog) {
        return new CatalogPageResponseDto(
                catalog.getId(),
                catalog.getTitle(),
                catalog.getDescription(),
                catalog.getCreatorId(),
                catalog.getPrice(),
                catalog.getStatus().name(),
                catalog.getDateOfEvent(),
                catalog.getCreatedAt());
    }
}
