package com.pm.bookingservice.repository;

import com.pm.bookingservice.entity.Booking;
import com.pm.bookingservice.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findBookingByTicketId(UUID ticketId);

    Optional<Booking> findBookingByCatalogIdAndUserIdAndBookingStatus(UUID catalogId, UUID userId, BookingStatus bookingStatus);

    Optional<Booking> findBookingByTicketIdAndUserId(UUID ticketId, UUID userId);

    Page<Booking> findByUserId(UUID userId, Pageable pageable);

    Page<Booking> findByUserIdAndBookingStatus(UUID userId, BookingStatus bookingStatus, Pageable pageable);

    Page<Booking> findByCatalogId(UUID catalogId, Pageable pageable);

    Page<Booking> findByCatalogIdAndBookingStatus(UUID catalogId, BookingStatus bookingStatus, Pageable pageable);
}
