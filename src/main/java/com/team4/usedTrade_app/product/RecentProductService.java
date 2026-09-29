package com.team4.usedTrade_app.product;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecentProductService {
    private static final int MAX_SIZE = 20;
    private static final int TTL_SECONDS = 7 * 24 * 60 * 60;
    private static final DefaultRedisScript<Long> UPDATE_SCRIPT = new DefaultRedisScript<>(
            "redis.call('LREM', KEYS[1], 0, ARGV[1]); " +
            "redis.call('LPUSH', KEYS[1], ARGV[1]); " +
            "redis.call('LTRIM', KEYS[1], 0, ARGV[2] - 1); " +
            "return redis.call('EXPIRE', KEYS[1], ARGV[3]);",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    public void addRecentProduct(Integer userId, Integer productId) {
        redisTemplate.execute(UPDATE_SCRIPT, List.of(key(userId)),
                productId.toString(), String.valueOf(MAX_SIZE), String.valueOf(TTL_SECONDS));
    }

    public List<Integer> getRecentProductIds(Integer userId) {
        List<String> values = redisTemplate.opsForList().range(key(userId), 0, -1);
        if (values == null) {
            return List.of();
        }
        return values.stream().map(value -> {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException exception) {
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    private String key(Integer userId) {
        return "recent:products:" + userId;
    }
}
