package com.pm.authservice.service;

import com.pm.authservice.dto.JwtAuthenticationDto;
import com.pm.authservice.dto.RefreshTokenDto;
import com.pm.authservice.dto.UserCredentialsDto;
import com.pm.authservice.entity.User;
import com.pm.authservice.enums.Role;
import com.pm.authservice.enums.TokenType;
import com.pm.authservice.payload.AuthResponse;
import com.pm.authservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public JwtAuthenticationDto login(UserCredentialsDto userCredentialsDto) throws AuthenticationException {

        User user = findByCredentials(userCredentialsDto);
        return jwtService.generateAuthToken(user.getEmail());

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

    public AuthResponse refreshToken(RefreshTokenDto refreshTokenDto) throws Exception {

    String refreshToken = refreshTokenDto.getRefreshToken();

        Claims claims = jwtService.extractClaims(refreshToken);

        if (claims.get("type") != TokenType.REFRESH) return new AuthResponse("Not a Refresh Token",false,refreshToken);



    if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {

        User user = findByEmail(jwtService.getEmailFromToken(refreshToken));

        return new AuthResponse("Token is updated", true, jwtService.refreshBaseToken(user.getEmail(), refreshToken));

    }

    return new AuthResponse("No RefreshToken",false,refreshToken);

    }

    public AuthResponse creatingUser(UserCredentialsDto userCredentialsDto) throws Exception {

            if (findByEmail(userCredentialsDto.getEmail())!=null) return new AuthResponse("Email EXISTS",false,userCredentialsDto);

        User user = User.builder()
                        .email(userCredentialsDto.getEmail())
                                .password(passwordEncoder.encode(userCredentialsDto.getPassword()))
                                        .role(Role.ROLE_USER)
                                                .build();

                                          // Default , if necessary should be changed manually (via update method in controller)
        userRepository.save(user);

        return new AuthResponse("User is created",true,user);
    }

    private User findByCredentials(UserCredentialsDto userCredentialsDto) throws AuthenticationException {

    Optional<User> user = userRepository.findUserByEmail(userCredentialsDto.getEmail());
    if (user.isPresent()){

        if (passwordEncoder.matches(userCredentialsDto.getPassword(), user.get().getPassword()))
            return user.get();

    }
        throw new AuthenticationException("Email or password is not correct");
    }

    private User findByEmail(String email) throws Exception {

        Optional<User> user = userRepository.findUserByEmail(email);
        return user.orElse(null);

    }
}
