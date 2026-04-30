package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.HistoricoVisualizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistoricoVisualizacaoRepository extends JpaRepository<HistoricoVisualizacao, Long> {

    List<HistoricoVisualizacao> findByUsuarioIdOrderByDataVisualizacaoDesc(Long usuarioId);

    @Query("SELECT h.produto.id, COUNT(h) AS total FROM HistoricoVisualizacao h " +
           "WHERE h.usuario.id = :usuarioId " +
           "GROUP BY h.produto.id ORDER BY total DESC")
    List<Object[]> findTopProdutosByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT DISTINCT h.produto.id FROM HistoricoVisualizacao h " +
           "WHERE h.usuario.id = :usuarioId ORDER BY h.dataVisualizacao DESC")
    List<Long> findProdutoIdsByUsuarioIdRecent(@Param("usuarioId") Long usuarioId);
}
