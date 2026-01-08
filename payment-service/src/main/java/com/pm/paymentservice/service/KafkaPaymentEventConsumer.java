package com.pm.paymentservice.service;

import com.pm.commonevents.PaymentEvent;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaPaymentEventConsumer {

    private final PaymentService paymentService;
    private final Logger logger = LoggerFactory.getLogger(KafkaPaymentEventConsumer.class);

    @KafkaListener(topics = "paymentevent",groupId = "payment-service")
    public void paymentListener(PaymentEvent paymentEvent){

    logger.info("Request for Payment = [ {} ] ",paymentEvent);

    paymentService.creatingPayment(paymentEvent);

    }



}
