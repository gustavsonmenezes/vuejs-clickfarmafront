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
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("chat_id", chatId);
        formData.add("text", texto);
        formData.add("parse_mode", "Markdown");

        return webClient.post()
                .uri("/bot" + botToken + "/sendMessage")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Mensagem enviada ao Telegram: {}", response))
                .doOnError(error -> log.error("Erro ao enviar mensagem ao Telegram", error))
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("Telegram nao retornou resposta ao enviar mensagem."));
    }
}
