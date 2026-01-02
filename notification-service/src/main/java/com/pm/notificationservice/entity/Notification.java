package com.pm.notificationservice.entity;

import com.pm.notificationservice.enums.NotificationEventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID notificationId;

    private UUID userId;

    private UUID catalogId;

    private String catalogName;

    private String userEmail;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private NotificationEventStatus event;

    private Instant createdAt;

}
