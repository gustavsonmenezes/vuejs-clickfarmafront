package com.clickfarma.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos_recompra")
public class AgendamentoRecompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"senha", "pedidos", "email", "telefone", "endereco", "dataCadastro"})
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    @JsonIgnoreProperties({"categoria", "dataCriacao", "dataAtualizacao"})
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonIgnoreProperties({"usuario", "itens", "rastreio", "observacoes", "enderecoEntrega", "metodoPagamento", "dataAtualizacao"})
    private Pedido pedido;

    private String posologiaTexto;
    private Integer diasDuracao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataProximaNotificacao;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public String getPosologiaTexto() {
        return posologiaTexto;
    }

    public void setPosologiaTexto(String posologiaTexto) {
        this.posologiaTexto = posologiaTexto;
    }

    public Integer getDiasDuracao() {
        return diasDuracao;
    }

    public void setDiasDuracao(Integer diasDuracao) {
        this.diasDuracao = diasDuracao;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataProximaNotificacao() {
        return dataProximaNotificacao;
    }

    public void setDataProximaNotificacao(LocalDateTime dataProximaNotificacao) {
        this.dataProximaNotificacao = dataProximaNotificacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
