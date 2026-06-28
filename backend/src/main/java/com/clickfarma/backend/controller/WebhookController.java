package com.clickfarma.backend.controller;

import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.repository.PedidoRepository;
import com.clickfarma.backend.service.PagamentoService;
import com.clickfarma.backend.service.PedidoService;
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

    private final PedidoRepository pedidoRepository;
    private final PagamentoService pagamentoService;
    private final PedidoService pedidoService;

    @PostMapping("/mercadopago")
    public ResponseEntity<String> receberNotificacao(@RequestBody Map<String, Object> payload) {
        log.info("📢 Webhook MP recebido: {}", payload);

        try {
            String action = (String) payload.get("action");
            String topic = (String) payload.get("type");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");

            if (action == null && topic == null && data == null) {
                topic = (String) payload.get("topic");
                if (topic == null) topic = "payment";
            }

            String paymentId = data != null ? String.valueOf(data.get("id"))
                    : payload.get("id") != null ? String.valueOf(payload.get("id"))
                    : payload.get("data_id") != null ? String.valueOf(payload.get("data_id"))
                    : null;

            if (paymentId == null || paymentId.equals("null")) {
                log.warn("Webhook sem paymentId - provavelmente teste do MP");
                return ResponseEntity.ok("OK");
            }

            String status = pagamentoService.consultarStatusPagamento(Long.parseLong(paymentId));
            log.info("Payment {} status: {}", paymentId, status);

            if ("approved".equals(status)) {
                Pedido pedido = pedidoRepository.findByPagamentoMpId(Long.parseLong(paymentId));
                if (pedido != null) {
                    pedidoService.processarPagamentoAprovado(pedido.getId());
                    log.info("✅ Pedido {} pago com sucesso!", pedido.getCodigoPedido());
                } else {
                    log.warn("Pedido nao encontrado para pagamento MP: {}", paymentId);
                }
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("❌ Erro ao processar webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok("OK");
        }
    }
}
