package com.pm.paymentservice.service;

import com.pm.commonevents.exception.AlreadyExistsException;
import com.pm.commonevents.exception.NotFoundException;
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
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {

    private final KafkaPaymentEventProducer kafkaPaymentEventProducer;
    private final PaymentRepository paymentRepository;
    private final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final RedisService redisService;

    public PaymentEvent confirmingPayment(UUID bookingId){

       // Payment payment = paymentRepository.findPaymentsByTicketId(paymentRequestDto.ticketId(),paymentRequestDto.buyerId()).getLast();

        PaymentEvent paymentEvent = redisService.getPayment(bookingId);

        if (paymentEvent==null) throw new NotFoundException("CONFIRM PAYMENT: Payment probably is expired because it is ");

         logger.info("Payment event = [ {} ]",paymentEvent);




           Payment payment =  paymentRepository.save(Payment.builder()
                    .ticketId(paymentEvent.ticketId())
                    .bookingId(paymentEvent.bookingId())
                    .price(paymentEvent.price())
                    .buyerId(paymentEvent.buyerId())
                    .paymentStatus(PaymentStatus.SUCCEEDED)
                    .createdAt(Instant.now())
                    .paidAt(Instant.now())
                    .build());


            kafkaPaymentEventProducer.sendingPaymentEvent(new PaymentEvent(payment.getTicketId(),payment.getBuyerId(),payment.getBookingId(), payment.getPrice()),PaymentStatus.SUCCEEDED);

 //           payment.setPaymentStatus(PaymentStatus.FAILED);
 //           paymentRepository.save(payment);



        return paymentEvent;

    }

    public void cancelPayment(UUID bookingId){

        PaymentEvent paymentEvent = redisService.getPayment(bookingId);

        if (paymentEvent == null) throw new NotFoundException("CANCEL PAYMENT: payment probably expired because it is ");

        logger.info("CANCELLING PAYMENT: {}",bookingId);

  //      redisService.deletePayment(paymentEvent.bookingId());
        kafkaPaymentEventProducer.sendingPaymentEvent(
                new PaymentEvent(paymentEvent.ticketId(),paymentEvent.buyerId(),paymentEvent.bookingId(),paymentEvent.price()),PaymentStatus.FAILED);

    }

    public void creatingPayment(PaymentEvent paymentEvent){

        if (redisService.doesExists(paymentEvent.bookingId())){ throw new AlreadyExistsException("CREATE PAYMENT: payment ");}

            redisService.savePayment(paymentEvent);

    }

    public void expirePayment(UUID bookingId){

        redisService.deletePayment(bookingId);

    }
/*
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
    */

}
