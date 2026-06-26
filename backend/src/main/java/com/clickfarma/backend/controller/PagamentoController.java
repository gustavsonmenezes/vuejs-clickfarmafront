package com.clickfarma.backend.controller;

import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.repository.PedidoRepository;
import com.clickfarma.backend.service.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;
    private final PedidoRepository pedidoRepository;

    @GetMapping("/status/{pedidoId}")
    public ResponseEntity<Map<String, Object>> statusPagamento(@PathVariable Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido nao encontrado"));

        String statusPedido = pedido.getStatus().name();

        if (pedido.getPagamentoMpId() != null
                && pedido.getStatus() == Pedido.StatusPedido.AGUARDANDO_PAGAMENTO) {
            String mpStatus = pagamentoService.consultarStatusPagamento(pedido.getPagamentoMpId());

            if ("approved".equals(mpStatus)) {
                pedido.setStatus(Pedido.StatusPedido.PAGO);
                pedido.setDataAtualizacao(java.time.LocalDateTime.now());
                pedidoRepository.save(pedido);
                statusPedido = "PAGO";
            }
        }

        return ResponseEntity.ok(Map.of(
                "status", statusPedido,
                "pedidoId", pedidoId,
                "codigoPedido", pedido.getCodigoPedido()
        ));
    }
}
