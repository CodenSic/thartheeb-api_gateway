package com.thartheeb.gateway.error;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public final class GatewayErrorWriter {
    private GatewayErrorWriter() {
    }

    public static Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String message) {
        if (exchange.getResponse().isCommitted()) return Mono.error(new IllegalStateException(message));
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"timestamp\":\"" + Instant.now() + "\",\"path\":\""
            + escape(exchange.getRequest().getPath().value()) + "\",\"status\":" + status.value()
            + ",\"error\":\"" + escape(status.getReasonPhrase()) + "\",\"message\":\""
            + escape(message) + "\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
            .replace("\r", "\\r").replace("\n", "\\n");
    }
}
