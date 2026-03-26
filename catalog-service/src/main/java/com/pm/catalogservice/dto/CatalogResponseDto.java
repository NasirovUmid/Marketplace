package com.pm.catalogservice.dto;

import com.pm.catalogservice.entity.Catalog;
import com.pm.catalogservice.entity.Ticket;

import java.util.List;

public record CatalogResponseDto(
        Catalog catalog,
        List<TicketResponseDto> ticketList,
        long totalTicketsNumber,
        int availableTicketsNumber) {
}
