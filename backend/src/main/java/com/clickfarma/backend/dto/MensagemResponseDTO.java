package com.clickfarma.backend.dto;

public class MensagemResponseDTO {
    private String mensagem;
    private boolean sucesso;
    private Object dados;

    public MensagemResponseDTO(String mensagem, boolean sucesso) {
        this.mensagem = mensagem;
        this.sucesso = sucesso;
    }

    public MensagemResponseDTO(String mensagem, boolean sucesso, Object dados) {
        this.mensagem = mensagem;
        this.sucesso = sucesso;
        this.dados = dados;
    }

    // Getters e Setters
    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public Object getDados() {
        return dados;
    }

    public void setDados(Object dados) {
        this.dados = dados;
    }
}