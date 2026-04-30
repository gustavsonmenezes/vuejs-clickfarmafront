package com.clickfarma.backend.service;

import com.clickfarma.backend.model.*;
import com.clickfarma.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PagamentoService {

    @Autowired private PagamentoRepository pagamentoRepository;
    @Autowired private FarmaciaRepository farmaciaRepository;
    @Autowired private MotoboyRepository motoboyRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    public String criarLinkPagamento(Double valor, Long pedidoId) {
        // Por enquanto retorna um link simulado de PIX/Pagamento
        // Futuramente integrar com SDK do Mercado Pago
        return "https://www.mercadopago.com.br/checkout/v1/redirect?pref_id=simulated_" + pedidoId;
    }

    public List<Map<String, Object>> listarTodos() {
        return pagamentoRepository.findAllOrderByDataDesc()
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> listarPendentes() {
        return pagamentoRepository.findByStatusOrderByDataCriacaoDesc(Pagamento.StatusPagamento.PENDENTE)
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> listarPorFarmacia(Long farmaciaId) {
        return pagamentoRepository.findByFarmaciaIdOrderByDataCriacaoDesc(farmaciaId)
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> listarPorMotoboy(Long motoboyId) {
        return pagamentoRepository.findByMotoboyIdOrderByDataCriacaoDesc(motoboyId)
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> gerarPagamentoFarmacia(Long farmaciaId, String periodo) {
        Farmacia farmacia = farmaciaRepository.findById(farmaciaId)
                .orElseThrow(() -> new RuntimeException("Farmácia não encontrada"));

        YearMonth ym = YearMonth.parse(periodo, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime inicio = ym.atDay(1).atStartOfDay();
        LocalDateTime fim = ym.atEndOfMonth().atTime(23, 59, 59);

        List<Pedido> pedidos = pedidoRepository.findByFarmaciaIdAndDataPedidoBetween(farmaciaId, inicio, fim);
        BigDecimal valorBruto = pedidos.stream()
                .map(Pedido::getValorTotal).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxa = valorBruto.multiply(new BigDecimal("0.005"));
        BigDecimal liquido = valorBruto.subtract(taxa);

        Pagamento p = new Pagamento();
        p.setTipo(Pagamento.TipoPagamento.FARMACIA);
        p.setFarmacia(farmacia);
        p.setValorBruto(valorBruto);
        p.setValorTaxa(taxa);
        p.setValorLiquido(liquido);
        p.setChavePix(farmacia.getChavePix());
        p.setTipoChavePix(farmacia.getTipoChavePix());
        p.setReferenciaPeriodo(periodo);
        p.setStatus(Pagamento.StatusPagamento.PENDENTE);

        return toMap(pagamentoRepository.save(p));
    }

    @Transactional
    public Map<String, Object> gerarPagamentoMotoboy(Long motoboyId, String periodo) {
        Motoboy motoboy = motoboyRepository.findById(motoboyId)
                .orElseThrow(() -> new RuntimeException("Motoboy não encontrado"));

        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getMotoboy() != null && p.getMotoboy().getId().equals(motoboyId)
                        && p.getStatus() == Pedido.StatusPedido.ENTREGUE)
                .collect(Collectors.toList());
        BigDecimal valorBruto = BigDecimal.valueOf(pedidos.size()).multiply(new BigDecimal("15.00"));

        Pagamento pg = new Pagamento();
        pg.setTipo(Pagamento.TipoPagamento.MOTOBOY);
        pg.setMotoboy(motoboy);
        pg.setValorBruto(valorBruto);
        pg.setValorTaxa(BigDecimal.ZERO);
        pg.setValorLiquido(valorBruto);
        pg.setChavePix(motoboy.getChavePix());
        pg.setTipoChavePix(motoboy.getTipoChavePix());
        pg.setReferenciaPeriodo(periodo);
        pg.setStatus(Pagamento.StatusPagamento.PENDENTE);

        return toMap(pagamentoRepository.save(pg));
    }

    @Transactional
    public Map<String, Object> marcarComoPago(Long id, String observacoes) {
        Pagamento p = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
        p.setStatus(Pagamento.StatusPagamento.PAGO);
        p.setDataPagamento(LocalDateTime.now());
        if (observacoes != null) p.setObservacoes(observacoes);
        return toMap(pagamentoRepository.save(p));
    }

    private Map<String, Object> toMap(Pagamento p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("tipo", p.getTipo());
        m.put("status", p.getStatus());
        m.put("valorBruto", p.getValorBruto());
        m.put("valorTaxa", p.getValorTaxa());
        m.put("valorLiquido", p.getValorLiquido());
        m.put("chavePix", p.getChavePix());
        m.put("tipoChavePix", p.getTipoChavePix());
        m.put("referenciaPeriodo", p.getReferenciaPeriodo());
        m.put("dataPagamento", p.getDataPagamento());
        m.put("dataCriacao", p.getDataCriacao());
        m.put("observacoes", p.getObservacoes());
        if (p.getFarmacia() != null) {
            m.put("farmaciaId", p.getFarmacia().getId());
            m.put("farmaciaNome", p.getFarmacia().getNome());
        }
        if (p.getMotoboy() != null) {
            m.put("motoboyId", p.getMotoboy().getId());
            m.put("motoboyNome", p.getMotoboy().getNome());
        }
        return m;
    }
}