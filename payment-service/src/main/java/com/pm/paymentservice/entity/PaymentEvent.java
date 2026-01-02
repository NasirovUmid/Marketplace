package com.pm.paymentservice.entity;

import com.pm.paymentservice.enums.PaymentStatus;

import java.util.UUID;

public record PaymentEvent(UUID ticketId, UUID buyerId) {
}
