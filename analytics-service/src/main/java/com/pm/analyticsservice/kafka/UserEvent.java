package com.pm.analyticsservice.kafka;

import java.time.Instant;
import java.util.UUID;

public record UserEvent(UUID userId, String email, UserEvent userEvent, Instant timeOfCreation, String address) {
}
