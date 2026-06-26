package com.clickfarma.backend.dto.entregador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EntregadorRequestDTO {

    @NotBlank
    private String nome;

    @NotBlank
    @Size(min = 11, max = 11)
    private String cpf;

    @NotBlank
    @Size(min = 6)
    private String senha;

    private String telefone;
    private String cnh;
    private String placaVeiculo;
    private String modeloVeiculo;
    private String chavePix;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCnh() { return cnh; }
    public void setCnh(String cnh) { this.cnh = cnh; }

    public String getPlacaVeiculo() { return placaVeiculo; }
    public void setPlacaVeiculo(String placaVeiculo) { this.placaVeiculo = placaVeiculo; }

    public String getModeloVeiculo() { return modeloVeiculo; }
    public void setModeloVeiculo(String modeloVeiculo) { this.modeloVeiculo = modeloVeiculo; }

    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }
}
