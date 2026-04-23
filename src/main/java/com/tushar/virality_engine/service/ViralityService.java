package com.tushar.virality_engine.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ViralityService {

    private final RedisTemplate<String, Long> redisTemplate;

    public ViralityService(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String viralityKey(Long postId) {
        return "post:" + postId + ":virality_score";
    }

    private String botCountKey(Long postId) {
        return "post:" + postId + ":bot_count";
    }

    private String cooldownKey(Long botId, Long userId) {
        return "cooldown:bot_" + botId + ":human_" + userId;
    }

    // Called when bot replies → +1
    // Called when human likes → +20
    // Called when human comments → +50
    public void addViralityScore(Long postId, long points) {
        redisTemplate.opsForValue().increment(viralityKey(postId), points);
    }

    // Returns false if bot count exceeds 100 → reject with 429
    public boolean checkAndIncrementBotCount(Long postId) {
        Long count = redisTemplate.opsForValue().increment(botCountKey(postId), 1);
        if (count == null || count > 100) {
            redisTemplate.opsForValue().decrement(botCountKey(postId));
            return false;
        }
        return true;
    }

    // Returns false if cooldown exists → reject interaction
    public boolean checkCooldown(Long botId, Long userId) {
        String key = cooldownKey(botId, userId);
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            return false;
        }
        redisTemplate.opsForValue().set(key, 1L, 10, TimeUnit.MINUTES);
        return true;
    }
}