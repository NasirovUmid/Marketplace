package com.pm.userservice.kafka;

import com.pm.userservice.entity.User;
import com.pm.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class KafkaEventConsumer {

    private final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);
    private final UserRepository userRepository;

    public KafkaEventConsumer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    //  Anyways i get EMAIL and ID from auth-service though its vreating new one or updating I WILL DIFFER BY EVENT
    //  I will create user id here
    @KafkaListener(topics = "users",groupId = "user-service")
    public void kafkaConsumeEvent(UserEvent userEvent){


        log.info("User Event : [User Id = {} , Email = {} , EventTYPE = {}, Date = {}]",
                userEvent.id(),userEvent.email(),userEvent.eventType(),userEvent.localDate());

         userRepository.save(new User(userEvent.id(), null,
                 userEvent.email(), null, null, "C:\\Java\\27\\monke.jpg", null, LocalDate.now()));

         // I gave identical ID from auth-service user.id and email , also default values

    }

}
