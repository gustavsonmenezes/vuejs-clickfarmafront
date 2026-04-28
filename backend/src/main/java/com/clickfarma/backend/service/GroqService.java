package com.clickfarma.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class GroqService {

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
