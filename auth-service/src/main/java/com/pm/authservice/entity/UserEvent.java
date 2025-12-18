package com.pm.authservice.entity;

import com.pm.authservice.enums.UserEventType;

import java.time.LocalDate;
import java.util.UUID;

public record UserEvent(UUID id, String email, UserEventType eventType, LocalDate localDate) {
}
