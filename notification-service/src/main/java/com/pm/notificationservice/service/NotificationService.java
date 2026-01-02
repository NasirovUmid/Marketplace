package com.pm.notificationservice.service;

import com.pm.notificationservice.entity.Notification;
import com.pm.notificationservice.entity.UserNotificationEvent;
import com.pm.notificationservice.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void saveUser(UserNotificationEvent userNotificationEvent){

        notificationRepository.save(
                Notification.builder()
                        .userId(userNotificationEvent.userId())
                        .userEmail(userNotificationEvent.userEmail())
                        .phoneNumber(userNotificationEvent.phoneNumber())
                        .event(userNotificationEvent.status())
                        .createdAt(Instant.now())
                .build());

    }

}
