package com.pm.authservice;

import com.pm.authservice.dto.*;
import com.pm.authservice.entity.User;
import com.pm.authservice.enums.Role;
import com.pm.authservice.enums.TokenType;
import com.pm.authservice.redis.RefreshTokenService;
import com.pm.authservice.repository.UserRepository;
import com.pm.authservice.service.JwtService;
import com.pm.authservice.service.KafkaEventProducer;
import com.pm.authservice.service.UserService;
import io.jsonwebtoken.Claims;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @Mock
    private KafkaEventProducer kafkaEventProducer;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void testing_CreationRequest() {

        UUID userId = UUID.randomUUID();
        String accessToken = "accesstoken";
        String refreshToken = "refreshToken";

        CreationRequest creationRequest = new CreationRequest(
                "prix@gmail.com",
                "apples",
                Instant.now().minusSeconds(3600),
                "998933082555"
        );

        User user = new User(
                userId,
                "prix@gmail.com",
                "apples",
                Role.ROLE_USER);

        when(userRepository.existsByEmail(creationRequest.email())).thenReturn(false);
        when(jwtService.generateAuthToken(anyString())).thenReturn(new JwtAuthenticationDto(accessToken, refreshToken));
        when(passwordEncoder.encode(anyString())).thenReturn(creationRequest.password());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserCreationResponseDto userCreationResponseDto = userService.creatingUser(creationRequest);

        assertThat(userCreationResponseDto).isNotNull();
        assertThat(userCreationResponseDto.accessToken()).isEqualTo(accessToken);
        assertThat(userCreationResponseDto.refreshToken()).isEqualTo(refreshToken);

        System.out.println(userCreationResponseDto);
    }

    @Test
    void testing_Login() {

        UUID userId = UUID.randomUUID();
        String accessToken = "accesstoken";
        String refreshToken = "refreshToken";

        UserCredentialsDto userCredentialsDto = new UserCredentialsDto(
                "rixie@gmail.com",
                "apples"
        );

        User user = new User(
                userId,
                "rixie@gmail.com",
                "apples",
                Role.ROLE_USER);

        when(userRepository.findUserByEmail(userCredentialsDto.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAuthToken(anyString())).thenReturn(new JwtAuthenticationDto(accessToken, refreshToken));

        JwtAuthenticationDto jwtAuthenticationDto = userService.login(userCredentialsDto);

        assertThat(jwtAuthenticationDto).isNotNull();
        assertThat(jwtAuthenticationDto.token()).isEqualTo(accessToken);
        assertThat(jwtAuthenticationDto.refreshToken()).isEqualTo(refreshToken);

        System.out.println(jwtAuthenticationDto.token() + "  " + jwtAuthenticationDto.refreshToken());
    }

    @Test
    void testing_RefreshToken() {

        UUID userId = UUID.randomUUID();
        String newAccessToken = UUID.randomUUID().toString();

        RefreshTokenDto refreshTokenDto = new RefreshTokenDto("oldrefreshtoken");
        Claims claims = mock(Claims.class);
        User user = new User(
                userId,
                "rixie@gmail.com",
                "apples",
                Role.ROLE_USER);


        when(jwtService.extractClaims(anyString())).thenReturn(claims);
        when(claims.get("type")).thenReturn(TokenType.REFRESH);
        when(jwtService.getEmailFromToken(refreshTokenDto.refreshToken())).thenReturn(user.getEmail());
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(refreshTokenService.exists(user.getId().toString())).thenReturn(true);
        when(refreshTokenService.getRefreshToken(userId.toString())).thenReturn(refreshTokenDto.refreshToken());
        when(jwtService.refreshBaseToken(user.getEmail(), refreshTokenDto.refreshToken())).
                thenReturn(new JwtAuthenticationDto(newAccessToken, refreshTokenDto.refreshToken()));

        JwtAuthenticationDto result = userService.refreshToken(refreshTokenDto);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(newAccessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshTokenDto.refreshToken());

    }

    @Test
    void testing_LogOut() {

        String id = UUID.randomUUID().toString();

        when(refreshTokenService.exists(id.toString())).thenReturn(true);

        userService.logOut(id);

    }

    @Test
    void testing_ChangePassword() throws BadRequestException {

        UUID userId = UUID.randomUUID();

        ChangePasswordDto changePasswordDto = new ChangePasswordDto(
                "rixie@gmail.com",
                "apples",
                "bananas"
        );

        User user = new User(
                userId,
                "rixie@gmail.com",
                "apples",
                Role.ROLE_USER);

        when(userRepository.findUserByEmail(changePasswordDto.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        userService.changePassword(changePasswordDto);
    }
}
