package com.pm.userservice.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {



    private String email;
    private String imageName;

}
