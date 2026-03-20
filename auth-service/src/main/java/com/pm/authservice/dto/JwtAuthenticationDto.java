package com.pm.authservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record JwtAuthenticationDto(
        @NotNull
        String token,

        @NotNull
        String refreshToken
) {


}
