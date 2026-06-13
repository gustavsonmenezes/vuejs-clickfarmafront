package com.clickfarma.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleLoginRequestDTO {

    @NotBlank(message = "Credencial Google é obrigatória")
    private String credential;

    public String getCredential() { return credential; }
    public void setCredential(String credential) { this.credential = credential; }
}
