package com.pm.authservice.dto;

import java.util.UUID;

public record UserCreationResponseDto(
        UUID userId,
        String accessToken,
        String refreshToken
) {
}
