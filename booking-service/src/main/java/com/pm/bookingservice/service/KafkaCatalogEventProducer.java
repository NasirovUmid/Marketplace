package com.pm.bookingservice.service;

import com.pm.bookingservice.entity.PaymentEvent;
import com.pm.bookingservice.entity.TicketEvent;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaCatalogEventProducer {

    private final KafkaTemplate<String,TicketEvent> kafkaTemplate;
    private final KafkaTemplate<String,PaymentEvent> paymentEventKafkaTemplate;

    private final Logger logger = LoggerFactory.getLogger(KafkaCatalogEventProducer.class);

    // To -> catalog-service
    public void cancellingTicket(TicketEvent ticketEvent){

        logger.info("Ticket event ( Cancel ) = [ {} ]",ticketEvent);

        kafkaTemplate.send("ticket.fail",ticketEvent);

    }

    public void reservingTicket(TicketEvent ticketEvent){

        logger.info("Ticket event ( Reserve ) = [ {} ]",ticketEvent);

        kafkaTemplate.send("ticket.reserve",ticketEvent);

    }

    public void sellingTicket(TicketEvent ticketEvent){

        logger.info("Ticket event ( Sell ) = [ {} ]",ticketEvent);

        kafkaTemplate.send("ticket.success",ticketEvent);

    }



    // I send it -> payment-service
    public void requestPaymentConfirmation(PaymentEvent paymentEvent){

        logger.info("Payment event = [ {} ]",paymentEvent);

        paymentEventKafkaTemplate.send("paymentevent",paymentEvent);

    }

}
