package com.pm.userservice.service;

import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.entity.User;
import com.pm.userservice.mapper.UserMapper;
import com.pm.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class UserService {

    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponseDTO> userList(){

        List<User> users = userRepository.findAll();

       return users.stream().map(UserMapper::toDTO).toList();

    }
}
