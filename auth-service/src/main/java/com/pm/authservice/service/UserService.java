package com.pm.authservice.service;

import com.pm.authservice.dto.*;
import com.pm.authservice.entity.User;
import com.pm.authservice.enums.Role;
import com.pm.authservice.enums.TokenType;
import com.pm.authservice.exception.InvalidTokenException;
import com.pm.authservice.redis.RefreshTokenService;
import com.pm.authservice.repository.UserRepository;
import com.pm.commonevents.UserEvent;
import com.pm.commonevents.enums.UserEventType;
import com.pm.commonevents.exception.AlreadyExistsException;
import com.pm.commonevents.exception.NotFoundException;
import io.jsonwebtoken.Claims;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final KafkaEventProducer kafkaEventProducer;
    private final RefreshTokenService refreshTokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, KafkaEventProducer kafkaEventProducer, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.kafkaEventProducer = kafkaEventProducer;
        this.refreshTokenService = refreshTokenService;
    }

    public JwtAuthenticationDto login(UserCredentialsDto userCredentialsDto) {

        User user = findByCredentials(userCredentialsDto);

        JwtAuthenticationDto jwtAuthenticationDto = jwtService.generateAuthToken(user.getEmail());

        refreshTokenService.save(user.getId().toString(), jwtAuthenticationDto.refreshToken(), 2);

        return jwtAuthenticationDto;
    }


    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) {

        String refreshToken = refreshTokenDto.refreshToken();

        Claims claims = jwtService.extractClaims(refreshToken);
        String type = claims.get("type", String.class);

        if (!TokenType.REFRESH.name().equals(type))
            throw new InvalidTokenException(refreshTokenDto.refreshToken());

        jwtService.validateJwtToken(refreshToken);

        if (refreshToken != null) {

            User user = findByEmail(jwtService.getEmailFromToken(refreshToken));

            boolean exists = refreshTokenService.exists(user.getId().toString());

            if (!exists)
                throw new InvalidTokenException(refreshTokenDto.refreshToken());

            String refresh = refreshTokenService.getRefreshToken(user.getId().toString());

            return jwtService.refreshBaseToken(user.getEmail(), refresh);
        }

        throw new InvalidTokenException(null);
    }

    @Transactional
    public UserCreationResponseDto creatingUser(CreationRequest creatingRequest) {

        validateIdempotency(creatingRequest.email());

        User user1 = userRepository.save(User.builder()
                .email(creatingRequest.email())
                .password(passwordEncoder.encode(creatingRequest.password()))
                .role(Role.ROLE_USER) //default value
                .build());

        JwtAuthenticationDto jwtAuthenticationDto = jwtService.generateAuthToken(user1.getEmail());

        refreshTokenService.save(user1.getId().toString(), jwtAuthenticationDto.refreshToken(), 2);

        kafkaEventProducer.sendEvent(new UserEvent(user1.getId(), user1.getEmail(), creatingRequest.phoneNumber(), UserEventType.USER_CREATED, Instant.now(), "Auth-service", creatingRequest.birthDate()));

        return new UserCreationResponseDto(user1.getId(), jwtAuthenticationDto.token(), jwtAuthenticationDto.refreshToken());
    }

    private User findByCredentials(UserCredentialsDto userCredentialsDto) {

        User user = userRepository.findUserByEmail(userCredentialsDto.email()).orElseThrow(() -> new NotFoundException("LOGIN: User with [ " + userCredentialsDto.email() + " ]"));

        if (!passwordEncoder.matches(userCredentialsDto.password(), user.getPassword())) {//First decoding and comparing
            throw new BadCredentialsException("Email or Password is invalid");
        }

        return user;
    }

    private User findByEmail(String email) {

        Optional<User> user = userRepository.findUserByEmail(email);
        return user.orElseThrow(() -> new NotFoundException("User with email = " + email));
    }

    private void validateIdempotency(String email) {

        if (userRepository.existsByEmail(email)) {
            throw new AlreadyExistsException("REGISTRATION: User with = [ " + email + " ] ");
        }
    }

    public void validateToken(String token) {

        jwtService.validateJwtToken(token);

    }

    public void logOut(String token) {

        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            throw new InvalidTokenException(token);
        }

        String emailFromToken = jwtService.getEmailFromToken(token.substring(7));

        UUID userId = findByEmail(emailFromToken).getId();

        if (!refreshTokenService.exists(String.valueOf(userId)))
            throw new InvalidTokenException("The id for logging out was ");

        refreshTokenService.deleteById(String.valueOf(userId));
    }

    public void changePassword(ChangePasswordDto changePasswordDto) throws BadRequestException {

        if (changePasswordDto.oldPassword().equals(changePasswordDto.newPassword())) {
            throw new BadRequestException();
        }

        User user = findByCredentials(new UserCredentialsDto(changePasswordDto.email(), changePasswordDto.oldPassword()));

        if (passwordEncoder.matches(changePasswordDto.oldPassword(), user.getPassword())) {

            user.setPassword(passwordEncoder.encode(changePasswordDto.newPassword()));
            userRepository.save(user);
            refreshTokenService.deleteById(String.valueOf(user.getId()));
            return;
        }

        throw new BadCredentialsException("Wrong  username or password");

    }
}
