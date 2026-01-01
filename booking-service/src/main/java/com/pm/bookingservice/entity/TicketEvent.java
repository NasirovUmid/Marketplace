package com.pm.bookingservice.entity;

import java.time.Instant;
import java.util.UUID;

public record TicketEvent(UUID catalogId, UUID ticketId, UUID buyerId) {
}
