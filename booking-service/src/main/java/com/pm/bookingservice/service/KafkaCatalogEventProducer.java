package com.pm.bookingservice.service;

import com.pm.bookingservice.entity.BookingNotificationEvent;
import com.pm.bookingservice.entity.PaymentEvent;
import com.pm.bookingservice.entity.TicketEvent;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.JsonKafkaHeaderMapper;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class KafkaCatalogEventProducer {

    private final KafkaTemplate<String,TicketEvent> kafkaTemplate;
    private final KafkaTemplate<String,PaymentEvent> paymentEventKafkaTemplate;
    private final KafkaTemplate<String,BookingNotificationEvent> bookingNotificationEventKafkaTemplate;

    private final Logger logger = LoggerFactory.getLogger(KafkaCatalogEventProducer.class);

    // To -> catalog-service
    public void cancellingTicket(TicketEvent ticketEvent){

        logger.info("Ticket event ( Cancel ) = [ {} ]",ticketEvent);

        kafkaTemplate.send(MessageBuilder.withPayload(ticketEvent)
                .setHeader(KafkaHeaders.TOPIC,"ticket.success")
                .setHeader("__TypeId__",ticketEvent.getClass())
                .build());

       // "com.pm.commonevents.TicketEvent"

        //kafkaTemplate.send("ticket.fail",ticketEvent);

    }

    public void reservingTicket(TicketEvent ticketEvent){

        logger.info("Ticket event ( Reserve ) = [ {} ]",ticketEvent);

        kafkaTemplate.send(MessageBuilder
                .withPayload(ticketEvent)
                .setHeader(KafkaHeaders.TOPIC,"ticket.reserve")
                .setHeader("__TypeId__",ticketEvent.getClass())
                .build());


       // kafkaTemplate.send("ticket.reserve",ticketEvent);

    }

    public void sellingTicket(TicketEvent ticketEvent){

        logger.info("Ticket event ( Sell ) = [ {} ]",ticketEvent);

        kafkaTemplate.send(MessageBuilder.withPayload(ticketEvent)
                .setHeader(KafkaHeaders.TOPIC,"ticket.success")
                .setHeader("__TypeId__",ticketEvent.getClass())
                .build());
        //"com.pm.commonevents.TicketEvent"

       // kafkaTemplate.send("ticket.success",ticketEvent);

    }

    // I send it -> payment-service
    public void requestPaymentConfirmation(PaymentEvent paymentEvent){

        logger.info("Payment event = [ {} ]",paymentEvent);

        paymentEventKafkaTemplate.send(MessageBuilder
                .withPayload(paymentEvent)
                .setHeader(KafkaHeaders.TOPIC,"paymentevent")
                .setHeader("__TypeId__",paymentEvent.getClass())
                .build());
        //"com.pm.commonevents.PaymentEvent"
        //paymentEventKafkaTemplate.send("paymentevent",paymentEvent);


    }

    public void sendingNotification(BookingNotificationEvent bookingNotificationEvent){

        logger.info("notification event = [ {} ]",bookingNotificationEvent);

        bookingNotificationEventKafkaTemplate.send(MessageBuilder
                .withPayload(bookingNotificationEvent)
                .setHeader(KafkaHeaders.TOPIC,"booking")
                .setHeader("__TypeId__",bookingNotificationEvent.getClass())
                .build());

    }

}
