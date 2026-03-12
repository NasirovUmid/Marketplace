package com.pm.bookingservice.controller;

import com.pm.bookingservice.dto.AdminBookingDto;
import com.pm.bookingservice.dto.BookingRequestDto;
import com.pm.bookingservice.dto.UserBookingDto;
import com.pm.bookingservice.entity.Booking;
import com.pm.bookingservice.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings/")
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("catalog/{catalogId}")
    public Page<AdminBookingDto> getCatalog(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "createdat,desc") String sort,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam() String status,
                                            @PathVariable() UUID catalogId) {

        return bookingService.getCatalogBookings(catalogId, page, size, sort, status);
    }

    @GetMapping("my")
    public Page<UserBookingDto> getPersonalBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdat,desc") String sort,
            @RequestParam(defaultValue = "BOOKING_CONFIRMED") String status,
            @RequestHeader("X-User-Id") UUID userId) {


        return bookingService.getUsersBookings(userId, size, page, sort, status);

    }

    @PostMapping("create")
    public ResponseEntity<Booking> creatingBooking(@RequestBody BookingRequestDto booking) {

        Booking newBooking = bookingService.creatingBooking(booking);

        return newBooking != null ? ResponseEntity.status(HttpStatus.CREATED).build() : ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

    }

    @PostMapping("cancel/{bookingId}")
    public ResponseEntity<String> cancellingBooking(@PathVariable UUID bookingId) {

        boolean result = bookingService.cancellingBooking(bookingId);

        return result ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

    }


}
