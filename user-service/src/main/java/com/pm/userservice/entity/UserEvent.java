package com.pm.userservice.entity;

import com.pm.userservice.enums.UserEventType;

import java.time.Instant;
import java.util.UUID;

public record UserEvent(UUID id,
                        String email, UserEventType eventType, Instant timeOfCreation, String address,Instant birthDate) {
}
