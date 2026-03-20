package com.pm.paymentservice.controller;

import com.pm.commonevents.exception.ApiProblem;
import com.pm.paymentservice.service.PaymentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Confirms Payment", description = "Imitation of Frontend , Manual Confirmation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "succeeded",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid format",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @PostMapping("/pay")
    public ResponseEntity<String> paying(@RequestParam("id") String paymentIntentId) throws StripeException {

        return ResponseEntity.ok(paymentService.debugConfirm(paymentIntentId));

    }


}
