package com.pm.catalogservice.entity;

import java.time.Instant;
import java.util.UUID;

public record CatalogNotificationEvent(UUID catalogId, String catalogName,UUID creatorId, String catalogStatus, Instant createdAt) {
}
