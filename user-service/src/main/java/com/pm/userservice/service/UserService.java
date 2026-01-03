package com.pm.userservice.service;

import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.dto.UserUpdateRequestDTO;
import com.pm.userservice.entity.User;
import com.pm.userservice.entity.UserNotificationEvent;
import com.pm.userservice.enums.UserEventType;
import com.pm.userservice.kafka.KafkaEventConsumer;
import com.pm.userservice.kafka.KafkaNotificationEventProducer;
import com.pm.userservice.kafka.UserEvent;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final KafkaEventConsumer kafkaEventConsumer;
    private final KafkaNotificationEventProducer kafkaNotificationEventProducer;


    public UserService(UserRepository userRepository, KafkaEventConsumer kafkaEventConsumer, KafkaNotificationEventProducer kafkaNotificationEventProducer) {
        this.userRepository = userRepository;
        this.kafkaEventConsumer = kafkaEventConsumer;
        this.kafkaNotificationEventProducer = kafkaNotificationEventProducer;
    }

    public List<UserResponseDTO> userList(){

        List<User> users = userRepository.findAll();

       return users.stream().map(UserMapper::toDTO).toList();

    }

    public void userCreating(UserEvent userEvent){

        userRepository.save(new User(userEvent.id(), null,
                userEvent.email(), null, null, "C:\\Java\\27\\monke.jpg", userEvent.birthDate(), userEvent.timeOfCreation()));
    }

    public UserResponseDTO userUpdating(UserUpdateRequestDTO updateRequestDTO){

        User user = userRepository.findByEmail(updateRequestDTO.getEmail());

        if (updateRequestDTO.getFullName().isPresent()) {
            user.setFullName(updateRequestDTO.getFullName().toString());
        }

        // IF ITS NULL MAYBE USER WANT TO REMOVE BIO?
            user.setBio(updateRequestDTO.getBio().toString());


        // I WILL CHANGE USEREVENTS AND WILL MAKE OBLIGATORY IN REGISTRATION WRITE BITRHDATE SO HERE SHOULD NOT BE CHANCE TO CHANGE IT
        //    user.setBirthDate(LocalDate.parse(updateRequestDTO.getBirthDate().toString())); I DID AT THE MOMENT

        // I WANNA MAKE PHONE NUMBER OBLIGATORY SO IN FUTURE I CAN USE IN TWILIO
        if (updateRequestDTO.getPhoneNumber().isPresent()){
            user.setPhoneNumber(Integer.parseInt(updateRequestDTO.getPhoneNumber().toString()));
        }

        User editedUser = userRepository.save(user);

        kafkaNotificationEventProducer.sendingNotificationEvent(
                new UserNotificationEvent(editedUser.getId(), editedUser.getEmail(), editedUser.getPhoneNumber().toString(), UserEventType.USER_CREATED.name(),editedUser.getRegisteredDate()));


        return UserMapper.toDTO(editedUser);

    }


    public void deletingUser(UUID id){
        userRepository.deleteById(id);
    }


}
