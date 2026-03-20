package com.pm.userservice.dto;

import com.pm.userservice.entity.User;

public record UserResponseDTO(
        String fullName,
        String email,
        String phoneNumber,
        String bio,
        String imageUrl
) {
    public static UserResponseDTO from(User user) {

        return new UserResponseDTO(user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getBio(),
                user.getImageUrl());
    }
}
