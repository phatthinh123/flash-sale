package com.flashsale.flashsale.integration.lock;

import com.flashsale.flashsale.business.port.DistributedLockPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisDistributedLockAdapter implements DistributedLockPort {

    private static final Duration LOCK_TTL = Duration.ofSeconds(5);

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean tryLock(String lockKey) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, UUID.randomUUID().toString(), LOCK_TTL));
    }

    @Override
    public void unlock(String lockKey) {
        redisTemplate.delete(lockKey);
    }
}




