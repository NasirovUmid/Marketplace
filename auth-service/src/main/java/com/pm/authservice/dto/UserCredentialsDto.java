package com.pm.authservice.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class UserCredentialsDto {

    private String email;
    private String password;
    private Instant birthDate;
    private String phoneNumber;
}
