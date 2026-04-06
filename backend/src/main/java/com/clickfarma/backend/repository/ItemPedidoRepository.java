package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Produto;
import java.util.Optional;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    List<ItemPedido> findByPedidoId(Long pedidoId);

    List<ItemPedido> findByProdutoId(Long produtoId);

    Optional<ItemPedido> findByPedidoAndProdutoId(Pedido pedido, Long produtoId);
}