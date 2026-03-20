package com.pm.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "UserUpdateRequestDto", description = "used for updating user profile")
public record UserUpdateRequestDTO(

        @Schema(example = "umid")
        String fullName,

        @Schema(example = "umid@gmail.com")
        @NotNull @Email
        String email,

        @Schema(example = "addict and super racist")
        String bio,

        @Schema(example = "+998933082174")
        @Pattern(regexp = "^(\\\\+?998)?\\\\d{9}$", message = "A number has to be in the format of (+)998XXXXXXXXX")
        String phoneNumber

) {
}
