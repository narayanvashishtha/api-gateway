package com.example.api_gateway.ratelimiter.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final int TIME_WINDOW_SECONDS = 60;
    private static final int MAX_REQUESTS = 10;

    public RateLimiterService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String clientIp) {
        String key = "rate_limit:" + clientIp;

        Long count = redisTemplate.opsForValue().increment(key);

        if(count == 1){
            redisTemplate.expire(key, Duration.ofSeconds(TIME_WINDOW_SECONDS));
        }
        return count <= MAX_REQUESTS;
    }
}
