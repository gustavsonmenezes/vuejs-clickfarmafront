package com.clickfarma.backend.service;

import com.clickfarma.backend.config.WhatsAppCloudConfig;
import com.clickfarma.backend.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WhatsAppCloudService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppCloudService.class);

    private final WhatsAppCloudConfig config;
    private final RestTemplate restTemplate;

    public WhatsAppCloudService(WhatsAppCloudConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    public boolean isAvailable() {
        return config.isEnabled()
                && !config.getPhoneNumberId().isBlank()
                && !config.getAccessToken().isBlank();
    }

    public void enviarTexto(String para, String texto) {
        if (!isAvailable()) {
            log.info("[MOCK WhatsApp] Para: {} | Mensagem:\n{}", para, texto);
            return;
        }
        try {
            String url = config.getApiUrl() + "/" + config.getPhoneNumberId() + "/messages";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getAccessToken());
            Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", para,
                "type", "text",
                "text", Map.of("preview_url", true, "body", texto)
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, entity, Map.class);
        } catch (Exception e) {
            log.error("Erro ao enviar texto WhatsApp: {}", e.getMessage());
        }
    }

    public EnvioResult enviarNotificacaoStatus(Pedido pedido) {
        String telefone = limparTelefone(pedido.getUsuario().getTelefone());
        if (telefone == null) {
            return new EnvioResult(false, "Telefone não disponível");
        }

        if (!isAvailable()) {
            log.info("WhatsApp Cloud não configurado. Simulando envio para {}", telefone);
            return simularEnvio(telefone);
        }

        String mensagem = montarMensagemStatus(pedido);
        return enviarMensagem(telefone, mensagem);
    }

    private EnvioResult enviarMensagem(String para, String mensagem) {
        try {
            String url = config.getApiUrl() + "/" + config.getPhoneNumberId() + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getAccessToken());

            Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", para,
                "type", "text",
                "text", Map.of("preview_url", true, "body", mensagem)
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("WhatsApp enviado com sucesso para {}", para);
                return new EnvioResult(true, "Mensagem enviada via WhatsApp Cloud API");
            }

            log.error("Erro ao enviar WhatsApp: {}", response.getBody());
            return new EnvioResult(false, "Erro na API: " + response.getStatusCode());

        } catch (Exception e) {
            log.error("Erro ao enviar mensagem WhatsApp: {}", e.getMessage());
            return new EnvioResult(false, "Erro: " + e.getMessage());
        }
    }

    private EnvioResult simularEnvio(String para) {
        return new EnvioResult(true, "Mensagem simulada para " + para + " (WhatsApp Cloud não configurado)");
    }

    private String montarMensagemStatus(Pedido pedido) {
        StringBuilder sb = new StringBuilder();
        sb.append("📦 *ClickFarma - Atualização do Pedido*\n\n");
        sb.append("Olá *").append(pedido.getUsuario().getNome()).append("*!\n\n");
        sb.append("Seu pedido *#").append(pedido.getCodigoPedido()).append("* ");
        sb.append("está com status: *").append(formatarStatus(pedido.getStatus())).append("*\n\n");
        sb.append("💚 ClickFarma - Sua farmácia de confiança");
        return sb.toString();
    }

    private String limparTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) return null;
        String cleaned = telefone.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("55") && cleaned.length() >= 12) return cleaned;
        if (cleaned.length() >= 11) return "55" + cleaned;
        if (cleaned.length() == 10) return "55" + cleaned;
        return cleaned;
    }

    private String formatarStatus(Pedido.StatusPedido status) {
        if (status == null) return "Desconhecido";
        return switch (status) {
            case AGUARDANDO_PAGAMENTO -> "Aguardando Pagamento";
            case PAGO -> "Pago";
            case EM_PREPARACAO -> "Em Preparação";
            case ENVIADO -> "Enviado";
            case EM_TRANSITO -> "Em Trânsito";
            case ENTREGUE -> "Entregue";
            case CANCELADO -> "Cancelado";
        };
    }

    public static class EnvioResult {
        private final boolean sucesso;
        private final String mensagem;

        public EnvioResult(boolean sucesso, String mensagem) {
            this.sucesso = sucesso;
            this.mensagem = mensagem;
        }

        public boolean isSucesso() { return sucesso; }
        public String getMensagem() { return mensagem; }
    }
}
