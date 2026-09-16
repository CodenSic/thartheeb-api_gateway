package com.thartheeb.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter implements WebFilter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong requests = new AtomicLong();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Rule rule = rule(exchange.getRequest().getPath().value());
        if (rule == null) return chain.filter(exchange);
        long now = Instant.now().getEpochSecond();
        String remote = exchange.getRequest().getRemoteAddress() == null ? "unknown"
            : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        String key = remote + '|' + rule.name();
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket(now));
        long retryAfter;
        synchronized (bucket) {
            if (now - bucket.startedAt >= rule.windowSeconds()) {
                bucket.startedAt = now;
                bucket.count = 0;
            }
            bucket.count++;
            retryAfter = rule.windowSeconds() - (now - bucket.startedAt);
            if (bucket.count <= rule.limit()) return chain.filter(exchange);
        }
        if (requests.incrementAndGet() % 1000 == 0) {
            buckets.entrySet().removeIf(entry -> now - entry.getValue().startedAt > 600);
        }
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set("Retry-After", Long.toString(Math.max(1, retryAfter)));
        byte[] body = ("{\"type\":\"urn:thartheeb:error:rate-limit\","
            + "\"title\":\"RATE_LIMIT_EXCEEDED\",\"status\":429,"
            + "\"detail\":\"Too many requests. Retry later.\","
            + "\"error\":\"Too Many Requests\","
            + "\"message\":\"Too many requests. Retry later.\"}")
            .getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(
            exchange.getResponse().bufferFactory().wrap(body)));
    }

    private Rule rule(String path) {
        if (path.equals("/v1/auth/vendor-registration")) return new Rule("registration", 5, 300);
        if (path.equals("/v1/auth/login") || path.equals("/v1/auth/mfa/verify") ||
            path.equals("/v1/auth/password-resets")) return new Rule("authentication", 10, 60);
        if (path.equals("/v1/auth/token/refresh")) return new Rule("refresh", 30, 60);
        return null;
    }

    private record Rule(String name, int limit, long windowSeconds) {}
    private static final class Bucket {
        private long startedAt;
        private int count;
        private Bucket(long startedAt) { this.startedAt = startedAt; }
    }
}
