package com.clickfarma.backend.controller;

import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final PedidoRepository pedidoRepository;

    @PostMapping("/mercadopago")
    public ResponseEntity<String> receberNotificacao(@RequestBody Map<String, Object> payload) {
        log.info("📢 Webhook recebido: {}", payload);

        try {
            // Extrair dados da notificação
            String paymentId = (String) payload.get("id");
            String topic = (String) payload.get("topic");

            if ("payment".equals(topic)) {
                // Aqui você pode consultar o status do pagamento
                // e atualizar o pedido no banco de dados
                log.info("✅ Pagamento {} processado", paymentId);
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("❌ Erro ao processar webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
