package com.clickfarma.backend.dto;

import java.math.BigDecimal;

public class DeliveryOptionDTO {

    private String tipo;
    private String nome;
    private BigDecimal valor;
    private String prazoEstimado;

    public DeliveryOptionDTO() {
    }

    public DeliveryOptionDTO(String tipo, String nome, BigDecimal valor, String prazoEstimado) {
        this.tipo = tipo;
        this.nome = nome;
        this.valor = valor;
        this.prazoEstimado = prazoEstimado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getPrazoEstimado() {
        return prazoEstimado;
    }

    public void setPrazoEstimado(String prazoEstimado) {
        this.prazoEstimado = prazoEstimado;
    }
}
