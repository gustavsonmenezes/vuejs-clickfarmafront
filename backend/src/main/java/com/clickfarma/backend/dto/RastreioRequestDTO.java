package com.clickfarma.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class RastreioRequestDTO {

    @NotNull(message = "ID do pedido é obrigatório")
    private Long pedidoId;

    @NotBlank(message = "Transportadora é obrigatória")
    private String transportadora;

    private LocalDateTime dataPrevisaoEntrega;

    // Getters e Setters
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    public String getTransportadora() { return transportadora; }
    public void setTransportadora(String transportadora) { this.transportadora = transportadora; }

    public LocalDateTime getDataPrevisaoEntrega() { return dataPrevisaoEntrega; }
    public void setDataPrevisaoEntrega(LocalDateTime dataPrevisaoEntrega) { this.dataPrevisaoEntrega = dataPrevisaoEntrega; }
}