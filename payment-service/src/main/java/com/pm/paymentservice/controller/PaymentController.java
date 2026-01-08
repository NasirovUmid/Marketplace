package com.pm.paymentservice.controller;

import com.pm.paymentservice.dto.PaymentRequestDto;
import com.pm.paymentservice.entity.Payment;
import com.pm.paymentservice.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    // I return payment for testing in fact Frontend as I guess shoulnt know details just the fact of receiving payment confirmation
    @PostMapping("/confirm")
    public ResponseEntity<Payment> confirming(@RequestBody PaymentRequestDto paymentRequestDto){

        Payment payment = paymentService.confirmingPayment(paymentRequestDto);

        return payment != null ? ResponseEntity.ok().body(payment) :
                ResponseEntity.status(
                        HttpStatus.valueOf("the Request from booking-service hasnt come yet maybe or problem with saving to Database")).build();

    }


}
