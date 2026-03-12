package com.pm.userservice.dto;

import java.time.Instant;

public record UserAdminFilter(String email,
                              Instant registeredFrom,
                              Instant registeredTo,
                              Instant birthFrom,
                              Instant birthTo) {
}
