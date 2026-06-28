package com.clickfarma.backend.dto;

import com.clickfarma.backend.model.Pedido;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PedidoResponseDTO {

    private Long id;
    private String codigoPedido;
    private Long usuarioId;
    private String usuarioNome;
    private List<ItemPedidoResponseDTO> itens;
    private String status;
    private String metodoPagamento;
    private String enderecoEntrega;
    private String observacoes;
    private LocalDateTime dataPedido;
    private Double subtotal;
    private Double valorFrete;
    private Double totalFinal;
    private Long farmaciaId;
    private String linkPagamento;
    private String whatsappLink;
    private Boolean whatsappEnviado;
    private String whatsappMensagem;

    private String pixQrCodeBase64;
    private String pixCopiaECola;
    private String pixExpiracao;

    // Construtor padrão
    public PedidoResponseDTO() {}

    // Construtor que aceita a entidade Pedido
    public PedidoResponseDTO(Pedido pedido) {
        this.id = pedido.getId();
        this.codigoPedido = pedido.getCodigoPedido();
        this.usuarioId = pedido.getUsuario().getId();
        this.usuarioNome = pedido.getUsuario().getNome();
        this.status = pedido.getStatus().name();
        this.metodoPagamento = pedido.getMetodoPagamento();
        this.enderecoEntrega = pedido.getEnderecoEntrega();
        this.observacoes = pedido.getObservacoes();
        this.dataPedido = pedido.getDataPedido();
        this.subtotal = pedido.getValorTotal() != null ? pedido.getValorTotal().doubleValue() : 0.0;
        this.farmaciaId = pedido.getFarmaciaId();
        this.valorFrete = pedido.getValorFrete() != null ? pedido.getValorFrete().doubleValue() : 0.0;
        this.totalFinal = this.subtotal + this.valorFrete;
        this.itens = pedido.getItens().stream()
                .map(ItemPedidoResponseDTO::new)
                .collect(Collectors.toList());
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Long getFarmaciaId() { return farmaciaId; }
    public void setFarmaciaId(Long farmaciaId) { this.farmaciaId = farmaciaId; }

    public Double getValorFrete() { return valorFrete; }
    public void setValorFrete(Double valorFrete) { this.valorFrete = valorFrete; }

    public Double getTotalFinal() { return totalFinal; }
    public void setTotalFinal(Double totalFinal) { this.totalFinal = totalFinal; }

    public String getLinkPagamento() { return linkPagamento; }
    public void setLinkPagamento(String linkPagamento) { this.linkPagamento = linkPagamento; }

    public String getWhatsappLink() { return whatsappLink; }
    public void setWhatsappLink(String whatsappLink) { this.whatsappLink = whatsappLink; }

    public Boolean getWhatsappEnviado() { return whatsappEnviado; }
    public void setWhatsappEnviado(Boolean whatsappEnviado) { this.whatsappEnviado = whatsappEnviado; }

    public String getWhatsappMensagem() { return whatsappMensagem; }
    public void setWhatsappMensagem(String whatsappMensagem) { this.whatsappMensagem = whatsappMensagem; }

    public String getPixQrCodeBase64() { return pixQrCodeBase64; }
    public void setPixQrCodeBase64(String pixQrCodeBase64) { this.pixQrCodeBase64 = pixQrCodeBase64; }

    public String getPixCopiaECola() { return pixCopiaECola; }
    public void setPixCopiaECola(String pixCopiaECola) { this.pixCopiaECola = pixCopiaECola; }

    public String getPixExpiracao() { return pixExpiracao; }
    public void setPixExpiracao(String pixExpiracao) { this.pixExpiracao = pixExpiracao; }
}
