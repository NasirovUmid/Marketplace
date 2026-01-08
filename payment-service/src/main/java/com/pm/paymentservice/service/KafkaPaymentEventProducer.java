package com.pm.paymentservice.service;

import com.pm.commonevents.PaymentEvent;
import com.pm.paymentservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaPaymentEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final Logger logger = LoggerFactory.getLogger(KafkaPaymentEventProducer.class);


    // I am gonna differ by topic the status of payment -> directing to right method
    public void sendingPaymentEvent(PaymentEvent paymentEvent, PaymentStatus paymentStatus){

        logger.info("Payment details = [ {} ] , Status of payment = [ {} ]",paymentEvent,paymentStatus.name());


                            // Status = 1 - SUCCESS , 2 - FAILED
        kafkaTemplate.send("payment."+paymentStatus,paymentEvent);

/*
        kafkaTemplate.send(MessageBuilder
                .withPayload(paymentEvent)
                .setHeader(KafkaHeaders.TOPIC,"payment."+paymentStatus.name())
                .setHeader("__TypeId__","com.pm.commonevents.PaymentEvent")
                .build());
*/
        //paymentEvent.getClass()
        //"com.pm.bookingservice.entity.PaymentEvent"
    }

}
