package com.tushar.virality_engine.service;

import com.tushar.virality_engine.entity.User;
import com.tushar.virality_engine.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserRepository userRepository;

    public NotificationScheduler(StringRedisTemplate stringRedisTemplate,
            UserRepository userRepository) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.userRepository = userRepository;
    }

    // Runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void sweepPendingNotifications() {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            String key = "user:" + user.getId() + ":pending_notifs";
            Long size = stringRedisTemplate.opsForList().size(key);

            if (size != null && size > 0) {
                // Pop all messages
                List<String> notifs = stringRedisTemplate.opsForList()
                        .range(key, 0, -1);

                // Log summarized message
                if (notifs != null && !notifs.isEmpty()) {
                    System.out.println("Summarized Push Notification: "
                            + notifs.get(0) + " and " + (size - 1)
                            + " others interacted with your posts.");
                }

                // Clear the list
                stringRedisTemplate.delete(key);
            }
        }
    }
}
