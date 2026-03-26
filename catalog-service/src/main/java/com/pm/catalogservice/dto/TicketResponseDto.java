package com.pm.catalogservice.dto;

import com.pm.catalogservice.entity.Ticket;
import com.pm.catalogservice.enums.TicketStatus;

import java.util.UUID;

public record TicketResponseDto(
        UUID ticketId,
        UUID catalogId,
        TicketStatus status,
        UUID buyerId
) {
    public static TicketResponseDto from(Ticket ticket) {
        return new TicketResponseDto(
                ticket.getTicketId(),
                ticket.getCatalog().getId(),
                ticket.getStatus(),
                ticket.getBuyerId()
        );
    }
}
