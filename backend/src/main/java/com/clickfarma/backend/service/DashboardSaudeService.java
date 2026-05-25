package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.DashboardSaudeResponseDTO;
import com.clickfarma.backend.model.ItemPedido;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardSaudeService {

    @Autowired
    private PedidoRepository pedidoRepository;

    private static final int DIAS_DURACAO_ESTOQUE_PADRAO = 30;
    private static final DateTimeFormatter MES_FORMATTER = DateTimeFormatter.ofPattern("MMM/yy");

    public DashboardSaudeResponseDTO getDashboardSaude(Long usuarioId) {
        List<Pedido> pedidos = pedidoRepository.findPedidosComItensByUsuario(usuarioId);

        if (pedidos == null || pedidos.isEmpty()) {
            return criarDashboardVazio();
        }

        DashboardSaudeResponseDTO dto = new DashboardSaudeResponseDTO();
        dto.setResumo(calcularResumo(pedidos));
        dto.setGastosMensais(calcularGastosMensais(pedidos));
        dto.setDistribuicaoCategoria(calcularDistribuicaoCategoria(pedidos));
        dto.setTopMedicamentos(calcularTopMedicamentos(pedidos));
        dto.setPrevisaoReposicao(calcularPrevisaoReposicao(pedidos));

        return dto;
    }

    private DashboardSaudeResponseDTO criarDashboardVazio() {
        DashboardSaudeResponseDTO dto = new DashboardSaudeResponseDTO();
        dto.setResumo(new DashboardSaudeResponseDTO.ResumoGeral(BigDecimal.ZERO, 0, "N/A", BigDecimal.ZERO));
        dto.setGastosMensais(Collections.emptyList());
        dto.setDistribuicaoCategoria(Collections.emptyList());
        dto.setTopMedicamentos(Collections.emptyList());
        dto.setPrevisaoReposicao(Collections.emptyList());
        return dto;
    }

    private DashboardSaudeResponseDTO.ResumoGeral calcularResumo(List<Pedido> pedidos) {
        BigDecimal totalGasto = pedidos.stream()
                .map(Pedido::getValorTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> freqMap = new HashMap<>();
        for (Pedido p : pedidos) {
            for (ItemPedido item : p.getItens()) {
                String nome = item.getProduto().getNome();
                freqMap.put(nome, freqMap.getOrDefault(nome, 0L) + 1);
            }
        }

        String medicamentoFrequente = freqMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        BigDecimal economiaEstimada = totalGasto.multiply(BigDecimal.valueOf(0.10));

        return new DashboardSaudeResponseDTO.ResumoGeral(
                totalGasto, pedidos.size(), medicamentoFrequente, economiaEstimada
        );
    }

    private List<DashboardSaudeResponseDTO.GastoMensal> calcularGastosMensais(List<Pedido> pedidos) {
        Map<String, BigDecimal> gastosPorMes = new TreeMap<>();

        for (Pedido p : pedidos) {
            if (p.getDataPedido() != null && p.getValorTotal() != null) {
                String mes = p.getDataPedido().format(MES_FORMATTER);
                gastosPorMes.merge(mes, p.getValorTotal(), BigDecimal::add);
            }
        }

        return gastosPorMes.entrySet().stream()
                .map(e -> new DashboardSaudeResponseDTO.GastoMensal(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<DashboardSaudeResponseDTO.DistribuicaoCategoria> calcularDistribuicaoCategoria(List<Pedido> pedidos) {
        Map<String, BigDecimal> gastosPorCategoria = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Pedido p : pedidos) {
            for (ItemPedido item : p.getItens()) {
                Produto prod = item.getProduto();
                String categoria = prod.getCategoria() != null ? prod.getCategoria().getNome() : "Outros";
                BigDecimal subtotal = item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO;
                gastosPorCategoria.merge(categoria, subtotal, BigDecimal::add);
                total = total.add(subtotal);
            }
        }

        BigDecimal totalFinal = total;
        return gastosPorCategoria.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(e -> {
                    double percentual = totalFinal.compareTo(BigDecimal.ZERO) > 0
                            ? e.getValue().multiply(BigDecimal.valueOf(100)).divide(totalFinal, 2, RoundingMode.HALF_UP).doubleValue()
                            : 0.0;
                    return new DashboardSaudeResponseDTO.DistribuicaoCategoria(e.getKey(), e.getValue(), percentual);
                })
                .collect(Collectors.toList());
    }

    private List<DashboardSaudeResponseDTO.TopMedicamento> calcularTopMedicamentos(List<Pedido> pedidos) {
        Map<String, int[]> mapaQtd = new LinkedHashMap<>();
        Map<String, BigDecimal> mapaGasto = new LinkedHashMap<>();
        Map<String, String> mapaUltimaCompra = new LinkedHashMap<>();

        List<Pedido> pedidosOrdenados = pedidos.stream()
                .sorted(Comparator.comparing(Pedido::getDataPedido))
                .collect(Collectors.toList());

        for (Pedido p : pedidosOrdenados) {
            String dataStr = p.getDataPedido() != null ? p.getDataPedido().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM")) : "";
            for (ItemPedido item : p.getItens()) {
                String nome = item.getProduto().getNome();
                int qtd = item.getQuantidade() != null ? item.getQuantidade() : 0;
                BigDecimal subtotal = item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO;

                mapaQtd.computeIfAbsent(nome, k -> new int[]{0});
                mapaQtd.get(nome)[0] += qtd;

                mapaGasto.merge(nome, subtotal, BigDecimal::add);
                mapaUltimaCompra.put(nome, dataStr);
            }
        }

        return mapaQtd.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]))
                .limit(5)
                .map(e -> new DashboardSaudeResponseDTO.TopMedicamento(
                        e.getKey(),
                        e.getValue()[0],
                        mapaGasto.getOrDefault(e.getKey(), BigDecimal.ZERO),
                        mapaUltimaCompra.getOrDefault(e.getKey(), "")
                ))
                .collect(Collectors.toList());
    }

    private List<DashboardSaudeResponseDTO.PrevisaoReposicao> calcularPrevisaoReposicao(List<Pedido> pedidos) {
        Map<String, LocalDate> ultimaCompraPorProduto = new LinkedHashMap<>();
        Map<String, String> categoriaPorProduto = new LinkedHashMap<>();

        List<Pedido> pedidosOrdenados = pedidos.stream()
                .sorted(Comparator.comparing(Pedido::getDataPedido))
                .collect(Collectors.toList());

        for (Pedido p : pedidosOrdenados) {
            LocalDate data = p.getDataPedido() != null ? p.getDataPedido().toLocalDate() : LocalDate.now();
            for (ItemPedido item : p.getItens()) {
                String nome = item.getProduto().getNome();
                ultimaCompraPorProduto.put(nome, data);
                Produto prod = item.getProduto();
                categoriaPorProduto.put(nome, prod.getCategoria() != null ? prod.getCategoria().getNome() : "Outros");
            }
        }

        LocalDate hoje = LocalDate.now();
        return ultimaCompraPorProduto.entrySet().stream()
                .map(e -> {
                    String produto = e.getKey();
                    LocalDate ultima = e.getValue();
                    long diasDesdeCompra = ChronoUnit.DAYS.between(ultima, hoje);
                    int diasRestantes = Math.max(0, DIAS_DURACAO_ESTOQUE_PADRAO - (int) diasDesdeCompra);
                    String status;
                    if (diasRestantes <= 3) status = "critico";
                    else if (diasRestantes <= 10) status = "atencao";
                    else status = "ok";

                    return new DashboardSaudeResponseDTO.PrevisaoReposicao(
                            produto,
                            categoriaPorProduto.getOrDefault(produto, "Outros"),
                            ultima.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            diasRestantes,
                            status
                    );
                })
                .sorted(Comparator.comparingInt(DashboardSaudeResponseDTO.PrevisaoReposicao::getDiasRestantes))
                .collect(Collectors.toList());
    }
}
