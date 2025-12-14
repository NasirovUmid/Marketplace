package com.pm.authservice.filter;

import com.pm.authservice.config.UsersDetails;
import com.pm.authservice.service.CustomUserDetailsService;
import com.pm.authservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    //i used lombok @NonNull bc org.springframework.lang.NonNull is deprecated

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

    String token = getTokenFromRequest(request);
    if (token!=null&& jwtService.validateJwtToken(token)){

    setCustomUserDetailsToSecurityContextHolder(token);

        }
    filterChain.doFilter(request,response);
    }

    private void setCustomUserDetailsToSecurityContextHolder(String token) {

        String email = jwtService.getEmailFromToken(token);
        UsersDetails usersDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                usersDetails,null,usersDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String getTokenFromRequest(HttpServletRequest request){

    String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (bearerToken != null && bearerToken.startsWith("Bearer ")){

        return bearerToken.substring(7);

    }
    return null;
    }
}
