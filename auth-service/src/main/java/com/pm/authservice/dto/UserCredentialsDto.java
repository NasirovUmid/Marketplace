package com.pm.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCredentialsDto(
        @Email @NotNull
        String email,
        @NotNull @Size(min = 8, max = 45)
        String password
) {


}
