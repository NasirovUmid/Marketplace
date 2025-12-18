package com.pm.userservice.service;

import com.pm.userservice.dto.UserCreationRequestDTO;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.dto.UserUpdateRequestDTO;
import com.pm.userservice.entity.User;
import com.pm.userservice.exception.EmailAlreadyExistsException;
import com.pm.userservice.exception.UserNotFoundException;
import com.pm.userservice.kafka.KafkaEventConsumer;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.repository.UserRepository;
import org.springframework.kafka.event.KafkaEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.CONFLICT;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final KafkaEventConsumer kafkaEventConsumer;


    public UserService(UserRepository userRepository,KafkaEventConsumer kafkaEventConsumer) {
        this.userRepository = userRepository;
        this.kafkaEventConsumer = kafkaEventConsumer;
    }

    public List<UserResponseDTO> userList(){

        List<User> users = userRepository.findAll();

       return users.stream().map(UserMapper::toDTO).toList();

    }

    public UserResponseDTO userCreating(UserCreationRequestDTO creationRequestDTO){

            Optional<User> user =  userRepository.findById(creationRequestDTO.getId());

            if (user.isEmpty()) return null;

          userRepository.save(UserMapper.toCreatingModel(user.get()));

            return UserMapper.toDTO(user.get());
    }

    public UserResponseDTO userUpdating(UserUpdateRequestDTO updateRequestDTO){

        User user = userRepository.findByEmail(updateRequestDTO.getEmail());

        if (updateRequestDTO.getFullName().isPresent()) {
            user.setFullName(updateRequestDTO.getFullName().toString());
        }

        if (updateRequestDTO.getBio().isPresent()){
            user.setBio(updateRequestDTO.getBio().toString());
        }

        if (updateRequestDTO.getBirthDate().isPresent()){
            user.setBirthDate(LocalDate.parse(updateRequestDTO.getBirthDate().toString()));
        }

        if (updateRequestDTO.getPhoneNumber().isPresent()){
            user.setPhoneNumber(Integer.parseInt(updateRequestDTO.getPhoneNumber().toString()));
        }

        User editedUser = userRepository.save(user);

        return UserMapper.toDTO(editedUser);

    }

    public void deletingUser(UUID id){
        userRepository.deleteById(id);
    }

    public User findingUser(UUID id){

        return userRepository.findUserById(id);

    }

}
