package com.pm.paymentservice.service;

import com.pm.commonevents.PaymentEvent;
import com.pm.commonevents.exception.AlreadyExistsException;
import com.pm.commonevents.exception.NotFoundException;
import com.pm.paymentservice.dto.PaymentIntentDto;
import com.pm.paymentservice.entity.Payment;
import com.pm.paymentservice.enums.PaymentStatus;
import com.pm.paymentservice.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {

    private final KafkaPaymentEventProducer kafkaPaymentEventProducer;
    private final PaymentRepository paymentRepository;
    private final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final RedisService redisService;

    @Transactional
    public void confirmingPayment(UUID bookingId) {

        // Payment payment = paymentRepository.findPaymentsByTicketId(paymentRequestDto.ticketId(),paymentRequestDto.buyerId()).getLast();

        PaymentIntentDto paymentEvent = redisService.getPayment(bookingId);

        if (paymentEvent == null)
            throw new NotFoundException("CONFIRM PAYMENT: Payment probably is expired because it is ");

        logger.info("Payment event = [ {} ]", paymentEvent);

        Payment payment = paymentRepository.save(Payment.builder()
                .ticketId(paymentEvent.ticketId())
                .bookingId(paymentEvent.bookingId())
                .price(paymentEvent.price())
                .buyerId(paymentEvent.buyerId())
                .paymentStatus(PaymentStatus.SUCCEEDED)
                .createdAt(Instant.now())
                .paidAt(Instant.now())
                .build());

        kafkaPaymentEventProducer.sendingPaymentEvent(new PaymentEvent(payment.getTicketId(), payment.getBuyerId(), payment.getBookingId(), payment.getPrice()), PaymentStatus.SUCCEEDED);
    }

    @Transactional
    public void cancelPayment(UUID bookingId) {

        PaymentIntentDto paymentEvent = redisService.getPayment(bookingId);

        if (paymentEvent == null)
            throw new NotFoundException("CANCEL PAYMENT: payment probably expired because it is ");

        logger.info("CANCELLING PAYMENT: {}", bookingId);

        //      redisService.deletePayment(paymentEvent.bookingId());
        kafkaPaymentEventProducer.sendingPaymentEvent(
                new PaymentEvent(paymentEvent.ticketId(), paymentEvent.buyerId(), paymentEvent.bookingId(), paymentEvent.price()), PaymentStatus.FAILED);

    }

    @Transactional
    public void creatingPayment(PaymentEvent paymentEvent) throws StripeException {

        if (redisService.doesExists(paymentEvent.bookingId())) {
            throw new AlreadyExistsException("CREATE PAYMENT: payment ");
        }

        PaymentIntentCreateParams intentCreateParams =
                PaymentIntentCreateParams.builder()
                        .setAmount(paymentEvent.price())
                        .setCurrency("eur")
                        .setPaymentMethod("pm_card_visa")
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .setAllowRedirects(
                                                PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER
                                        )
                                        .build()
                        )
                        .putMetadata("bookingId", paymentEvent.bookingId().toString())
                        .build();

        PaymentIntent intent = PaymentIntent.create(intentCreateParams);

        String paymentIntentId = intent.getId();
        String clientSecret = intent.getClientSecret();
        logger.info("paymentIntentId = {} , clientSecret = {}", paymentIntentId, clientSecret);
        redisService.savePayment(new PaymentIntentDto(paymentEvent.ticketId(), paymentEvent.buyerId(), paymentEvent.bookingId(), paymentEvent.price(), paymentIntentId, clientSecret));

    }

    public void expirePayment(UUID bookingId) {

        redisService.deletePayment(bookingId);

    }

    public String debugConfirm(String paymentIntentId) throws StripeException {

        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                .setPaymentMethod("pm_card_visa")
                .build();
        PaymentIntent confirmed = intent.confirm(params);
        return confirmed.getStatus();
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
