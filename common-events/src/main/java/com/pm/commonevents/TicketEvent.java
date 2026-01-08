package com.pm.commonevents;

import java.util.UUID;

public record TicketEvent(UUID catalogId, UUID ticketId, UUID buyerId) {
}
