package com.pm.userservice.entity;

import java.util.UUID;

public record UserNotificationEvent(UUID userId,String userEmail,String phoneNumber,String status) {
}
