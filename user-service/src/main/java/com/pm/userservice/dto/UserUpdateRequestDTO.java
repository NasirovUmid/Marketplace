package com.pm.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Optional;

@Data
public class UserUpdateRequestDTO {


    Optional<String> fullName;

    String email;

    Optional<String> bio;

    Optional<String> phoneNumber;


    //its easier to check whether its null and now i expect it to fill profile data completely later after creation



}
