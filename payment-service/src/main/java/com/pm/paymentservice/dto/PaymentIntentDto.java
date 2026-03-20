package com.pm.paymentservice.dto;

import java.util.UUID;
import java.io.Serializable;

public record PaymentIntentDto(
        UUID ticketId,
        UUID buyerId,
        UUID bookingId,
        Long price,
        String paymentIntentId,
        String clientSecret) implements Serializable {
}
