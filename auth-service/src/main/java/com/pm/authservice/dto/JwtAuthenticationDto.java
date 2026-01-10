package com.pm.authservice.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class JwtAuthenticationDto {

    private String token;
    private String refreshToken;

}
