package com.pm.authservice.service;

import com.pm.authservice.dto.JwtAuthenticationDto;
import com.pm.authservice.dto.RefreshTokenDto;
import com.pm.authservice.dto.UserCredentialsDto;
import com.pm.authservice.entity.User;
import com.pm.authservice.entity.UserEvent;
import com.pm.authservice.enums.Role;
import com.pm.authservice.enums.TokenType;
import com.pm.authservice.enums.UserEventType;
import com.pm.authservice.kafka.KafkaEventProducer;
import com.pm.authservice.payload.AuthResponse;
import com.pm.authservice.redis.RefreshTokenService;
import com.pm.authservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

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

    public AuthResponse login(UserCredentialsDto userCredentialsDto) throws AuthenticationException {

        Optional<User> user = findByCredentials(userCredentialsDto);

        try {

        if (user == null) return new AuthResponse("Unauthorized",false,userCredentialsDto);


        JwtAuthenticationDto jwtAuthenticationDto = jwtService.generateAuthToken(user.get().getEmail());

        refreshTokenService.save(user.get().getId().toString(), jwtAuthenticationDto.getRefreshToken(),2);

        }catch (NullPointerException e){

            System.out.println(e.getMessage());
        }

        return new AuthResponse("Tokens are created",true,userCredentialsDto.getEmail());

    }
    public User getUserByEmail(String email){

        User user = null;
        try {
            user = userRepository.findUserByEmail(email).orElseThrow(ChangeSetPersister.NotFoundException::new);
        } catch (ChangeSetPersister.NotFoundException e) {
            throw new RuntimeException(e);
        }

        return user;
    }

    //After Extracting email and finding User by that email I seek for saved RefreshToken by user.id and compare RefreshTokens

    public AuthResponse refreshToken(RefreshTokenDto refreshTokenDto) throws Exception {

        String refreshToken = refreshTokenDto.getRefreshToken();


        Claims claims = jwtService.extractClaims(refreshToken);

    if (claims.get("type") != TokenType.REFRESH)
        return new AuthResponse("Not a Refresh Token",false,refreshToken); //checking whether Refresh or not


    if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {

        User user = findByEmail(jwtService.getEmailFromToken(refreshToken));

        if (!refreshTokenService.exists(user.getId().toString())||
                !refreshTokenService.getRefreshToken(user.getId().toString()).equals(refreshTokenDto.getRefreshToken()))
            return new AuthResponse("Fake RefreshToken",false,refreshToken);



        return new AuthResponse("Token is updated", true, jwtService.refreshBaseToken(user.getEmail(), refreshToken));

       }

         return new AuthResponse("No RefreshToken",false,refreshToken);

    }

    public AuthResponse creatingUser(UserCredentialsDto userCredentialsDto) throws Exception {

            if (findByEmail(userCredentialsDto.getEmail())!=null) return new AuthResponse("Email EXISTS",false,userCredentialsDto.getEmail());

        User user = User.builder()
                        .email(userCredentialsDto.getEmail())
                                .password(passwordEncoder.encode(userCredentialsDto.getPassword()))
                                        .role(Role.ROLE_USER) //default value
                                                .build();

        // Default , if necessary should be changed manually (via update method in controller)
        User user1 = userRepository.save(user);

        refreshTokenService.save(user1.getId().toString(),user1.getEmail(),2);
        kafkaEventProducer.sendEvent(new UserEvent(user1.getId(),user1.getEmail(), UserEventType.USER_CREATED, LocalDate.now()));

        return new AuthResponse("User is created",true,user.getEmail());
    }

    private Optional<User> findByCredentials(UserCredentialsDto userCredentialsDto) throws AuthenticationException {

    Optional<User> user = userRepository.findUserByEmail(userCredentialsDto.getEmail());
    if (user.isPresent()){

        if (passwordEncoder.matches(userCredentialsDto.getPassword(), user.get().getPassword()))//First decoding and comparing
            return user;

    }
        return null;
    }

    private User findByEmail(String email) throws Exception {

        Optional<User> user = userRepository.findUserByEmail(email);
        return user.orElse(null);

    }

    public boolean validateToken(String token){

        return jwtService.validateJwtToken(token);

    }
}
