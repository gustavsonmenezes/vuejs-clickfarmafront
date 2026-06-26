package com.clickfarma.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ViaCepService {

    private static final Logger log = LoggerFactory.getLogger(ViaCepService.class);
    private static final String VIACEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    @Autowired
    private RestTemplate restTemplate;

    @Cacheable(value = "cep", unless = "#result == null || #result.isEmpty()")
    public Map<String, String> buscarEndereco(String cep) {
        String numeros = cep.replaceAll("\\D", "");
        if (numeros.length() != 8) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "CEP deve conter 8 digitos");
            return erro;
        }

        try {
            log.info("Buscando CEP: {}", numeros);

            ViaCepResponse response = restTemplate.getForObject(
                    VIACEP_URL, ViaCepResponse.class, numeros);

            if (response == null || response.erro != null) {
                Map<String, String> erro = new HashMap<>();
                erro.put("erro", "CEP nao encontrado");
                return erro;
            }

            Map<String, String> result = new HashMap<>();
            result.put("cep", response.cep);
            result.put("logradouro", response.logradouro != null ? response.logradouro : "");
            result.put("bairro", response.bairro != null ? response.bairro : "");
            result.put("cidade", response.localidade != null ? response.localidade : "");
            result.put("estado", response.uf != null ? response.uf : "");
            return result;

        } catch (Exception e) {
            log.error("Erro ao buscar CEP {}: {}", numeros, e.getMessage());
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Erro ao consultar CEP: " + e.getMessage());
            return erro;
        }
    }

    private static class ViaCepResponse {
        public String cep;
        public String logradouro;
        public String bairro;
        public String localidade;
        public String uf;
        public String erro;
    }
}
