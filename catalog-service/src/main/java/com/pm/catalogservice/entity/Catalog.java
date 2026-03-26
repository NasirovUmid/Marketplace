package com.pm.catalogservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pm.catalogservice.enums.CatalogStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
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

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Ticket> numberOfTickets;

    @Enumerated(EnumType.STRING)
    private CatalogStatus status;

    private Instant dateOfEvent;

    private Instant createdAt;

}
