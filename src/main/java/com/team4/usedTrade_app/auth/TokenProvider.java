package com.team4.usedTrade_app.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component

public class TokenProvider {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpireMillis;
    private final long refreshExpireMillis;

    public TokenProvider(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(
                properties.secret().getBytes(StandardCharsets.UTF_8)
        );
        this.accessExpireMillis =
                properties.accessExpireMinutes() * 60 * 1000;
        this.refreshExpireMillis =
                properties.refreshExpireDays() * 24 * 60 * 60 * 1000;
    }

    public String issueAccessToken(Integer userId) {
        return build(userId, TYPE_ACCESS, accessExpireMillis).compact();
    }

    public String issueRefreshToken(Integer userId) {
        return build(userId, TYPE_REFRESH, refreshExpireMillis).compact();
    }

    private io.jsonwebtoken.JwtBuilder build(
            Integer userId,
            String type,
            long expireMillis) {

        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TYPE, type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key);
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getUserId(String token) {
        return Integer.valueOf(parse(token).getSubject());
    }

    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(
                parse(token).get(CLAIM_TYPE, String.class)
        );
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(
                parse(token).get(CLAIM_TYPE, String.class)
        );
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}