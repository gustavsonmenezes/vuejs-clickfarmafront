package com.clickfarma.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class PedidoRequestDTO {

    @NotNull(message = "ID do usuário é obrigatório")
    private Long usuarioId;

    @NotNull(message = "Itens do pedido são obrigatórios")
    @Size(min = 1, message = "Pedido deve ter pelo menos 1 item")
    private List<ItemPedidoRequestDTO> itens;

    private String metodoPagamento;

    private String enderecoEntrega;

    private String observacoes;

    // CAMPOS PARA FRETE E TOTAL
    private Double subtotal;
    private Double valorFrete;
    private Double totalFinal;

    // CAMPOS PARA UBER DIRECT
    private String tipoEntrega;
    private String uberQuoteId;

    // Getters e Setters
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public List<ItemPedidoRequestDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoRequestDTO> itens) { this.itens = itens; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getValorFrete() { return valorFrete; }
    public void setValorFrete(Double valorFrete) { this.valorFrete = valorFrete; }

    public Double getTotalFinal() { return totalFinal; }
    public void setTotalFinal(Double totalFinal) { this.totalFinal = totalFinal; }

    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }

    public String getUberQuoteId() { return uberQuoteId; }
    public void setUberQuoteId(String uberQuoteId) { this.uberQuoteId = uberQuoteId; }
}
