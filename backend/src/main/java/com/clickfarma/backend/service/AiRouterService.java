package com.clickfarma.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

@Service
public class AiRouterService {

    @Value("${ai.provider:groq}")
    private String aiProvider;

    @Autowired(required = false)
    private GeminiService geminiService;

    @Autowired(required = false)
    private GroqService groqService;

    public Mono<String> chat(String mensagem) {
        if ("gemini".equalsIgnoreCase(aiProvider) && geminiService != null) {
            return geminiService.chat(mensagem);
        } else if (groqService != null) {
            return groqService.chat(mensagem);
        } else {
            return Mono.just("Nenhum servico de IA disponivel");
        }
    }

    public Mono<String> analyzeCart(List<Map<String, Object>> cartItems, Double totalPrice) {
        if ("gemini".equalsIgnoreCase(aiProvider) && geminiService != null) {
            return geminiService.analyzeCart(cartItems, totalPrice);
        } else if (groqService != null) {
            return groqService.analyzeCart(cartItems, totalPrice);
        } else {
            return Mono.just("Servico de IA nao disponivel");
        }
    }

    public Mono<String> getWellnessSuggestions(String userId, String userName) {
        if ("gemini".equalsIgnoreCase(aiProvider) && geminiService != null) {
            return geminiService.getWellnessSuggestions(userId, userName);
        } else if (groqService != null) {
            return groqService.getWellnessSuggestions(userId, userName);
        } else {
            return Mono.just("Servico de IA nao disponivel");
        }
    }
}
