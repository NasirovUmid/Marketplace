package com.pm.commonevents;

import java.time.Instant;
import java.util.UUID;

public record BookingNotificationEvent(UUID bookingId, UUID catalogId, UUID userId, String email, String catalogName, String  status,
                                       Instant time) {
}
