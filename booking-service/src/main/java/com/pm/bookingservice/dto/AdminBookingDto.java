package com.pm.bookingservice.dto;

import com.pm.bookingservice.entity.Booking;

import java.time.Instant;
import java.util.UUID;

public record AdminBookingDto(
        UUID bookingId,
        UUID catalogId,
        String catalogName,
        Long price,
        UUID ticketId,
        UUID userId,
        String email,
        String bookingStatus,
        Instant createdAt,
        Instant actionDate
) {

    public static AdminBookingDto from(Booking booking) {

        Instant action = booking == null ? null : booking.getConfirmedAt() != null ? booking.getConfirmedAt() : booking.getCancelledAt();

        return new AdminBookingDto(
                booking.getBookingId(),
                booking.getCatalogId(),
                booking.getCatalogName(),
                booking.getPrice(),
                booking.getTicketId(),
                booking.getUserId(),
                booking.getEmail(),
                booking.getBookingStatus().name(),
                booking.getCreatedAt(),
                action);
    }
}
