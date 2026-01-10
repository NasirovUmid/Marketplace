package com.pm.authservice.controller;

import com.pm.authservice.dto.JwtAuthenticationDto;
import com.pm.authservice.dto.RefreshTokenDto;
import com.pm.authservice.dto.UserCredentialsDto;
import com.pm.authservice.payload.AuthResponse;
import com.pm.authservice.service.UserService;
import io.swagger.v3.oas.annotations.headers.Header;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/auth/")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService1) {
        this.userService = userService1;
    }

    //created new JwtToken and RefreshToken
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserCredentialsDto userCredentialsDto) throws AuthenticationException {

        AuthResponse authResponse = userService.login(userCredentialsDto);

        return authResponse.isSuccess() ? ResponseEntity.ok().body(authResponse) : ResponseEntity.unprocessableContent().body(authResponse);

    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserCredentialsDto userCredentialsDto) throws Exception {

        AuthResponse success = userService.creatingUser(userCredentialsDto);

            return success.isSuccess() ? ResponseEntity.ok().body(success) : ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();

    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) throws Exception {

            return ResponseEntity.ok().body(userService.refreshToken(refreshTokenDto));

    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validate(@RequestHeader("Authorization")String header){

        // Authorization: Bearer <token>

        if (header==null || !header.startsWith("Bearer ")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userService.validateToken(header.substring(7)) ?
                ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    }

    // I had problem with redis and here we ar , this method for checking redis password
    @PostConstruct
    public void checkRedis() {
        System.out.println("REDIS PASSWORD = " +
                System.getenv("SPRING_DATA_REDIS_PASSWORD"));
    }

    // Email cant be changed , new emails are registered but passwords
    // so in the future i can add update password its simple but probably I can skip it ,just it`s bare useful


}
