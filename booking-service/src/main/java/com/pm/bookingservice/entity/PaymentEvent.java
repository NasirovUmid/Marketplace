package com.pm.bookingservice.entity;

import java.util.UUID;

public record PaymentEvent(UUID ticketId,UUID buyerId) {
}
