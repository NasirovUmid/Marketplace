package com.pm.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(nullable = true)
    private String fullName;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = true)
    private String  bio;

    @Column(nullable = true)
    private String imageUrl;

    @Column(nullable = false)
    private Instant birthDate;

    @Column(nullable = false)
    private Instant registeredDate;
}
