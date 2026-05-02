package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Pedido.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(Long usuarioId);

    List<Pedido> findByStatus(StatusPedido status);

    List<Pedido> findByDataPedidoBetween(LocalDateTime inicio, LocalDateTime fim);

    List<Pedido> findTop10ByOrderByDataPedidoDesc();

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.itens WHERE p.usuario.id = :usuarioId")
    List<Pedido> findPedidosComItensByUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.rastreio WHERE p.codigoPedido = :codigo")
    Pedido findByCodigoPedidoWithRastreio(@Param("codigo") String codigo);

    @Query("SELECT ip.produto.id, SUM(ip.quantidade), SUM(ip.subtotal) FROM ItemPedido ip GROUP BY ip.produto.id ORDER BY SUM(ip.quantidade) DESC")
    List<Object[]> findTopProdutos();
}
