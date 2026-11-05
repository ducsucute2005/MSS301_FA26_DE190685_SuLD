package com.mss301.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final Set<String> PUBLIC_POST_PATHS = Set.of(
        "/api/auth/login",
        "/api/auth/refresh"
    );
    private static final String FALLBACK_PREFIX = "/fallback";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        ServerHttpRequest sanitizedRequest = request.mutate()
            .headers(headers -> {
                headers.remove(USER_ID_HEADER);
                headers.remove(USER_ROLE_HEADER);
            })
            .build();
        ServerWebExchange sanitizedExchange = exchange.mutate().request(sanitizedRequest).build();

        if (isPublic(request.getMethod(), path)) {
            return chain.filter(sanitizedExchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        try {
            Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(authorization.substring("Bearer ".length()))
                .getPayload();

            if (!"access".equals(claims.get("type", String.class))
                    || claims.getSubject() == null
                    || !(claims.get("role") instanceof Number role)) {
                return unauthorized(exchange);
            }

            ServerHttpRequest authenticatedRequest = sanitizedRequest.mutate()
                .header(USER_ID_HEADER, claims.getSubject())
                .header(USER_ROLE_HEADER, String.valueOf(role.intValue()))
                .build();
            return chain.filter(exchange.mutate().request(authenticatedRequest).build());
        } catch (RuntimeException exception) {
            return unauthorized(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublic(HttpMethod method, String path) {
        if (HttpMethod.POST.equals(method) && PUBLIC_POST_PATHS.contains(path)) {
            return true;
        }
        return FALLBACK_PREFIX.equals(path)
            || path.startsWith(FALLBACK_PREFIX + "/")
            || (HttpMethod.GET.equals(method) && "/api/watches".equals(path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
