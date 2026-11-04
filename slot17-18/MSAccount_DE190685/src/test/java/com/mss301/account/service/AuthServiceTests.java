package com.mss301.account.service;

import com.mss301.account.dto.LoginRequest;
import com.mss301.account.dto.LoginResponse;
import com.mss301.account.entity.StoreAccount;
import com.mss301.account.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private StoreAccount activeAccount;

    @BeforeEach
    void setUp() {
        activeAccount = StoreAccount.builder()
                .accountId(1)
                .username("adminsys")
                .email("admin@drawingpen.com.vn")
                .password("$2a$encoded")
                .role(1)
                .isActive(true)
                .build();
    }

    @Test
    void loginReturnsTokensForValidCredentials() {
        when(accountRepository.findByUsername("adminsys")).thenReturn(Optional.of(activeAccount));
        when(passwordEncoder.matches("@1", "$2a$encoded")).thenReturn(true);
        when(jwtService.generateAccessToken("1", 1)).thenReturn("access");
        when(jwtService.generateRefreshToken("1")).thenReturn("refresh");

        LoginResponse response = authService.login(new LoginRequest("adminsys", "@1"));

        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(1, response.role());
        assertEquals("adminsys", response.username());
    }

    @Test
    void loginRejectsInactiveAccountBeforeCheckingPassword() {
        StoreAccount inactiveAccount = StoreAccount.builder()
                .accountId(4)
                .username("member02")
                .password("$2a$encoded")
                .role(3)
                .isActive(false)
                .build();
        when(accountRepository.findByUsername("member02")).thenReturn(Optional.of(inactiveAccount));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(new LoginRequest("member02", "@1")));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(passwordEncoder, never()).matches("@1", "$2a$encoded");
    }

    @Test
    void loginRejectsInvalidPassword() {
        when(accountRepository.findByUsername("adminsys")).thenReturn(Optional.of(activeAccount));
        when(passwordEncoder.matches("@wrong", "$2a$encoded")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(new LoginRequest("adminsys", "@wrong")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void loginRejectsUnknownUsername() {
        when(accountRepository.findByUsername("missing")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(new LoginRequest("missing", "@1")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(passwordEncoder, never()).matches("@1", "$2a$encoded");
    }

    @Test
    void refreshRejectsAccessToken() {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(jwtService.parseToken("access")).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("access");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.refresh("access"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(accountRepository, never()).findById(1);
    }

    @Test
    void refreshIssuesNewTokensForValidRefreshToken() {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(jwtService.parseToken("refresh")).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(claims.getSubject()).thenReturn("1");
        when(accountRepository.findById(1)).thenReturn(Optional.of(activeAccount));
        when(jwtService.generateAccessToken("1", 1)).thenReturn("new-access");
        when(jwtService.generateRefreshToken("1")).thenReturn("new-refresh");

        LoginResponse response = authService.refresh("refresh");

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
        assertEquals("adminsys", response.username());
    }
}
