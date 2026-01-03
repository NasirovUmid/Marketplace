package com.pm.paymentservice.entity;

import java.util.UUID;

public record PaymentEvent(UUID ticketId, UUID buyerId) {
}
