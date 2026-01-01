package com.pm.catalogservice.entity;

import com.pm.catalogservice.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID ticketId;

    @ManyToOne
    private Catalog catalog;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private UUID buyerId;

}
