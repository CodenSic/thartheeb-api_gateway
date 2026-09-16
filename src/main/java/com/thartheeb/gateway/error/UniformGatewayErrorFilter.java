package com.thartheeb.gateway.error;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class UniformGatewayErrorFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).onErrorResume(error -> {
            if (exchange.getResponse().isCommitted()) return Mono.error(error);
            if (error instanceof ResponseStatusException statusException) {
                HttpStatus status = HttpStatus.resolve(statusException.getStatusCode().value());
                if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
                String message = statusException.getReason() == null
                    ? status.getReasonPhrase() : statusException.getReason();
                return GatewayErrorWriter.write(exchange, status, message);
            }
            return GatewayErrorWriter.write(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected gateway error occurred.");
        });
    }
}
