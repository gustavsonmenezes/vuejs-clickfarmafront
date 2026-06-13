package com.clickfarma.backend;

import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BackendApplicationTests {

    @Test
    void deveCriarProdutoComValoresCorretos() {
        Produto produto = new Produto("Paracetamol", BigDecimal.valueOf(12.50), 100);

        assertEquals("Paracetamol", produto.getNome());
        assertEquals(0, BigDecimal.valueOf(12.50).compareTo(produto.getPreco()));
        assertEquals(100, produto.getEstoque());
    }

    @Test
    void deveCriarPedidoComStatusInicial() {
        Pedido pedido = new Pedido();
        assertEquals(Pedido.StatusPedido.AGUARDANDO_PAGAMENTO, pedido.getStatus());
    }
}
