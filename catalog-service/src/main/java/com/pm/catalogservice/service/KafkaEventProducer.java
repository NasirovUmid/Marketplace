package com.pm.catalogservice.service;

import com.pm.commonevents.CatalogNotificationEvent;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, CatalogNotificationEvent> kafkaTemplate;
    private final Logger logger = LoggerFactory.getLogger(KafkaEventProducer.class);

    public void sendCatalogNotification(CatalogNotificationEvent catalogNotificationEvent){

        logger.info("Created catalog = [ {} ]",catalogNotificationEvent);

        kafkaTemplate.send("catalog",catalogNotificationEvent);

    }

}
