package com.pm.catalogservice.service;

import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;
import com.pm.catalogservice.enums.TicketStatus;
import com.pm.catalogservice.repository.TicketRepository;
import com.pm.commonevents.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CatalogService catalogService;


    public List<Ticket> availableTickets(UUID catalogId) {

        Catalog catalog = catalogService.getCatalogById(catalogId);

        if (catalog == null)
            throw new NotFoundException("FIND CATALOG BY ID FOR TICKET: Catalog with ID = [ " + catalogId + " ] ");

        List<Ticket> ticketList = ticketRepository.findTicketsByCatalog(catalog)
                .stream().filter(ticket -> ticket.getStatus().equals(TicketStatus.AVAILABLE)).toList();

        if (ticketList == null)
            throw new NotFoundException("FIND AVAILABLE TICKETS BY CATALOG : Tickets of [ " + catalog.getTitle() + " ] ");
        if (ticketList.isEmpty()) return null;

        return ticketList;
    }

    public void changeTicketStatus(UUID catalogId, UUID ticketId, TicketStatus ticketStatus, UUID buyerId) {

        Optional<Ticket> ticket = ticketRepository.findAllByCatalogId(catalogId).stream().filter(ticket1 -> ticket1.getTicketId().equals(ticketId)).findFirst();

        if (ticket.isEmpty()) throw new NotFoundException("CHANGE STATUS: Ticket with ID = [ " + ticketId + " ] ");

        Ticket newTicket = ticket.get();

        newTicket.setStatus(ticketStatus);
        newTicket.setBuyerId(buyerId);

        ticketRepository.save(newTicket);

    }

}
