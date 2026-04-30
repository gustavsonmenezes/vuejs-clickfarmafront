package com.clickfarma.backend.dto;

public class LoginResponseDTO {
    private String token;
    private String tipo;
    private Long id;
    private String nome;
    private String email;
    private String role;

    public LoginResponseDTO(String token, Long id, String nome, String email, String role) {
        this.token = token;
        this.tipo = "Bearer";
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
    }

    // Getters e Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}