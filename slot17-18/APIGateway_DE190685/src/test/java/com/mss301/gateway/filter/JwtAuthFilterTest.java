package com.mss301.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    private static final String SECRET = "mss301-watch-management-secret-key-2025-very-long-string";
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private final JwtAuthFilter filter = new JwtAuthFilter();
    private final GatewayFilterChain chain = mock(GatewayFilterChain.class);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "jwtSecret", SECRET);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void validAccessTokenReplacesSpoofedIdentityHeaders() {
        String token = token("adminsys", 1, "access", Instant.now().plusSeconds(300));
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/brands")
                .header("X-User-Id", "attacker")
                .header("X-User-Role", "1")
                .header("Authorization", "Bearer " + token)
        );
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        when(chain.filter(any())).thenAnswer(invocation -> {
            forwarded.set(invocation.getArgument(0));
            return Mono.empty();
        });

        filter.filter(exchange, chain).block();

        assertEquals("adminsys", forwarded.get().getRequest().getHeaders().getFirst("X-User-Id"));
        assertEquals("1", forwarded.get().getRequest().getHeaders().getFirst("X-User-Role"));
    }

    @Test
    void staffTokenCannotBeOverriddenBySpoofedAdminRole() {
        String token = token("staff02", 2, "access", Instant.now().plusSeconds(300));
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.delete("/api/watches/1")
                .header("X-User-Role", "1")
                .header("Authorization", "Bearer " + token)
        );
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        when(chain.filter(any())).thenAnswer(invocation -> {
            forwarded.set(invocation.getArgument(0));
            return Mono.empty();
        });

        filter.filter(exchange, chain).block();

        assertEquals("2", forwarded.get().getRequest().getHeaders().getFirst("X-User-Role"));
    }

    @Test
    void rejectsRefreshTokenOnProtectedEndpoint() {
        MockServerWebExchange exchange = protectedExchange(
            token("adminsys", null, "refresh", Instant.now().plusSeconds(300)));

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void rejectsExpiredAccessToken() {
        MockServerWebExchange exchange = protectedExchange(
            token("adminsys", 1, "access", Instant.now().minusSeconds(1)));

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        SecretKey otherKey = Keys.hmacShaKeyFor(
            "another-signing-key-that-is-long-enough-for-hmac".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
            .subject("adminsys")
            .claim("type", "access")
            .claim("role", 1)
            .issuedAt(Date.from(Instant.now().minusSeconds(5)))
            .expiration(Date.from(Instant.now().plusSeconds(300)))
            .signWith(otherKey)
            .compact();
        MockServerWebExchange exchange = protectedExchange(token);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void publicWatchListStripsIdentityHeadersWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/watches")
                .header("X-User-Id", "attacker")
                .header("X-User-Role", "1")
        );
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        when(chain.filter(any())).thenAnswer(invocation -> {
            forwarded.set(invocation.getArgument(0));
            return Mono.empty();
        });

        filter.filter(exchange, chain).block();

        assertNull(forwarded.get().getRequest().getHeaders().getFirst("X-User-Id"));
        assertNull(forwarded.get().getRequest().getHeaders().getFirst("X-User-Role"));
    }

    @Test
    void protectedWatchMethodIsNotPublic() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/watches")
                .header("X-User-Role", "1")
        );

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void onlyPostLoginAndRefreshArePublicAuthEndpoints() {
        MockServerWebExchange login = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/auth/login"));
        filter.filter(login, chain).block();
        verify(chain).filter(any());

        MockServerWebExchange wrongMethod = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/auth/login"));
        filter.filter(wrongMethod, chain).block();
        assertEquals(HttpStatus.UNAUTHORIZED, wrongMethod.getResponse().getStatusCode());
    }

    private MockServerWebExchange protectedExchange(String token) {
        return MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/brands")
                .header("Authorization", "Bearer " + token)
        );
    }

    private String token(String subject, Integer role, String type, Instant expiration) {
        var builder = Jwts.builder()
            .subject(subject)
            .claim("type", type)
            .issuedAt(Date.from(Instant.now().minusSeconds(5)))
            .expiration(Date.from(expiration));
        if (role != null) {
            builder.claim("role", role);
        }
        return builder.signWith(SIGNING_KEY).compact();
    }
}
