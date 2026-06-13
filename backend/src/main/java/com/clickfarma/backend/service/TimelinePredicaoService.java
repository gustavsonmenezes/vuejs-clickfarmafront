package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.TimelinePrevisaoDTO;
import com.clickfarma.backend.model.AgendamentoRecompra;
import com.clickfarma.backend.model.ItemPedido;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.repository.AgendamentoRecompraRepository;
import com.clickfarma.backend.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimelinePredicaoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private AgendamentoRecompraRepository agendamentoRecompraRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double UNIDADE_PADRAO_POR_DIA = 1.0 / 30.0;
    private static final double ESTOQUE_MINIMO_SEGURANCA = 0.2;

    public TimelinePrevisaoDTO preverTimeline(Long usuarioId) {
        List<Pedido> pedidos = pedidoRepository.findPedidosComItensByUsuario(usuarioId);
        if (pedidos == null || pedidos.isEmpty()) {
            TimelinePrevisaoDTO vazio = new TimelinePrevisaoDTO();
            vazio.setMedicamentos(Collections.emptyList());
            vazio.setResumo(new TimelinePrevisaoDTO.ResumoTimeline(0, 0, 0, 0));
            return vazio;
        }

        List<AgendamentoRecompra> agendamentos = agendamentoRecompraRepository.findByUsuarioId(usuarioId);
        Set<Long> produtosComAgendamento = agendamentos.stream()
                .filter(a -> "PENDENTE".equals(a.getStatus()) || "ATIVO".equals(a.getStatus()))
                .map(a -> a.getProduto().getId())
                .collect(Collectors.toSet());

        Map<Long, List<ItemPedido>> itemsPorProduto = new LinkedHashMap<>();
        for (Pedido p : pedidos) {
            for (ItemPedido item : p.getItens()) {
                itemsPorProduto.computeIfAbsent(item.getProduto().getId(), k -> new ArrayList<>()).add(item);
            }
        }

        LocalDate hoje = LocalDate.now();
        List<TimelinePrevisaoDTO.ItemTimeline> timelineItems = new ArrayList<>();
        int criticos = 0, atencao = 0, ok = 0;

        for (Map.Entry<Long, List<ItemPedido>> entry : itemsPorProduto.entrySet()) {
            Long produtoId = entry.getKey();
            List<ItemPedido> itens = entry.getValue();
            if (itens.isEmpty()) continue;

            ItemPedido primeiroItem = itens.get(0);
            String produtoNome = primeiroItem.getProduto().getNome();
            String categoria = primeiroItem.getProduto().getCategoria() != null
                    ? primeiroItem.getProduto().getCategoria().getNome() : "Outros";

            List<ItemPedido> ordenados = itens.stream()
                    .sorted(Comparator.comparing(i -> i.getPedido().getDataPedido()))
                    .collect(Collectors.toList());

            List<TimelinePrevisaoDTO.EventoCompra> historico = new ArrayList<>();
            int totalUnidades = 0;
            LocalDate primeiraCompra = null;
            LocalDate ultimaCompra = null;
            BigDecimal ultimoValor = BigDecimal.ZERO;

            for (ItemPedido item : ordenados) {
                LocalDate data = item.getPedido().getDataPedido() != null
                        ? item.getPedido().getDataPedido().toLocalDate() : hoje;
                int qtd = item.getQuantidade() != null ? item.getQuantidade() : 0;
                BigDecimal valor = item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO;

                totalUnidades += qtd;
                if (primeiraCompra == null) primeiraCompra = data;
                ultimaCompra = data;
                ultimoValor = valor;

                historico.add(new TimelinePrevisaoDTO.EventoCompra(
                        data.format(DATE_FMT), qtd, valor
                ));
            }

            LocalDate primeira = primeiraCompra != null ? primeiraCompra : hoje;
            LocalDate ultima = ultimaCompra != null ? ultimaCompra : hoje;

            int totalHistorico = ordenados.size();
            int ultimaQtd = ordenados.get(ordenados.size() - 1).getQuantidade() != null
                    ? ordenados.get(ordenados.size() - 1).getQuantidade() : 0;

            double consumoDiario;
            if (totalHistorico <= 1) {
                consumoDiario = ultimaQtd * UNIDADE_PADRAO_POR_DIA;
            } else {
                long diasSpan = ChronoUnit.DAYS.between(primeira, ultima);
                if (diasSpan <= 0) {
                    consumoDiario = ultimaQtd * UNIDADE_PADRAO_POR_DIA;
                } else {
                    int unidadesConsumidas = totalUnidades - ultimaQtd;
                    consumoDiario = (double) unidadesConsumidas / diasSpan;
                }
            }

            if (consumoDiario <= 0) {
                consumoDiario = ultimaQtd * UNIDADE_PADRAO_POR_DIA;
            }

            long diasDesdeUltimaCompra = ChronoUnit.DAYS.between(ultima, hoje);
            double estoqueAtual = Math.max(0, ultimaQtd - (consumoDiario * diasDesdeUltimaCompra));

            long diasAteEsgotar;
            if (consumoDiario > 0) {
                diasAteEsgotar = (long) Math.floor(estoqueAtual / consumoDiario);
            } else {
                diasAteEsgotar = 365;
            }

            String status;
            if (diasAteEsgotar <= 3) status = "critico";
            else if (diasAteEsgotar <= 10) status = "atencao";
            else status = "ok";

            switch (status) {
                case "critico": criticos++; break;
                case "atencao": atencao++; break;
                default: ok++; break;
            }

            LocalDate dataEsgotamento = hoje.plusDays(diasAteEsgotar);
            boolean temAgendamento = produtosComAgendamento.contains(produtoId);
            Long agendamentoId = null;
            if (temAgendamento) {
                agendamentoId = agendamentos.stream()
                        .filter(a -> a.getProduto().getId().equals(produtoId))
                        .map(AgendamentoRecompra::getId)
                        .findFirst().orElse(null);
            }

            String recomendacao = gerarRecomendacao(status, temAgendamento, produtoNome);

            TimelinePrevisaoDTO.ItemTimeline item = new TimelinePrevisaoDTO.ItemTimeline();
            item.setProdutoId(produtoId);
            item.setProdutoNome(produtoNome);
            item.setCategoria(categoria);
            item.setTotalUnidadesCompradas(totalUnidades);
            item.setPrimeiraCompra(primeira.format(DATE_FMT));
            item.setUltimaCompra(ultima.format(DATE_FMT));
            item.setConsumoDiarioEstimado(Math.round(consumoDiario * 1000.0) / 1000.0);
            item.setEstoqueRestanteEstimado(Math.round(estoqueAtual * 100.0) / 100.0);
            item.setDiasAteEsgotar(Math.max(0, diasAteEsgotar));
            item.setDataPrevisaoEsgotamento(dataEsgotamento.format(DATE_FMT));
            item.setStatus(status);
            item.setTemAgendamentoAtivo(temAgendamento);
            item.setAgendamentoId(agendamentoId);
            item.setRecomendacao(recomendacao);
            item.setHistoricoCompras(historico);

            timelineItems.add(item);
        }

        timelineItems.sort(Comparator.comparingLong(TimelinePrevisaoDTO.ItemTimeline::getDiasAteEsgotar));

        TimelinePrevisaoDTO result = new TimelinePrevisaoDTO();
        result.setMedicamentos(timelineItems);
        result.setResumo(new TimelinePrevisaoDTO.ResumoTimeline(
                timelineItems.size(), criticos, atencao, ok
        ));

        return result;
    }

    public Map<String, Object> preverPorProduto(Long usuarioId, Long produtoId) {
        TimelinePrevisaoDTO timeline = preverTimeline(usuarioId);
        return timeline.getMedicamentos().stream()
                .filter(m -> m.getProdutoId().equals(produtoId))
                .findFirst()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("produto", item.getProdutoNome());
                    map.put("consumoDiario", item.getConsumoDiarioEstimado());
                    map.put("estoqueRestante", item.getEstoqueRestanteEstimado());
                    map.put("diasAteEsgotar", item.getDiasAteEsgotar());
                    map.put("dataPrevisao", item.getDataPrevisaoEsgotamento());
                    map.put("status", item.getStatus());
                    map.put("recomendacao", item.getRecomendacao());
                    map.put("historico", item.getHistoricoCompras());
                    return map;
                })
                .orElse(Collections.emptyMap());
    }

    private String gerarRecomendacao(String status, boolean temAgendamento, String produtoNome) {
        if (temAgendamento) {
            return "Recompra já agendada para " + produtoNome + ". Aguardando notificação.";
        }
        switch (status) {
            case "critico":
                return "Estoque crítico! Recomendamos comprar " + produtoNome + " imediatamente.";
            case "atencao":
                return "Estoque baixo. Programe a compra de " + produtoNome + " nos próximos dias.";
            default:
                return "Estoque OK. Acompanhe no seu próximo ciclo de compras.";
        }
    }
}
