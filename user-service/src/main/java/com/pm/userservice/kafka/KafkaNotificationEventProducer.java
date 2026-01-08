package com.pm.userservice.kafka;

import com.pm.commonevents.UserNotificationEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaNotificationEventProducer {

    private final KafkaTemplate<String , UserNotificationEvent> kafkaTemplate;

    public void sendingNotificationEvent(UserNotificationEvent userNotificationEvent){

        kafkaTemplate.send("user.created",userNotificationEvent);

    }
}
