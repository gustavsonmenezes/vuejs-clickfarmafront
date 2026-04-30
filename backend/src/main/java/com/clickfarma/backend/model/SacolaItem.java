package com.clickfarma.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sacola_itens")
public class SacolaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "data_adicao")
    private LocalDateTime dataAdicao;

    public SacolaItem() {
        this.dataAdicao = LocalDateTime.now();
    }

    public SacolaItem(Usuario usuario, Produto produto, Integer quantidade) {
        this.usuario = usuario;
        this.produto = produto;
        this.quantidade = quantidade;
        this.dataAdicao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public LocalDateTime getDataAdicao() { return dataAdicao; }
    public void setDataAdicao(LocalDateTime dataAdicao) { this.dataAdicao = dataAdicao; }
}
