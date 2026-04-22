package com.tushar.virality_engine.controller;

import com.tushar.virality_engine.entity.Bot;
import com.tushar.virality_engine.service.BotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bots")
@RequiredArgsConstructor
public class BotController {

    private final BotService botService;

    @PostMapping
    public ResponseEntity<Bot> createBot(@RequestBody Bot bot) {
        Bot created = botService.createBot(bot);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bot> getBotById(@PathVariable Long id) {
        Bot bot = botService.getBotById(id);
        return ResponseEntity.ok(bot);
    }
}