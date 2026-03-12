package com.pm.paymentservice.config;

import com.pm.paymentservice.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class RedisExpirationListener {

    private final PaymentService paymentService;

    public void handleMessage(String key) {

        if (key.startsWith("payment:")) {

            String bookingId = key.replace("booking:", "");

            paymentService.expirePayment(UUID.fromString(bookingId));

        }

    }

}
