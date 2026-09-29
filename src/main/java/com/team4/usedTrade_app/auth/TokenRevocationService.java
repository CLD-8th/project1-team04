package com.team4.usedTrade_app.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {
    private final StringRedisTemplate redisTemplate;
    private final TokenProvider tokenProvider;

    public boolean isRevoked(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(accessToken)));
    }

    public void revoke(String accessToken) {
        Duration remaining = Duration.between(Instant.now(), tokenProvider.getExpiresAt(accessToken));
        if (remaining.isPositive()) {
            redisTemplate.opsForValue().set(key(accessToken), "1", remaining);
        }
    }

    private String key(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return "auth:revoked:" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", exception);
        }
    }
}
