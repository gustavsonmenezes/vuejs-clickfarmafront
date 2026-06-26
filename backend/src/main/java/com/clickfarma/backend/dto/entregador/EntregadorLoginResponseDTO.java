package com.clickfarma.backend.dto.entregador;

public class EntregadorLoginResponseDTO {
    private String token;
    private EntregadorResponseDTO entregador;

    public EntregadorLoginResponseDTO() {}

    public EntregadorLoginResponseDTO(String token, EntregadorResponseDTO entregador) {
        this.token = token;
        this.entregador = entregador;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public EntregadorResponseDTO getEntregador() { return entregador; }
    public void setEntregador(EntregadorResponseDTO entregador) { this.entregador = entregador; }
}
