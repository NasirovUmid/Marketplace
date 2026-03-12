package com.pm.catalogservice.controller;

import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;
import com.pm.catalogservice.service.CatalogService;
import com.pm.catalogservice.service.TicketService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ticket")
@AllArgsConstructor
public class TicketController {

    private final TicketService ticketService;


    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> availableTickets(UUID catalogId){

        List<Ticket> ticketList = ticketService.availableTickets(catalogId);

        return ticketList != null ? ResponseEntity.ok().body(ticketList) : ResponseEntity.status(HttpStatus.valueOf("no tickets left")).build();

    }

}
