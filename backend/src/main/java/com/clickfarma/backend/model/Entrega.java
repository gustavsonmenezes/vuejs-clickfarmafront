package com.clickfarma.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "entregas")
public class Entrega {

    public enum StatusEntrega {
        PENDENTE,
        ACEITA,
        RETIRADA,
        EM_ROTA,
        ENTREGUE,
        RECUSADA,
        CANCELADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "codigo_pedido", length = 50)
    private String codigoPedido;

    @Column(name = "cliente_nome", length = 100)
    private String clienteNome;

    @Column(name = "cliente_telefone", length = 20)
    private String clienteTelefone;

    @Column(name = "endereco_origem", columnDefinition = "TEXT")
    private String enderecoOrigem;

    @Column(name = "endereco_destino", columnDefinition = "TEXT")
    private String enderecoDestino;

    @Column(name = "latitude_origem")
    private Double latitudeOrigem;

    @Column(name = "longitude_origem")
    private Double longitudeOrigem;

    @Column(name = "latitude_destino")
    private Double latitudeDestino;

    @Column(name = "longitude_destino")
    private Double longitudeDestino;

    @Column(name = "taxa_entrega", precision = 10, scale = 2)
    private BigDecimal taxaEntrega;

    @Column(name = "distancia_km", precision = 10, scale = 2)
    private BigDecimal distanciaKm;

    @ManyToOne
    @JoinColumn(name = "entregador_id")
    private Entregador entregador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEntrega status;

    @Column(name = "mensagem_telegram", columnDefinition = "TEXT")
    private String mensagemTelegram;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "aceito_em")
    private LocalDateTime aceitoEm;

    @Column(name = "retirado_em")
    private LocalDateTime retiradoEm;

    @Column(name = "entregue_em")
    private LocalDateTime entregueEm;

    public Entrega() {}

    public Entrega(Long pedidoId, String codigoPedido) {
        this.pedidoId = pedidoId;
        this.codigoPedido = codigoPedido;
        this.status = StatusEntrega.PENDENTE;
        this.criadoEm = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    public String getCodigoPedido() { return codigoPedido; }
    public void setCodigoPedido(String codigoPedido) { this.codigoPedido = codigoPedido; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public String getClienteTelefone() { return clienteTelefone; }
    public void setClienteTelefone(String clienteTelefone) { this.clienteTelefone = clienteTelefone; }

    public String getEnderecoOrigem() { return enderecoOrigem; }
    public void setEnderecoOrigem(String enderecoOrigem) { this.enderecoOrigem = enderecoOrigem; }

    public String getEnderecoDestino() { return enderecoDestino; }
    public void setEnderecoDestino(String enderecoDestino) { this.enderecoDestino = enderecoDestino; }

    public Double getLatitudeOrigem() { return latitudeOrigem; }
    public void setLatitudeOrigem(Double latitudeOrigem) { this.latitudeOrigem = latitudeOrigem; }

    public Double getLongitudeOrigem() { return longitudeOrigem; }
    public void setLongitudeOrigem(Double longitudeOrigem) { this.longitudeOrigem = longitudeOrigem; }

    public Double getLatitudeDestino() { return latitudeDestino; }
    public void setLatitudeDestino(Double latitudeDestino) { this.latitudeDestino = latitudeDestino; }

    public Double getLongitudeDestino() { return longitudeDestino; }
    public void setLongitudeDestino(Double longitudeDestino) { this.longitudeDestino = longitudeDestino; }

    public BigDecimal getTaxaEntrega() { return taxaEntrega; }
    public void setTaxaEntrega(BigDecimal taxaEntrega) { this.taxaEntrega = taxaEntrega; }

    public BigDecimal getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(BigDecimal distanciaKm) { this.distanciaKm = distanciaKm; }

    public Entregador getEntregador() { return entregador; }
    public void setEntregador(Entregador entregador) { this.entregador = entregador; }

    public StatusEntrega getStatus() { return status; }
    public void setStatus(StatusEntrega status) { this.status = status; }

    public String getMensagemTelegram() { return mensagemTelegram; }
    public void setMensagemTelegram(String mensagemTelegram) { this.mensagemTelegram = mensagemTelegram; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public LocalDateTime getAceitoEm() { return aceitoEm; }
    public void setAceitoEm(LocalDateTime aceitoEm) { this.aceitoEm = aceitoEm; }

    public LocalDateTime getRetiradoEm() { return retiradoEm; }
    public void setRetiradoEm(LocalDateTime retiradoEm) { this.retiradoEm = retiradoEm; }

    public LocalDateTime getEntregueEm() { return entregueEm; }
    public void setEntregueEm(LocalDateTime entregueEm) { this.entregueEm = entregueEm; }
}
