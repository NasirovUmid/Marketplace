package com.pm.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;

public record AdminFilterDto(@Email String email,
                             @PastOrPresent Instant birthDateFrom,
                             @PastOrPresent Instant birthDateTo,
                             @PastOrPresent Instant registerDateFrom,
                             @PastOrPresent Instant registerDateTo) {
}
