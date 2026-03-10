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
@CrossOrigin(origins = "http://localhost:8082")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/resumo")
    public ResponseEntity<?> resumoGeral() {
        try {
            Map<String, Object> resumo = dashboardService.obterResumoGeral();
            return ResponseEntity.ok(resumo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @GetMapping("/vendas")
    public ResponseEntity<?> vendasPorPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        try {
            Map<String, Object> vendas = dashboardService.obterVendasPorPeriodo(inicio, fim);
            return ResponseEntity.ok(vendas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @GetMapping("/produtos-populares")
    public ResponseEntity<?> produtosPopulares(@RequestParam(defaultValue = "10") Integer limite) {
        try {
            Map<String, Object> produtos = dashboardService.obterProdutosPopulares(limite);
            return ResponseEntity.ok(produtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @GetMapping("/pedidos-por-status")
    public ResponseEntity<?> pedidosPorStatus() {
        try {
            Map<String, Object> status = dashboardService.obterPedidosPorStatus();
            return ResponseEntity.ok(status);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }
}