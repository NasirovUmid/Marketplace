package com.pm.catalogservice.entity;

import com.pm.catalogservice.enums.Status;
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
public class Catalog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String description;

    private double price;

    private UUID creatorId;

    private long numberOfTickets;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate dateOfEvent;




}
