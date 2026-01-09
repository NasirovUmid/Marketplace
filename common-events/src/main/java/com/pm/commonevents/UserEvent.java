package com.pm.commonevents;

import com.pm.commonevents.enums.UserEventType;

import java.time.Instant;
import java.util.UUID;

public record UserEvent(UUID id,
                        String email,String phoneNumber, UserEventType eventType, Instant timeOfCreation, String address, Instant birthDate) {
}
