package com.pm.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BookingRequestDto {

    private UUID ticketId;

    private UUID userId;

    private UUID catalogId;

    private String catalogName;

}
