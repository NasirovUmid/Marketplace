package com.pm.analyticservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventConsumer {

    private final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    @KafkaListener(topics = "users",groupId = "analytics-service")
    public void kafkaConsumerEvent(UserEvent userEvent){


        log.info("User Event received: [ {} ]",userEvent);

       log.info("User Event : [User Id = {} , Email = {} , EventTYPE = {}, Date = {}, From = {}]",
                userEvent.id(),userEvent.email(),userEvent.eventType(),userEvent.timeOfCreation(),userEvent.address());



    }

}
