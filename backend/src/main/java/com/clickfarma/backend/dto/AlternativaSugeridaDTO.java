package com.clickfarma.backend.dto;

/**
 * Produto do catálogo sugerido como similar ou mesma classe terapêutica.
 */
public class AlternativaSugeridaDTO {

    private Long produtoId;
    private String nome;
    private Double preco;
    private Integer estoque;
    private String motivoIndicacao;

    public AlternativaSugeridaDTO() {
    }

    public AlternativaSugeridaDTO(Long produtoId, String nome, Double preco, Integer estoque, String motivoIndicacao) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.motivoIndicacao = motivoIndicacao;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public String getMotivoIndicacao() {
        return motivoIndicacao;
    }

    public void setMotivoIndicacao(String motivoIndicacao) {
        this.motivoIndicacao = motivoIndicacao;
    }
}
