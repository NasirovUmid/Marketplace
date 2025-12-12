package com.pm.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Email
    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = true)
    private Integer phoneNumber;

    @Column(nullable = true)
    private StringBuffer bio;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private LocalDate registeredDate;
}
