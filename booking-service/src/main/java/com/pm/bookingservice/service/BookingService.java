package com.pm.bookingservice.service;

import com.pm.bookingservice.dto.AdminBookingDto;
import com.pm.bookingservice.dto.BookingRequestDto;
import com.pm.bookingservice.dto.UserBookingDto;
import com.pm.bookingservice.entity.Booking;
import com.pm.commonevents.BookingNotificationEvent;
import com.pm.commonevents.PaymentEvent;
import com.pm.commonevents.TicketEvent;
import com.pm.bookingservice.enums.BookingStatus;
import com.pm.bookingservice.repository.BookingRepository;
import com.pm.commonevents.exception.AlreadyExistsException;
import com.pm.commonevents.exception.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RedisBookingService redisBookingService;
    private final KafkaCatalogEventProducer kafkaCatalogEventProducer;

    public Page<AdminBookingDto> getCatalogBookings(UUID catalogId, int page, Integer size, String sort, String status) {

        int effectiveSize = (size != null && (size == 20 || size == 50)) ? size : 20;
        Pageable pageable = PageRequest.of(Math.max(page, 0), effectiveSize, toSort(sort));

        Page<Booking> result;
        if (status == null || status.isBlank()) {
            result = bookingRepository.findByCatalogId(catalogId, pageable);
        } else {
            result = bookingRepository.findByCatalogIdAndBookingStatus(catalogId, BookingStatus.valueOf(status), pageable);
        }

        return result.map(AdminBookingDto::from);

    }

    @Transactional(readOnly = true)
    public Page<UserBookingDto> getUsersBookings(UUID userId, int size, int page, String sort, String status) {

        int pageSize = size == 20 ? 20 : 10;
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize, toSort(sort));

        Page<Booking> result;
        if (status == null || status.isBlank()) {
            result = bookingRepository.findByUserId(userId, pageable);
        } else {
            result = bookingRepository.findByUserIdAndBookingStatus(userId, BookingStatus.valueOf(status), pageable);
        }

        return result.map(UserBookingDto::from);
    }

    @Transactional
    public Booking creatingBooking(@NonNull BookingRequestDto booking) {

        if (checkBooking(booking.catalogId(), booking.userId()) != null)
            throw new AlreadyExistsException("BOOKING CREATE: Booking is waiting PAYMENT and ");

        Booking booking1 = bookingRepository.save(Booking.builder()
                .ticketId(booking.ticketId())
                .userId(booking.userId())
                .email(booking.email())
                .catalogId(booking.catalogId())
                .catalogName(booking.catalogName())
                .price(booking.price())
                .bookingStatus(BookingStatus.PENDING_PAYMENT)
                .createdAt(Instant.now())
                .build());


        redisBookingService.saveBooking(booking1.getBookingId(), booking1.getTicketId(), booking1.getUserId());

        // request for payment -> payment-service
        kafkaCatalogEventProducer.requestPaymentConfirmation(new PaymentEvent(booking1.getTicketId(), booking1.getUserId(), booking1.getBookingId(), booking1.getPrice()));

        // reserving ticket - catalog-service
        kafkaCatalogEventProducer.reservingTicket(new TicketEvent(booking1.getCatalogId(), booking1.getTicketId(), booking1.getUserId()));

        return booking1;
    }

    @Transactional
    public void expireBooking(UUID bookingId) {

        Booking booking = notnull(bookingRepository.findById(bookingId));

        if (booking.getBookingStatus() == BookingStatus.PENDING_PAYMENT) {

            booking.setBookingStatus(BookingStatus.EXPIRED);
            booking.setCancelledAt(Instant.now());
            kafkaCatalogEventProducer.cancellingTicket(new TicketEvent(booking.getCatalogId(), booking.getTicketId(), booking.getUserId()));

            bookingRepository.save(booking);

            kafkaCatalogEventProducer.sendingNotification(
                    new BookingNotificationEvent(booking.getBookingId(), booking.getCatalogId(), booking.getUserId(), booking.getEmail(),
                            booking.getCatalogName(), BookingStatus.EXPIRED.name(), booking.getCancelledAt()));

        }

    }

    @Transactional
    public boolean cancellingBooking(UUID bookingId) {

        Booking booking = notnull(bookingRepository.findById(bookingId));

        booking.setCancelledAt(Instant.now());
        booking.setBookingStatus(BookingStatus.BOOKING_CANCELLED);
        redisBookingService.deleteBooking(booking.getBookingId());
        kafkaCatalogEventProducer.cancellingTicket(new TicketEvent(booking.getCatalogId(), booking.getTicketId(), booking.getUserId()));
        kafkaCatalogEventProducer.sendingNotification(
                new BookingNotificationEvent(booking.getBookingId(), booking.getCatalogId(), booking.getUserId(), booking.getEmail(),
                        booking.getCatalogName(), BookingStatus.BOOKING_CANCELLED.name(), booking.getCancelledAt()));

        redisBookingService.deleteBooking(bookingId);

        return true;
    }

    @Transactional
    public void confirmingBooking(PaymentEvent paymentEvent, boolean isSuccess) throws ChangeSetPersister.NotFoundException {

        Booking booking = notnull(bookingRepository.findBookingByTicketId(paymentEvent.ticketId()));

        if (isSuccess) {

            booking.setBookingStatus(BookingStatus.BOOKING_CONFIRMED);
            booking.setConfirmedAt(Instant.now());

            kafkaCatalogEventProducer.sellingTicket(new TicketEvent(booking.getCatalogId(), booking.getTicketId(), booking.getUserId()));

            kafkaCatalogEventProducer.sendingNotification(
                    new BookingNotificationEvent(booking.getBookingId(), booking.getCatalogId(), booking.getUserId(), booking.getEmail(),
                            booking.getCatalogName(), booking.getBookingStatus().name(), booking.getConfirmedAt()));

        } else {

            booking.setBookingStatus(BookingStatus.BOOKING_CANCELLED);
            booking.setCancelledAt(Instant.now());

            kafkaCatalogEventProducer.cancellingTicket(new TicketEvent(booking.getCatalogId(), booking.getTicketId(), booking.getUserId()));

            kafkaCatalogEventProducer.sendingNotification(
                    new BookingNotificationEvent(booking.getBookingId(), booking.getCatalogId(),
                            booking.getUserId(), booking.getEmail(), booking.getCatalogName(), booking.getBookingStatus().name(), booking.getCancelledAt()));

        }

        redisBookingService.deleteBooking(booking.getBookingId());
        bookingRepository.save(booking);
    }

    private Booking checkBooking(UUID catalogId, UUID userId) {

        Optional<Booking> booking = bookingRepository.findBookingByCatalogIdAndUserIdAndBookingStatus(catalogId, userId, BookingStatus.PENDING_PAYMENT);

        return booking.orElse(null);
    }

    private <T> T notnull(Optional<T> booking) {

        return booking.orElseThrow(() -> new NotFoundException("CANCEL BOOKING: Booking  = [ " + booking + " ] "));
    }

    private Sort toSort(String sort) {
        if (sort == null) return Sort.by("createdAt").descending();
        String s = sort.trim().toLowerCase();
        if (s.equals("createdat,asc")) return Sort.by("createdAt").ascending();
        return Sort.by("createdAt").descending();
    }
}
