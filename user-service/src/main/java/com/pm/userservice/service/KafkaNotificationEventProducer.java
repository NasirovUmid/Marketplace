package com.pm.userservice.service;

import com.pm.commonevents.UserNotificationEvent;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaNotificationEventProducer {

    private final KafkaTemplate<String , UserNotificationEvent> kafkaTemplate;
    private final Logger logger = LoggerFactory.getLogger(KafkaNotificationEventProducer.class);

    public void sendingNotificationEvent(UserNotificationEvent userNotificationEvent){

        logger.info("User Notification = [ {} ]",userNotificationEvent);

        kafkaTemplate.send("user.created",userNotificationEvent);

    }
}
