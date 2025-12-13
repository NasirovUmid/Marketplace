package com.pm.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserCreationRequestDTO {

    private UUID id;

    @Email(message = "Email should be valid")
    private String email;

    private String event;

    //I assumed that other fields can be filled later so we receive onle the most necessary


/*
    @NotBlank(message = "It has to contain something")
    private String bio;

    @NotBlank(message = "Its necessary to know")
    private LocalDate birthDate;

    @NotBlank(message = "Contact number must be")
    private Integer phoneNumber;
*/
}
