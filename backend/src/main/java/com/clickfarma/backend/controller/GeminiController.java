package com.clickfarma.backend.controller;

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
}