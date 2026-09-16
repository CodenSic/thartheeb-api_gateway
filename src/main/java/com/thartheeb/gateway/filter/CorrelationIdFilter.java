package com.thartheeb.gateway.filter;

import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements WebFilter {
    public static final String HEADER = "X-Correlation-ID";
    private static final Pattern SAFE = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String supplied = exchange.getRequest().getHeaders().getFirst(HEADER);
        String correlationId = supplied != null && SAFE.matcher(supplied).matches()
            ? supplied : UUID.randomUUID().toString();
        exchange.getResponse().getHeaders().set(HEADER, correlationId);
        exchange.getResponse().getHeaders().set("Cache-Control", "no-store");
        exchange.getResponse().getHeaders().set("X-Content-Type-Options", "nosniff");
        var request = exchange.getRequest().mutate().headers(headers ->
            headers.set(HEADER, correlationId)).build();
        return chain.filter(exchange.mutate().request(request).build());
    }
}
