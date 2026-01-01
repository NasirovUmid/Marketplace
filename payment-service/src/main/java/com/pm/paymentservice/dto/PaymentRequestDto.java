package com.pm.paymentservice.dto;

import com.pm.paymentservice.enums.PaymentStatus;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PaymentRequestDto {

    private UUID ticketId;

    private UUID buyerId;

    private boolean isPaid;


}
