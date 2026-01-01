package com.pm.bookingservice.entity;

import com.pm.bookingservice.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bookingId;

    private UUID catalogId;

    private UUID ticketId;

    private UUID userId;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    private Instant createdAt;

    private Instant confirmedAt;

    private Instant cancelledAt;

}
