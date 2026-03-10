package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.*;
import com.clickfarma.backend.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "http://localhost:8082")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<?> criarPedido(@Valid @RequestBody PedidoRequestDTO pedidoDTO) {
        try {
            PedidoResponseDTO novoPedido = pedidoService.criarPedido(pedidoDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MensagemResponseDTO(
                            "Pedido criado com sucesso!",
                            true,
                            novoPedido
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            PedidoResponseDTO pedido = pedidoService.buscarPorId(id);
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(pedidoService.buscarPorUsuario(usuarioId));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<?> buscarPorCodigo(@PathVariable String codigo) {
        try {
            PedidoResponseDTO pedido = pedidoService.buscarPorCodigo(codigo);
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            PedidoResponseDTO pedido = pedidoService.atualizarStatus(id, status);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Status atualizado com sucesso!",
                    true,
                    pedido
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @GetMapping("/{codigoPedido}/rastreio")
    public ResponseEntity<?> rastrearPedido(@PathVariable String codigoPedido) {
        try {
            RastreioResponseDTO rastreio = pedidoService.rastrearPedido(codigoPedido);
            return ResponseEntity.ok(rastreio);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @PostMapping("/{pedidoId}/rastreio/atualizar")
    public ResponseEntity<?> atualizarRastreio(
            @PathVariable Long pedidoId,
            @RequestParam String localizacao,
            @RequestParam String status) {
        try {
            RastreioResponseDTO rastreio = pedidoService.atualizarRastreio(pedidoId, localizacao, status);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Rastreio atualizado com sucesso!",
                    true,
                    rastreio
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }
}

