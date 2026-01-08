package com.pm.paymentservice.service;

import com.pm.paymentservice.dto.PaymentRequestDto;
import com.pm.paymentservice.entity.Payment;
import com.pm.commonevents.PaymentEvent;
import com.pm.paymentservice.enums.PaymentStatus;
import com.pm.paymentservice.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {

    private final KafkaPaymentEventProducer kafkaPaymentEventProducer;
    private final PaymentRepository paymentRepository;
    private final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    public Payment confirmingPayment(PaymentRequestDto paymentRequestDto){

        Payment payment = paymentRepository.findPaymentsByTicketId(paymentRequestDto.ticketId(),paymentRequestDto.buyerId()).getLast();

        if (payment==null || payment.getPaymentStatus().equals(PaymentStatus.SUCCEEDED)||
                payment.getPaymentStatus().equals(PaymentStatus.FAILED)) return null;

         logger.info("Payment = [ {} ]",payment);


        if (paymentRequestDto.isPaid()){
            payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(Instant.now());
            paymentRepository.save(payment);
            kafkaPaymentEventProducer.sendingPaymentEvent(new PaymentEvent(payment.getTicketId(),payment.getBuyerId()),PaymentStatus.SUCCEEDED);
        }else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            kafkaPaymentEventProducer.sendingPaymentEvent(new PaymentEvent(payment.getTicketId(),payment.getBuyerId()),PaymentStatus.FAILED);
        }

        return payment;

    }

    public void creatingPayment(PaymentEvent paymentEvent){

        Payment payment = paymentRepository.findPaymentByTicketId(paymentEvent.ticketId(),paymentEvent.buyerId());

        if (payment == null || !payment.getPaymentStatus().name().equals(PaymentStatus.SUCCEEDED.name())) {

            paymentRepository.save(Payment.builder()
                    .ticketId(paymentEvent.ticketId())
                    .buyerId(paymentEvent.buyerId())
                    .paymentStatus(PaymentStatus.CREATED)
                    .createdAt(Instant.now())
                    .paidAt(null)
                    .build()
                    );
        }



    }
}
