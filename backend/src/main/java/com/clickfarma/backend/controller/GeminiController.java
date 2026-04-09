package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.CartAnalysisRequestDTO;
import com.clickfarma.backend.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "*")
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/chat")
    public Mono<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String mensagem = request.get("message");
        return geminiService.chat(mensagem)
                .map(resposta -> Map.of("response", resposta));
    }

    /**
     * Endpoint para análise inteligente de carrinho
     * Recebe os itens do carrinho e retorna sugestões de economia, dicas de uso, etc
     */
    @PostMapping("/analyze-cart")
    public Mono<Map<String, String>> analyzeCart(@RequestBody CartAnalysisRequestDTO request) {
        return geminiService.analyzeCart(request.getItems(), request.getTotalPrice())
                .map(resposta -> Map.of("analysis", resposta));
    }
}
