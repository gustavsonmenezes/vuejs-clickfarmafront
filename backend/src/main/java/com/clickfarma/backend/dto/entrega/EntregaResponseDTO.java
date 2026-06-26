package com.clickfarma.backend.dto.entrega;

import com.clickfarma.backend.model.Entrega;
import com.clickfarma.backend.dto.entregador.EntregadorResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EntregaResponseDTO {
    private Long id;
    private Long pedidoId;
    private String codigoPedido;
    private String clienteNome;
    private String clienteTelefone;
    private String enderecoOrigem;
    private String enderecoDestino;
    private Double latitudeOrigem;
    private Double longitudeOrigem;
    private Double latitudeDestino;
    private Double longitudeDestino;
    private BigDecimal taxaEntrega;
    private BigDecimal distanciaKm;
    private String status;
    private EntregadorResponseDTO entregador;
    private LocalDateTime criadoEm;
    private LocalDateTime aceitoEm;
    private LocalDateTime retiradoEm;
    private LocalDateTime entregueEm;

    public static EntregaResponseDTO fromEntity(Entrega e) {
        EntregaResponseDTO dto = new EntregaResponseDTO();
        dto.setId(e.getId());
        dto.setPedidoId(e.getPedidoId());
        dto.setCodigoPedido(e.getCodigoPedido());
        dto.setClienteNome(e.getClienteNome());
        dto.setClienteTelefone(e.getClienteTelefone());
        dto.setEnderecoOrigem(e.getEnderecoOrigem());
        dto.setEnderecoDestino(e.getEnderecoDestino());
        dto.setLatitudeOrigem(e.getLatitudeOrigem());
        dto.setLongitudeOrigem(e.getLongitudeOrigem());
        dto.setLatitudeDestino(e.getLatitudeDestino());
        dto.setLongitudeDestino(e.getLongitudeDestino());
        dto.setTaxaEntrega(e.getTaxaEntrega());
        dto.setDistanciaKm(e.getDistanciaKm());
        dto.setStatus(e.getStatus().name());
        if (e.getEntregador() != null) {
            dto.setEntregador(EntregadorResponseDTO.fromEntity(e.getEntregador()));
        }
        dto.setCriadoEm(e.getCriadoEm());
        dto.setAceitoEm(e.getAceitoEm());
        dto.setRetiradoEm(e.getRetiradoEm());
        dto.setEntregueEm(e.getEntregueEm());
        return dto;
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public EntregadorResponseDTO getEntregador() { return entregador; }
    public void setEntregador(EntregadorResponseDTO entregador) { this.entregador = entregador; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAceitoEm() { return aceitoEm; }
    public void setAceitoEm(LocalDateTime aceitoEm) { this.aceitoEm = aceitoEm; }
    public LocalDateTime getRetiradoEm() { return retiradoEm; }
    public void setRetiradoEm(LocalDateTime retiradoEm) { this.retiradoEm = retiradoEm; }
    public LocalDateTime getEntregueEm() { return entregueEm; }
    public void setEntregueEm(LocalDateTime entregueEm) { this.entregueEm = entregueEm; }
}
