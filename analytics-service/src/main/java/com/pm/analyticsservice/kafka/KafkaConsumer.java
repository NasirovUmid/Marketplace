package com.pm.analyticsservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaConsumer {


    private final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = "users",groupId = "analytics-service")
    public void listener(Map<String, Object> userEvent){


        log.info("User Event received: [ {} ]",userEvent);


    }

}
