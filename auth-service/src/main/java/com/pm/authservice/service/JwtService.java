package com.pm.authservice.service;

import com.pm.authservice.dto.JwtAuthenticationDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtService {

    private final Logger logger = (Logger) LogManager.getLogger(JwtService.class);

    private final Key secretKey;

    public JwtService(@Value("${jwt.secret}") String  secret) {

        byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtAuthenticationDto generateAuthToken(String email){

        // jwtAuthenticationDto.setToken(generateJwtToken(email));
       // jwtAuthenticationDto

        //Gotta try builder
        return JwtAuthenticationDto.builder()
                .token(generateJwtToken(email))
                .refreshToken(generateRefreshToken(email))
                .build();
    }


    public JwtAuthenticationDto refreshBaseToken(String email,String refreshToken){

        /*
        JwtAuthenticationDto jwtAuthenticationDto =new JwtAuthenticationDto();
        jwtAuthenticationDto.setToken(generateJwtToken(email));
        jwtAuthenticationDto.setRefreshToken(refreshToken);

         */

        return JwtAuthenticationDto.builder()
                .token(generateJwtToken(email))
                .refreshToken(refreshToken)
                .build();

        // for refreshing ONLY IF  refresh token isnt already expired
    }


    public String getEmailFromToken(String token){

        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateJwtToken(String token){

        try {
            Jwts.parser()
                    .verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;

        }catch (ExpiredJwtException expiredJwtException){
            logger.error("JWT expired!",expiredJwtException);
        }catch (UnsupportedJwtException unsupportedJwtException){
            logger.error("Unsupported jwt",unsupportedJwtException);
        }catch (MalformedJwtException malformedJwtException){
            logger.error("Ugly jwt REMAKE!",malformedJwtException);
        }catch (SecurityException securityException){
            logger.error("Security Exception",securityException);
        }catch (Exception exception){
            logger.error("I dunno ,mb Invalid Token",exception);
        }
        return false;
    }

    private String generateJwtToken(String email){

        Date date =Date.from(LocalDateTime.now().plusMinutes(10).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .subject(email)
                .expiration(date)
                .signWith(secretKey)
                .compact();
        //subject - identification , without claim() which is additional for payload
    }

    private String generateRefreshToken(String email){

    Date date = Date.from(LocalDateTime.now().plusHours(5).atZone(ZoneId.systemDefault()).toInstant());

    return Jwts.builder()
            .subject(email)
            .expiration(date)
            .signWith(secretKey)
            .compact();
    //almost the same but for refreshing for more time
    }
}
