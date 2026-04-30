package com.clickfarma.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_visualizacoes")
public class HistoricoVisualizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "data_visualizacao")
    private LocalDateTime dataVisualizacao;

    public HistoricoVisualizacao() {
        this.dataVisualizacao = LocalDateTime.now();
    }

    public HistoricoVisualizacao(Usuario usuario, Produto produto) {
        this.usuario = usuario;
        this.produto = produto;
        this.dataVisualizacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public LocalDateTime getDataVisualizacao() { return dataVisualizacao; }
    public void setDataVisualizacao(LocalDateTime dataVisualizacao) { this.dataVisualizacao = dataVisualizacao; }
}
