package com.clickfarma.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.clickfarma.backend.model.Produto;

@Service
public class GroqService {

    @Value("${GROQ_API_KEY:}")
    private String apiKey;

    @Autowired
    private ProdutoRepository produtoRepository;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GroqService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<String> chat(String mensagem) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_groq_api_key_here")) {
            System.err.println("❌ Chave da API Groq/Llama não configurada");
            return Mono.just("⚠️ Chave da API Llama não configurada no .env. (Verifique GROQ_API_KEY)");
        }

        // Recupera produtos em tempo real do BD para construir o contexto:
        List<Produto> produtos = null;
        try {
            if (produtoRepository != null) {
                produtos = produtoRepository.findAll();
            }
        } catch(Exception e) {
            System.err.println("Aviso: Não foi possível carregar o repositório de Produtos para injeção do Groq.");
        }

        StringBuilder catalogoContexto = new StringBuilder();
        if (produtos != null && !produtos.isEmpty()) {
            catalogoContexto.append("\nNOSSO ESTOQUE ATUAL:\n");
            for (Produto p : produtos) {
                if (p.getEstoque() != null && p.getEstoque() > 0) {
                    catalogoContexto.append("- ").append(p.getNome()).append(" (R$ ").append(p.getPreco()).append(")\n");
                }
            }
            catalogoContexto.append("\nIMPORTANTE: NUNCA sugira, invente ou recomende medicamentos que NÃO estejam listados acima. Só ofereça produtos listados no NOSSO ESTOQUE ATUAL. Se uma condição não for tratável com nosso estoque, explique cordialmente.\n");
        } else {
            catalogoContexto.append("\n[Nenhum produto em estoque no momento. Informe ao cliente para retornar mais tarde.]\n");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(Map.of("role", "system", "content", 
            "Você é o Agente de IA oficial da farmácia ClickFarma. Suas regras estritas são: \n" +
            "1. Foque EXCLUSIVAMENTE em produtos da ClickFarma, dicas de bem-estar e interações de itens de farmácia.\n" +
            "2. Apenas recomende produtos que NÃO necessitam de prescrição médica. NUNCA recomende antibióticos ou tarjas-preta.\n" +
            "3. Mantenha respostas curtas, em MARKDOWN amigável." +
            catalogoContexto.toString()
        ));
        messages.add(Map.of("role", "user", "content", mensagem));
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        return processarRequisicaoChat(requestBody);
    }

    public Mono<String> chatWithHistory(List<Map<String, String>> conversacao) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_groq_api_key_here")) {
            System.err.println("❌ Chave da API Groq/Llama não configurada");
            return Mono.just("⚠️ Chave da API Llama não configurada no .env. (Verifique GROQ_API_KEY)");
        }

        List<Produto> produtos = null;
        try {
            if (produtoRepository != null) {
                produtos = produtoRepository.findAll();
            }
        } catch(Exception e) {
            System.err.println("Aviso: Não foi possível carregar o repositório.");
        }

        StringBuilder catalogoContexto = new StringBuilder();
        if (produtos != null && !produtos.isEmpty()) {
            catalogoContexto.append("\nNOSSO ESTOQUE ATUAL:\n");
            for (Produto p : produtos) {
                if (p.getEstoque() != null && p.getEstoque() > 0) {
                    catalogoContexto.append("- ").append(p.getNome()).append(" (R$ ").append(p.getPreco()).append(")\n");
                }
            }
            catalogoContexto.append("\nIMPORTANTE: NUNCA sugira, invente ou recomende medicamentos que NÃO estejam listados acima. Só ofereça produtos listados no NOSSO ESTOQUE ATUAL. Se uma condição não for tratavel com o que temos, explique cordialmente.\n");
        } else {
            catalogoContexto.append("\n[Nenhum produto em estoque no momento. Informe ao cliente para retornar mais tarde.]\n");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(Map.of("role", "system", "content", 
            "Você é o Agente de IA oficial da farmácia ClickFarma. Suas regras estritas são: \n" +
            "1. Foque EXCLUSIVAMENTE em tutoria, produtos, saúde e dicas de bem-estar.\n" +
            "2. Apenas recomende produtos que NÃO necessitam de prescrição médica.\n" +
            "3. Mantenha respostas curtas e humanas, em MARKDOWN." +
            catalogoContexto.toString()
        ));
        
        // Add the history. Frontend sends role and content
        messages.addAll(conversacao);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        return processarRequisicaoChat(requestBody);
    }

    private Mono<String> processarRequisicaoChat(Map<String, Object> requestBody) {
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
                });
    }

    public Mono<String> analyzeCart(List<Map<String, Object>> cartItems, Double totalPrice) {
        String prompt = buildCartAnalysisPrompt(cartItems, totalPrice);
        return this.chat(prompt);
    }

    public Mono<String> getWellnessSuggestions(String userId, String userName) {
        String prompt = String.format(
            "Voce e um assistente de saude da ClickFarma. O usuario %s solicitou recomendacoes de bem-estar. Forneca 3 dicas praticas de saude em MARKDOWN.",
            userName != null ? userName : "Cliente"
        );
        return this.chat(prompt);
    }

    private String buildCartAnalysisPrompt(List<Map<String, Object>> cartItems, Double totalPrice) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analise o carrinho de compras e forneca sugestoes em MARKDOWN:\n\n");
        for (Map<String, Object> item : cartItems) {
            prompt.append("- ").append(item.get("name")).append("\n");
        }
        prompt.append("Total: R$ ").append(totalPrice);
        return prompt.toString();
    }

    public Mono<String> extractTextFromImage(String base64Image) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_groq_api_key_here")) {
            return Mono.just("Erro: Chave da API Groq não configurada");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.2-90b-vision-preview");

        // Format is data:image/jpeg;base64,... 
        String dataUrl = base64Image;
        if (!dataUrl.startsWith("data:image")) {
            dataUrl = "data:image/jpeg;base64," + base64Image;
        }

        List<Map<String, Object>> messages = new java.util.ArrayList<>();
        
        List<Map<String, Object>> contentParts = new java.util.ArrayList<>();
        contentParts.add(Map.of("type", "text", "text", "Por favor, atue como um farmacêutico e leia a imagem. Extraia SOMENTE os nomes dos princípios ativos (remédios) escritos nesta receita. Responda num formato limpo delimitando por vírgula."));
        contentParts.add(Map.of("type", "image_url", "image_url", Map.of("url", dataUrl)));

        messages.add(Map.of("role", "user", "content", contentParts));
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.2);

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
                        return "Erro ao processar visão: " + e.getMessage();
                    }
                })
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class, e -> {
                    System.err.println("Groq Vision API Error: " + e.getStatusCode());
                    System.err.println("Response Body: " + e.getResponseBodyAsString());
                    return Mono.just("Erro: 400 Bad Request from Groq Vision API");
                });
    }
}
