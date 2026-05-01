package com.clickfarma.backend.dto;

import java.time.LocalDateTime;

public class TelegramLinkResponseDTO {
    private String botUsername;
    private String deepLink;
    private String token;
    private LocalDateTime expiresAt;
    private boolean vinculado;

    public TelegramLinkResponseDTO(String botUsername, String deepLink, String token, LocalDateTime expiresAt, boolean vinculado) {
        this.botUsername = botUsername;
        this.deepLink = deepLink;
        this.token = token;
        this.expiresAt = expiresAt;
        this.vinculado = vinculado;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isVinculado() {
        return vinculado;
    }

    public void setVinculado(boolean vinculado) {
        this.vinculado = vinculado;
    }
}
