package com.pm.catalogservice.entity;


import com.pm.commonevents.enums.CatalogStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
public class Catalog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String description;

    private Double price;

    private UUID creatorId;

    private Long numberOfTickets;

    @Enumerated(EnumType.STRING)
    private CatalogStatus status;

    private Instant dateOfEvent;

    private Instant createdAt;

}
