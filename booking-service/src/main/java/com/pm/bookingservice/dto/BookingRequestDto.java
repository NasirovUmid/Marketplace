package com.pm.bookingservice.dto;


import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record BookingRequestDto(
        @NotNull
        UUID ticketId,
        @NotNull
        UUID userId,
        @NotNull
        UUID catalogId,
        @NotNull
        String catalogName,
        @NotNull @Email
        String email,
        @NotNull @Positive @Digits(integer = 10, fraction = 2)
        Long price
){
}
