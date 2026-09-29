package com.team4.usedTrade_app.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        Integer userId,
        String nickname
) {
}