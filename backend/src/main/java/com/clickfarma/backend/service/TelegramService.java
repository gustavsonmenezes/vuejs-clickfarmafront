package com.clickfarma.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    @Value("${telegram.bot.token}")
    private String botToken;

    private final WebClient webClient;

    public TelegramService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.telegram.org" ).build();
    }

    public String enviarMensagem(String chatId, String texto) {
        if (chatId == null || chatId.isEmpty()) {
            log.warn("chatId vazio, ignorando envio de mensagem");
            return null;
        }
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("chat_id", chatId);
        formData.add("text", texto);
        formData.add("parse_mode", "Markdown");

        try {
            return webClient.post()
                    .uri("/bot" + botToken + "/sendMessage")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Mensagem enviada ao chat {}: {}", chatId, response))
                    .doOnError(error -> log.error("Erro ao enviar mensagem para chat {}: {}", chatId, error.getMessage()))
                    .blockOptional()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Falha ao enviar mensagem Telegram para chat {}: {}", chatId, e.getMessage());
            return null;
        }
    }
}
