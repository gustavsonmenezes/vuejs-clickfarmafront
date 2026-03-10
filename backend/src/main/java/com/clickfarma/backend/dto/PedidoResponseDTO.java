package com.clickfarma.backend.dto;

import com.clickfarma.backend.model.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PedidoResponseDTO {
    private Long id;
    private String codigoPedido;
    private Long usuarioId;
    private String usuarioNome;
    private List<ItemPedidoResponseDTO> itens;
    private LocalDateTime dataPedido;
    private String status;
    private BigDecimal valorTotal;
    private String metodoPagamento;
    private String enderecoEntrega;
    private String observacoes;
    private RastreioResponseDTO rastreio;

    public PedidoResponseDTO(Pedido pedido) {
        this.id = pedido.getId();
        this.codigoPedido = pedido.getCodigoPedido();
        this.dataPedido = pedido.getDataPedido();
        this.status = pedido.getStatus() != null ? pedido.getStatus().name() : null;
        this.valorTotal = pedido.getValorTotal();
        this.metodoPagamento = pedido.getMetodoPagamento();
        this.enderecoEntrega = pedido.getEnderecoEntrega();
        this.observacoes = pedido.getObservacoes();

        if (pedido.getUsuario() != null) {
            this.usuarioId = pedido.getUsuario().getId();
            this.usuarioNome = pedido.getUsuario().getNome();
        }

        if (pedido.getItens() != null) {
            this.itens = pedido.getItens().stream()
                    .map(ItemPedidoResponseDTO::new)
                    .collect(Collectors.toList());
        }

        if (pedido.getRastreio() != null) {
            this.rastreio = new RastreioResponseDTO(pedido.getRastreio());
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoPedido() { return codigoPedido; }
    public void setCodigoPedido(String codigoPedido) { this.codigoPedido = codigoPedido; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public List<ItemPedidoResponseDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoResponseDTO> itens) { this.itens = itens; }

    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public RastreioResponseDTO getRastreio() { return rastreio; }
    public void setRastreio(RastreioResponseDTO rastreio) { this.rastreio = rastreio; }
}