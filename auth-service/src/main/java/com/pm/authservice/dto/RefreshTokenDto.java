package com.pm.authservice.dto;

import jakarta.validation.constraints.NotNull;

public record RefreshTokenDto(
        @NotNull String refreshToken
) {

}
