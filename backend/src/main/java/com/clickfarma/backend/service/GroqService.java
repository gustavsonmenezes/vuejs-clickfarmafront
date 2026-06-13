package com.clickfarma.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);

    @Value("${GROQ_API_KEY:${groq.api.key:}}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private ChatToolService chatToolService;

    public GroqService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<String> chat(String mensagem) {
        return chat(mensagem, 0.3, null, null);
    }

    public Mono<String> chat(String mensagem, String weatherContext) {
        return chat(mensagem, 0.3, weatherContext, null);
    }

    public Mono<String> chat(String mensagem, Long usuarioId) {
        return chat(mensagem, 0.3, null, usuarioId);
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Mono<String> chat(String mensagem, String weatherContext, Long usuarioId) {
        return chat(mensagem, 0.3, weatherContext, usuarioId);
    }

    public Mono<String> chat(String mensagem, double temperature, String weatherContext) {
        return chat(mensagem, temperature, weatherContext, null);
    }

    public Mono<String> chat(String mensagem, double temperature, String weatherContext, Long usuarioId) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new IllegalStateException("GROQ_API_KEY (ou groq.api.key) nao configurada"));
        }

        String systemPrompt = buildSystemPrompt(weatherContext, usuarioId);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", mensagem));

        List<Map<String, Object>> tools = chatToolService.getToolDefinitions();

        return chatWithTools(messages, tools, usuarioId, 0, temperature);
    }

    private Mono<String> chatWithTools(List<Map<String, Object>> messages, List<Map<String, Object>> tools,
                                        Long usuarioId, int depth, double temperature) {
        if (depth > 5) {
            return Mono.just("Limite de consultas atingido. Tente perguntar de forma mais direta.");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", messages);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", 500);

        if (tools != null && !tools.isEmpty()) {
            requestBody.put("tools", tools);
            requestBody.put("tool_choice", "auto");
        }

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> processResponse(responseBody, messages, tools, usuarioId, depth, temperature))
                .onErrorResume(e -> {
                    log.error("Erro na chamada Groq: {}", e.getMessage());
                    return Mono.just("Desculpe, ocorreu um erro ao processar sua solicitação. Tente novamente.");
                });
    }

    private Mono<String> processResponse(String responseBody, List<Map<String, Object>> messages,
                                          List<Map<String, Object>> tools, Long usuarioId,
                                          int depth, double temperature) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messageNode = root.path("choices").get(0).path("message");
            JsonNode toolCalls = messageNode.path("tool_calls");

            if (toolCalls.isMissingNode() || toolCalls.isEmpty()) {
                String content = messageNode.path("content").asText("");
                if (content.isBlank()) {
                    content = "Nao entendi. Pode reformular?";
                }
                return Mono.just(content);
            }

            List<Map<String, Object>> updatedMessages = new ArrayList<>(messages);
            updatedMessages.add(jsonNodeToMessageMap(messageNode));

            for (int i = 0; i < toolCalls.size(); i++) {
                JsonNode tc = toolCalls.get(i);
                String toolName = tc.path("function").path("name").asText();
                String args = tc.path("function").path("arguments").asText();
                String toolCallId = tc.path("id").asText();

                log.info("Tool call #{}: {} (args: {})", i, toolName, args);
                String result = chatToolService.executeTool(toolName, args, usuarioId);
                log.info("Tool result #{}: {} chars", i, result.length());

                Map<String, Object> toolMessage = new LinkedHashMap<>();
                toolMessage.put("role", "tool");
                toolMessage.put("tool_call_id", toolCallId);
                toolMessage.put("content", result);
                updatedMessages.add(toolMessage);
            }

            return chatWithTools(updatedMessages, tools, usuarioId, depth + 1, temperature);
        } catch (Exception e) {
            log.error("Erro ao processar resposta Groq: {}", e.getMessage());
            return Mono.just("Desculpe, tive um problema ao processar a resposta.");
        }
    }

    private String buildSystemPrompt(String weatherContext, Long usuarioId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Voce e o chatbot da ClickFarma, uma farmacia online. ");
        sb.append("REGRAS ABSOLUTAS — NAO QUEBRE NENHUMA:\n");
        sb.append("1. MAXIMO 2-3 frases. NUNCA mais que 3 linhas.\n");
        sb.append("2. VAI DIRETO AO PONTO. Sem saudacoes, sem 'claro', sem 'existem varios'.\n");
        sb.append("3. Cite apenas 1-2 nomes de remedios com dosagem.\n");
        sb.append("4. NAO faca listas. NAO use bullet points. NAO use numeracao.\n");
        sb.append("5. NUNCA diga 'consulte um medico' a menos que seja emergencia real.\n");
        sb.append("6. Responda em portugues brasileiro.\n");
        sb.append("7. Se nao souber: 'Nao tenho essa informacao.'\n");
        sb.append("8. SOBRE O DESENVOLVEDOR: Se perguntarem quem criou o sistema, responda exatamente: 'O Sistema ClickFarma foi desenvolvido por Gustavson Barros e Douglas Tranquilino.'\n");
        sb.append("9. REGRA DE OURO: Quando o usuario perguntar sobre sintomas ou pedir recomendacao de remedio, voce DEVE incluir a tag |CARRINHO:NomeExatoProduto| no final da frase. Exemplo: 'Para dor de cabeca use Dipirona. |CARRINHO:Dipirona|'\n");
        sb.append("10. FUNCIONAMENTO DAS FERRAMENTAS: Quando o usuario perguntar sobre dados pessoais (meus pedidos, meus medicamentos, rastrear entrega, consultar estoque), voce DEVE chamar a ferramenta correspondente. NAO tente adivinhar ou inventar dados.\n");
        sb.append("11. Exiba os dados retornados pelas ferramentas de forma amigavel e resumida. Se a ferramenta retornar dados em JSON, interprete e formate como texto natural.\n");
        sb.append("12. Se pedirem para agendar recompra, cadastrar algo ou fazer acao, explique que voce pode ajudar e oriente o usuario a usar a interface.\n");
        sb.append("13. Exemplo de uso correto: Usuario: 'Quero ver meus pedidos' → Voce chama buscarPedidos() → retorna JSON → voce formata: 'Voce tem 5 pedidos. O mais recente foi em 15/06 no valor de R$89,90.'");

        if (weatherContext != null && !weatherContext.isBlank()) {
            sb.append("\n13. CONTEXTO DE CLIMA: ").append(weatherContext)
              .append(". Use essa informacao para sugerir produtos relevantes ao clima quando apropriado.");
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> jsonNodeToMessageMap(JsonNode node) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("role", node.path("role").asText());

        JsonNode content = node.path("content");
        if (content.isNull() || content.isMissingNode()) {
            map.put("content", null);
        } else {
            map.put("content", content.asText());
        }

        JsonNode toolCalls = node.path("tool_calls");
        if (!toolCalls.isMissingNode() && !toolCalls.isEmpty()) {
            List<Map<String, Object>> tcList = new ArrayList<>();
            for (JsonNode tc : toolCalls) {
                Map<String, Object> tcMap = new LinkedHashMap<>();
                tcMap.put("id", tc.path("id").asText());
                tcMap.put("type", tc.path("type").asText("function"));

                Map<String, Object> funcMap = new LinkedHashMap<>();
                funcMap.put("name", tc.path("function").path("name").asText());
                funcMap.put("arguments", tc.path("function").path("arguments").asText());
                tcMap.put("function", funcMap);

                tcList.add(tcMap);
            }
            map.put("tool_calls", tcList);
        }

        return map;
    }

    public Mono<String> analyzeCart(List<Map<String, Object>> cartItems, Double totalPrice) {
        String prompt = buildCartAnalysisPrompt(cartItems, totalPrice);
        return chatWithSystemPrompt(prompt, cartAnalysisPrompt());
    }

    private String cartAnalysisPrompt() {
        return "Voce e um consultor de farmacia da ClickFarma. " +
            "Analise os itens do carrinho e responda de forma concisa (MAXIMO 5 linhas) com:\n" +
            "1. **Economia**: sugira 1 generico ou alternativa mais barata\n" +
            "2. **Alerta**: mencione interacoes se houver\n" +
            "3. **Dica**: 1 conselho de uso\n" +
            "Seja direto, sem introducoes.";
    }

    private Mono<String> chatWithSystemPrompt(String userMessage, String systemPrompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userMessage)
        ));
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 200);

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseBody);
                        return root.path("choices").get(0).path("message").path("content").asText();
                    } catch (Exception e) {
                        return "Erro ao processar resposta: " + e.getMessage();
                    }
                })
                .onErrorResume(e -> Mono.just("Erro tecnico ao analisar carrinho."));
    }

    public Mono<String> getWellnessSuggestions(String userId, String userName) {
        String prompt = String.format(
            "Voce e um assistente de saude da ClickFarma. Responda em MAXIMO 3 linhas. " +
            "De 1 dica pratica de bem-estar para %s.",
            userName != null ? userName : "Cliente"
        );
        return this.chat(prompt);
    }

    private static final String VISION_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";

    public Mono<String> chatWithVision(String imageBase64, String prompt) {
        return chatWithVisionModel(VISION_MODEL, imageBase64, prompt);
    }

    private Mono<String> chatWithVisionModel(String model, String imageBase64, String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new IllegalStateException("GROQ_API_KEY (ou groq.api.key) nao configurada"));
        }

        String mimeType = detectMimeType(imageBase64);
        String cleanBase64;
        if (imageBase64.contains(",")) {
            cleanBase64 = imageBase64.substring(imageBase64.indexOf(",") + 1);
        } else {
            cleanBase64 = imageBase64;
        }

        String imageUri = "data:" + mimeType + ";base64," + cleanBase64;

        Map<String, Object> imageUrlContent = new HashMap<>();
        imageUrlContent.put("type", "image_url");
        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", imageUri);
        imageUrlContent.put("image_url", imageUrl);

        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(Map.of(
            "role", "user",
            "content", List.of(textContent, imageUrlContent)
        )));
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 4096);

        log.info("Enviando imagem para Groq Vision (model: {}, mimeType: {}, base64 size: {} bytes)", model, mimeType, cleanBase64.length());

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                    response.bodyToMono(String.class).flatMap(body -> {
                        log.error("Groq Vision 4xx (model {}) - resposta: {}", model, body);
                        return Mono.error(new IllegalStateException("Groq Vision erro 4xx (" + model + "): " + body));
                    })
                )
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseBody);
                        JsonNode choices = root.path("choices");
                        if (choices.isMissingNode() || choices.isEmpty()) {
                            log.error("Groq Vision sem choices na resposta: {}", responseBody);
                            return "Erro Vision: resposta sem choices";
                        }
                        String content = choices.get(0).path("message").path("content").asText();
                        log.info("Groq Vision (model {}) respondeu com {} caracteres", model, content.length());
                        return content;
                    } catch (Exception e) {
                        log.error("Erro ao parsear resposta Groq Vision: {}", responseBody);
                        return "Erro ao processar resposta Vision: " + e.getMessage();
                    }
                });
    }

    private String detectMimeType(String imageBase64) {
        if (imageBase64.contains(",")) {
            String prefix = imageBase64.substring(0, imageBase64.indexOf(",")).toLowerCase();
            if (prefix.contains("png")) return "image/png";
            if (prefix.contains("webp")) return "image/webp";
            if (prefix.contains("gif")) return "image/gif";
        }
        return "image/jpeg";
    }

    public List<String> buscarNomesPorSintoma(String sintoma) {
        String systemPrompt = "Voce e um assistente de farmacia. Retorne APENAS um JSON valido com um array de strings contendo nomes de medicamentos e produtos de farmacia comuns para tratar o sintoma informado. Nao inclua explicacoes, nem markdown, nem backticks. Apenas o JSON puro. Exemplo: [\"Dipirona\", \"Paracetamol\", \"Ibuprofeno\"]";
        String userPrompt = "Liste medicamentos para: " + sintoma;

        try {
            String response = chatWithSystemPromptSync(userPrompt, systemPrompt);

            String jsonStr = response.trim();
            if (jsonStr.startsWith("```")) {
                int firstBrace = jsonStr.indexOf('[');
                int lastBrace = jsonStr.lastIndexOf(']') + 1;
                if (firstBrace >= 0 && lastBrace > firstBrace) {
                    jsonStr = jsonStr.substring(firstBrace, lastBrace);
                }
            }

            JsonNode node = objectMapper.readTree(jsonStr);
            if (node.isArray()) {
                List<String> nomes = new ArrayList<>();
                for (JsonNode item : node) {
                    if (item.isTextual() && !item.asText().isBlank()) {
                        nomes.add(item.asText());
                    }
                }
                return nomes;
            }
        } catch (Exception e) {
            log.error("Erro ao buscar nomes por sintoma '{}': {}", sintoma, e.getMessage());
        }
        return List.of();
    }

    private String chatWithSystemPromptSync(String userMessage, String systemPrompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("GROQ_API_KEY nao configurada");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userMessage)
        ));
        requestBody.put("temperature", 0.2);
        requestBody.put("max_tokens", 300);

        String responseBody = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Erro ao parsear resposta Groq: {}", e.getMessage());
            throw new RuntimeException("Erro ao processar resposta da IA", e);
        }
    }

    private String buildCartAnalysisPrompt(List<Map<String, Object>> cartItems, Double totalPrice) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Itens no carrinho:\n");
        for (Map<String, Object> item : cartItems) {
            String name = (String) item.get("name");
            Double price = ((Number) item.get("price")).doubleValue();
            Integer quantity = ((Number) item.get("quantity")).intValue();
            prompt.append(String.format("- %s (R$ %.2f x %d)\n", name, price, quantity));
        }
        prompt.append(String.format("Total: R$ %.2f", totalPrice));
        return prompt.toString();
    }
}
