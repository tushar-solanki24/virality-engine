package com.tushar.virality_engine.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationService {

    private final RedisTemplate<String, Long> redisTemplate;
    // Use StringRedisTemplate for notification strings
    private final StringRedisTemplate stringRedisTemplate;

    public NotificationService(RedisTemplate<String, Long> redisTemplate,
                               StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private String pendingNotifKey(Long userId) {
        return "user:" + userId + ":pending_notifs";
    }

    private String notifCooldownKey(Long userId) {
        return "notif:cooldown:user_" + userId;
    }

    public void handleBotNotification(Long userId, String botName) {
        String cooldownKey = notifCooldownKey(userId);
        Boolean onCooldown = redisTemplate.hasKey(cooldownKey);

        if (Boolean.TRUE.equals(onCooldown)) {
            // Push to pending list
            stringRedisTemplate.opsForList().rightPush(
                pendingNotifKey(userId),
                botName + " replied to your post"
            );
        } else {
            // Send immediately + set 15 min cooldown
            System.out.println("Push Notification Sent to User: " + userId);
            redisTemplate.opsForValue().set(cooldownKey, 1L, 15, TimeUnit.MINUTES);
        }
    }
}
