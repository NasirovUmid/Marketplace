package com.pm.authservice.controller;

import com.pm.authservice.dto.JwtAuthenticationDto;
import com.pm.authservice.dto.RefreshTokenDto;
import com.pm.authservice.dto.UserCredentialsDto;
import com.pm.authservice.entity.User;
import com.pm.authservice.payload.AuthResponse;
import com.pm.authservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        JwtAuthenticationDto jwtAuthenticationDto = userService.login(userCredentialsDto);

        return ResponseEntity.ok().body(new AuthResponse("Jwt token and Refresh token are Created",true,jwtAuthenticationDto));

    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserCredentialsDto userCredentialsDto) throws Exception {


            return ResponseEntity.ok().body(userService.creatingUser(userCredentialsDto));


    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) throws Exception {


            return ResponseEntity.ok().body(userService.refreshToken(refreshTokenDto));


    }



}
