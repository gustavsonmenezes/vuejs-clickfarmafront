package com.clickfarma.backend.dto;

import jakarta.validation.constraints.NotBlank;
public class MotoboyRequestDTO {
    @NotBlank
    private String nome;
    @NotBlank
    private String cpf;
    private String telefone;
    @NotBlank
    private String email;
    @NotBlank
    private String senha;
    private String chavePix;
    private String tipoChavePix;

    // Getters and Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }

    public String getTipoChavePix() { return tipoChavePix; }
    public void setTipoChavePix(String tipoChavePix) { this.tipoChavePix = tipoChavePix; }
}
