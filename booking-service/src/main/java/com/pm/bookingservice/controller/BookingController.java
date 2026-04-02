package com.pm.bookingservice.controller;

import com.pm.bookingservice.dto.AdminBookingDto;
import com.pm.bookingservice.dto.BookingRequestDto;
import com.pm.bookingservice.dto.UserBookingDto;
import com.pm.bookingservice.service.BookingService;
import com.pm.commonevents.exception.ApiProblem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/bookings/")
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Page of Bookings", description = "Returns page of Catalog`s Bookings ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of Bookings",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Catalog was not found",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @GetMapping("{catalogId}")
    public Page<AdminBookingDto> getCatalog(@RequestParam(defaultValue = "0", name = "page") int page,
                                            @RequestParam(defaultValue = "createdat,desc", name = "sort") String sort,
                                            @RequestParam(defaultValue = "10", name = "size") int size,
                                            @RequestParam(defaultValue = "PENDING_PAYMENT", name = "status") String status,
                                            @PathVariable(name = "catalogId") UUID catalogId) {

        return bookingService.getCatalogBookings(catalogId, page, size, sort, status);
    }

    @Operation(summary = "page of Personal Bookings", description = "Returns page of User`s Bookings ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of Bookings",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("my")
    public Page<UserBookingDto> getPersonalBookings(
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size,
            @RequestParam(defaultValue = "createdat,desc", name = "sort") String sort,
            @RequestParam(defaultValue = "BOOKING_CONFIRMED", name = "status") String status,
            @RequestHeader("X-User-Id") UUID userId) {

        return bookingService.getUsersBookings(userId, size, page, sort, status);
    }

    @Operation(summary = "Create Booking", description = "Creates new Booking ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "409", description = "Conflict with already existing data",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @PostMapping("create")
    public ResponseEntity<UUID> creatingBooking(@Valid @RequestBody BookingRequestDto booking) {

        UUID bookingId = bookingService.creatingBooking(booking);

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingId);
    }

    @Operation(summary = "Cancel Booking", description = "Cancels pending booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking canceled successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Booking was not found ( expired )",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @PostMapping("cancel/{bookingId}")
    public ResponseEntity<Void> cancellingBooking(@PathVariable UUID bookingId) {

        bookingService.cancellingBooking(bookingId);

        return ResponseEntity.ok().build();

    }


}
