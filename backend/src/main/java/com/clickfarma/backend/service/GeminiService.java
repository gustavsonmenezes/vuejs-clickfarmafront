package com.clickfarma.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    private final WebClient webClient;

    public GeminiService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    /**
     * Analisa o carrinho do usuário e fornece sugestões inteligentes
     * Inclui: economia potencial, dicas de uso, alertas de interação
     */
    public Mono<String> analyzeCart(List<Map<String, Object>> cartItems, Double totalPrice) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ Chave da API Gemini não configurada");
            return Mono.just("⚠️ Chave da API Gemini não configurada. Configure a variável GEMINI_API_KEY.");
        }

        // Construir prompt inteligente para análise de carrinho
        String prompt = buildCartAnalysisPrompt(cartItems, totalPrice);
        System.out.println("📤 Enviando análise de carrinho para Gemini");

        return this.chat(prompt);
    }

    /**
     * Constrói um prompt estruturado para análise de carrinho
     */
    private String buildCartAnalysisPrompt(List<Map<String, Object>> cartItems, Double totalPrice) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Você é um assistente especializado em farmácia e saúde. ");
        prompt.append("Analise o carrinho de compras a seguir e forneça sugestões úteis em MARKDOWN:\n\n");

        prompt.append("**Itens no Carrinho:**\n");
        for (Map<String, Object> item : cartItems) {
            String name = (String) item.get("name");
            Double price = ((Number) item.get("price")).doubleValue();
            Integer quantity = ((Number) item.get("quantity")).intValue();
            String category = (String) item.get("category");
            String description = (String) item.get("description");

            prompt.append(String.format("- **%s** (Categoria: %s)\n", name, category));
            prompt.append(String.format("  - Descrição: %s\n", description));
            prompt.append(String.format("  - Preço unitário: R$ %.2f | Quantidade: %d | Subtotal: R$ %.2f\n\n",
                    price, quantity, price * quantity));
        }

        prompt.append(String.format("**Total do Carrinho:** R$ %.2f\n\n", totalPrice));

        prompt.append("Por favor, forneça:\n");
        prompt.append("1. **Análise de Economia**: Existem genéricos ou versões mais baratas disponíveis?\n");
        prompt.append("2. **Dicas de Uso**: Conselhos sobre como usar os medicamentos (horários, com/sem alimentos)?\n");
        prompt.append("3. **Alertas**: Possíveis interações entre medicamentos ou contraindicações?\n");
        prompt.append("4. **Sugestões**: Outros produtos que poderiam complementar a compra?\n\n");
        prompt.append("Responda de forma clara e concisa, usando MARKDOWN com negrito, listas e seções bem definidas.");

        return prompt.toString();
    }

    /**
     * Gera recomendações de bem-estar personalizadas
     */
    public Mono<String> getWellnessSuggestions(String userId, String userName) {
        String prompt = String.format(
                "Olá Gemini! Você é um assistente de saúde da ClickFarma. " +
                        "O usuário %s (ID: %s) solicitou recomendações de bem-estar. " +
                        "Forneça 3 dicas práticas de saúde, nutrição ou cuidados pessoais em MARKDOWN. " +
                        "Seja amigável e use emojis.",
                userName != null ? userName : "Cliente",
                userId != null ? userId : "N/A"
        );
        return this.chat(prompt);
    }

    public Mono<String> chat(String mensagem) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ Chave da API Gemini não configurada");
            return Mono.just("⚠️ Chave da API Gemini não configurada. Configure a variável GEMINI_API_KEY no .env do backend.");
        }

        System.out.println("📤 Enviando mensagem para Gemini");

        Map<String, Object> request = new HashMap<>();

        // Add System Instruction to limit behavior
        Map<String, Object> systemInstruction = new HashMap<>();
        systemInstruction.put("parts", List.of(
            Map.of("text", "Você é o Agente de IA oficial da farmácia ClickFarma. Suas regras estritas são: \n" +
                           "1. Foque EXCLUSIVAMENTE em produtos da ClickFarma, dicas de bem-estar corporais/vitamínicos e interações de itens de farmácia.\n" +
                           "2. Apenas recomende produtos que NÃO necessitam de prescrição médica. Nunca recomende antibióticos ou tarjas-preta.\n" +
                           "3. Evite responder a qualquer assunto fora do escopo de farmácia, saúde e catálogo da ClickFarma.\n" +
                           "4. Mantenha respostas curtas, em MARKDOWN amigável.")
        ));
        request.put("systemInstruction", systemInstruction);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(
            Map.of("text", mensagem)
        ));

        request.put("contents", List.of(content));

        return webClient.post()
                .uri("/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (candidates != null && !candidates.isEmpty()) {
                            Map<String, Object> candidate = candidates.get(0);
                            Map<String, Object> contentResp = (Map<String, Object>) candidate.get("content");
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResp.get("parts");
                            return (String) parts.get(0).get("text");
                        }
                        return "Desculpe, não consegui processar sua mensagem.";
                    } catch (Exception e) {
                        return "Erro interno ao processar resposta.";
                    }
                })
                .onErrorResume(e -> Mono.just("Desculpe, ocorreu um erro de conexão com a IA ou a chave de API está ausente/inválida."));
    }

    public Mono<String> extractTextFromImage(String base64Image) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.just("Erro: Chave da API Gemini não configurada");
        }

        String mimeType = "image/jpeg";
        String cleanBase64 = base64Image;
        if (base64Image.contains(",")) {
            cleanBase64 = base64Image.split(",")[1];
        }

        Map<String, Object> request = new HashMap<>();

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(
            Map.of("text", "Atue como farmacêutico e leia a imagem. Extraia SOMENTE os nomes dos princípios ativos (remédios) descritos, separados por vírgula. Não escreva mais nada."),
            Map.of("inlineData", Map.of(
                "mimeType", mimeType,
                "data", cleanBase64
            ))
        ));

        request.put("contents", List.of(content));

        return webClient.post()
                .uri("/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (candidates != null && !candidates.isEmpty()) {
                            Map<String, Object> candidate = candidates.get(0);
                            Map<String, Object> contentResp = (Map<String, Object>) candidate.get("content");
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResp.get("parts");
                            return (String) parts.get(0).get("text");
                        }
                        return "Erro: Falha ao extrair texto da imagem pela inteligência artificial.";
                    } catch (Exception e) {
                        return "Erro interno ao processar visão.";
                    }
                })
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class, e -> {
                    System.err.println("Gemini Vision HTTP Error " + e.getStatusCode());
                    System.err.println("Gemini Response Body: " + e.getResponseBodyAsString());
                    return Mono.just("Erro: Falha gravíssima na leitura inteligente da API Vision. " + e.getStatusCode());
                })
                .onErrorResume(e -> {
                    System.err.println("Erro inesperado no Gemini Vision: " + e.getMessage());
                    return Mono.just("Erro: Falha gravíssima na leitura inteligente da API Vision.");
                });
    }
}