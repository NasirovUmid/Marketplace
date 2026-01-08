package com.pm.commonevents;

import java.util.UUID;

public record PaymentEvent(UUID ticketId, UUID buyerId) {
}
