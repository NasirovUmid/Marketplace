package com.pm.bookingservice.service;

import com.pm.bookingservice.entity.Booking;
import com.pm.bookingservice.entity.TicketEvent;
import com.pm.bookingservice.enums.BookingStatus;
import com.pm.bookingservice.repository.BookingRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RedisBookingService {

    private final RedisTemplate<String, List<UUID>> redisTemplate;
    private final BookingRepository bookingRepository;
    private final KafkaCatalogEventProducer kafkaCatalogEventProducer;

    public void saveBooking(UUID bookingId,UUID ticketId,UUID userId){
    redisTemplate.opsForValue().set("booking:"+bookingId,Arrays.asList(ticketId,userId),10, TimeUnit.MINUTES);

    }

    public void deleteBooking(UUID bookingId){

        redisTemplate.delete("booking:"+bookingId);

    }

    public void expireBooking(UUID bookingId){

        Booking booking = bookingRepository.findById(bookingId).get();

        if (booking.getBookingStatus() == BookingStatus.PENDING_PAYMENT){

            booking.setBookingStatus(BookingStatus.EXPIRED);
            kafkaCatalogEventProducer.cancellingTicket(new TicketEvent(booking.getCatalogId(),booking.getTicketId(),booking.getUserId()));
            bookingRepository.save(booking);

        }

    }


}
