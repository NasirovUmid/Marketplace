package com.pm.paymentservice.controller;

import com.pm.commonevents.exception.ApiProblem;
import com.pm.paymentservice.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1/webhook")
public class StripeWebhookController {

    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String endpointSecret;

    @Value("${STRIPE_API_KEY}")
    private String apiKey;

    @PostConstruct
    private void init() {

        Stripe.apiKey = apiKey;

    }

    private final PaymentService paymentService;

    public StripeWebhookController(PaymentService paymentService) {

        this.paymentService = paymentService;

    }


    @Operation(summary = "Receives status", description = "Receives status from Provider")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Payment was not found ( expired )",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripEvent(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("EndpointSecret = {} , Payload = {}  ,  sigHeader =  {} ", endpointSecret, payload, sigHeader);
        Event event;

        try {

            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            log.info("EVENT.TYPE = {}", event.getType());

        } catch (SignatureVerificationException e) {
            log.error("Stripe webhook verification failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature or smth.");
        }

        var stripeObjectOptional = event.getDataObjectDeserializer().getObject();

        if (stripeObjectOptional.isEmpty()) {
            log.error("Stripe object is empty for event type={}", event.getType());
            return ResponseEntity.badRequest().body("Empty event object");
        }

        Object stripeObject = stripeObjectOptional.get();
        log.info("Stripe object class = {}", stripeObject.getClass().getName());

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) stripeObject;

            paymentService.confirmingPayment(UUID.fromString(intent.getMetadata().get("bookingId")));
            return ResponseEntity.ok("successful");

        } else {
            PaymentIntent intent = (PaymentIntent) stripeObject;
            paymentService.cancelPayment(UUID.fromString(intent.getMetadata().get("bookingId")));
            return ResponseEntity.badRequest().body("unsuccessful");
        }
    }

}
