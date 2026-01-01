package com.pm.paymentservice.service;

import com.pm.paymentservice.entity.PaymentEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaPaymentEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "paymentevent")
    public void paymentListener(PaymentEvent paymentEvent){

    paymentService.creatingPayment(paymentEvent);

    }



}
