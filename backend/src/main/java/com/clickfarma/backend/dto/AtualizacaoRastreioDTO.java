package com.clickfarma.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AtualizacaoRastreioDTO {

    @NotBlank(message = "Status é obrigatório")
    private String status;

    @NotBlank(message = "Localização é obrigatória")
    private String localizacao;

    private String observacao;

    // Getters e Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}

