package com.clickfarma.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String modelName;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    public String generateResponse(String userMessage) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isEmpty()) {
            return "API key não configurada. Por favor, configure a GEMINI_API_KEY nas variáveis de ambiente.";
        }

        HttpClient client = HttpClient.newHttpClient();

        String url = String.format(GEMINI_API_URL, modelName, apiKey);

        String systemInstruction = """
            Você é um assistente virtual da ClickFarma, uma farmácia.
            Responda de forma amigável e profissional sobre:
            - Medicamentos e seus usos básicos (sempre recomendando consultar um médico)
            - Horário de funcionamento (08h às 22h, todos os dias)
            - Serviços oferecidos: entrega em domicílio, manipulação de medicamentos
            - Política de troca e devolução
            - Programas de desconto e fidelidade
            
            Importante: Nunca dê diagnósticos médicos. Sempre oriente a procurar um profissional de saúde.
            Mantenha respostas concisas e úteis em português.
            """;

        String requestBody = String.format("""
            {
                "contents": [
                    {
                        "parts": [
                            {"text": "%s"},
                            {"text": "%s"}
                        ]
                    }
                ]
            }
            """, systemInstruction, userMessage);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            try {
                String generatedText = jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0)
                        .getAsJsonObject()
                        .get("text")
                        .getAsString();
                return generatedText;
            } catch (Exception e) {
                return "Desculpe, não consegui processar sua mensagem. Tente novamente.";
            }
        } else {
            return "Erro ao conectar com a IA. Status: " + response.statusCode();
        }
    }
}