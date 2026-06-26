package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Entrega;
import com.clickfarma.backend.model.Entrega.StatusEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByPedidoIdAndStatus(Long pedidoId, StatusEntrega status);
    Optional<Entrega> findFirstByPedidoIdOrderByCriadoEmDesc(Long pedidoId);
    List<Entrega> findByStatus(StatusEntrega status);
    List<Entrega> findByEntregadorIdOrderByCriadoEmDesc(Long entregadorId);
    List<Entrega> findByEntregadorIdAndStatusOrderByCriadoEmDesc(Long entregadorId, StatusEntrega status);
    List<Entrega> findByStatusOrderByCriadoEmDesc(StatusEntrega status);
    List<Entrega> findByEntregadorIdAndStatusInOrderByCriadoEmDesc(Long entregadorId, List<StatusEntrega> statuses);
}
