package com.thartheeb.gateway.config;

import com.thartheeb.gateway.error.GatewayErrorWriter;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

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
                    "/v1/auth/password-resets", "/v1/auth/password-resets/confirm",
                    "/v1/auth/vendor-activations/validate",
                    "/v1/auth/vendor-activations/confirm").permitAll()
                .pathMatchers("/v1/vendors/*/fleet/**")
                    .hasAuthority("ROLE_VENDOR_ADMIN")
                .pathMatchers("/v1/vendor-applications/**", "/v1/vendors/**")
                    .hasAuthority("SCOPE_vendor")
                .anyExchange().hasAuthority("SCOPE_vendor"))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((exchange, error) -> GatewayErrorWriter.write(
                    exchange, HttpStatus.UNAUTHORIZED,
                    "Authentication is required to access this resource."))
                .accessDeniedHandler((exchange, error) -> GatewayErrorWriter.write(
                    exchange, HttpStatus.FORBIDDEN,
                    "You do not have permission to access this resource.")))
            .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> result = new ArrayList<>();
            String scope = jwt.getClaimAsString("scope");
            if (scope != null) for (String value : scope.split(" ")) {
                if (!value.isBlank()) result.add(new SimpleGrantedAuthority("SCOPE_" + value));
            }
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) roles.forEach(value ->
                result.add(new SimpleGrantedAuthority("ROLE_" + value)));
            return result;
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
