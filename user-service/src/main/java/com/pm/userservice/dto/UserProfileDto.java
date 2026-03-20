package com.pm.userservice.dto;

import com.pm.userservice.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(name = "UserProfileDto", description = "Personal profile data for user")
public record UserProfileDto(
        @Schema(example = "umid")
        @NotNull @Size(min = 0, max = 25)
        String fullName,

        @Schema(example = "umid@gmail.com")
        @NotNull @Email
        String email,

        @Schema(example = "+998933082174")
        @NotNull
        String phoneNumber,

        @Schema(example = "gamer and alcoholic")
        @NotNull
        String bio,

        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6:C:Images/165656.png", description = "first is id of user, it is special way of storing in minio")
        String imageUrl,

        @Schema(example = "2003-01-15T10:00:00Z")
        @NotNull
        Instant birthDate,

        @Schema(example = "2026-01-15T10:00:00Z")
        @NotNull
        Instant registerDate) {

    public static UserProfileDto from(User user) {

        return new UserProfileDto(user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getBio(),
                user.getImageUrl(),
                user.getBirthDate(),
                user.getRegisteredDate());
    }
}
