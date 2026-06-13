package com.clickfarma.backend.dto;

import com.clickfarma.backend.model.Usuario;
import java.time.LocalDateTime;

public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String endereco;
    private String telegramId;
    private LocalDateTime dataCadastro;
    private Integer quantidadePedidos;
    private String role;
    private String avatarUrl;
    private String googleId;

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.endereco = usuario.getEndereco();
        this.telegramId = usuario.getTelegramId();
        this.dataCadastro = usuario.getDataCadastro();
        this.quantidadePedidos = usuario.getPedidos() != null ?
                usuario.getPedidos().size() : 0;
        this.role = usuario.getRole();
        this.avatarUrl = usuario.getAvatarUrl();
        this.googleId = usuario.getGoogleId();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTelegramId() { return telegramId; }
    public void setTelegramId(String telegramId) { this.telegramId = telegramId; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public Integer getQuantidadePedidos() { return quantidadePedidos; }
    public void setQuantidadePedidos(Integer quantidadePedidos) {
        this.quantidadePedidos = quantidadePedidos;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
}
