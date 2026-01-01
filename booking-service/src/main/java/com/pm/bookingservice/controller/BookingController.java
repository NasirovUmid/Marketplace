package com.pm.bookingservice.controller;

import com.pm.bookingservice.dto.BookingRequestDto;
import com.pm.bookingservice.entity.Booking;
import com.pm.bookingservice.service.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/booking")
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // I dont think here should be full CRUD only creation and deletion . Update only for status and it will depend on Payment-service via Kafka

    public ResponseEntity<Booking> creatingBooking(@RequestBody BookingRequestDto booking){

        Booking newBooking = bookingService.creatingBooking(booking);

        return newBooking != null ? ResponseEntity.ok().body(newBooking) : ResponseEntity.badRequest().body(newBooking);

        //I will use @mapper and create BookingResponseDto 31.12.2025

    }

    @PostMapping("/delete/{bookingId}")
    public ResponseEntity<Void> cancellingBooking(@PathVariable UUID bookingId){

        bookingService.cancellingBooking(bookingId);

        return ResponseEntity.noContent().build();
    }


}
