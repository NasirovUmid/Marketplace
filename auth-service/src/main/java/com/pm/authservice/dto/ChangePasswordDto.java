package com.pm.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
        @Schema(example = "umid@gmail.com")
        @Email @NotNull
        String email,

        @Schema(example = "aaaaaaaaa")
        @NotNull @Size(min = 8, max = 45)
        String oldPassword,

        @Schema(example = "aaaaaaaa1")
        @NotNull @Size(min = 8, max = 45)
        String newPassword
) {
}
