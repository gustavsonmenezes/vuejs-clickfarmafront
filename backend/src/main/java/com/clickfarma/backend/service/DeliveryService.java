package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.DeliveryOptionDTO;
import com.clickfarma.backend.dto.DeliveryRequestDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeliveryService {

    public List<DeliveryOptionDTO> calculateDeliveryOptions(DeliveryRequestDTO request) {
        List<DeliveryOptionDTO> options = new ArrayList<>();
        String cep = request.getCep().replaceAll("\\D", "");

        // Validação básica de CEP
        if (cep.length() != 8) {
            throw new IllegalArgumentException("CEP inválido. Deve conter 8 dígitos.");
        }

        BigDecimal valorCarrinho = request.getValorTotalCarrinho() != null ? request.getValorTotalCarrinho() : BigDecimal.ZERO;

        // Regra de Frete Grátis
        boolean temFreteGratis = valorCarrinho.compareTo(new BigDecimal("100.00")) >= 0;

        // Simulando a lógica de uma transportadora baseada no CEP
        if (cep.startsWith("0") || cep.startsWith("1") || cep.startsWith("2")) {
            // Região Sudeste (São Paulo, Rio de Janeiro, etc)
            options.add(new DeliveryOptionDTO(
                    "MOTOBOY", 
                    "Entrega Expressa (Até 2h)", 
                    temFreteGratis ? BigDecimal.ZERO : new BigDecimal("9.90"), 
                    "Em até 2 horas"
            ));
            
            options.add(new DeliveryOptionDTO(
                    "NORMAL", 
                    "Correios - PAC", 
                    temFreteGratis ? BigDecimal.ZERO : new BigDecimal("15.50"), 
                    "3 a 5 dias úteis"
            ));
            
        } else if (cep.startsWith("4") || cep.startsWith("5") || cep.startsWith("7")) {
            // Nordeste / Sul
            options.add(new DeliveryOptionDTO(
                    "NORMAL", 
                    "Correios - PAC", 
                    temFreteGratis ? BigDecimal.ZERO : new BigDecimal("25.90"), 
                    "5 a 8 dias úteis"
            ));
            
            options.add(new DeliveryOptionDTO(
                    "EXPRESSA", 
                    "Sedex", 
                    new BigDecimal("45.00"), // Sedex não entra no frete grátis nessa simulação
                    "2 a 3 dias úteis"
            ));
            
        } else {
            // Resto do Brasil
            options.add(new DeliveryOptionDTO(
                    "NORMAL", 
                    "Transportadora Padrão", 
                    temFreteGratis ? BigDecimal.ZERO : new BigDecimal("35.00"), 
                    "7 a 10 dias úteis"
            ));
        }

        // Opção universal de retirada na loja sempre grátis
        options.add(new DeliveryOptionDTO(
                "RETIRADA", 
                "Retirar na Loja ClickFarma", 
                BigDecimal.ZERO, 
                "Imediato"
        ));

        return options;
    }
}
