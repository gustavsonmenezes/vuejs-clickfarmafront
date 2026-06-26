package com.clickfarma.backend.dto.entregador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EntregadorLoginRequestDTO {
    @NotBlank
    @Size(min = 11, max = 11)
    private String cpf;

    @NotBlank
    private String senha;

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
