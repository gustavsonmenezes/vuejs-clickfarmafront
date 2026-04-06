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