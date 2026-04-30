package com.clickfarma.backend.controller;

import com.clickfarma.backend.service.PagamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService pagamentoService;

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        return ResponseEntity.ok(pagamentoService.listarTodos());
    }

    @GetMapping("/pendentes")
    public ResponseEntity<?> listarPendentes() {
        return ResponseEntity.ok(pagamentoService.listarPendentes());
    }

    @GetMapping("/farmacia/{id}")
    public ResponseEntity<?> listarPorFarmacia(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.listarPorFarmacia(id));
    }

    @GetMapping("/motoboy/{id}")
    public ResponseEntity<?> listarPorMotoboy(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.listarPorMotoboy(id));
    }

    @PostMapping("/gerar/farmacia/{farmaciaId}")
    public ResponseEntity<?> gerarFarmacia(@PathVariable Long farmaciaId, @RequestBody Map<String, String> body) {
        try {
            String periodo = body.getOrDefault("periodo", java.time.YearMonth.now().toString());
            return ResponseEntity.ok(pagamentoService.gerarPagamentoFarmacia(farmaciaId, periodo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/gerar/motoboy/{motoboyId}")
    public ResponseEntity<?> gerarMotoboy(@PathVariable Long motoboyId, @RequestBody Map<String, String> body) {
        try {
            String periodo = body.getOrDefault("periodo", java.time.YearMonth.now().toString());
            return ResponseEntity.ok(pagamentoService.gerarPagamentoMotoboy(motoboyId, periodo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<?> marcarComoPago(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        try {
            String obs = body != null ? body.get("observacoes") : null;
            return ResponseEntity.ok(pagamentoService.marcarComoPago(id, obs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
