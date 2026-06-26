package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.entrega.EntregaResponseDTO;
import com.clickfarma.backend.model.Entrega;
import com.clickfarma.backend.model.Entrega.StatusEntrega;
import com.clickfarma.backend.model.Entregador;
import com.clickfarma.backend.repository.EntregaRepository;
import com.clickfarma.backend.repository.EntregadorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/entregas-entregador")
public class EntregaEntregadorController {

    private final EntregaRepository entregaRepository;
    private final EntregadorRepository entregadorRepository;

    public EntregaEntregadorController(EntregaRepository entregaRepository,
                                        EntregadorRepository entregadorRepository) {
        this.entregaRepository = entregaRepository;
        this.entregadorRepository = entregadorRepository;
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<EntregaResponseDTO>> listarPendentes() {
        return ResponseEntity.ok(
                entregaRepository.findByStatusOrderByCriadoEmDesc(StatusEntrega.PENDENTE)
                        .stream().map(EntregaResponseDTO::fromEntity).toList()
        );
    }

    @GetMapping("/entregador/{entregadorId}/ativas")
    public ResponseEntity<List<EntregaResponseDTO>> listarAtivas(@PathVariable Long entregadorId) {
        return ResponseEntity.ok(
                entregaRepository.findByEntregadorIdAndStatusInOrderByCriadoEmDesc(
                        entregadorId,
                        List.of(StatusEntrega.ACEITA, StatusEntrega.RETIRADA, StatusEntrega.EM_ROTA)
                ).stream().map(EntregaResponseDTO::fromEntity).toList()
        );
    }

    @GetMapping("/entregador/{entregadorId}/historico")
    public ResponseEntity<List<EntregaResponseDTO>> listarHistorico(@PathVariable Long entregadorId) {
        return ResponseEntity.ok(
                entregaRepository.findByEntregadorIdAndStatusOrderByCriadoEmDesc(
                        entregadorId, StatusEntrega.ENTREGUE
                ).stream().map(EntregaResponseDTO::fromEntity).toList()
        );
    }

    @GetMapping("/entregador/{entregadorId}/todas")
    public ResponseEntity<List<EntregaResponseDTO>> listarTodas(@PathVariable Long entregadorId) {
        return ResponseEntity.ok(
                entregaRepository.findByEntregadorIdOrderByCriadoEmDesc(entregadorId)
                        .stream().map(EntregaResponseDTO::fromEntity).toList()
        );
    }

    @PostMapping("/{entregaId}/aceitar/{entregadorId}")
    public ResponseEntity<?> aceitar(@PathVariable Long entregaId,
                                      @PathVariable Long entregadorId) {
        Entregador entregador = entregadorRepository.findById(entregadorId).orElse(null);
        if (entregador == null) return ResponseEntity.badRequest().body("Entregador não encontrado");
        if (!entregador.isAtivo()) return ResponseEntity.badRequest().body("Entregador inativo");

        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        if (entrega == null) return ResponseEntity.badRequest().body("Entrega não encontrada");
        if (entrega.getStatus() != StatusEntrega.PENDENTE) {
            return ResponseEntity.badRequest().body("Entrega já foi " + entrega.getStatus().name().toLowerCase());
        }

        entrega.setEntregador(entregador);
        entrega.setStatus(StatusEntrega.ACEITA);
        entrega.setAceitoEm(LocalDateTime.now());
        entregaRepository.save(entrega);

        return ResponseEntity.ok(EntregaResponseDTO.fromEntity(entrega));
    }

    @PostMapping("/{entregaId}/recusar/{entregadorId}")
    public ResponseEntity<?> recusar(@PathVariable Long entregaId,
                                      @PathVariable Long entregadorId) {
        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        if (entrega == null) return ResponseEntity.badRequest().body("Entrega não encontrada");
        if (entrega.getStatus() != StatusEntrega.PENDENTE) {
            return ResponseEntity.badRequest().body("Entrega já foi " + entrega.getStatus().name().toLowerCase());
        }
        return ResponseEntity.ok("Entrega recusada");
    }

    @PostMapping("/{entregaId}/retirar")
    public ResponseEntity<?> marcarRetirada(@PathVariable("entregaId") Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        if (entrega == null) return ResponseEntity.badRequest().body("Entrega não encontrada");
        if (entrega.getStatus() != StatusEntrega.ACEITA) {
            return ResponseEntity.badRequest().body("Entrega precisa estar ACEITA para retirar");
        }
        entrega.setStatus(StatusEntrega.RETIRADA);
        entrega.setRetiradoEm(LocalDateTime.now());
        entregaRepository.save(entrega);
        return ResponseEntity.ok(EntregaResponseDTO.fromEntity(entrega));
    }

    @PostMapping("/{entregaId}/iniciar-rota")
    public ResponseEntity<?> iniciarRota(@PathVariable Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        if (entrega == null) return ResponseEntity.badRequest().body("Entrega não encontrada");
        if (entrega.getStatus() != StatusEntrega.RETIRADA) {
            return ResponseEntity.badRequest().body("Entrega precisa estar RETIRADA para iniciar rota");
        }
        entrega.setStatus(StatusEntrega.EM_ROTA);
        entregaRepository.save(entrega);
        return ResponseEntity.ok(EntregaResponseDTO.fromEntity(entrega));
    }

    @PostMapping("/{entregaId}/finalizar")
    public ResponseEntity<?> finalizar(@PathVariable Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        if (entrega == null) return ResponseEntity.badRequest().body("Entrega não encontrada");
        if (entrega.getStatus() != StatusEntrega.EM_ROTA && entrega.getStatus() != StatusEntrega.RETIRADA) {
            return ResponseEntity.badRequest().body("Entrega não pode ser finalizada");
        }
        entrega.setStatus(StatusEntrega.ENTREGUE);
        entrega.setEntregueEm(LocalDateTime.now());
        entregaRepository.save(entrega);
        return ResponseEntity.ok(EntregaResponseDTO.fromEntity(entrega));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return entregaRepository.findById(id)
                .map(e -> ResponseEntity.ok(EntregaResponseDTO.fromEntity(e)))
                .orElse(ResponseEntity.notFound().build());
    }
}
