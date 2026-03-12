package com.pm.authservice.service;

import com.pm.authservice.dto.ChangePasswordDto;
import com.pm.authservice.dto.JwtAuthenticationDto;
import com.pm.authservice.dto.RefreshTokenDto;
import com.pm.authservice.dto.UserCredentialsDto;
import com.pm.authservice.entity.User;
import com.pm.authservice.exception.InvalidTokenException;
import com.pm.commonevents.UserEvent;
import com.pm.authservice.enums.Role;
import com.pm.authservice.enums.TokenType;
import com.pm.commonevents.enums.UserEventType;
import com.pm.authservice.payload.AuthResponse;
import com.pm.authservice.redis.RefreshTokenService;
import com.pm.authservice.repository.UserRepository;
import com.pm.commonevents.exception.AlreadyExistsException;
import com.pm.commonevents.exception.InternalProblemException;
import com.pm.commonevents.exception.NotFoundException;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public AuthResponse login(UserCredentialsDto userCredentialsDto) {

        User user = findByCredentials(userCredentialsDto);
        if (user == null) throw new NotFoundException("LOGIN: User with [ " + userCredentialsDto.getEmail() + " ]");


        JwtAuthenticationDto jwtAuthenticationDto = jwtService.generateAuthToken(user.getEmail());

        // Redis method
        refreshTokenService.save(user.getId().toString(), jwtAuthenticationDto.getRefreshToken(), 2);


        return new AuthResponse("Tokens are created =  'User login' ", true, jwtAuthenticationDto);

    }

    //After Extracting email and finding User by that email I seek for saved RefreshToken by user.id and compare RefreshTokens

    public AuthResponse refreshToken(RefreshTokenDto refreshTokenDto) {

        String refreshToken = refreshTokenDto.getRefreshToken();

        Claims claims = jwtService.extractClaims(refreshToken);

        if (claims.get("type") != TokenType.REFRESH)
            //return new AuthResponse("Not a Refresh Token",false,refreshToken); //checking whether Refresh or not
            throw new InvalidTokenException(refreshTokenDto.getRefreshToken());

        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {

            User user = findByEmail(jwtService.getEmailFromToken(refreshToken));

            //checkingRedis
            boolean exists = refreshTokenService.exists(user.getId().toString());

            if (!exists)
                //return new AuthResponse("Fake RefreshToken",false,refreshToken);
                throw new InvalidTokenException(refreshTokenDto.getRefreshToken());

            String refresh = refreshTokenService.getRefreshToken(user.getId().toString());


            return new AuthResponse("Token is updated", true, jwtService.refreshBaseToken(user.getEmail(), refresh));

        }

        // return new AuthResponse("RefreshToken is expired , Login is needed",false,refreshToken);

        throw new InvalidTokenException(refreshTokenDto.getRefreshToken());
    }

    public AuthResponse creatingUser(UserCredentialsDto userCredentialsDto) {

        if (findByEmail(userCredentialsDto.getEmail()) != null) {
            throw new AlreadyExistsException("REGISTRATION: User with = [ " + userCredentialsDto.getEmail() + " ] ");
            //        return new AuthResponse("Email EXISTS",false,userCredentialsDto.getEmail());
        }


        // Default , if necessary should be changed manually (via update method in controller)
        User user1 = userRepository.save(User.builder()
                .email(userCredentialsDto.getEmail())
                .password(passwordEncoder.encode(userCredentialsDto.getPassword()))
                .role(Role.ROLE_USER) //default value
                .build());

        JwtAuthenticationDto jwtAuthenticationDto = jwtService.generateAuthToken(user1.getEmail());

        refreshTokenService.save(user1.getId().toString(), jwtAuthenticationDto.getRefreshToken(), 2);

        kafkaEventProducer.sendEvent(new UserEvent(user1.getId(), user1.getEmail(), userCredentialsDto.getPhoneNumber(), UserEventType.USER_CREATED, Instant.now(), "Auth-service", userCredentialsDto.getBirthDate()));

        return new AuthResponse("User is created", true, jwtAuthenticationDto);
    }

    private User findByCredentials(UserCredentialsDto userCredentialsDto) {

        Optional<User> user = userRepository.findUserByEmail(userCredentialsDto.getEmail());
        if (user.isPresent()) {

            if (passwordEncoder.matches(userCredentialsDto.getPassword(), user.get().getPassword()))//First decoding and comparing
                return user.get();

        }
        return null;
    }

    private User findByEmail(String email) {

        Optional<User> user = userRepository.findUserByEmail(email);
        return user.orElse(null);

    }

    public boolean validateToken(String token) {

        return jwtService.validateJwtToken(token);

    }

    public boolean logOut(UUID uuid) {

        if (!refreshTokenService.exists(String.valueOf(uuid)))
            throw new NotFoundException("The id for logging out was ");

        return refreshTokenService.deleteById(String.valueOf(uuid));
    }

    public boolean changePassword(ChangePasswordDto changePasswordDto) {

        User user = findByCredentials(new UserCredentialsDto(changePasswordDto.email(), changePasswordDto.oldPassword(), null, null));

        if (user != null) {

            user.setPassword(changePasswordDto.newPassword());
            userRepository.save(user);
            refreshTokenService.deleteById(String.valueOf(user.getId()));
            return true;
        }

        return false;
    }
}
