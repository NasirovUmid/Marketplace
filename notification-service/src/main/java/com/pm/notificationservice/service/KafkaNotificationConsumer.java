package com.pm.notificationservice.service;

import com.pm.notificationservice.entity.UserNotificationEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaNotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user.created")
    public void userCreated(UserNotificationEvent userNotificationEvent){

    notificationService.saveUser(userNotificationEvent);

    }


}
