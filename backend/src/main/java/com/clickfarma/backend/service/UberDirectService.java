package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.*;
import com.clickfarma.backend.model.Entrega;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UberDirectService {

    private static final Logger log = LoggerFactory.getLogger(UberDirectService.class);

    @Value("${uber.direct.api.url:https://api.uber.com/v1/customers}")
    private String apiUrl;

    @Value("${uber.direct.client.id:}")
    private String clientId;

    @Value("${uber.direct.client.secret:}")
    private String clientSecret;

    @Value("${uber.direct.customer.id:}")
    private String customerId;

    @Value("${uber.direct.mode:telegram}")
    private String mode;

    @Value("${farmacia.nome:ClickFarma - Matriz}")
    private String farmaciaNome;

    @Value("${farmacia.endereco:Rua da Hora, 123, Recife - PE}")
    private String farmaciaEndereco;

    @Value("${farmacia.telefone:(81) 3333-3333}")
    private String farmaciaTelefone;

    @Value("${farmacia.latitude:-8.0476}")
    private double farmaciaLatitude;

    @Value("${farmacia.longitude:-34.8770}")
    private double farmaciaLongitude;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired(required = false)
    private TelegramService telegramService;

    @Autowired
    private EntregaService entregaService;

    @Value("${telegram.entregador.chat-id:}")
    private String entregadorChatId;

    public UberDirectQuoteResponseDTO getQuote(String dropoffAddress) {
        if ("api".equals(mode) && temCredenciais()) {
            return getQuoteDaApi(dropoffAddress);
        }
        return getQuoteDoTelegram(dropoffAddress);
    }

    public UberDirectDeliveryResponseDTO createDelivery(
            String quoteId,
            UberDirectContactDTO dropoff,
            List<UberDirectManifestItemDTO> manifestItems,
            String externalOrderId) {

        UberDirectDeliveryResponseDTO apiResult = null;

        if ("api".equals(mode) && temCredenciais()) {
            try {
                apiResult = criarEntregaNaApi(quoteId, dropoff, manifestItems, externalOrderId);
                log.info("Entrega criada na API Uber Direct: {}", apiResult.getId());
            } catch (Exception e) {
                log.error("API Uber Direct falhou, mantendo fallback Telegram: {}", e.getMessage());
            }
        }

        UberDirectDeliveryResponseDTO telegramResult = criarEntregaViaTelegram(
                quoteId, dropoff, manifestItems, externalOrderId);

        return apiResult != null ? apiResult : telegramResult;
    }

    public UberDirectDeliveryResponseDTO getDeliveryStatus(String deliveryId) {
        if ("api".equals(mode) && temCredenciais()) {
            return getStatusDaApi(deliveryId);
        }
        UberDirectDeliveryResponseDTO dto = new UberDirectDeliveryResponseDTO();
        dto.setId(deliveryId);
        dto.setStatus("in_transit");
        dto.setTrackingUrl(null);
        return dto;
    }

    public void cancelDelivery(String deliveryId) {
        if ("api".equals(mode) && temCredenciais()) {
            cancelarNaApi(deliveryId);
        }
        log.info("Entrega {} cancelada (modo telegram)", deliveryId);
    }

    private boolean temCredenciais() {
        return clientId != null && !clientId.isEmpty()
                && clientSecret != null && !clientSecret.isEmpty()
                && customerId != null && !customerId.isEmpty();
    }

    private UberDirectQuoteResponseDTO getQuoteDaApi(String dropoffAddress) {
        try {
            String token = obterTokenApi();
            String url = apiUrl + "/" + customerId + "/delivery_quotes";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("Content-Type", "application/json");

            String body = String.format("""
                    {
                        "pickup_address": "{\\"street_address\\":[\\"%s\\"],\\"city\\":\\"Recife\\",\\"state\\":\\"PE\\",\\"zip_code\\":\\"50000-000\\",\\"country\\":\\"BR\\"}",
                        "dropoff_address": "{\\"street_address\\":[\\"%s\\"],\\"city\\":\\"Recife\\",\\"state\\":\\"PE\\",\\"zip_code\\":\\"50000-000\\",\\"country\\":\\"BR\\"}"
                    }
                    """, farmaciaEndereco.replace("\"", "\\\""), dropoffAddress.replace("\"", "\\\""));

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<UberDirectQuoteResponseDTO> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, UberDirectQuoteResponseDTO.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao chamar Uber Direct API (quote), usando fallback Telegram: {}", e.getMessage());
            return getQuoteDoTelegram(dropoffAddress);
        }
    }

    private UberDirectQuoteResponseDTO getQuoteDoTelegram(String dropoffAddress) {
        String fakeQuoteId = "qt_" + UUID.randomUUID().toString().substring(0, 8);
        double distanciaKm = estimarDistancia(dropoffAddress);
        double fee = 7.0 + (distanciaKm * 1.50);
        int minutos = (int) Math.ceil(distanciaKm * 3 + 15);

        UberDirectQuoteResponseDTO dto = new UberDirectQuoteResponseDTO();
        dto.setQuoteId(fakeQuoteId);
        dto.setDeliveryFee(Math.round(fee * 100.0) / 100.0);
        dto.setCurrency("BRL");
        dto.setEstimatedTime(minutos + " min");
        dto.setEstimatedDistanceKm(Math.round(distanciaKm * 10.0) / 10.0);
        dto.setExpiresAt(Instant.now().plusSeconds(900).toString());
        return dto;
    }

    private UberDirectDeliveryResponseDTO criarEntregaNaApi(
            String quoteId, UberDirectContactDTO dropoff,
            List<UberDirectManifestItemDTO> manifestItems, String externalOrderId) {
        try {
            String token = obterTokenApi();
            String url = apiUrl + "/" + customerId + "/deliveries";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("Content-Type", "application/json");

            StringBuilder itemsJson = new StringBuilder("[");
            for (int i = 0; i < manifestItems.size(); i++) {
                if (i > 0) itemsJson.append(",");
                UberDirectManifestItemDTO item = manifestItems.get(i);
                itemsJson.append(String.format("""
                        {"name":"%s","quantity":%d,"size":"%s"}
                        """, item.getName(), item.getQuantity(), item.getSize() != null ? item.getSize() : "small"));
            }
            itemsJson.append("]");

            String body = String.format("""
                    {
                        "quote_id": "%s",
                        "pickup_address": "{\\"street_address\\":[\\"%s\\"],\\"city\\":\\"Recife\\",\\"state\\":\\"PE\\",\\"zip_code\\":\\"50000-000\\",\\"country\\":\\"BR\\"}",
                        "pickup_name": "%s",
                        "pickup_phone_number": "%s",
                        "pickup_latitude": %f,
                        "pickup_longitude": %f,
                        "dropoff_address": "{\\"street_address\\":[\\"%s\\"],\\"city\\":\\"Recife\\",\\"state\\":\\"PE\\",\\"zip_code\\":\\"50000-000\\",\\"country\\":\\"BR\\"}",
                        "dropoff_name": "%s",
                        "dropoff_phone_number": "%s",
                        "dropoff_latitude": %f,
                        "dropoff_longitude": %f,
                        "manifest_items": %s,
                        "external_order_id": "%s"
                    }
                    """,
                    quoteId,
                    farmaciaEndereco.replace("\"", "\\\""),
                    farmaciaNome, farmaciaTelefone,
                    farmaciaLatitude, farmaciaLongitude,
                    (dropoff.getAddress() != null ? dropoff.getAddress() : "").replace("\"", "\\\""),
                    dropoff.getName() != null ? dropoff.getName() : "",
                    dropoff.getPhone() != null ? dropoff.getPhone() : "",
                    dropoff.getLatitude() != null ? dropoff.getLatitude() : farmaciaLatitude,
                    dropoff.getLongitude() != null ? dropoff.getLongitude() : farmaciaLongitude,
                    itemsJson.toString(),
                    externalOrderId != null ? externalOrderId : ""
            );

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<UberDirectDeliveryResponseDTO> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, UberDirectDeliveryResponseDTO.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao chamar Uber Direct API (delivery), fallback Telegram: {}", e.getMessage());
            return criarEntregaViaTelegram(quoteId, dropoff, manifestItems, externalOrderId);
        }
    }

    private UberDirectDeliveryResponseDTO criarEntregaViaTelegram(
            String quoteId, UberDirectContactDTO dropoff,
            List<UberDirectManifestItemDTO> manifestItems, String externalOrderId) {

        String deliveryId = "ud_" + UUID.randomUUID().toString().substring(0, 10);

        String itensStr = "";
        for (UberDirectManifestItemDTO item : manifestItems) {
            itensStr += "  - " + item.getName() + " x" + item.getQuantity() + "\n";
        }

        String enderecoCompleto = dropoff.getAddress() != null ? dropoff.getAddress() : "";
        String codigoPedido = externalOrderId != null ? "#" + externalOrderId : deliveryId;

        Entrega entrega = entregaService.criarEntrega(
                externalOrderId != null ? Long.parseLong(externalOrderId.replaceAll("\\D", "")) : 0,
                codigoPedido,
                itensStr);

        entregaService.broadcastEntrega(entrega, enderecoCompleto, farmaciaNome, farmaciaEndereco);

        // Seed do entregador inicial da config se ainda não existir
        entregaService.seedFromConfig(entregadorChatId, "Entregador ClickFarma");

        UberDirectDeliveryResponseDTO dto = new UberDirectDeliveryResponseDTO();
        dto.setId(deliveryId);
        dto.setQuoteId(quoteId);
        dto.setStatus("pickup_scheduled");
        dto.setExternalOrderId(externalOrderId);
        dto.setCreatedAt(Instant.now().toString());

        UberDirectContactDTO pickupInfo = new UberDirectContactDTO();
        pickupInfo.setName(farmaciaNome);
        pickupInfo.setAddress(farmaciaEndereco);
        pickupInfo.setPhone(farmaciaTelefone);
        dto.setPickup(pickupInfo);
        dto.setDropoff(dropoff);
        dto.setManifestItems(manifestItems);

        return dto;
    }

    private void notificarEntregadorTelegram(
            String deliveryId, UberDirectContactDTO dropoff,
            List<UberDirectManifestItemDTO> items, String externalOrderId) {

        if (telegramService == null || entregadorChatId == null || entregadorChatId.isEmpty()) {
            log.warn("Telegram nao configurado. Entregador nao foi notificado.");
            return;
        }

        try {
            StringBuilder msg = new StringBuilder();
            msg.append("NOVA ENTREGA UBER DIRECT\n");
            msg.append("========================\n\n");
            msg.append("Pedido: #").append(externalOrderId != null ? externalOrderId : deliveryId).append("\n");
            msg.append("Cliente: ").append(dropoff.getName() != null ? dropoff.getName() : "N/A").append("\n");
            msg.append("Endereço: ").append(dropoff.getAddress() != null ? dropoff.getAddress() : "N/A").append("\n");
            msg.append("Telefone: ").append(dropoff.getPhone() != null ? dropoff.getPhone() : "N/A").append("\n\n");
            msg.append("Itens:\n");
            for (UberDirectManifestItemDTO item : items) {
                msg.append("  - ").append(item.getName()).append(" x").append(item.getQuantity()).append("\n");
            }
            msg.append("\n");
            msg.append("Retirar na farmácia: ").append(farmaciaNome).append("\n");
            msg.append("Endereço: ").append(farmaciaEndereco).append("\n\n");
            msg.append("Responda esta mensagem com:\n");
            msg.append("/aceitar - Aceitar entrega\n");
            msg.append("/recusar - Recusar entrega");

            telegramService.enviarMensagem(entregadorChatId, msg.toString());
            log.info("Entregador notificado via Telegram para entrega {}", deliveryId);
        } catch (Exception e) {
            log.error("Erro ao notificar entregador via Telegram", e);
        }
    }

    private String obterTokenApi() {
        String tokenUrl = "https://auth.uber.com/oauth/v2/token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        String body = "client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&grant_type=client_credentials"
                + "&scope=eats.deliveries";

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<UberTokenResponse> response = restTemplate.exchange(
                tokenUrl, HttpMethod.POST, request, UberTokenResponse.class);

        if (response.getBody() != null) {
            return response.getBody().accessToken;
        }
        throw new RuntimeException("Falha ao obter token Uber Direct");
    }

    private UberDirectDeliveryResponseDTO getStatusDaApi(String deliveryId) {
        try {
            String token = obterTokenApi();
            String url = apiUrl + "/" + customerId + "/deliveries/" + deliveryId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<UberDirectDeliveryResponseDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, UberDirectDeliveryResponseDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao buscar status na API: {}", e.getMessage());
            UberDirectDeliveryResponseDTO dto = new UberDirectDeliveryResponseDTO();
            dto.setId(deliveryId);
            dto.setStatus("in_transit");
            return dto;
        }
    }

    private void cancelarNaApi(String deliveryId) {
        try {
            String token = obterTokenApi();
            String url = apiUrl + "/" + customerId + "/deliveries/" + deliveryId + "/cancel";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            log.error("Erro ao cancelar entrega na API: {}", e.getMessage());
        }
    }

    private double estimarDistancia(String endereco) {
        String cep = endereco.replaceAll("\\D", "");
        if (cep.length() >= 5) {
            try {
                int cepNum = Integer.parseInt(cep.substring(0, 5));
                int cepFarmacia = 50000;
                double diff = Math.abs(cepNum - cepFarmacia);
                return Math.max(1, (diff / 10000.0) * 5.0);
            } catch (NumberFormatException e) {
                return 3.0;
            }
        }
        return 3.0;
    }

    private static class UberTokenResponse {
        @JsonProperty("access_token")
        public String accessToken;
        @JsonProperty("token_type")
        public String tokenType;
        @JsonProperty("expires_in")
        public int expiresIn;

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }

        public int getExpiresIn() { return expiresIn; }
        public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }
    }
}
