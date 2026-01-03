package com.pm.notificationservice.entity;

import com.pm.notificationservice.enums.NotificationEventStatus;

import java.time.Instant;
import java.util.UUID;

public record BookingNotificationEvent(UUID bookingId, UUID catalogId, UUID userId, String email, String catalogName,
                                       NotificationEventStatus status, Instant time) {
}
