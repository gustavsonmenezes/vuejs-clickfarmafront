package com.clickfarma.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class DeliveryRequestDTO {

    @NotBlank(message = "O CEP é obrigatório para o cálculo do frete")
    private String cep;
    
    // Opcional: O valor total do carrinho pode influenciar no frete grátis
    private BigDecimal valorTotalCarrinho;

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public BigDecimal getValorTotalCarrinho() {
        return valorTotalCarrinho;
    }

    public void setValorTotalCarrinho(BigDecimal valorTotalCarrinho) {
        this.valorTotalCarrinho = valorTotalCarrinho;
    }
}
