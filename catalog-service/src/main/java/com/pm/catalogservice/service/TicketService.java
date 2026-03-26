package com.pm.catalogservice.service;

import com.pm.catalogservice.dto.TicketResponseDto;
import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;
import com.pm.catalogservice.enums.TicketStatus;
import com.pm.catalogservice.repository.CatalogRepository;
import com.pm.catalogservice.repository.TicketRepository;
import com.pm.commonevents.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CatalogRepository catalogRepository;


    public List<Ticket> availableTickets(UUID catalogId) {

        Optional<Catalog> catalog = catalogRepository.findCatalogById(catalogId);

        if (catalog.isEmpty()) {
            throw new NotFoundException("FIND CATALOG BY ID FOR TICKET: Catalog with ID = [ " + catalogId + " ] ");
        }

        Optional<List<Ticket>> ticketList = ticketRepository.findTicketsByCatalog(catalog.get());

        if (ticketList.isEmpty()) {
            throw new NotFoundException("TICKETS OF CATALOG = " + catalog);
        }

        return ticketList.get().stream().filter(ticket -> ticket.getStatus().equals(TicketStatus.AVAILABLE)).toList();
    }

    public void changeTicketStatus(UUID catalogId, UUID ticketId, TicketStatus ticketStatus, UUID buyerId) throws BadRequestException {

        Optional<Ticket> ticket = ticketRepository.findAllByCatalogId(catalogId).stream().filter(ticket1 -> ticket1.getTicketId().equals(ticketId)).findFirst();

        if (ticket.isEmpty()) throw new NotFoundException("CHANGE STATUS: Ticket with ID = [ " + ticketId + " ] ");

        Ticket newTicket = ticket.get();

        TicketStatus checkedStatus = statusCheck(ticketStatus, newTicket.getStatus());

        newTicket.setStatus(checkedStatus);
        newTicket.setBuyerId(buyerId);

        ticketRepository.save(newTicket);

    }

    public List<TicketResponseDto> getTicketList(Catalog catalog) {


        Optional<List<Ticket>> ticketList = ticketRepository.findTicketsByCatalog(catalog);

        if (ticketList.isEmpty()) {
            throw new NotFoundException("TICKETS WAS ");
        }

        return ticketList.get().stream().map(TicketResponseDto::from).toList();
    }

    private TicketStatus statusCheck(TicketStatus newStatus, TicketStatus oldStatus) throws BadRequestException {

        if (TicketStatus.AVAILABLE.equals(newStatus) && TicketStatus.SOLD.equals(oldStatus)) {

            throw new BadRequestException();

        } else if (TicketStatus.SOLD.equals(newStatus) && (TicketStatus.SOLD.equals(oldStatus) || TicketStatus.AVAILABLE.equals(oldStatus))) {

            throw new BadRequestException();

        } else if (TicketStatus.RESERVED.equals(newStatus) && (TicketStatus.RESERVED.equals(oldStatus) || TicketStatus.SOLD.equals(oldStatus))) {

            throw new BadRequestException();

        }

        return newStatus;
    }

}
