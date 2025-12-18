package com.pm.authservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Getter
public class JwtAuthenticationDto {

    private String token;
    private String refreshToken;

}
