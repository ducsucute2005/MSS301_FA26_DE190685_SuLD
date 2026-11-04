package com.mss301.account.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTests {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "mss301-watch-management-test-secret-key-2025",
                900_000,
                604_800_000);
    }

    @Test
    void accessTokenContainsSubjectRoleAndAccessType() {
        String token = jwtService.generateAccessToken("1", 1);

        Claims claims = jwtService.parseToken(token);

        assertEquals("1", claims.getSubject());
        assertEquals(1, claims.get("role", Integer.class));
        assertEquals("access", claims.get("type", String.class));
        assertEquals(900_000L, claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    @Test
    void refreshTokenHasRefreshTypeAndSevenDayExpiration() {
        String token = jwtService.generateRefreshToken("1");

        Claims claims = jwtService.parseToken(token);

        assertEquals("1", claims.getSubject());
        assertEquals("refresh", claims.get("type", String.class));
        assertEquals(604_800_000L, claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        JwtService otherService = new JwtService(
                "a-different-watch-management-test-secret-key",
                900_000,
                604_800_000);

        String token = jwtService.generateAccessToken("1", 1);

        assertThrows(JwtException.class, () -> otherService.parseToken(token));
    }
}
