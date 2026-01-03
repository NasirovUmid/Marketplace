package com.pm.bookingservice.service;

import com.pm.bookingservice.entity.PaymentEvent;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaEventConsumer {

    private final BookingService bookingService;
    private final Logger logger = LoggerFactory.getLogger(KafkaEventConsumer.class);

    // payment.{enum`s ordinal} 1 - SUCCESS , 2 - FAILED

    @KafkaListener(topics = "payment.1",groupId = "payment-status")
    public void successfulPaymentEventListener(PaymentEvent paymentEvent){

        try {
            bookingService.confirmingBooking(paymentEvent,true);
        } catch (ChangeSetPersister.NotFoundException e) {

            logger.error("successful payment   --  Not found = [ {} ]",paymentEvent);

        }

    }

    @KafkaListener(topics = "payment.2",groupId = "payment-status")
    public void failedPaymentEventListener(PaymentEvent paymentEvent){

        try {
            bookingService.confirmingBooking(paymentEvent,false);
        } catch (ChangeSetPersister.NotFoundException e) {

            logger.error("failed payment    --  Not found = [ {} ]",paymentEvent);

        }
    }


}
