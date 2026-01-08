package com.pm.bookingservice.service;

import com.pm.bookingservice.dto.BookingRequestDto;
import com.pm.bookingservice.entity.Booking;
import com.pm.commonevents.BookingNotificationEvent;
import com.pm.commonevents.PaymentEvent;
import com.pm.commonevents.TicketEvent;
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

        Booking booking1 = bookingRepository.save(Booking.builder()
                .ticketId(booking.ticketId())
                .userId(booking.userId())
                .email(booking.email())
                .catalogId(booking.catalogId())
                .catalogName(booking.catalogName())
                .bookingStatus(BookingStatus.PENDING_PAYMENT)
                .createdAt(Instant.now())
                .build());


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
            booking.setCancelledAt(Instant.now());
            kafkaCatalogEventProducer.cancellingTicket(new TicketEvent(booking.getCatalogId(),booking.getTicketId(),booking.getUserId()));

            bookingRepository.save(booking);

            kafkaCatalogEventProducer.sendingNotification(
                    new BookingNotificationEvent(booking.getBookingId(),booking.getCatalogId(),booking.getUserId(),booking.getEmail(),
                            booking.getCatalogName(),BookingStatus.EXPIRED.name(),booking.getCancelledAt()));

        }

    }

    public void cancellingBooking(UUID bookingId){

        Booking booking = bookingRepository.findById(bookingId).get();

        booking.setCancelledAt(Instant.now());
        booking.setBookingStatus(BookingStatus.BOOKING_CANCELLED);
        redisBookingService.deleteBooking(booking.getBookingId());
        kafkaCatalogEventProducer.cancellingTicket(new TicketEvent(booking.getCatalogId(),booking.getTicketId(),booking.getUserId()));
        kafkaCatalogEventProducer.sendingNotification(
                new BookingNotificationEvent(booking.getBookingId(),booking.getCatalogId(),booking.getUserId(),booking.getEmail(),
                        booking.getCatalogName(),BookingStatus.BOOKING_CANCELLED.name(),booking.getCancelledAt()));


        redisBookingService.deleteBooking(bookingId);

    }

    // I thought anyways I would check status with if so I didnt add Status to Payment event record
    // I need to delete redis value as it can automatically send event by ending of TTL
    public void confirmingBooking(PaymentEvent paymentEvent,boolean isSuccess) throws ChangeSetPersister.NotFoundException {

        Booking booking = bookingRepository.findBookingByTicketId(paymentEvent.ticketId());

        if (booking == null) throw new ChangeSetPersister.NotFoundException();

        if (isSuccess){

            booking.setBookingStatus(BookingStatus.BOOKING_CONFIRMED);
            booking.setConfirmedAt(Instant.now());
            redisBookingService.deleteBooking(booking.getBookingId());

            kafkaCatalogEventProducer.sellingTicket(new TicketEvent(booking.getCatalogId(),booking.getTicketId(),booking.getUserId()));

            kafkaCatalogEventProducer.sendingNotification(
                    new BookingNotificationEvent(booking.getBookingId(),booking.getCatalogId(),booking.getUserId(),booking.getEmail(),
                            booking.getCatalogName(), booking.getBookingStatus().name(), booking.getConfirmedAt()));


        }else {

            booking.setBookingStatus(BookingStatus.BOOKING_CANCELLED);
            booking.setCancelledAt(Instant.now());
            redisBookingService.deleteBooking(booking.getBookingId());

            kafkaCatalogEventProducer.cancellingTicket(new TicketEvent(booking.getCatalogId(),booking.getTicketId(),booking.getUserId()));

            kafkaCatalogEventProducer.sendingNotification(
                    new BookingNotificationEvent(booking.getBookingId(),booking.getCatalogId(),
                            booking.getUserId(),booking.getEmail() ,booking.getCatalogName(), booking.getBookingStatus().name(), booking.getCancelledAt()));

        }
    }

}
