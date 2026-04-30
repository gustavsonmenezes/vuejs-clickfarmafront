package com.clickfarma.backend.dto;

import com.clickfarma.backend.model.Motoboy;
import java.time.LocalDateTime;

public class MotoboyResponseDTO {
    private Long id;
    private String nome;
    private String cpf;
    private String telefone;
    private LocalDateTime dataCadastro;

    public MotoboyResponseDTO(Motoboy motoboy) {
        this.id = motoboy.getId();
        this.nome = motoboy.getNome();
        this.cpf = motoboy.getCpf();
        this.telefone = motoboy.getTelefone();
        this.dataCadastro = motoboy.getDataCadastro();
    }

    // Getters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
}
