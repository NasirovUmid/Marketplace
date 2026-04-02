package com.pm.authservice;

import com.pm.authservice.dto.*;
import com.pm.authservice.entity.User;
import com.pm.authservice.enums.Role;
import com.pm.authservice.redis.RefreshTokenService;
import com.pm.authservice.repository.UserRepository;
import com.pm.authservice.service.JwtService;
import com.pm.authservice.service.KafkaEventProducer;
import com.pm.authservice.service.UserService;
import com.pm.commonevents.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthIntegrationTests {


    @MockitoBean
    private KafkaEventProducer kafkaEventProducer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    // 2. Контейнер Redis (подстраиваем под себя)
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withEnv("REDIS_PASSWORD", "mypassword") // Устанавливаем свой пароль
            .withCommand("redis-server --requirepass mypassword");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        if (!postgreSQLContainer.isRunning()) {
            postgreSQLContainer.start();
        }
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.data.redis.password", () -> "mypassword");
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }


    @Test
    @DisplayName("POST /auth/register should save new user and send event to user-service via kafka also store refreshtoken in redis")
    void createUser_ShouldSaveUserAndSendEventViaKafkaAndSaveRefreshTokenInRedis() throws Exception {

        CreationRequest creationRequest = new CreationRequest(
                "trixie@gmail.com",
                "breeds12345",
                Instant.now().minusSeconds(3600),
                "998933082555"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationRequest)))
                .andDo(print())
                .andExpect(status().isCreated());

        User user = userRepository.findAll().getFirst();

        System.out.println(user);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(creationRequest.email());
        System.out.println(user.getPassword() + "    " + creationRequest.password());
        assertThat(passwordEncoder.matches(user.getPassword(), creationRequest.password()));

        ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);

        verify(kafkaEventProducer).sendEvent(captor.capture());

        UserEvent userEvent = captor.getValue();

        assertThat(userEvent).isNotNull();
        assertThat(userEvent.id()).isEqualTo(user.getId());
        assertThat(userEvent.email()).isEqualTo(user.getEmail());

        log.info(userEvent.toString());
    }

    @Test
    @DisplayName("POST /register Sends already existing email and triggers AlreadyExistsException")
    void createUser_ShouldAttemptToSaveExistingUser() throws Exception {

        userRepository.save(User.builder()
                .email("trililiye@gmail.com")
                .password(passwordEncoder.encode("banane123"))
                .role(Role.ROLE_USER) //default value
                .build());

        CreationRequest creationRequest = new CreationRequest(
                "trililiye@gmail.com",
                "uaaaaa12345",
                Instant.now().minusSeconds(3600),
                "998933082555"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationRequest)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /auth/login authenticates and returns tokens")
    void login_ShouldLoginAndReturnsTokens() throws Exception {

        userRepository.save(User.builder()
                .email("trililiye@gmail.com")
                .password(passwordEncoder.encode("banane123"))
                .role(Role.ROLE_USER) //default value
                .build());

        UserCredentialsDto userCredentialsDto = new UserCredentialsDto(
                "trililiye@gmail.com",
                "banane123"
        );

        MvcResult mvcResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCredentialsDto)))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        User user = userRepository.findAll().getFirst();

        JwtAuthenticationDto result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), JwtAuthenticationDto.class);

        String refreshToken = refreshTokenService.getRefreshToken(user.getId().toString());

        assertThat(result.refreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("POST /login checks the validation of password by sending wrong one")
    void login_ShouldReturnUnauthorized401() throws Exception {

        userRepository.save(User.builder()
                .email("trililiye@gmail.com")
                .password(passwordEncoder.encode("banane123"))
                .role(Role.ROLE_USER) //default value
                .build());

        UserCredentialsDto userCredentialsDto = new UserCredentialsDto(
                "trililiye@gmail.com",
                "nanane123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCredentialsDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

    }

    @Test
    @DisplayName("POST /refresh refreshes Access-Token")
    void refresh_ShouldReturnNewAccessToken() throws Exception {

        CreationRequest creationRequest = new CreationRequest(
                "trililiye@gmail.com",
                "banane123",
                Instant.now().minusSeconds(3600),
                "998933082555"
        );

        userService.creatingUser(creationRequest);
        String refresh = jwtService.generateAuthToken("trililiye@gmail.com").refreshToken();

        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(refresh);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /validate Validates token")
    void validate_ShouldReturn200() throws Exception {

        CreationRequest creationRequest = new CreationRequest(
                "lilililila@gmail.com",
                "banane123",
                Instant.now().minusSeconds(3600),
                "998933082555"
        );

        userService.creatingUser(creationRequest);

        String accessToken = jwtService.generateAuthToken("lilililila@gmail.com").token();

        mockMvc.perform(post("/auth/validate")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("POST /log-out/{id} deletes refreshToken from redis")
    void logOut_ShouldReturn200() throws Exception {

        CreationRequest creationRequest = new CreationRequest(
                "lilililila@gmail.com",
                "banane123",
                Instant.now().minusSeconds(3600),
                "998933082555"
        );

        UserCreationResponseDto userCreationResponseDto = userService.creatingUser(creationRequest);

        mockMvc.perform(post("/auth/log-out")
                        .header("Authorization", "Bearer " + userCreationResponseDto.accessToken()))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("POST /change-password")
    void changePassword_ShouldReturn200() throws Exception {

        CreationRequest creationRequest = new CreationRequest(
                "lelelala@gmail.com",
                "banane123",
                Instant.now().minusSeconds(3600),
                "998933082555"
        );
        userService.creatingUser(creationRequest);

        ChangePasswordDto changePasswordDto = new ChangePasswordDto(
                "lelelala@gmail.com",
                "banane123",
                "bubune456"
        );

        mockMvc.perform(patch("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andDo(print())
                .andExpect(status().isOk());

    }
}
