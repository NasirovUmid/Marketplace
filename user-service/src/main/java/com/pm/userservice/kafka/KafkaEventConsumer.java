package com.pm.userservice.kafka;

import com.pm.userservice.dto.UserCreationRequestDTO;
import com.pm.userservice.entity.User;
import com.pm.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventConsumer {

    private final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);


    //  Anyways i get EMAIL and ID from auth-service though its vreating new one or updating I WILL DIFFER BY EVENT
    @KafkaListener(topics = "users",groupId = "user-service")
    public void kafkaConsumeEvent(UserEvent userEvent){


        log.info("User Event : [User Id = {} , Email = {} , EventTYPE = {}]",
                userEvent.id(),userEvent.email(),userEvent.eventType());

    }

}
