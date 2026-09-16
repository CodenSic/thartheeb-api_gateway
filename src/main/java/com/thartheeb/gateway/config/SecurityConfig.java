package com.thartheeb.gateway.config;

import com.thartheeb.gateway.error.GatewayErrorWriter;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchange -> exchange
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                    "/openapi/**").permitAll()
                .pathMatchers("/v1/auth/service-token", "/internal/**").denyAll()
                .pathMatchers("/v1/auth/vendor-registration", "/v1/auth/login",
                    "/v1/auth/mfa/verify", "/v1/auth/token/refresh",
                    "/v1/auth/password-resets", "/v1/auth/password-resets/confirm").permitAll()
                .anyExchange().authenticated())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((exchange, error) -> GatewayErrorWriter.write(
                    exchange, HttpStatus.UNAUTHORIZED,
                    "Authentication is required to access this resource."))
                .accessDeniedHandler((exchange, error) -> GatewayErrorWriter.write(
                    exchange, HttpStatus.FORBIDDEN,
                    "You do not have permission to access this resource.")))
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
            .build();
    }
}
