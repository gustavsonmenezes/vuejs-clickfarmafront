package com.clickfarma.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
public class Pagamento {

    public enum TipoPagamento { FARMACIA, MOTOBOY }
    public enum StatusPagamento { PENDENTE, PAGO, CANCELADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPagamento tipo;

    @ManyToOne
    @JoinColumn(name = "farmacia_id")
    private Farmacia farmacia;

    @ManyToOne
    @JoinColumn(name = "motoboy_id")
    private Motoboy motoboy;

    @Column(name = "valor_bruto", precision = 12, scale = 2)
    private BigDecimal valorBruto;

    @Column(name = "valor_taxa", precision = 12, scale = 2)
    private BigDecimal valorTaxa;

    @Column(name = "valor_liquido", precision = 12, scale = 2)
    private BigDecimal valorLiquido;

    @Column(name = "chave_pix", length = 150)
    private String chavePix;

    @Column(name = "tipo_chave_pix", length = 20)
    private String tipoChavePix;

    @Column(name = "referencia_periodo", length = 20)
    private String referenciaPeriodo; // ex: "2025-04"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(length = 500)
    private String observacoes;

    public Pagamento() {
        this.dataCriacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoPagamento getTipo() { return tipo; }
    public void setTipo(TipoPagamento tipo) { this.tipo = tipo; }
    public Farmacia getFarmacia() { return farmacia; }
    public void setFarmacia(Farmacia farmacia) { this.farmacia = farmacia; }
    public Motoboy getMotoboy() { return motoboy; }
    public void setMotoboy(Motoboy motoboy) { this.motoboy = motoboy; }
    public BigDecimal getValorBruto() { return valorBruto; }
    public void setValorBruto(BigDecimal valorBruto) { this.valorBruto = valorBruto; }
    public BigDecimal getValorTaxa() { return valorTaxa; }
    public void setValorTaxa(BigDecimal valorTaxa) { this.valorTaxa = valorTaxa; }
    public BigDecimal getValorLiquido() { return valorLiquido; }
    public void setValorLiquido(BigDecimal valorLiquido) { this.valorLiquido = valorLiquido; }
    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }
    public String getTipoChavePix() { return tipoChavePix; }
    public void setTipoChavePix(String tipoChavePix) { this.tipoChavePix = tipoChavePix; }
    public String getReferenciaPeriodo() { return referenciaPeriodo; }
    public void setReferenciaPeriodo(String referenciaPeriodo) { this.referenciaPeriodo = referenciaPeriodo; }
    public StatusPagamento getStatus() { return status; }
    public void setStatus(StatusPagamento status) { this.status = status; }
    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDateTime dataPagamento) { this.dataPagamento = dataPagamento; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
