package com.pm.paymentservice.repository;

import com.pm.paymentservice.entity.Payment;
import com.pm.paymentservice.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Payment findPaymentByTicketId(UUID ticketId, UUID buyerId);
    List<Payment> findPaymentsByTicketId(UUID ticketId, UUID buyerId);
}
