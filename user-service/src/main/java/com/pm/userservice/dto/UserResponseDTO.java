package com.pm.userservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserResponseDTO {

    private String fullName;

    private String email;

    private String bio;

    private LocalDate birthDate;

    private LocalDate registeredDate;
}
