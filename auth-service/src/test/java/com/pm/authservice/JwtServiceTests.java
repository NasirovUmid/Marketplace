package com.pm.authservice;

import com.pm.authservice.dto.JwtAuthenticationDto;
import com.pm.authservice.enums.TokenType;
import com.pm.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;


public class JwtServiceTests {

    private JwtService jwtService;

    private SecretKey testKey;

    // Тебе нужно как-то пробросить secretKey, если он берется из @Value

    @BeforeEach
    void setUp() {
        // Генерируем реальную строку в Base64 для теста (минимум 32 байта для HMAC)
        String rawSecret = "very-long-secret-key-for-testing-purposes-only-32-chars";
        String base64Secret = Base64.getEncoder().encodeToString(rawSecret.getBytes());

        // Создаем сервис как обычный объект
        jwtService = new JwtService(base64Secret);

        this.testKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
    }

    @Test
    void testing_GenerateAuthToken() {

        String email = "nihil@gmail.com";

        JwtAuthenticationDto jwtAuthenticationDto = jwtService.generateAuthToken(email);

        assertThat(jwtAuthenticationDto).isNotNull();
        assertThat(jwtAuthenticationDto.refreshToken()).isNotNull();
        assertThat(jwtAuthenticationDto.token()).isNotNull();

        Claims claims = Jwts.parser()
                .verifyWith(testKey)
                .build()
                .parseSignedClaims(jwtAuthenticationDto.token())
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(email);
        assertThat(claims.get("type")).isEqualTo("ACCESS");
        System.out.println(claims.getSubject());
        System.out.println(claims.get("type"));
    }

    @Test
    void testing_RefreshBaseToken() {

        String email = "nihil@gmail.com";
        String refreshToken = "refreshtoken";

        JwtAuthenticationDto jwtAuthenticationDto = jwtService.refreshBaseToken(email, refreshToken);

        assertThat(jwtAuthenticationDto).isNotNull();
        assertThat(jwtAuthenticationDto.refreshToken()).isEqualTo(refreshToken);
        assertThat(jwtAuthenticationDto.token()).isNotNull();

        Claims claims = Jwts.parser()
                .verifyWith(testKey)
                .build()
                .parseSignedClaims(jwtAuthenticationDto.token())
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(email);
        assertThat(claims.get("type")).isEqualTo("ACCESS");
        System.out.println(claims.getSubject());
        System.out.println(claims.get("type"));

    }

    @Test
    void testing_GetEmailFromToken() {

        String email = "nihil@gmail.com";
        Date date = Date.from(LocalDateTime.now().plusHours(5).atZone(ZoneId.systemDefault()).toInstant());


        String token = Jwts.builder()
                .subject(email)
                .claim("type", TokenType.ACCESS)
                .expiration(date)
                .signWith(testKey)
                .compact();

        String email1 = jwtService.getEmailFromToken(token);

        assertThat(email1).isNotNull();
        assertThat(email1).isEqualTo(email);

        Claims claims = Jwts.parser()
                .verifyWith(testKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(new Date());

        System.out.println("Now " + new Date() + "  Expiration Date " + claims.getExpiration());
    }

    @Test
    void testing_ValidateJwtToken() {

        String email = "nihil@gmail.com";
        Date date = Date.from(LocalDateTime.now().plusHours(5).atZone(ZoneId.systemDefault()).toInstant());

        String token = Jwts.builder()
                .subject(email)
                .claim("type", TokenType.ACCESS)
                .expiration(date)
                .signWith(testKey)
                .compact();

        jwtService.validateJwtToken(token);
    }


}
