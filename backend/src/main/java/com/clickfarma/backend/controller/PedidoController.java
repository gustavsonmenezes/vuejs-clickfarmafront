package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.*;
import com.clickfarma.backend.service.PedidoService;
import com.clickfarma.backend.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PagamentoService pagamentoService;

    @PostMapping
    public ResponseEntity<?> criarPedido(@Valid @RequestBody PedidoRequestDTO pedidoDTO) {
        try {
            PedidoResponseDTO novoPedido = pedidoService.criarPedido(pedidoDTO);

            // A lógica de pagamento foi movida para dentro do PedidoService
            // para garantir que o link só seja gerado após o sucesso da criação do pedido.

            return ResponseEntity.status(HttpStatus.CREATED).body(novoPedido);
        } catch (RuntimeException e) {
            // Retorna a mensagem de erro exata do serviço (ex: "Estoque insuficiente")
            return ResponseEntity.badRequest().body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Listar todos os pedidos
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    // GET - Buscar pedido por ID
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

    // GET - Buscar pedidos por usuário
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(pedidoService.buscarPorUsuario(usuarioId));
    }

    // GET - Buscar pedidos por farmácia
    @GetMapping("/farmacia/{farmaciaId}")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorFarmacia(@PathVariable Long farmaciaId) {
        return ResponseEntity.ok(pedidoService.buscarPorFarmacia(farmaciaId));
    }

    // GET - Buscar pedido por código
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

    // GET - Buscar pedidos por status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(pedidoService.buscarPorStatus(status));
    }

    // GET - Buscar pedidos por período
    @GetMapping("/periodo")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(pedidoService.buscarPorPeriodo(inicio, fim));
    }

    // GET - Buscar pedidos recentes
    @GetMapping("/recentes")
    public ResponseEntity<List<PedidoResponseDTO>> buscarRecentes() {
        return ResponseEntity.ok(pedidoService.buscarRecentes());
    }

    // GET - Relatório de pedidos (admin)
    @GetMapping("/relatorio")
    public ResponseEntity<?> gerarRelatorio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        try {
            Object relatorio = pedidoService.gerarRelatorio(inicio, fim);
            return ResponseEntity.ok(relatorio);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // PATCH - Atualizar status do pedido
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

    // DELETE - Cancelar/deletar pedido
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarPedido(@PathVariable Long id) {
        try {
            pedidoService.cancelarPedido(id);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Pedido cancelado com sucesso!",
                    true
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Rastrear pedido
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

    // POST - Atualizar rastreio
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