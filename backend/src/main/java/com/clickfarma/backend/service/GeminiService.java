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

    @Value("${gemini.api.key:}")
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

    public Mono<String> chat(String mensagem) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ Chave da API Gemini não configurada");
            return Mono.just("⚠️ Chave da API Gemini não configurada. Configure a variável GEMINI_API_KEY.");
        }

        System.out.println("📤 Enviando mensagem para Gemini: " + mensagem);

        Map<String, Object> request = new HashMap<>();

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(
                Map.of("text", mensagem)
        ));

        request.put("contents", List.of(content));

        return webClient.post()
                .uri("/models/gemini-2.0-flash:generateContent?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        System.out.println("📥 Resposta recebida do Gemini");
                        List<Map<String, Object>> candidates =
                                (List<Map<String, Object>>) response.get("candidates");
                        if (candidates != null && !candidates.isEmpty()) {
                            Map<String, Object> candidate = candidates.get(0);
                            Map<String, Object> contentResp =
                                    (Map<String, Object>) candidate.get("content");
                            List<Map<String, Object>> parts =
                                    (List<Map<String, Object>>) contentResp.get("parts");
                            String resposta = (String) parts.get(0).get("text");
                            System.out.println("✅ Resposta processada com sucesso");
                            return resposta;
                        }
                        System.err.println("⚠️ Nenhum candidato encontrado na resposta");
                        return "Desculpe, não consegui processar sua mensagem.";
                    } catch (Exception e) {
                        System.err.println("❌ Erro ao processar resposta: " + e.getMessage());
                        e.printStackTrace();
                        return "Erro ao processar resposta: " + e.getMessage();
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("❌ Erro na chamada à API Gemini: " + e.getMessage());
                    e.printStackTrace();
                    return Mono.just("Desculpe, estou com problemas técnicos. Erro: " + e.getMessage());
                });
    }
}
