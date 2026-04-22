package com.tushar.virality_engine.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tushar.virality_engine.entity.Bot;
import com.tushar.virality_engine.repository.BotRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BotService {

    private final BotRepository botRepository;

    public Bot createBot(Bot bot) {
        // check duplicate
        if (botRepository.existsByName(bot.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Bot name already taken!");
        }
        return botRepository.save(bot);
    }

    public Bot getBotById(@NonNull Long id) {
        return botRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bot not found!"));
    }
}