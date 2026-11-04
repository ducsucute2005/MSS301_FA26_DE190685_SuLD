package com.mss301.account.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Integer role,
        String username) {
}
