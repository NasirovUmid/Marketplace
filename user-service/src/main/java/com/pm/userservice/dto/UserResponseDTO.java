package com.pm.userservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class UserResponseDTO {

    private String fullName;

    private String email;

    private String bio;

    private Instant birthDate;

    private Instant registeredDate;
}
