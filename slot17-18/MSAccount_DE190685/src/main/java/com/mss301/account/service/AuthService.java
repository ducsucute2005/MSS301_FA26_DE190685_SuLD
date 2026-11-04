package com.mss301.account.service;

import com.mss301.account.dto.LoginRequest;
import com.mss301.account.dto.LoginResponse;
import com.mss301.account.entity.StoreAccount;
import com.mss301.account.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid username or password";

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        StoreAccount account = accountRepository.findByUsername(request.username())
                .orElseThrow(() -> unauthorized(INVALID_CREDENTIALS));

        if (Boolean.FALSE.equals(account.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
        }

        if (!passwordEncoder.matches(request.password(), account.getPassword())) {
            throw unauthorized(INVALID_CREDENTIALS);
        }

        String subject = account.getAccountId().toString();
        return createLoginResponse(
                account,
                jwtService.generateAccessToken(subject, account.getRole()),
                jwtService.generateRefreshToken(subject));
    }

    public LoginResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parseToken(refreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw unauthorized("Invalid or expired refresh token");
        }

        if (!"refresh".equals(claims.get("type", String.class))) {
            throw unauthorized("Token is not a refresh token");
        }

        String subject = claims.getSubject();
        Integer accountId;
        try {
            accountId = Integer.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw unauthorized("Account not found");
        }

        StoreAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> unauthorized("Account not found"));

        return createLoginResponse(
                account,
                jwtService.generateAccessToken(subject, account.getRole()),
                jwtService.generateRefreshToken(subject));
    }

    private LoginResponse createLoginResponse(StoreAccount account, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .role(account.getRole())
                .username(account.getUsername())
                .build();
    }

    private ResponseStatusException unauthorized(String reason) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, reason);
    }
}
