package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/resumo")
    public ResponseEntity<?> resumoGeral() {
        try {
            return ResponseEntity.ok(dashboardService.obterResumoGeral());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<?> dashboardAdmin() {
        return ResponseEntity.ok(dashboardService.obterDashboardAdmin());
    }

    @GetMapping("/admin/rankings")
    public ResponseEntity<?> rankingsAdmin() {
        return ResponseEntity.ok(dashboardService.obterRankingsAdmin());
    }

    @GetMapping("/admin/vendas-semana")
    public ResponseEntity<?> vendasSemana() {
        return ResponseEntity.ok(dashboardService.obterVendasSemana());
    }

    @GetMapping("/admin/pedidos-por-status")
    public ResponseEntity<?> pedidosPorStatus() {
        return ResponseEntity.ok(dashboardService.obterPedidosPorStatus());
    }

    @GetMapping("/farmacia/{id}")
    public ResponseEntity<?> dashboardFarmacia(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.obterDashboardFarmacia(id));
    }

    @GetMapping("/farmacia/{id}/vendas-semana")
    public ResponseEntity<?> vendasSemanaFarmacia(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.obterVendasSemanaFarmacia(id));
    }

    @GetMapping("/vendas")
    public ResponseEntity<?> vendasPorPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        try {
            return ResponseEntity.ok(dashboardService.obterVendasPorPeriodo(inicio, fim));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @GetMapping("/produtos-populares")
    public ResponseEntity<?> produtosPopulares(@RequestParam(defaultValue = "10") Integer limite) {
        return ResponseEntity.ok(dashboardService.obterProdutosPopulares(limite));
    }
}