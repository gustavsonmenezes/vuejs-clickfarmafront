package com.clickfarma.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);

    @Value("${GROQ_API_KEY:${groq.api.key:}}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GroqService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<String> chat(String mensagem) {
        return chat(mensagem, 0.7);
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Mono<String> chat(String mensagem, double temperature) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new IllegalStateException("GROQ_API_KEY (ou groq.api.key) não configurada"));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", List.of(Map.of("role", "user", "content", mensagem)));
        requestBody.put("temperature", temperature);

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseBody);
                        return root.path("choices").get(0).path("message").path("content").asText();
                    } catch (Exception e) {
                        return "Erro ao processar resposta: " + e.getMessage();
                    }
                });
    }

    public Mono<String> analyzeCart(List<Map<String, Object>> cartItems, Double totalPrice) {
        String prompt = buildCartAnalysisPrompt(cartItems, totalPrice);
        return this.chat(prompt);
    }

    public Mono<String> getWellnessSuggestions(String userId, String userName) {
        String prompt = String.format(
            "Voce e um assistente de saude da ClickFarma. O usuario %s solicitou recomendacoes de bem-estar. Forneca 3 dicas praticas de saude em MARKDOWN.",
            userName != null ? userName : "Cliente"
        );
        return this.chat(prompt);
    }

    private static final String VISION_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";

    public Mono<String> chatWithVision(String imageBase64, String prompt) {
        return chatWithVisionModel(VISION_MODEL, imageBase64, prompt);
    }

    private Mono<String> chatWithVisionModel(String model, String imageBase64, String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new IllegalStateException("GROQ_API_KEY (ou groq.api.key) nao configurada"));
        }

        String mimeType = detectMimeType(imageBase64);
        String cleanBase64;
        if (imageBase64.contains(",")) {
            cleanBase64 = imageBase64.substring(imageBase64.indexOf(",") + 1);
        } else {
            cleanBase64 = imageBase64;
        }

        String imageUri = "data:" + mimeType + ";base64," + cleanBase64;

        Map<String, Object> imageUrlContent = new HashMap<>();
        imageUrlContent.put("type", "image_url");
        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", imageUri);
        imageUrlContent.put("image_url", imageUrl);

        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(Map.of(
            "role", "user",
            "content", List.of(textContent, imageUrlContent)
        )));
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 4096);

        log.info("Enviando imagem para Groq Vision (model: {}, mimeType: {}, base64 size: {} bytes)", model, mimeType, cleanBase64.length());

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                    response.bodyToMono(String.class).flatMap(body -> {
                        log.error("Groq Vision 4xx (model {}) - resposta: {}", model, body);
                        return Mono.error(new IllegalStateException("Groq Vision erro 4xx (" + model + "): " + body));
                    })
                )
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseBody);
                        JsonNode choices = root.path("choices");
                        if (choices.isMissingNode() || choices.isEmpty()) {
                            log.error("Groq Vision sem choices na resposta: {}", responseBody);
                            return "Erro Vision: resposta sem choices";
                        }
                        String content = choices.get(0).path("message").path("content").asText();
                        log.info("Groq Vision (model {}) respondeu com {} caracteres", model, content.length());
                        return content;
                    } catch (Exception e) {
                        log.error("Erro ao parsear resposta Groq Vision: {}", responseBody);
                        return "Erro ao processar resposta Vision: " + e.getMessage();
                    }
                });
    }

    private String detectMimeType(String imageBase64) {
        if (imageBase64.contains(",")) {
            String prefix = imageBase64.substring(0, imageBase64.indexOf(",")).toLowerCase();
            if (prefix.contains("png")) return "image/png";
            if (prefix.contains("webp")) return "image/webp";
            if (prefix.contains("gif")) return "image/gif";
        }
        return "image/jpeg";
    }

    private String buildCartAnalysisPrompt(List<Map<String, Object>> cartItems, Double totalPrice) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analise o carrinho de compras e forneca sugestoes em MARKDOWN:\n\n");
        for (Map<String, Object> item : cartItems) {
            prompt.append("- ").append(item.get("name")).append("\n");
        }
        prompt.append("Total: R$ ").append(totalPrice);
        return prompt.toString();
    }
}
