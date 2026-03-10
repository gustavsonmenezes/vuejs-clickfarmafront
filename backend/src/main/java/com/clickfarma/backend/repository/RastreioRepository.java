package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Rastreio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RastreioRepository extends JpaRepository<Rastreio, Long> {

    Optional<Rastreio> findByPedidoId(Long pedidoId);

    Optional<Rastreio> findByCodigoRastreio(String codigoRastreio);

    boolean existsByPedidoId(Long pedidoId);
}