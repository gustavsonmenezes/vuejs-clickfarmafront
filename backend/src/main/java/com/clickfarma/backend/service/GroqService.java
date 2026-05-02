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
        return chat(mensagem, 0.3);
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Mono<String> chat(String mensagem, double temperature) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new IllegalStateException("GROQ_API_KEY (ou groq.api.key) não configurada"));
        }

        String systemPrompt = "Voce e o chatbot da ClickFarma, uma farmacia online. " +
            "REGRAS ABSOLUTAS — NAO QUEBRE NENHUMA:\n" +
            "1. MAXIMO 2-3 frases. NUNCA mais que 3 linhas.\n" +
            "2. VAI DIRETO AO PONTO. Sem saudacoes, sem 'claro', sem 'existem varios'.\n" +
            "3. Cite apenas 1-2 nomes de remedios com dosagem.\n" +
            "4. NAO faca listas. NAO use bullet points. NAO use numeracao.\n" +
            "5. NUNCA diga 'consulte um medico' a menos que seja emergencia real.\n" +
            "6. Responda em portugues brasileiro.\n" +
            "7. Se nao souber: 'Nao tenho essa informacao.'";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", mensagem)
        ));
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", 150);

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
        return chatWithSystemPrompt(prompt, cartAnalysisPrompt());
    }

    private String cartAnalysisPrompt() {
        return "Voce e um consultor de farmacia da ClickFarma. " +
            "Analise os itens do carrinho e responda de forma concisa (MAXIMO 5 linhas) com:\n" +
            "1. **Economia**: sugira 1 generico ou alternativa mais barata\n" +
            "2. **Alerta**: mencione interacoes se houver\n" +
            "3. **Dica**: 1 conselho de uso\n" +
            "Seja direto, sem introducoes.";
    }

    private Mono<String> chatWithSystemPrompt(String userMessage, String systemPrompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userMessage)
        ));
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 200);

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
                })
                .onErrorResume(e -> Mono.just("Erro tecnico ao analisar carrinho."));
    }

    public Mono<String> getWellnessSuggestions(String userId, String userName) {
        String prompt = String.format(
            "Voce e um assistente de saude da ClickFarma. Responda em MAXIMO 3 linhas. " +
            "De 1 dica pratica de bem-estar para %s.",
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
        prompt.append("Itens no carrinho:\n");
        for (Map<String, Object> item : cartItems) {
            String name = (String) item.get("name");
            Double price = ((Number) item.get("price")).doubleValue();
            Integer quantity = ((Number) item.get("quantity")).intValue();
            prompt.append(String.format("- %s (R$ %.2f x %d)\n", name, price, quantity));
        }
        prompt.append(String.format("Total: R$ %.2f", totalPrice));
        return prompt.toString();
    }
}
