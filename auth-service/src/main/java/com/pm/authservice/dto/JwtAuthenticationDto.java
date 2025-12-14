package com.pm.authservice.dto;

import lombok.Builder;
import lombok.Data;

@Builder
public class JwtAuthenticationDto {

    private String token;
    private String refreshToken;

}
