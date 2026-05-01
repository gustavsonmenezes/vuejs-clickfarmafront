package com.clickfarma.backend.controller;

import com.clickfarma.backend.service.TelegramIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:8081", "http://localhost:8082"})
public class TelegramWebhookController {

    private final TelegramIntegrationService telegramIntegrationService;

    public TelegramWebhookController(TelegramIntegrationService telegramIntegrationService) {
        this.telegramIntegrationService = telegramIntegrationService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receberWebhook(@RequestBody Map<String, Object> update) {
        telegramIntegrationService.processarUpdateTelegram(update);
        return ResponseEntity.ok("OK");
    }
}
