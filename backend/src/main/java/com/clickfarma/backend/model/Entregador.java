package com.clickfarma.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entregadores")
public class Entregador {

    public enum StatusEntregador {
        PENDENTE,
        ATIVO,
        BLOQUEADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 20)
    private String telefone;

    @Column(unique = true, length = 11)
    private String cpf;

    @Column(name = "senha")
    private String senha;

    @Column(name = "cnh", length = 20)
    private String cnh;

    @Column(name = "placa_veiculo", length = 10)
    private String placaVeiculo;

    @Column(name = "modelo_veiculo", length = 50)
    private String modeloVeiculo;

    @Column(name = "chave_pix", length = 100)
    private String chavePix;

    @Column(name = "telegram_chat_id", length = 100, unique = true)
    private String telegramChatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEntregador status = StatusEntregador.PENDENTE;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    public Entregador() {
        this.dataCadastro = LocalDateTime.now();
    }

    public Entregador(String nome, String telegramChatId) {
        this.nome = nome;
        this.telegramChatId = telegramChatId;
        this.status = StatusEntregador.ATIVO;
        this.ativo = true;
        this.dataCadastro = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (dataCadastro == null) dataCadastro = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getCnh() { return cnh; }
    public void setCnh(String cnh) { this.cnh = cnh; }

    public String getPlacaVeiculo() { return placaVeiculo; }
    public void setPlacaVeiculo(String placaVeiculo) { this.placaVeiculo = placaVeiculo; }

    public String getModeloVeiculo() { return modeloVeiculo; }
    public void setModeloVeiculo(String modeloVeiculo) { this.modeloVeiculo = modeloVeiculo; }

    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }

    public String getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(String telegramChatId) { this.telegramChatId = telegramChatId; }

    public StatusEntregador getStatus() { return status; }
    public void setStatus(StatusEntregador status) { this.status = status; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) { this.ultimaAtualizacao = ultimaAtualizacao; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
