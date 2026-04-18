package com.clickfarma.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OCRService {

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
     * @param imagemBase64 A imagem em formato Base64 (com ou sem o prefixo data:image/...)
     * @return O texto extraído da imagem
     */
    public Mono<String> extrairTextoDeImagem(String imagemBase64) {
        log.info("Iniciando OCR na imagem via OCR Space API");

        // Remove qualquer prefixo data:image que possa ter vindo junto
        String imagemLimpa = imagemBase64;
        if (imagemBase64.contains(",")) {
            imagemLimpa = imagemBase64.substring(imagemBase64.indexOf(",") + 1);
        }

        // CORREÇÃO: Usar MultiValueMap para garantir que o WebClient faça o URL Encoding correto da imagem Base64
        // Sem isso, caracteres como '+' e '/' são corrompidos na transmissão
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("apikey", apiKey);
        formData.add("base64Image", "data:image/jpeg;base64," + imagemLimpa); // Adiciona prefixo esperado pela API
        formData.add("language", language);
        formData.add("isOverlayRequired", "false");
        formData.add("detectOrientation", "true");
        formData.add("scale", "true");
        formData.add("isTable", "true"); // Ajuda a manter a estrutura de linhas em receitas médicas
        formData.add("OCREngine", "2"); // Engine 2 é melhor para textos complexos e manuscritos

        return webClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Accept", "application/json")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        JsonNode root = objectMapper.readTree(response);

                        // Verifica se houve erro na resposta da API
                        if (root.has("OCRExitCode") && root.get("OCRExitCode").asInt() != 1) {
                            String errorMessage = "Erro desconhecido";
                            if (root.has("ErrorMessage") && !root.get("ErrorMessage").isNull()) {
                                errorMessage = root.get("ErrorMessage").get(0).asText();
                            }
                            log.error("Erro retornado pela API OCR Space: {}", errorMessage);
                            return "Erro OCR Space: " + errorMessage;
                        }

                        // Extrai o texto dos resultados processados
                        JsonNode parsedResults = root.get("ParsedResults");
                        if (parsedResults != null && parsedResults.isArray() && parsedResults.size() > 0) {
                            String textoExtraido = parsedResults.get(0)
                                    .get("ParsedText")
                                    .asText();

                            if (textoExtraido == null || textoExtraido.trim().isEmpty()) {
                                log.warn("API retornou sucesso, mas nenhum texto foi extraído.");
                                return "Nenhum texto identificado na imagem.";
                            }

                            log.info("Texto extraído com sucesso ({} caracteres)", textoExtraido.length());
                            return textoExtraido;
                        }

                        return "Nenhum resultado processado encontrado.";

                    } catch (Exception e) {
                        log.error("Erro ao processar JSON de resposta do OCR Space", e);
                        return "Erro ao processar resposta: " + e.getMessage();
                    }
                })
                .onErrorResume(e -> {
                    log.error("Falha na comunicação com OCR Space API", e);
                    return Mono.just("Erro de conexão: " + e.getMessage());
                });
    }
}