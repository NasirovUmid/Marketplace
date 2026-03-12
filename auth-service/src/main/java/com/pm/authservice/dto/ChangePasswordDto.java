package com.pm.authservice.dto;

public record ChangePasswordDto(String email, String oldPassword, String newPassword) {
}
