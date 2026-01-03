package com.pm.bookingservice.entity;

import com.pm.bookingservice.enums.BookingStatus;

import java.time.Instant;
import java.util.UUID;

public record BookingNotificationEvent(UUID bookingId, UUID catalogId, UUID userId, String email, String catalogName, String  status,
                                       Instant time) {
}
