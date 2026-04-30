package com.clickfarma.backend.service;

import com.clickfarma.backend.model.Farmacia;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.repository.FarmaciaRepository;
import com.clickfarma.backend.repository.PedidoRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    @Autowired
    private com.clickfarma.backend.repository.MotoboyRepository motoboyRepository;

    public Map<String, Object> obterResumoGeral() {
        Map<String, Object> resumo = new HashMap<>();
        resumo.put("totalUsuarios", usuarioRepository.count());
        resumo.put("totalProdutos", produtoRepository.count());
        resumo.put("totalPedidos", pedidoRepository.count());
        resumo.put("pedidosRecentes", pedidoRepository.findTop10ByOrderByDataPedidoDesc().size());
        resumo.put("produtosEstoqueBaixo", produtoRepository.findByEstoqueLessThan(10).size());
        return resumo;
    }

    public Map<String, Object> obterDashboardAdmin() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Pedido> todosPedidos = pedidoRepository.findAll();
        BigDecimal faturamentoTotal = todosPedidos.stream()
                .map(Pedido::getValorTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comissaoTotal = faturamentoTotal.multiply(new BigDecimal("0.005"));

        dashboard.put("faturamentoTotal", faturamentoTotal);
        dashboard.put("comissaoTotal", comissaoTotal);
        dashboard.put("totalPedidos", todosPedidos.size());
        dashboard.put("totalClientes", usuarioRepository.countByRole("CUSTOMER"));
        dashboard.put("totalFarmacias", farmaciaRepository.count());

        return dashboard;
    }

    public Map<String, Object> obterRankingsAdmin() {
        Map<String, Object> rankings = new HashMap<>();

        List<Pedido> todosPedidos = pedidoRepository.findAll();

        // Top Farmácias por Faturamento
        List<Farmacia> todasFarmacias = farmaciaRepository.findAll();
        List<Map<String, Object>> topFarmacias = new ArrayList<>();
        for (Farmacia f : todasFarmacias) {
            BigDecimal faturamento = todosPedidos.stream()
                    .filter(p -> p.getFarmacia() != null && p.getFarmacia().getId().equals(f.getId()))
                    .map(Pedido::getValorTotal)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long pedidoCount = todosPedidos.stream()
                    .filter(p -> p.getFarmacia() != null && p.getFarmacia().getId().equals(f.getId()))
                    .count();
            Map<String, Object> item = new HashMap<>();
            item.put("id", f.getId());
            item.put("nome", f.getNome());
            item.put("cidade", f.getCidade());
            item.put("faturamento", faturamento);
            item.put("pedidos", pedidoCount);
            topFarmacias.add(item);
        }
        topFarmacias.sort((a, b) -> ((BigDecimal) b.get("faturamento")).compareTo((BigDecimal) a.get("faturamento")));
        rankings.put("topFarmacias", topFarmacias.stream().limit(5).collect(Collectors.toList()));

        // Top Clientes por Valor Gasto
        Map<Long, BigDecimal> gastosPorUsuario = todosPedidos.stream()
                .filter(p -> p.getValorTotal() != null && p.getUsuario() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getUsuario().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Pedido::getValorTotal, BigDecimal::add)
                ));

        List<Map<String, Object>> topClientes = gastosPorUsuario.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("usuarioId", entry.getKey());
                    item.put("totalGasto", entry.getValue());
                    usuarioRepository.findById(entry.getKey()).ifPresent(u -> {
                        item.put("nome", u.getNome());
                        item.put("email", u.getEmail());
                    });
                    return item;
                })
                .collect(Collectors.toList());
        rankings.put("topClientes", topClientes);

        // Top Motoboys por Entregas
        Map<Long, Long> entregasPorMotoboy = todosPedidos.stream()
                .filter(p -> p.getMotoboy() != null && p.getStatus() == Pedido.StatusPedido.ENTREGUE)
                .collect(Collectors.groupingBy(p -> p.getMotoboy().getId(), Collectors.counting()));

        List<Map<String, Object>> topMotoboys = entregasPorMotoboy.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("motoboyId", entry.getKey());
                    item.put("entregas", entry.getValue());
                    motoboyRepository.findById(entry.getKey()).ifPresent(m -> {
                        item.put("nome", m.getNome());
                    });
                    return item;
                })
                .collect(Collectors.toList());
        rankings.put("topMotoboys", topMotoboys);

        return rankings;
    }

    public Map<String, Object> obterVendasSemana() {
        Map<String, Object> resultado = new HashMap<>();
        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.with(DayOfWeek.MONDAY);

        String[] diasNomes = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};
        List<String> labels = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        List<Long> quantidades = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicioSemana.plusDays(i);
            LocalDateTime inicio = dia.atStartOfDay();
            LocalDateTime fim = dia.plusDays(1).atStartOfDay();

            List<Pedido> pedidosDia = pedidoRepository.findByDataPedidoBetween(inicio, fim);
            BigDecimal totalDia = pedidosDia.stream()
                    .map(Pedido::getValorTotal)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            labels.add(diasNomes[i]);
            valores.add(totalDia.doubleValue());
            quantidades.add((long) pedidosDia.size());
        }

        resultado.put("labels", labels);
        resultado.put("valores", valores);
        resultado.put("quantidades", quantidades);
        return resultado;
    }

    public Map<String, Object> obterDashboardFarmacia(Long farmaciaId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<Pedido> pedidosFarmacia = pedidoRepository.findByFarmaciaId(farmaciaId);
        BigDecimal faturamentoBruto = pedidosFarmacia.stream()
                .map(Pedido::getValorTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comissao = faturamentoBruto.multiply(new BigDecimal("0.005"));
        BigDecimal faturamentoLiquido = faturamentoBruto.subtract(comissao);

        long pedidosPendentes = pedidosFarmacia.stream()
                .filter(p -> p.getStatus() == Pedido.StatusPedido.AGUARDANDO_PAGAMENTO ||
                             p.getStatus() == Pedido.StatusPedido.PAGO)
                .count();

        dashboard.put("faturamentoBruto", faturamentoBruto);
        dashboard.put("faturamentoLiquido", faturamentoLiquido);
        dashboard.put("comissaoPaga", comissao);
        dashboard.put("totalPedidos", pedidosFarmacia.size());
        dashboard.put("pedidosPendentes", pedidosPendentes);
        dashboard.put("totalProdutos", produtoRepository.findByFarmaciaId(farmaciaId).size());

        return dashboard;
    }

    public Map<String, Object> obterVendasSemanaFarmacia(Long farmaciaId) {
        Map<String, Object> resultado = new HashMap<>();
        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.with(DayOfWeek.MONDAY);

        String[] diasNomes = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};
        List<String> labels = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicioSemana.plusDays(i);
            LocalDateTime inicio = dia.atStartOfDay();
            LocalDateTime fim = dia.plusDays(1).atStartOfDay();

            List<Pedido> pedidosDia = pedidoRepository.findByFarmaciaIdAndDataPedidoBetween(farmaciaId, inicio, fim);
            BigDecimal totalDia = pedidosDia.stream()
                    .map(Pedido::getValorTotal)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            labels.add(diasNomes[i]);
            valores.add(totalDia.doubleValue());
        }

        resultado.put("labels", labels);
        resultado.put("valores", valores);
        return resultado;
    }

    public Map<String, Object> obterVendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null) inicio = LocalDateTime.now().minusDays(30);
        if (fim == null) fim = LocalDateTime.now();

        List<Pedido> pedidos = pedidoRepository.findByDataPedidoBetween(inicio, fim);
        BigDecimal valorTotal = pedidos.stream()
                .map(Pedido::getValorTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> vendas = new HashMap<>();
        vendas.put("periodo", inicio + " até " + fim);
        vendas.put("totalPedidos", pedidos.size());
        vendas.put("valorTotal", valorTotal);
        return vendas;
    }

    public Map<String, Object> obterProdutosPopulares(Integer limite) {
        List<Produto> produtos = produtoRepository.findByEstoqueLessThan(50)
                .stream().limit(limite).collect(Collectors.toList());
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("total", produtos.size());
        resultado.put("produtos", produtos.stream()
                .map(p -> Map.of("id", p.getId(), "nome", p.getNome(),
                        "preco", p.getPreco(), "estoque", p.getEstoque()))
                .collect(Collectors.toList()));
        return resultado;
    }

    public Map<String, Object> obterPedidosPorStatus() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        Map<Pedido.StatusPedido, Long> contagem = pedidos.stream()
                .filter(p -> p.getStatus() != null)
                .collect(Collectors.groupingBy(Pedido::getStatus, Collectors.counting()));

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
