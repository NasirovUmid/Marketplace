package com.pm.userservice.dto;

import java.time.Instant;

public record UserProfileDto(String fullName, String email, String phoneNumber, String bio, String imageUrl,
                             Instant birthDate, Instant registerDate) {
}
