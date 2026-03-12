package com.pm.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCredentialsDto {

    private String email;
    private String password;
    private Instant birthDate;
    private String phoneNumber;
}
