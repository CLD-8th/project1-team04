package com.team4.usedTrade_app.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentProductService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_SIZE = 20;

    public void addRecentProduct(Integer userId, Integer productId) {
        String key = "recent:user:" + userId;
        redisTemplate.opsForList().remove(key, 0, String.valueOf(productId));
        redisTemplate.opsForList().leftPush(key, String.valueOf(productId));
        redisTemplate.opsForList().trim(key, 0, MAX_SIZE - 1);
    }
    // FR-07 최근 본 상품 ID 조회
    public List<Integer> getRecentProductIds(Integer userId) {
        String key = "recent:user:" + userId;
        List<String> productIds = redisTemplate.opsForList().range(key, 0, -1);
        if (productIds == null) return List.of();
        return productIds.stream().map(Integer::valueOf).toList();
    }

}

