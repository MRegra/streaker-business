package com.streaker.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisRateLimiterService {

    private final StringRedisTemplate redis;
    private final int limit;
    private final int windowSec;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Safe to store Spring-managed StringRedisTemplate")
    public RedisRateLimiterService(StringRedisTemplate redis,
                                   @Value("${rate.limiter.limit:5}") int limit,
                                   @Value("${rate.limiter.window:60}") int windowSec) {
        this.redis = redis;
        this.limit = limit;
        this.windowSec = windowSec;
    }

    public boolean tryAcquire(String key) {
        String redisKey = "rl:" + key;
        Long count = redis.opsForValue().increment(redisKey);
        if (count == null) {
            return false;
        }
        if (count == 1) {
            redis.expire(redisKey, Duration.ofSeconds(windowSec));
        }
        return count <= limit;
    }
}
