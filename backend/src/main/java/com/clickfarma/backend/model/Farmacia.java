package com.clickfarma.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmacias")
public class Farmacia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String cnpj;

    @Column(length = 200)
    private String endereco;

    @Column(length = 100)
    private String cidade;

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String email;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "chave_pix", length = 150)
    private String chavePix;

    @Column(name = "tipo_chave_pix", length = 20)
    private String tipoChavePix; // CPF, CNPJ, EMAIL, TELEFONE, ALEATORIA

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    public Farmacia() {
        this.dataCadastro = LocalDateTime.now();
    }

    public Farmacia(String nome, String cnpj, Usuario usuario) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.usuario = usuario;
        this.dataCadastro = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }
    public String getTipoChavePix() { return tipoChavePix; }
    public void setTipoChavePix(String tipoChavePix) { this.tipoChavePix = tipoChavePix; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
