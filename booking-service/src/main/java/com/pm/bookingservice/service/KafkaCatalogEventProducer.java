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

    private final Logger logger = LoggerFactory.getLogger(KafkaCatalogEventProducer.class);

    public void cancellingTicket(TicketEvent ticketEvent){

        logger.info("Ticket event = [ {} ]",ticketEvent);

        kafkaTemplate.send("ticket.fail",ticketEvent);

    }

    public void requestPaymentConfirmation(PaymentEvent paymentEvent){

    }

}
