package com.pm.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Optional;

@Data
public class UserUpdateRequestDTO {

    @Size(min = 5,max = 50, message ="FullName has to be between these pointers!" )
    Optional<String> fullName;

    @NotBlank(message = "Email is required")
    @Email
    String email;

    @NotBlank
    Optional<String> bio;

    Optional<Integer> phoneNumber;


    //its easier to check whether its null and now i expect it to fill profile data completely later after creation



}
