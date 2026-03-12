package com.pm.paymentservice.controller;

import com.pm.commonevents.PaymentEvent;
import com.pm.paymentservice.dto.PaymentRequestDto;
import com.pm.paymentservice.entity.Payment;
import com.pm.paymentservice.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<String> confirming(@RequestBody PaymentRequestDto paymentRequestDto) {

        PaymentEvent payment = paymentService.confirmingPayment(paymentRequestDto.bookingId());

        return payment != null ? ResponseEntity.ok().build() :
                ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();

    }

    @PostMapping("/pay")
    public ResponseEntity<?> paying(@RequestBody PaymentRequestDto paymentRequestDto) throws StripeException {

        PaymentIntentCreateParams intentCreateParams =
                PaymentIntentCreateParams.builder()
                        .setAmount(paymentRequestDto.amount())
                        .setCurrency("eur")
                        .setPaymentMethod(paymentRequestDto.paymentMethodId())
                        .setConfirm(true)
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .putMetadata("bookingId", paymentRequestDto.bookingId().toString())
                        .build();

        PaymentIntent intent = PaymentIntent.create(intentCreateParams);


        return intent.getStatus().equals("succeeded") ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }


}
