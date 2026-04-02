package com.pm.authservice;

import com.pm.authservice.controller.AuthController;
import com.pm.authservice.dto.*;
import com.pm.authservice.exception.GlobalExceptionHandler;
import com.pm.authservice.exception.InvalidTokenException;
import com.pm.authservice.filter.JwtFilter;
import com.pm.authservice.service.UserService;
import com.pm.commonevents.exception.AlreadyExistsException;
import com.pm.commonevents.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Test
    void register_ShouldReturn201_WhenUserCreated() throws Exception {

        CreationRequest creationRequest = new CreationRequest(
                "lixie@gmail.com",
                "mangos123",
                Instant.parse("2000-01-01T00:00:00Z"),
                "+998933082568"
        );

        UserCreationResponseDto userCreationResponseDto = new UserCreationResponseDto(
                UUID.randomUUID(),
                "access",
                "refresh"
        );

        when(userService.creatingUser(creationRequest)).thenReturn(userCreationResponseDto);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(userCreationResponseDto)));

        verify(userService).creatingUser(creationRequest);

    }

    @Test
    void register_ShouldReturn409_WhenUserAlreadyExists() throws Exception {

        CreationRequest creationRequest = new CreationRequest(
                "lixie@gmail.com",
                "mangos123",
                Instant.parse("2000-01-01T00:00:00Z"),
                "+998933082568"
        );

        when(userService.creatingUser(any(CreationRequest.class))).thenThrow(new AlreadyExistsException("User with email = " + creationRequest.email()));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("User with email = " + creationRequest.email() + " ALREADY EXISTS !!"));

        verify(userService).creatingUser(creationRequest);

    }

    @Test
    void login_ShouldReturn200_CredentialsValid() throws Exception {

        UserCredentialsDto userCredentialsDto = new UserCredentialsDto(
                "lihiam@gmail.com",
                "tangos123"
        );

        JwtAuthenticationDto jwtAuthenticationDto = new JwtAuthenticationDto("access", "refresh");

        when(userService.login(userCredentialsDto)).thenReturn(jwtAuthenticationDto);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCredentialsDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(jwtAuthenticationDto)));

        verify(userService).login(userCredentialsDto);

    }

    @Test
    void login_ShouldReturn404_UserNotFound() throws Exception {

        UserCredentialsDto userCredentialsDto = new UserCredentialsDto(
                "lihiam@gmail.com",
                "tangos123"
        );

        when(userService.login(userCredentialsDto)).thenThrow(new NotFoundException("User with Email " + userCredentialsDto.email()));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCredentialsDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("not_found_exception"))
                .andExpect(jsonPath("$.title").value("Not Found"));

        verify(userService).login(userCredentialsDto);

    }

    @Test
    void refreshToken_ShouldReturn200_WhenTokenRefreshed() throws Exception {

        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(
                "refreshed-refreshtoken"
        );

        JwtAuthenticationDto jwtAuthenticationDto = new JwtAuthenticationDto(
                "refreshed-refreshtoken",
                "new-accestoken"
        );

        when(userService.refreshToken(refreshTokenDto)).thenReturn(jwtAuthenticationDto);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(jwtAuthenticationDto)));

        verify(userService).refreshToken(refreshTokenDto);
    }

    @Test
    void refreshToken_ShouldReturn403_WhenTokenIsExpired() throws Exception {

        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(
                "refreshed-refreshtoken"
        );

        when(userService.refreshToken(refreshTokenDto)).thenThrow(new InvalidTokenException(refreshTokenDto.refreshToken()));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("invalid_token_exception"));

        verify(userService).refreshToken(refreshTokenDto);
    }

    @Test
    void validate_ShouldReturn200_WhenTokenIsValid() throws Exception {

        String bearer = "Bearer fake.jwt.token";

        mockMvc.perform(post("/auth/validate")
                        .header("Authorization", bearer))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).validateToken(anyString());

    }

    @Test
    void logOut_ShouldReturn200_WhenLoggedOutSuccessfully() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/auth/log-out/{id}", id))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).logOut(String.valueOf(id));

    }

    @Test
    void changePassword_ShouldReturn200_WhenPasswordSuccessfullyChanged() throws Exception {

        ChangePasswordDto changePasswordDto = new ChangePasswordDto(
                "trixie@gmail.com",
                "oldpassword",
                "newpassword"
        );

        mockMvc.perform(patch("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).changePassword(changePasswordDto);
    }
}
