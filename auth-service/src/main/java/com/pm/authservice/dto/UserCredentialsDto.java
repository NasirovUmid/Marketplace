package com.pm.authservice.dto;

import lombok.Data;

@Data
public class UserCredentialsDto {

    private String email;
    private String password;
}
