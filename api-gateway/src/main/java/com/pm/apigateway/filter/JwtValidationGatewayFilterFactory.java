package com.pm.apigateway.filter;

import com.pm.apigateway.configuration.SecurityConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;


@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final WebClient webClient;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final SecurityConfig securityConfig;

    public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder, @Value("${auth.service.url}") String authServiceUrl, SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }


    @Override
    public GatewayFilter apply(Object config) {

        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String path = exchange.getRequest().getURI().getPath();

            boolean isPublic = securityConfig.getPublicUrls().stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, path));

            if (isPublic) {
                return chain.filter(exchange); // Пропускаем без проверки токена
            }

            if (token == null || !token.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            boolean isAdminRequired = isMatch(path, securityConfig.getAdminUrls());

            WebClient.RequestHeadersSpec<?> uriSpec = webClient.get().uri(uriBuilder -> uriBuilder
                    .path("/validate")
                    .queryParam("adminOnly", isAdminRequired)
                    .build());

            WebClient.ResponseSpec responseSpec = uriSpec.header(HttpHeaders.AUTHORIZATION, token).retrieve();

            return responseSpec.onStatus(HttpStatusCode::isError, httpStatusCode -> {

                HttpStatusCode status = httpStatusCode.statusCode();


                if (status.equals(HttpStatus.UNAUTHORIZED)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                } else if (status.equals(HttpStatus.FORBIDDEN)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                } else {
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                }
            }).bodyToMono(Void.class).then(chain.filter(exchange));
        };
    }

    private boolean isMatch(String path, List<String> patterns) {
        return patterns != null && patterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

//            WebClient.RequestHeadersSpec<?> request =
//                    (WebClient.RequestHeadersSpec<?>) webClient.get()
//                            .uri("/validate")
//                            .header(HttpHeaders.AUTHORIZATION, token);
//            return request.exchangeToMono(clientResponse -> {
//
//                if (clientResponse.statusCode().is2xxSuccessful()) {
//                    return chain.filter(exchange);
//                }
//
//                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//
//                return exchange.getResponse().setComplete();
//
//            });

}
