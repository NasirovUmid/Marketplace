package com.pm.bookingservice.dto;


import java.util.UUID;

public record BookingRequestDto(UUID ticketId,UUID userId,UUID catalogId,String catalogName,String email) {

}
