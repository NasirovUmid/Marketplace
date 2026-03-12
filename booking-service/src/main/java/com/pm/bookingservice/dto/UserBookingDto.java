package com.pm.bookingservice.dto;

import com.pm.bookingservice.entity.Booking;

import java.time.Instant;

public record UserBookingDto(
        String catalogName,
        Long price,
        String email,
        Instant createdAt,
        Instant actionDate) {

    public static UserBookingDto from(Booking b) {
        Instant action = b == null ? null : b.getConfirmedAt() != null ? b.getConfirmedAt() : b.getCancelledAt();

        return new UserBookingDto(
                b.getCatalogName(),
                b.getPrice(),
                b.getEmail(),
                b.getCreatedAt(),
                action
        );
    }
}
