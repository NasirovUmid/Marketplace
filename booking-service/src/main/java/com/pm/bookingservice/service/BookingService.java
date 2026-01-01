package com.pm.bookingservice.service;

import com.pm.bookingservice.dto.BookingRequestDto;
import com.pm.bookingservice.entity.Booking;
import com.pm.bookingservice.entity.PaymentEvent;
import com.pm.bookingservice.entity.TicketEvent;
import com.pm.bookingservice.enums.BookingStatus;
import com.pm.bookingservice.repository.BookingRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RedisBookingService redisBookingService;
    private final KafkaCatalogEventProducer kafkaCatalogEventProducer;

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

         // request for payment -> payment-service
         kafkaCatalogEventProducer.requestPaymentConfirmation(new PaymentEvent(booking1.getTicketId(),booking1.getUserId()));

         // reserving ticket - catalog-service
         kafkaCatalogEventProducer.reservingTicket(new TicketEvent(booking1.getCatalogId(),booking1.getTicketId(),booking1.getUserId()));

         return booking1;
    }


    public void expireBooking(UUID bookingId){

        Booking booking = bookingRepository.findById(bookingId).get();

        if (booking.getBookingStatus() == BookingStatus.PENDING_PAYMENT){

            booking.setBookingStatus(BookingStatus.EXPIRED);
            kafkaCatalogEventProducer.cancellingTicket(new TicketEvent(booking.getCatalogId(),booking.getTicketId(),booking.getUserId()));
            bookingRepository.save(booking);

        }

    }

    public void cancellingBooking(UUID bookingId){

        Booking booking = bookingRepository.findById(bookingId).get();

        booking.setCancelledAt(Instant.now());
        booking.setBookingStatus(BookingStatus.CANCELLED);
        redisBookingService.deleteBooking(booking.getBookingId());
        kafkaCatalogEventProducer.cancellingTicket(new TicketEvent(booking.getCatalogId(),booking.getTicketId(),booking.getUserId()));


        redisBookingService.deleteBooking(bookingId);

    }

    // I thought anyways I would check status with if so I didnt add Status to Payment event record
    // I need to delete redis value as it can automatically send event by ending of TTL
    public void confirmingBooking(PaymentEvent paymentEvent,boolean isSuccess) throws ChangeSetPersister.NotFoundException {

        Booking booking = bookingRepository.findBookingByTicketId(paymentEvent.ticketId());

        if (booking == null) throw new ChangeSetPersister.NotFoundException();

        if (isSuccess){

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setConfirmedAt(Instant.now());
            redisBookingService.deleteBooking(booking.getBookingId());
            kafkaCatalogEventProducer.sellingTicket(new TicketEvent(booking.getCatalogId(),booking.getTicketId(),booking.getUserId()));


        }else {

            booking.setBookingStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(Instant.now());
            redisBookingService.deleteBooking(booking.getBookingId());
            kafkaCatalogEventProducer.cancellingTicket(new TicketEvent(booking.getCatalogId(),booking.getTicketId(),booking.getUserId()));

        }
    }

}
