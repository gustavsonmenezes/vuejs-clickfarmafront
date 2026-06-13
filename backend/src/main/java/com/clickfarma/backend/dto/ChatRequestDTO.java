package com.clickfarma.backend.dto;

public class ChatRequestDTO {
    private String message;
    private String weatherContext;
    private Long usuarioId;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getWeatherContext() { return weatherContext; }
    public void setWeatherContext(String weatherContext) { this.weatherContext = weatherContext; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
