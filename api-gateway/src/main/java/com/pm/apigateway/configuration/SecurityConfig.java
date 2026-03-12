package com.pm.apigateway.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityConfig {

    private final List<String> publicUrls = List.of("http://localhost:8087/catalog");
    private final List<String> adminUrls = List.of();
    private final List<String> authenticated = List.of();

    public List<String> getPublicUrls() {
        return publicUrls;
    }

    public List<String> getAdminUrls() {
        return adminUrls;
    }

    public List<String> getAuthenticated() {
        return authenticated;
    }
}
