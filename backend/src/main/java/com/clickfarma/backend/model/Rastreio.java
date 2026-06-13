package com.clickfarma.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rastreios")
public class Rastreio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pedido_id", unique = true)
    private Pedido pedido;

    @Column(name = "codigo_rastreio", unique = true, length = 50)
    private String codigoRastreio;

    @Column(length = 100)
    private String transportadora;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @Column(name = "data_previsao_entrega")
    private LocalDateTime dataPrevisaoEntrega;

    @Column(name = "data_entrega_real")
    private LocalDateTime dataEntregaReal;

    @Column(length = 50)
    private String status;

    @Column(length = 200)
    private String ultimaLocalizacao;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;

    @Column(length = 500)
    private String historico; // Armazenar histórico em JSON

    @Column(nullable = true)
    private Double latitude;

    @Column(nullable = true)
    private Double longitude;

    // Construtores
    public Rastreio() {}

    public Rastreio(Pedido pedido) {
        this.pedido = pedido;
        this.codigoRastreio = gerarCodigoRastreio();
        this.status = "AGUARDANDO_ENVIO";
        this.ultimaAtualizacao = LocalDateTime.now();
        this.historico = "[]";
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

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

    public String getHistorico() { return historico; }
    public void setHistorico(String historico) { this.historico = historico; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    // Métodos auxiliares
    private String gerarCodigoRastreio() {
        return "CLF" + System.currentTimeMillis() +
                String.format("%04d", (int)(Math.random() * 10000));
    }

    public void adicionarEvento(String localizacao, String descricao) {
        this.ultimaLocalizacao = localizacao;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void atualizarLocalizacao(Double latitude, Double longitude, String endereco) {
        this.latitude = latitude;
        this.longitude = longitude;
        if (endereco != null) this.ultimaLocalizacao = endereco;
        this.ultimaAtualizacao = LocalDateTime.now();
    }
}