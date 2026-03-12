package com.pm.userservice.controller;

import com.pm.userservice.dto.UserProfileDto;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.dto.UserUpdateRequestDTO;
import com.pm.userservice.entity.User;
import com.pm.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/")
@Tag(name = "Users", description = "API for user management, The list of endpoints \"users\" GET - returns list of all users")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    @Operation(summary = "This method sends all users` profile ")
    public Page<User> usersList(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "registeredDate,desc") String sort,
                                @RequestParam(defaultValue = "20") int size,
                                @RequestParam(required = false) String filter) {


        Page<User> page1 = userService.userList(Math.max(page, 0), sort, size == 20 ? size : size == 50 ? size : 20, filter);

        return page1;
    }

    @GetMapping("{id}/avatar")
    public ResponseEntity<Void> getAvatar(@PathVariable UUID id) {


    }

    @GetMapping("{id}/")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable UUID id) {

        return ResponseEntity.ok().build();
    }


    //we again need confirmation of validation from kafka but it doesnt check its containing so we do it
    //Also we will get here file
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Editing existing user`s details")
    public ResponseEntity<UserResponseDTO> updatingUser(@RequestPart UserUpdateRequestDTO userUpdateRequestDTO, @RequestPart(value = "image", required = false) MultipartFile multipartFile) {

        if (userUpdateRequestDTO.getFullName().isEmpty() && userUpdateRequestDTO.getBio().isEmpty()
                && userUpdateRequestDTO.getPhoneNumber().isEmpty() && userUpdateRequestDTO.getImage().isEmpty()) {
            throw new RuntimeException("Request contains no new changes, WTF!");
        }

        UserUpdateRequestDTO updateRequestDTO;

        if (!multipartFile.isEmpty()) {

            updateRequestDTO = userUpdateRequestDTO;
            updateRequestDTO.setImage(multipartFile);
        }

        UserResponseDTO userResponseDTO = userService.userUpdating(userUpdateRequestDTO);

        return ResponseEntity.ok().body(userResponseDTO);

    }

}
