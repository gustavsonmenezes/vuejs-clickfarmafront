package com.clickfarma.backend.service;

import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.PedidoRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Map<String, Object> obterResumoGeral() {
        Map<String, Object> resumo = new HashMap<>();

        // Totais
        long totalUsuarios = usuarioRepository.count();
        long totalProdutos = produtoRepository.count();
        long totalPedidos = pedidoRepository.count();

        resumo.put("totalUsuarios", totalUsuarios);
        resumo.put("totalProdutos", totalProdutos);
        resumo.put("totalPedidos", totalPedidos);

        // Pedidos recentes
        List<Pedido> pedidosRecentes = pedidoRepository.findTop10ByOrderByDataPedidoDesc();
        resumo.put("pedidosRecentes", pedidosRecentes.size());

        // Produtos com estoque baixo
        long estoqueBaixo = produtoRepository.findByEstoqueLessThan(10).size();
        resumo.put("produtosEstoqueBaixo", estoqueBaixo);

        return resumo;
    }

    public Map<String, Object> obterVendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null) {
            inicio = LocalDateTime.now().minusDays(30);
        }
        if (fim == null) {
            fim = LocalDateTime.now();
        }

        List<Pedido> pedidos = pedidoRepository.findByDataPedidoBetween(inicio, fim);

        Map<String, Object> vendas = new HashMap<>();
        vendas.put("periodo", inicio + " até " + fim);
        vendas.put("totalPedidos", pedidos.size());

        BigDecimal valorTotal = pedidos.stream()
                .map(Pedido::getValorTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vendas.put("valorTotal", valorTotal);

        if (!pedidos.isEmpty()) {
            BigDecimal media = valorTotal.divide(BigDecimal.valueOf(pedidos.size()), 2, BigDecimal.ROUND_HALF_UP);
            vendas.put("mediaPorPedido", media);
        }

        return vendas;
    }

    public Map<String, Object> obterProdutosPopulares(Integer limite) {
        // Implementar lógica de produtos mais vendidos
        // Por enquanto, retorna produtos com estoque mais baixo (supostamente os mais vendidos)
        List<Produto> produtos = produtoRepository.findByEstoqueLessThan(50)
                .stream()
                .limit(limite)
                .collect(Collectors.toList());

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("total", produtos.size());
        resultado.put("produtos", produtos.stream()
                .map(p -> Map.of(
                        "id", p.getId(),
                        "nome", p.getNome(),
                        "preco", p.getPreco(),
                        "estoque", p.getEstoque()
                ))
                .collect(Collectors.toList()));

        return resultado;
    }

    public Map<String, Object> obterPedidosPorStatus() {
        List<Pedido> pedidos = pedidoRepository.findAll();

        Map<Pedido.StatusPedido, Long> contagem = pedidos.stream()
                .filter(p -> p.getStatus() != null)
                .collect(Collectors.groupingBy(
                        Pedido::getStatus,
                        Collectors.counting()
                ));

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("total", pedidos.size());

        Map<String, Long> statusMap = new HashMap<>();
        for (Map.Entry<Pedido.StatusPedido, Long> entry : contagem.entrySet()) {
            statusMap.put(entry.getKey().name(), entry.getValue());
        }
        resultado.put("porStatus", statusMap);

        return resultado;
    }
}


