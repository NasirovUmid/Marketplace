package com.pm.userservice.mapper;

import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.entity.User;

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


    public static User toCreatingModel(User user){

        User user1 = User.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .imageUrl(user.getImageUrl())
                .birthDate(user.getBirthDate())
                .build();

        return user;

    }


}
