package com.pm.bookingservice.service;

import com.pm.bookingservice.dto.BookingRequestDto;
import com.pm.bookingservice.entity.Booking;
import com.pm.bookingservice.enums.BookingStatus;
import com.pm.bookingservice.repository.BookingRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RedisBookingService redisBookingService;

    public Booking creatingBooking(@NonNull BookingRequestDto booking){

        Booking newBooking = Booking.builder()
                .ticketId(booking.getTicketId())
                .userId(booking.getUserId())
                .catalogId(booking.getCatalogId())
                .bookingStatus(BookingStatus.PENDING_PAYMENT)
                .createdAt(Instant.now())
                .build();

        Booking booking1 = bookingRepository.save(newBooking);

         redisBookingService.saveBooking(booking1.getBookingId(),booking1.getTicketId(),booking1.getUserId());

         return booking1;
    }

    public void cancellingBooking(UUID bookingId){

        redisBookingService.deleteBooking(bookingId);

        
    }

}
