package com.pm.authservice.controller;

import com.pm.authservice.dto.*;
import com.pm.authservice.service.UserService;
import com.pm.commonevents.exception.ApiProblem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth/")
@Tag(name = "Auth-service", description = "API for authentication of users, The list of endpoints /login, /register, /refresh, /validate, /log-out/{id}, /change-password")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Login in existing account", description = "Entering already created account to get tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns new Tokens ( Access token && Refresh token )",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtAuthenticationDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @PostMapping("login")
    public ResponseEntity<JwtAuthenticationDto> login(@Valid @RequestBody UserCredentialsDto userCredentialsDto) {

        JwtAuthenticationDto jwtAuthenticationDto = userService.login(userCredentialsDto);

        return ResponseEntity.ok().body(jwtAuthenticationDto);
    }

    @Operation(summary = "Create account", description = "Creates new Account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New user successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserCreationResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "User already exists",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @PostMapping("register")
    public ResponseEntity<UserCreationResponseDto> register(@Valid @RequestBody CreationRequest creationRequest) {

        UserCreationResponseDto userCreationResponseDto = userService.creatingUser(creationRequest);

        return ResponseEntity.status(201).body(userCreationResponseDto);
    }

    @Operation(summary = "Refreshing AccessToken", description = "Returns new Access token and old Refresh Token ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refreshes token",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtAuthenticationDto.class))),
            @ApiResponse(responseCode = "401", description = "Expired Refresh Token",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class))),
            @ApiResponse(responseCode = "401", description = "Invalid Refresh Token",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @PostMapping("refresh")
    public ResponseEntity<JwtAuthenticationDto> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {

        JwtAuthenticationDto jwtAuthenticationDto = userService.refreshToken(refreshTokenDto);

        return ResponseEntity.ok().body(jwtAuthenticationDto);
    }

    @Operation(summary = "Validation of Token", description = "Validates Access Token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The token is valid",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = " Token is no valid",
                    content = @Content(mediaType = "application/problem + json"))
    })
    @PostMapping("validate")
    public ResponseEntity<Void> validate(@RequestHeader("Authorization") String header) {

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userService.validateToken(header.substring(7));

        return ResponseEntity.ok().build();
    }

    @PostConstruct
    public void checkRedis() {
        System.out.println("REDIS PASSWORD = " +
                System.getenv("SPRING_DATA_REDIS_PASSWORD"));
    }

    @Operation(summary = "Logging Out", description = "Logging out from account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refresh Token invalidated"),
            @ApiResponse(responseCode = "404", description = "The Refresh Token was not found / expired",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @PostMapping("log-out")
    public ResponseEntity<Void> logOut(@RequestHeader("Authorization") String token) {

        userService.logOut(token);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Changes Password", description = "Changes Password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password successfully changed"),
            @ApiResponse(responseCode = "404", description = "Email was not found",
                    content = @Content(mediaType = "application/problem + json",
                            schema = @Schema(implementation = ApiProblem.class)))
    })
    @PatchMapping("change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) throws BadRequestException {

        userService.changePassword(changePasswordDto);

        return ResponseEntity.ok().build();
    }
}
