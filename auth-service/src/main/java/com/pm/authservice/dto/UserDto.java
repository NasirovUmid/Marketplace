package com.pm.authservice.dto;

import com.pm.authservice.enums.Role;
import lombok.Data;

@Data
public class UserDto {

    private String id;
    private String email;
    private String password;
    private Role role;

}
