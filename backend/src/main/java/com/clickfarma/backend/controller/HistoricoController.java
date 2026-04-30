package com.clickfarma.backend.controller;

import com.clickfarma.backend.service.HistoricoVisualizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/historico")
public class HistoricoController {

    @Autowired
    private HistoricoVisualizacaoService historicoService;

    @PostMapping("/visualizacao")
    public ResponseEntity<?> registrarVisualizacao(@RequestBody Map<String, Long> body) {
        if (body.containsKey("usuarioId") && body.containsKey("produtoId")) {
            historicoService.registrar(body.get("usuarioId"), body.get("produtoId"));
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<?> getHistorico(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
            "maisVistos", historicoService.getProdutosMaisVistos(id),
            "recentes", historicoService.getProdutosRecentes(id)
        ));
    }
}
