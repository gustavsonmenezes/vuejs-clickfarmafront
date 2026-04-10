package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.CartAnalysisRequestDTO;
import com.clickfarma.backend.service.AiRouterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    @Autowired
    private AiRouterService aiRouterService;

    @PostMapping("/chat")
    public Mono<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String mensagem = request.get("message");
        return aiRouterService.chat(mensagem)
                .map(resposta -> Map.of("response", resposta));
    }

    @PostMapping("/wellness")
    public Mono<Map<String, String>> wellness(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String userName = request.get("userName");
        return aiRouterService.getWellnessSuggestions(userId, userName)
                .map(resposta -> Map.of("response", resposta));
    }

    @PostMapping("/analyze-cart")
    public Mono<Map<String, String>> analyzeCart(@RequestBody CartAnalysisRequestDTO request) {
        java.util.List<Map<String, Object>> itemsList = new java.util.ArrayList<>();

        for (CartAnalysisRequestDTO.CartItemDTO item : request.getItems()) {
            Map<String, Object> itemMap = new java.util.HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("category", item.getCategory());
            itemMap.put("description", item.getDescription());
            itemsList.add(itemMap);
        }

        return aiRouterService.analyzeCart(itemsList, request.getTotalPrice())
                .map(resposta -> Map.of("analysis", resposta));
    }
}