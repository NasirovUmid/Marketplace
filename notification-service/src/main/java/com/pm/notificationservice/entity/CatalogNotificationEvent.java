package com.pm.notificationservice.entity;

import com.pm.notificationservice.enums.NotificationEventStatus;

import java.time.Instant;
import java.util.UUID;

public record CatalogNotificationEvent(UUID catalogId, String catalogName, UUID creatorId, NotificationEventStatus catalogStatus,
                                       Instant createdAt) {
}
