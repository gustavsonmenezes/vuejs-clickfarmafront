package com.clickfarma.backend.controller;

import com.clickfarma.backend.service.WhatsAppAIAssistantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp/webhook")
public class WhatsAppWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    @Value("${whatsapp.cloud.webhook-verify-token:clickfarma123}")
    private String verifyToken;

    private final WhatsAppAIAssistantService whatsAppAIAssistantService;

    public WhatsAppWebhookController(WhatsAppAIAssistantService whatsAppAIAssistantService) {
        this.whatsAppAIAssistantService = whatsAppAIAssistantService;
    }

    @GetMapping
    public ResponseEntity<String> verificarWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.challenge") String challenge,
            @RequestParam("hub.verify_token") String token) {

        log.info("Verificacao webhook: mode={}, token={}", mode, token);

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Webhook verificado com sucesso!");
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Token invalido");
    }

    @PostMapping
    public ResponseEntity<String> receberMensagem(@RequestBody Map<String, Object> payload) {
        try {
            processarPayload(payload);
        } catch (Exception e) {
            log.error("Erro ao processar webhook WhatsApp: {}", e.getMessage());
        }
        return ResponseEntity.ok("OK");
    }

    @SuppressWarnings("unchecked")
    private void processarPayload(Map<String, Object> payload) {
        List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
        if (entries == null || entries.isEmpty()) return;

        for (Map<String, Object> entry : entries) {
            List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");
            if (changes == null || changes.isEmpty()) continue;

            for (Map<String, Object> change : changes) {
                Map<String, Object> value = (Map<String, Object>) change.get("value");
                if (value == null) continue;

                List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
                if (messages == null || messages.isEmpty()) continue;

                for (Map<String, Object> msg : messages) {
                    String from = (String) msg.get("from");
                    String type = (String) msg.get("type");

                    if (from == null || !"text".equals(type)) continue;

                    Map<String, Object> text = (Map<String, Object>) msg.get("text");
                    if (text == null) continue;

                    String body = (String) text.get("body");
                    if (body == null || body.isBlank()) continue;

                    log.info("WhatsApp de {}: {}", from, body);
                    whatsAppAIAssistantService.processMessage(from, body);
                }
            }
        }
    }
}
