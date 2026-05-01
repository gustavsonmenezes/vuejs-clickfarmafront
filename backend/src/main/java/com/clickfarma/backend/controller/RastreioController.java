package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.AtualizacaoRastreioDTO;
import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.dto.RastreioRequestDTO;
import com.clickfarma.backend.dto.RastreioResponseDTO;
import com.clickfarma.backend.service.RastreioService;
import com.clickfarma.backend.service.RastreioStreamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;

@RestController
@RequestMapping("/api/rastreios")
public class RastreioController {

    @Autowired
    private RastreioService rastreioService;

    @Autowired
    private RastreioStreamService rastreioStreamService;

    // GET - Stream SSE por pedido (atualizacoes em tempo real)
    @GetMapping(value = "/pedido/{pedidoId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPorPedido(@PathVariable Long pedidoId) {
        return rastreioStreamService.subscribe(pedidoId);
    }

    // POST - Criar novo rastreio
    @PostMapping
    public ResponseEntity<?> criarRastreio(@Valid @RequestBody RastreioRequestDTO requestDTO) {
        try {
            RastreioResponseDTO novoRastreio = rastreioService.criarRastreio(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MensagemResponseDTO(
                            "Rastreio criado com sucesso!",
                            true,
                            novoRastreio
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Listar todos os rastreios
    @GetMapping
    public ResponseEntity<List<RastreioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(rastreioService.listarTodos());
    }

    // GET - Buscar rastreio por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            RastreioResponseDTO rastreio = rastreioService.buscarPorId(id);
            return ResponseEntity.ok(rastreio);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Buscar rastreio por código
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<?> buscarPorCodigo(@PathVariable String codigo) {
        try {
            RastreioResponseDTO rastreio = rastreioService.buscarPorCodigo(codigo);
            return ResponseEntity.ok(rastreio);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Buscar rastreio por pedido
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<?> buscarPorPedidoId(@PathVariable Long pedidoId) {
        try {
            RastreioResponseDTO rastreio = rastreioService.buscarPorPedidoId(pedidoId);
            return ResponseEntity.ok(rastreio);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // PUT - Atualizar rastreio
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarRastreio(
            @PathVariable Long id,
            @Valid @RequestBody AtualizacaoRastreioDTO atualizacao) {
        try {
            RastreioResponseDTO rastreioAtualizado = rastreioService.atualizarRastreio(id, atualizacao);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Rastreio atualizado com sucesso!",
                    true,
                    rastreioAtualizado
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // DELETE - Deletar rastreio
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarRastreio(@PathVariable Long id) {
        try {
            rastreioService.deletarRastreio(id);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Rastreio deletado com sucesso!",
                    true
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }
}

