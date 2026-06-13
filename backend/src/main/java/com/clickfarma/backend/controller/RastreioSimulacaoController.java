package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.service.SimuladorEntregaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulacao")
public class RastreioSimulacaoController {

    @Autowired
    private SimuladorEntregaService simuladorEntregaService;

    @PostMapping("/iniciar/{pedidoId}")
    public ResponseEntity<?> iniciarSimulacao(@PathVariable Long pedidoId) {
        try {
            simuladorEntregaService.iniciarSimulacao(pedidoId);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Simulação de entrega iniciada para o pedido " + pedidoId, true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }
}
