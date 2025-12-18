package com.pm.userservice.controller;

import com.pm.userservice.dto.UserCreationRequestDTO;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.dto.UserUpdateRequestDTO;
import com.pm.userservice.entity.User;
import com.pm.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "API for user management")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "This method sends all users` profile")
    public ResponseEntity<List<UserResponseDTO>> usersList(){

    return ResponseEntity.ok().body(userService.userList());

    }


    //we need confirmation from kafka that user was created
    //we need not worry if user already exists then auth-service denies itself and request wont reach here
    @PostMapping
    @Operation(summary = "Creating a new User`s profile")
    public ResponseEntity<UserResponseDTO> userCreating(@RequestBody UserCreationRequestDTO userCreationRequestDTO){

        UserResponseDTO user = userService.userCreating(userCreationRequestDTO);

        if (user == null) ResponseEntity.status(HttpStatus.CONFLICT).body("Try later, the confirmation hasnt come yet ");

        return ResponseEntity.ok().body(user);

    }


    //we again need confirmation of validation from kafka but it doesnt check its containing so we do it
    //Also we will get here file
    @PutMapping("/update-user/{id}")
    @Operation(summary = "Editing existing user`s details")
    public ResponseEntity<UserResponseDTO> updatingUser(@RequestBody UserUpdateRequestDTO userUpdateRequestDTO){

        if (userUpdateRequestDTO.getFullName().isEmpty() && userUpdateRequestDTO.getBio().isEmpty()
        && userUpdateRequestDTO.getPhoneNumber().isEmpty() && userUpdateRequestDTO.getBirthDate().isEmpty()){
            throw new RuntimeException("Request contains no new changes, WTF!");
        }

         UserResponseDTO userResponseDTO = userService.userUpdating(userUpdateRequestDTO);

        return ResponseEntity.ok().body(userResponseDTO);

    }

}
