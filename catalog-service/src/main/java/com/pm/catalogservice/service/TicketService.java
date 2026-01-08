package com.pm.catalogservice.service;

import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;
import com.pm.commonevents.enums.TicketStatus;
import com.pm.catalogservice.repository.CatalogRepository;
import com.pm.catalogservice.repository.TicketRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CatalogRepository catalogRepository;


    public List<Ticket> availableTickets(UUID catalogId){

        List<Ticket> ticketList = ticketRepository.findTicketByCatalog(catalogRepository.findById(catalogId).get());

        return ticketList.stream().filter(ticket -> ticket.getStatus().equals(TicketStatus.AVAILABLE)).toList();

    }


    // I SIMPLIFIED search -> first, we group tickets by catalog and seek necessary ticket
    public void changeTicketStatus(UUID catalogId,UUID ticketId,TicketStatus ticketStatus,UUID buyerId){

        Ticket newTicket = ticketRepository.findAllByCatalogId(catalogId).stream().filter(ticket -> ticket.getTicketId().equals(ticketId)).findFirst().get();

        newTicket.setStatus(ticketStatus);
        newTicket.setBuyerId(buyerId);

        ticketRepository.save(newTicket);

    }


}
