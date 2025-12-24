package com.pm.authservice.kafka;

import com.pm.authservice.entity.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventProducer {

    private final Logger logger = LoggerFactory.getLogger(KafkaEventProducer.class);
    private final KafkaTemplate<String , UserEvent> kafkaTemplate;

    public KafkaEventProducer(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(UserEvent userEvent){

            try{

                 kafkaTemplate.send("users",userEvent);

                }catch (Exception exception){

                  logger.error("Error {} Sending USER EVENT -> :{}",exception,userEvent);

            }
    }
}
