package com.pm.paymentservice.service;

import com.pm.paymentservice.dto.PaymentRequestDto;
import com.pm.paymentservice.entity.Payment;
import com.pm.paymentservice.entity.PaymentEvent;
import com.pm.paymentservice.enums.PaymentStatus;
import com.pm.paymentservice.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class PaymentService {

    private final KafkaPaymentEventProducer kafkaPaymentEventProducer;
    private final PaymentRepository paymentRepository;

    public Payment confirmingPayment(PaymentRequestDto paymentRequestDto){

        Payment payment = paymentRepository.findPaymentByTicketId(paymentRequestDto.getTicketId());

        if (payment == null) return null;

        if (paymentRequestDto.isPaid()){
            payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(Instant.now());
            kafkaPaymentEventProducer.sendingPaymentEvent(new PaymentEvent(payment.getTicketId(),payment.getBuyerId()),PaymentStatus.SUCCEEDED);
        }else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            kafkaPaymentEventProducer.sendingPaymentEvent(new PaymentEvent(payment.getTicketId(),payment.getBuyerId()),PaymentStatus.FAILED);
        }

        return payment;

    }

    public void creatingPayment(PaymentEvent paymentEvent){

        Payment payment = Payment.builder()
                        .ticketId(paymentEvent.ticketId())
                                .buyerId(paymentEvent.buyerId())
                                      .paymentStatus(PaymentStatus.CREATED)
                                           .createdAt(Instant.now())
                                                 .paidAt(null)
                                                        .build();

        paymentRepository.save(payment);

    }
}
