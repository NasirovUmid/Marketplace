package com.pm.catalogservice.service;

import com.pm.catalogservice.entity.TicketEvent;
import com.pm.catalogservice.enums.TicketStatus;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaEventListener {

    private final TicketService ticketService;
    private final Logger logger = LoggerFactory.getLogger(KafkaEventListener.class);

    // I made one method for all of them

    @KafkaListener(topics = "ticket.fail",groupId = "ticket-service")
    public void makeAvailable(TicketEvent ticketEvent){

        // in case of expiration of payment (redis TTL) or manual cancel by user himself
        // unsuccessful payment

        logger.info(" CANCELLED TICKET = [ {} ]",ticketEvent);

    ticketService.changeTicketStatus(ticketEvent.catalogId(),ticketEvent.ticketId(), TicketStatus.AVAILABLE,null);

    }

    @KafkaListener(topics = "ticket.success",groupId = "ticket-service")
    public void makeTaken(TicketEvent ticketEvent){

        // when booking service receives <- order it sends message event to -> payment service if payment is confirmed ticket will be sold

        logger.info("BOOKED TICKET = [ {} ]",ticketEvent);

    ticketService.changeTicketStatus(ticketEvent.catalogId(),ticketEvent.ticketId(),TicketStatus.SOLD,ticketEvent.buyerId());

    }

    @KafkaListener(topics = "ticket.reserve",groupId = "ticket-service")
    public void makeReserved(TicketEvent ticketEvent){

        // this is for procedural situation bc ticket is in the process of being bought and this wont let others take it

        logger.info("RESERVED TICKET = [ {} ]",ticketEvent);

        ticketService.changeTicketStatus(ticketEvent.catalogId(),ticketEvent.ticketId(),TicketStatus.RESERVES,ticketEvent.buyerId());

    }

}
