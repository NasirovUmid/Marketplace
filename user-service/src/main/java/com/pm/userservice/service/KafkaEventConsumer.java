package com.pm.userservice.service;

import com.pm.commonevents.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventConsumer {

    private final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);
    private final UserService userService;

    public KafkaEventConsumer(UserService userService) {
        this.userService = userService;
    }


    //  Anyways i get EMAIL and ID from auth-service though its vreating new one or updating I WILL DIFFER BY EVENT
    //  I will create user id here
    @KafkaListener(topics = "users",groupId = "user-service")
    public void kafkaConsumeEvent(UserEvent userEvent){


        log.info("User Event : [User Id = {} , Email = {} , EventTYPE = {}, Date = {}, From = {}]",
                userEvent.id(),userEvent.email(),userEvent.eventType(),userEvent.timeOfCreation(),userEvent.address());

        userService.userCreating(userEvent);


         // I gave identical ID from auth-service user.id and email , also default values

    }

}
