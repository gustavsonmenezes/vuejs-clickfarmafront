package com.clickfarma.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "motoboys")
public class Motoboy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String cpf;

    @Column(length = 20)
    private String telefone;

    @Column(name = "chave_pix", length = 150)
    private String chavePix;

    @Column(name = "tipo_chave_pix", length = 20)
    private String tipoChavePix; // CPF, EMAIL, TELEFONE, ALEATORIA

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    public Motoboy() {
        this.dataCadastro = LocalDateTime.now();
    }

    public Motoboy(String nome, String cpf, Usuario usuario) {
        this.nome = nome;
        this.cpf = cpf;
        this.usuario = usuario;
        this.dataCadastro = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }
    public String getTipoChavePix() { return tipoChavePix; }
    public void setTipoChavePix(String tipoChavePix) { this.tipoChavePix = tipoChavePix; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
