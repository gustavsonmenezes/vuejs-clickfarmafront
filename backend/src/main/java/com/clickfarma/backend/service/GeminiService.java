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

        String prompt = buildCartAnalysisPrompt(cartItems, totalPrice);
        System.out.println("📤 Enviando análise de carrinho para Gemini");

        return chatWithSystemPrompt(prompt, cartAnalysisPrompt());
    }

    private String cartAnalysisPrompt() {
        return "Voce e um consultor de farmacia da ClickFarma. " +
            "Analise os itens do carrinho e responda de forma concisa (MAXIMO 5 linhas) com:\n" +
            "1. **Economia**: sugira 1 genérico ou alternativa mais barata\n" +
            "2. **Alerta**: mencione interações se houver\n" +
            "3. **Dica**: 1 conselho de uso\n" +
            "Seja direto, sem introducoes.";
    }

    private Mono<String> chatWithSystemPrompt(String userMessage, String systemPrompt) {
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(
                Map.of("text", systemPrompt + "\n\n" + userMessage)
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
                        List<Map<String, Object>> candidates =
                                (List<Map<String, Object>>) response.get("candidates");
                        if (candidates != null && !candidates.isEmpty()) {
                            Map<String, Object> candidate = candidates.get(0);
                            Map<String, Object> contentResp =
                                    (Map<String, Object>) candidate.get("content");
                            List<Map<String, Object>> parts =
                                    (List<Map<String, Object>>) contentResp.get("parts");
                            return (String) parts.get(0).get("text");
                        }
                        return "Nao consegui analisar o carrinho.";
                    } catch (Exception e) {
                        return "Erro ao processar resposta: " + e.getMessage();
                    }
                })
                .onErrorResume(e -> Mono.just("Erro tecnico ao analisar carrinho."));
    }

    /**
     * Constrói um prompt estruturado para análise de carrinho
     */
    private String buildCartAnalysisPrompt(List<Map<String, Object>> cartItems, Double totalPrice) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Você é um assistente de farmácia. Responda em MAXIMO 3 linhas. ");
        prompt.append("Analise o carrinho e forneça apenas:\n");
        prompt.append("- 1 dica de economia ou genérico disponível\n");
        prompt.append("- 1 alerta de interação se houver\n\n");
        prompt.append("Itens:\n");
        for (Map<String, Object> item : cartItems) {
            String name = (String) item.get("name");
            Double price = ((Number) item.get("price")).doubleValue();
            Integer quantity = ((Number) item.get("quantity")).intValue();
            prompt.append(String.format("- %s (R$ %.2f x %d)\n", name, price, quantity));
        }
        prompt.append(String.format("Total: R$ %.2f", totalPrice));
        return prompt.toString();
    }

    /**
     * Gera recomendações de bem-estar personalizadas
     */
    public Mono<String> getWellnessSuggestions(String userId, String userName) {
        String prompt = String.format(
                "Voce e assistente de saude da ClickFarma. Responda em MAXIMO 3 linhas. " +
                "Dê 1 dica pratica de bem-estar para %s.",
                userName != null ? userName : "Cliente"
        );
        return this.chat(prompt);
    }

    public Mono<String> chat(String mensagem) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ Chave da API Gemini não configurada");
            return Mono.just("⚠️ Chave da API Gemini não configurada. Configure a variável GEMINI_API_KEY.");
        }

        System.out.println("📤 Enviando mensagem para Gemini: " + mensagem);

        String systemPrompt = "Voce e o chatbot da ClickFarma, uma farmacia online. " +
            "REGRAS ABSOLUTAS — NAO QUEBRE NENHUMA:\n" +
            "1. MAXIMO 2-3 frases. NUNCA mais que 3 linhas.\n" +
            "2. VAI DIRETO AO PONTO. Sem saudacoes, sem 'claro', sem 'existem varios'.\n" +
            "3. Cite apenas 1-2 nomes de remedios com dosagem.\n" +
            "4. NAO faca listas. NAO use bullet points. NAO use numeracao.\n" +
            "5. NUNCA diga 'consulte um medico' a menos que seja emergencia real.\n" +
            "6. Responda em portugues brasileiro.\n" +
            "7. Se nao souber: 'Nao tenho essa informacao.'\n\n" +
            "Exemplo de resposta boa:\n" +
            "Usuario: 'Qual remedio para dor de cabeca?'\n" +
            "Voce: 'Para dor de cabeça leve, use Paracetamol 500mg ou Ibuprofeno 400mg. Tome a cada 6-8 horas com agua.'\n\n";

        Map<String, Object> request = new HashMap<>();

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(
                Map.of("text", systemPrompt + "Pergunta do usuario: " + mensagem)
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