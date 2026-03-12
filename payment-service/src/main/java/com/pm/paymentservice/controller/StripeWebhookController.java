package com.pm.paymentservice.controller;

import com.pm.paymentservice.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final PaymentService paymentService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripEvent(@RequestBody String payload, @RequestHeader("Strip-Signature") String sigHeader){

        Event event;

        try {

            event = Webhook.constructEvent(payload,sigHeader,endpointSecret);

        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature or smth.");
        }

            PaymentIntent intent = (PaymentIntent) event
                    .getDataObjectDeserializer()
                            .getObject()
                                    .orElseThrow();
        if ("payment_intent.succeeded".equals(event.getType())){

            paymentService.confirmingPayment(UUID.fromString(intent.getMetadata().get("bookingId")));

        }else {

            paymentService.cancelPayment(UUID.fromString(intent.getMetadata().get("bookingId")));

        }

        return ResponseEntity.ok("received");

    }

}
