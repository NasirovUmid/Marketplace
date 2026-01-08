package com.pm.catalogservice.dto;

import java.time.Instant;
import java.util.UUID;

public record CreationRequestDto(String title,
                                 String description,
                                 Double price,
                                 UUID  creatorId,
                                 Long numberOfTickets,
                                 Instant dateOfEvents) {
}
