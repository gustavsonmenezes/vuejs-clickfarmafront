package com.clickfarma.backend.dto.entregador;

import com.clickfarma.backend.model.Entregador;
import com.clickfarma.backend.model.Entregador.StatusEntregador;
import java.time.LocalDateTime;

public class EntregadorResponseDTO {
    private Long id;
    private String nome;
    private String telefone;
    private String cpf;
    private String cnh;
    private String placaVeiculo;
    private String modeloVeiculo;
    private String chavePix;
    private StatusEntregador status;
    private Double latitude;
    private Double longitude;
    private LocalDateTime ultimaAtualizacao;
    private LocalDateTime dataCadastro;

    public static EntregadorResponseDTO fromEntity(Entregador e) {
        EntregadorResponseDTO dto = new EntregadorResponseDTO();
        dto.setId(e.getId());
        dto.setNome(e.getNome());
        dto.setTelefone(e.getTelefone());
        dto.setCpf(e.getCpf());
        dto.setCnh(e.getCnh());
        dto.setPlacaVeiculo(e.getPlacaVeiculo());
        dto.setModeloVeiculo(e.getModeloVeiculo());
        dto.setChavePix(e.getChavePix());
        dto.setStatus(e.getStatus());
        dto.setLatitude(e.getLatitude());
        dto.setLongitude(e.getLongitude());
        dto.setUltimaAtualizacao(e.getUltimaAtualizacao());
        dto.setDataCadastro(e.getDataCadastro());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getCnh() { return cnh; }
    public void setCnh(String cnh) { this.cnh = cnh; }
    public String getPlacaVeiculo() { return placaVeiculo; }
    public void setPlacaVeiculo(String placaVeiculo) { this.placaVeiculo = placaVeiculo; }
    public String getModeloVeiculo() { return modeloVeiculo; }
    public void setModeloVeiculo(String modeloVeiculo) { this.modeloVeiculo = modeloVeiculo; }
    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }
    public StatusEntregador getStatus() { return status; }
    public void setStatus(StatusEntregador status) { this.status = status; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) { this.ultimaAtualizacao = ultimaAtualizacao; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
