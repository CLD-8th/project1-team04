package com.team4.usedTrade_app.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessExpireMinutes,
        long refreshExpireDays
) {
}