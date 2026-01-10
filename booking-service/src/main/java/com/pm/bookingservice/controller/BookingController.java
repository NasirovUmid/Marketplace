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

    @PostMapping("/create")
    public ResponseEntity<Booking> creatingBooking(@RequestBody BookingRequestDto booking){

        Booking newBooking = bookingService.creatingBooking(booking);

        return newBooking != null ? ResponseEntity.ok().body(newBooking) : ResponseEntity.badRequest().body(newBooking);

    }

    @PostMapping("/delete/{bookingId}")
    public ResponseEntity<String> cancellingBooking(@PathVariable UUID bookingId){

        String result = bookingService.cancellingBooking(bookingId);

        return result.startsWith("d") ? ResponseEntity.ok().body(result) : ResponseEntity.badRequest().body(result);

    }


}
