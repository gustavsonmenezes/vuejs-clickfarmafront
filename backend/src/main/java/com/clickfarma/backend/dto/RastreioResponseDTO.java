package com.clickfarma.backend.dto;

import com.clickfarma.backend.model.Rastreio;
import java.time.LocalDateTime;

public class RastreioResponseDTO {
    private Long id;
    private String codigoRastreio;
    private String transportadora;
    private LocalDateTime dataEnvio;
    private LocalDateTime dataPrevisaoEntrega;
    private LocalDateTime dataEntregaReal;
    private String status;
    private String ultimaLocalizacao;
    private LocalDateTime ultimaAtualizacao;

    public RastreioResponseDTO(Rastreio rastreio) {
        this.id = rastreio.getId();
        this.codigoRastreio = rastreio.getCodigoRastreio();
        this.transportadora = rastreio.getTransportadora();
        this.dataEnvio = rastreio.getDataEnvio();
        this.dataPrevisaoEntrega = rastreio.getDataPrevisaoEntrega();
        this.dataEntregaReal = rastreio.getDataEntregaReal();
        this.status = rastreio.getStatus();
        this.ultimaLocalizacao = rastreio.getUltimaLocalizacao();
        this.ultimaAtualizacao = rastreio.getUltimaAtualizacao();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoRastreio() { return codigoRastreio; }
    public void setCodigoRastreio(String codigoRastreio) { this.codigoRastreio = codigoRastreio; }

    public String getTransportadora() { return transportadora; }
    public void setTransportadora(String transportadora) { this.transportadora = transportadora; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public LocalDateTime getDataPrevisaoEntrega() { return dataPrevisaoEntrega; }
    public void setDataPrevisaoEntrega(LocalDateTime dataPrevisaoEntrega) { this.dataPrevisaoEntrega = dataPrevisaoEntrega; }

    public LocalDateTime getDataEntregaReal() { return dataEntregaReal; }
    public void setDataEntregaReal(LocalDateTime dataEntregaReal) { this.dataEntregaReal = dataEntregaReal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUltimaLocalizacao() { return ultimaLocalizacao; }
    public void setUltimaLocalizacao(String ultimaLocalizacao) { this.ultimaLocalizacao = ultimaLocalizacao; }

    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) { this.ultimaAtualizacao = ultimaAtualizacao; }
}
