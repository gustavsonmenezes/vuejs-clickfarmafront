package com.clickfarma.backend.dto;

import com.clickfarma.backend.model.Farmacia;
import java.time.LocalDateTime;

public class FarmaciaResponseDTO {
    private Long id;
    private String nome;
    private String cnpj;
    private String endereco;
    private String cidade;
    private String telefone;
    private String email;
    private String fotoUrl;
    private LocalDateTime dataCadastro;

    public FarmaciaResponseDTO(Farmacia farmacia) {
        this.id = farmacia.getId();
        this.nome = farmacia.getNome();
        this.cnpj = farmacia.getCnpj();
        this.endereco = farmacia.getEndereco();
        this.cidade = farmacia.getCidade();
        this.telefone = farmacia.getTelefone();
        this.email = farmacia.getEmail();
        this.fotoUrl = farmacia.getFotoUrl();
        this.dataCadastro = farmacia.getDataCadastro();
    }

    // Getters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCnpj() { return cnpj; }
    public String getEndereco() { return endereco; }
    public String getCidade() { return cidade; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public String getFotoUrl() { return fotoUrl; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
}
