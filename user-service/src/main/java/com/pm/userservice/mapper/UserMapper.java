package com.pm.userservice.mapper;

import com.pm.userservice.dto.UserRequestDTO;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.entity.User;
import com.pm.userservice.repository.UserRepository;

public class UserMapper {

    public static UserResponseDTO toDTO(User user){

        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .bio(user.getBio())
                .birthDate(user.getBirthDate())
                .registeredDate(user.getRegisteredDate())
                .build();

        return userResponseDTO;

    }

    /*
    public static User toModel(UserRequestDTO userRequestDTO){

        return

    }
*/

}
