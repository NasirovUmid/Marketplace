package com.pm.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users") // automatically created "user" but its busy so ERROR thats why use @table
public class User {

    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)  we will get id from request it should be generated in auth-service already No meaning creating new one
    private UUID id;

    @Column(nullable = true)
    private String fullName;


    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = true)
    private String  bio;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Instant birthDate;

    @Column(nullable = false)
    private Instant registeredDate;
}
