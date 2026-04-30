package com.clickfarma.backend.dto;

import com.clickfarma.backend.model.Produto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProdutoResponseDTO {
    private Long id;
    private String nome;
    private String descricaoBreve;
    private String descricao;
    private BigDecimal preco;
    private Integer estoque;
    private String imageUrl;
    private String principioAtivo;
    private String dosagem;
    private String laboratorio;
    private Boolean necessitaReceita;
    private Long categoriaId;
    private String categoriaNome;
    private Long farmaciaId;
    private String farmaciaNome;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    public ProdutoResponseDTO(Produto produto) {
        this.id = produto.getId();
        this.nome = produto.getNome();
        this.descricaoBreve = produto.getDescricaoBreve();
        this.descricao = produto.getDescricao();
        this.preco = produto.getPreco();
        this.estoque = produto.getEstoque();
        this.imageUrl = produto.getImageUrl();
        this.principioAtivo = produto.getPrincipioAtivo();
        this.dosagem = produto.getDosagem();
        this.laboratorio = produto.getLaboratorio();
        this.necessitaReceita = produto.getNecessitaReceita();
        this.dataCriacao = produto.getDataCriacao();
        this.dataAtualizacao = produto.getDataAtualizacao();
        if (produto.getCategoria() != null) {
            this.categoriaId = produto.getCategoria().getId();
            this.categoriaNome = produto.getCategoria().getNome();
        }
        if (produto.getFarmacia() != null) {
            this.farmaciaId = produto.getFarmacia().getId();
            this.farmaciaNome = produto.getFarmacia().getNome();
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricaoBreve() { return descricaoBreve; }
    public void setDescricaoBreve(String descricaoBreve) { this.descricaoBreve = descricaoBreve; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public Integer getEstoque() { return estoque; }
    public void setEstoque(Integer estoque) { this.estoque = estoque; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPrincipioAtivo() { return principioAtivo; }
    public void setPrincipioAtivo(String principioAtivo) { this.principioAtivo = principioAtivo; }
    public String getDosagem() { return dosagem; }
    public void setDosagem(String dosagem) { this.dosagem = dosagem; }
    public String getLaboratorio() { return laboratorio; }
    public void setLaboratorio(String laboratorio) { this.laboratorio = laboratorio; }
    public Boolean getNecessitaReceita() { return necessitaReceita; }
    public void setNecessitaReceita(Boolean necessitaReceita) { this.necessitaReceita = necessitaReceita; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public String getCategoriaNome() { return categoriaNome; }
    public void setCategoriaNome(String categoriaNome) { this.categoriaNome = categoriaNome; }
    public Long getFarmaciaId() { return farmaciaId; }
    public void setFarmaciaId(Long farmaciaId) { this.farmaciaId = farmaciaId; }
    public String getFarmaciaNome() { return farmaciaNome; }
    public void setFarmaciaNome(String farmaciaNome) { this.farmaciaNome = farmaciaNome; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
