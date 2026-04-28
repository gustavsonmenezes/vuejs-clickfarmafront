package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.AgendamentoRecompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRecompraRepository extends JpaRepository<AgendamentoRecompra, Long> {

    List<AgendamentoRecompra> findByUsuarioId(Long usuarioId);

    List<AgendamentoRecompra> findByDataProximaNotificacaoBeforeAndStatus(LocalDateTime data, String status);
}
