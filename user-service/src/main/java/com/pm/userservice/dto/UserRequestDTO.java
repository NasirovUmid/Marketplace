package com.pm.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDTO {



    private String name;

    @Email(message = "Email should be valid")
    private String email;
                              //for just little changes we need just email for finding user others dont have to be
                              //we just need to know which of these is null and we check that in service
                              //logic here is request can include only change in bio or number they dont have to fill all

    private String bio;

    private Integer phoneNumber;


}
