package com.pm.notificationservice.entity;

import com.pm.notificationservice.enums.NotificationEventStatus;

import java.util.UUID;

public record UserNotificationEvent(UUID userId, String userEmail, String phoneNumber, NotificationEventStatus status) {
}
