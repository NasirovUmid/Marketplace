package com.pm.authservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record JwtAuthenticationDto(

        String token,


        String refreshToken
) {


}
