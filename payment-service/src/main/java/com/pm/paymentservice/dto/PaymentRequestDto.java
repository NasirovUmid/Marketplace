package com.pm.paymentservice.dto;

import com.pm.paymentservice.enums.PaymentStatus;
import lombok.Getter;

import java.util.UUID;

public record PaymentRequestDto(UUID ticketId,UUID buyerId,Boolean isPaid) {

}
