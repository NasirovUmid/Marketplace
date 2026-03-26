package com.pm.userservice.controller;

import com.pm.commonevents.exception.ApiProblem;
import com.pm.userservice.dto.UserProfileDto;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.dto.UserUpdateRequestDTO;
import com.pm.userservice.entity.User;
import com.pm.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "API for user management, The list of endpoints /users, /update/{id}, /me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "List user (admin only) ",
            description = "Returns List of 20/50 users, supports page index, sorting and filter by email , registeredDateFrom | registeredDateTo and birthDateFrom | birthDateTo",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of users",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected Error",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @GetMapping()
    public Page<User> usersList(
            @RequestParam(defaultValue = "0",name = "page") int page,
            @Parameter(description = "Sorting. Allowed: registeredDate,desc | registeredDate,asc", example = "registeredDate,desc") @RequestParam(defaultValue = "registeredDate,desc",name = "sort") String sort,
            @Parameter(description = "Size. Allowed: 20 | 50 , by default: 20", example = "20") @RequestParam(defaultValue = "20",name = "size") int size,
            @RequestParam(required = false,name = "email") String email,
            @RequestParam(required = false,name = "birthDateFrom") Instant birthDateFrom,
            @RequestParam(required = false,name = "birthDateTo") Instant birthDateTo,
            @RequestParam(required = false,name = "registerDateFrom") Instant registerDateFrom,
            @RequestParam(required = false,name = "registerDateTo") Instant registerDateTo
    ) {

        Page<User> page1 = userService.userList(Math.max(page, 0), sort, size == 20 ? size : size == 50 ? size : 20, email, birthDateFrom, birthDateTo, registerDateFrom, registerDateTo);

        return page1;
    }

    @Operation(summary = "Update user(user only) ",
            description = "Updates user`s personal profile and it is waited to request include image",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updating user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User was not found in database",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDTO> updatingUser(
            @RequestPart("data") UserUpdateRequestDTO userUpdateRequestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        UserResponseDTO userResponseDTO = userService.userUpdating(userUpdateRequestDTO,image);

        return ResponseEntity.ok().body(userResponseDTO);

    }

    @Operation(summary = "Returns exact User (user only)",
            description = "Returns user`s personal details, also avatar image included",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns User",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "404", description = "the user was not found in database",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getUserProfile(@RequestHeader("X-User-Id") UUID userId) {

        UserProfileDto userProfile = userService.getUserProfile(userId);

        return ResponseEntity.ok().body(userProfile);
    }

}
