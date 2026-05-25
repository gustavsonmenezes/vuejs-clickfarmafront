package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.ProdutoResponseDTO;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ProdutoRepository produtoRepository;

    public static class WeatherResponse {
        public Main main;
        public List<WeatherItem> weather;
        public String name;

        public static class Main {
            public double temp;
        }

        public static class WeatherItem {
            public String main;
            public String description;
        }
    }

    @Cacheable(value = "weather", key = "#lat + ',' + #lon", unless = "#result == null")
    public Map<String, Object> getWeatherAndRecommendations(double lat, double lon) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (apiKey == null || apiKey.isBlank() || apiKey.equals("demo_key_placeholder")) {
                return getFallbackRecommendations();
            }

            String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=metric&lang=pt_br",
                    apiUrl, lat, lon, apiKey);
            WeatherResponse res = restTemplate.getForObject(url, WeatherResponse.class);

            if (res == null || res.main == null || res.weather == null || res.weather.isEmpty()) {
                throw new RuntimeException("Invalid weather response");
            }

            double temp = res.main.temp;
            String condition = res.weather.get(0).main;
            String description = res.weather.get(0).description;
            String city = res.name;

            result.put("temp", temp);
            result.put("condition", condition);
            result.put("conditionDescription", description);
            result.put("city", city);
            result.put("recommendedProducts", getRecommendedProducts(temp, condition));
        } catch (HttpClientErrorException.Unauthorized e) {
            return getFallbackRecommendations();
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RuntimeException("Limite de requisicoes de clima atingido. Tente novamente mais tarde.");
        } catch (Exception e) {
            return getFallbackRecommendations();
        }
        return result;
    }

    private Map<String, Object> getFallbackRecommendations() {
        Map<String, Object> result = new HashMap<>();
        double temp = 25.0;
        String condition = "Clear";
        result.put("temp", temp);
        result.put("condition", condition);
        result.put("conditionDescription", "clima amavel (modo demo)");
        result.put("city", "Sua regiao");
        result.put("recommendedProducts", getRecommendedProducts(temp, condition));
        return result;
    }

    private List<ProdutoResponseDTO> getRecommendedProducts(double temp, String condition) {
        List<Produto> allProducts = produtoRepository.findAll();

        List<String> keywords = new ArrayList<>();
        List<String> categoryKeywords = new ArrayList<>();

        if (temp < 15) {
            keywords.addAll(Arrays.asList("gripe", "resfriado", "xarope", "dipirona", "tosse", "febre", "paracetamol", "novagina", "tylenol", "advil", "ibuprofeno"));
            categoryKeywords.add("Medicamentos");
        } else if (temp <= 25) {
            keywords.addAll(Arrays.asList("vitamina", "higiene", "shampoo", "sabonete", "creme dental"));
            categoryKeywords.add("Vitaminas");
            categoryKeywords.add("Higiene");
        } else {
            keywords.addAll(Arrays.asList("protetor solar", "hidratante", "fps", "solar", "neutrogena", "la roche", "sundown"));
            categoryKeywords.add("Cosmeticos");
            categoryKeywords.add("Higiene");
        }

        if ("Rain".equalsIgnoreCase(condition) || "Drizzle".equalsIgnoreCase(condition)) {
            keywords.add("vitamina c");
            keywords.add("imunidade");
            categoryKeywords.add("Medicamentos");
        } else if ("Clear".equalsIgnoreCase(condition)) {
            keywords.add("protetor solar");
            keywords.add("fps");
        } else if ("Clouds".equalsIgnoreCase(condition)) {
            keywords.add("vitamina");
            keywords.add("higiene");
        }

        Set<Produto> matchedProducts = new LinkedHashSet<>();

        for (Produto p : allProducts) {
            if (p.getEstoque() == null || p.getEstoque() <= 0) continue;

            String nome = p.getNome().toLowerCase();
            String desc = p.getDescricao() != null ? p.getDescricao().toLowerCase() : "";
            String catNome = p.getCategoria() != null ? p.getCategoria().getNome() : "";

            boolean matchesKeyword = keywords.stream()
                    .anyMatch(k -> nome.contains(k) || desc.contains(k));
            boolean matchesCategory = categoryKeywords.stream()
                    .anyMatch(ck -> catNome.contains(ck));

            if (matchesKeyword || matchesCategory) {
                matchedProducts.add(p);
            }
        }

        return matchedProducts.stream()
                .limit(6)
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }
}
