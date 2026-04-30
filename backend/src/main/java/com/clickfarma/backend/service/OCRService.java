package com.clickfarma.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@Service
public class OCRService {
    private static final Logger log = LoggerFactory.getLogger(OCRService.class);

    @Value("${ocr.space.api.key}")
    private String apiKey;

    @Value("${ocr.space.api.url}")
    private String apiUrl;

    @Value("${ocr.space.language:por}")
    private String language;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OCRService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Extrai texto de uma imagem usando OCR.space API
     * @param imagemBase64 A imagem em formato Base64 (sem o prefixo data:image/...)
     * @return O texto extraído da imagem
     */
    public Mono<String> extrairTextoDeImagem(String imagemBase64) {
        log.info("Iniciando OCR na imagem");

        String activeApiKey = (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your_ocr_api_key_here")) ? apiKey : "helloworld";

        // Assegura prefixo data:image para o OCR Space
        String dataUrl = imagemBase64;
        if (!dataUrl.startsWith("data:image")) {
            dataUrl = "data:image/jpeg;base64," + imagemBase64;
        }

        try {
            // Encode the dataUrl to prevent invalid URI characters
            String encodedDataUrl = java.net.URLEncoder.encode(dataUrl, "UTF-8");
            
            String requestBody = String.format(
                    "apikey=%s&base64Image=%s&language=%s&isOverlayRequired=false&detectOrientation=true&scale=true",
                    activeApiKey, encodedDataUrl, language
            );

            return webClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Accept", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        log.debug("Resposta OCR: {}", response);
                        JsonNode root = objectMapper.readTree(response);

                        // Verifica se houve erro
                        if (root.has("OCRExitCode") && root.get("OCRExitCode").asInt() != 1) {
                            String errorMessage = root.has("ErrorMessage")
                                    ? root.get("ErrorMessage").asText()
                                    : "Erro desconhecido no OCR";
                            log.error("Erro OCR: {}", errorMessage);
                            return "Erro ao processar imagem: " + errorMessage;
                        }

                        // Extrai o texto das páginas
                        JsonNode parsedResults = root.get("ParsedResults");
                        if (parsedResults != null && parsedResults.size() > 0) {
                            String textoExtraido = parsedResults.get(0)
                                    .get("ParsedText")
                                    .asText();
                            log.info("Texto extraído com sucesso ({} caracteres)", textoExtraido.length());
                            return textoExtraido;
                        }

                        return "Nenhum texto encontrado na imagem";

                    } catch (Exception e) {
                        log.error("Erro ao processar resposta OCR", e);
                        return "Erro ao processar resposta: " + e.getMessage();
                    }
                })
                .onErrorResume(e -> {
                    log.error("Erro na chamada OCR API", e);
                    return Mono.just("Erro na comunicação com o serviço OCR: " + e.getMessage());
                });
        } catch (Exception e) {
            log.error("Erro ao codificar URL", e);
            return Mono.just("Erro ao processar a imagem localmente: " + e.getMessage());
        }
    }
}