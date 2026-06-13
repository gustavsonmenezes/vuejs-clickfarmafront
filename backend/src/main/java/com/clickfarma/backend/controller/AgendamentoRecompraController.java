package com.clickfarma.backend.controller;

import com.clickfarma.backend.model.AgendamentoRecompra;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.AgendamentoRecompraRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import com.clickfarma.backend.service.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recompra")
public class AgendamentoRecompraController {

    @Autowired
    private AgendamentoRecompraRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private TelegramService telegramService;

    // Listar agendamentos de um usuário
    @GetMapping("/usuario/{usuarioId}")
    public List<AgendamentoRecompra> listarPorUsuario(@PathVariable Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    // Criar agendamento de recompra
    @PostMapping
    public ResponseEntity<?> criarAgendamento(@RequestBody Map<String, Object> body) {
        try {
            Long usuarioId = Long.valueOf(body.get("usuarioId").toString());
            Long produtoId = Long.valueOf(body.get("produtoId").toString());

            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            Produto produto = produtoRepository.findById(produtoId)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            AgendamentoRecompra agendamento = new AgendamentoRecompra();
            agendamento.setUsuario(usuario);
            agendamento.setProduto(produto);

            String posologia = body.getOrDefault("posologiaTexto", "").toString();
            Integer diasDuracao = body.get("diasDuracao") != null
                    ? Integer.valueOf(body.get("diasDuracao").toString()) : 30;

            agendamento.setPosologiaTexto(posologia);
            agendamento.setDiasDuracao(diasDuracao);
            agendamento.setDataInicio(LocalDateTime.now());
            agendamento.setDataProximaNotificacao(LocalDateTime.now().plusDays(diasDuracao).minusDays(3));
            agendamento.setStatus("PENDENTE");

            AgendamentoRecompra salvo = repository.save(agendamento);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", salvo.getId());
            response.put("status", "PENDENTE");
            response.put("mensagem", "Recompra agendada com sucesso para " + produto.getNome());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // Cancelar um agendamento
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarAgendamento(@PathVariable Long id) {
        return repository.findById(id).map(agendamento -> {
            agendamento.setStatus("CANCELADO");
            repository.save(agendamento);
            return ResponseEntity.ok().body("Agendamento cancelado com sucesso.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // Rota de Teste: Forçar o envio de uma notificação agora
    @PostMapping("/{id}/testar-envio")
    public ResponseEntity<?> testarEnvio(@PathVariable Long id) {
        return repository.findById(id).map(agendamento -> {
            String mensagem = "🧪 *TESTE DE RECOMPRA*\n\nOlá! Este é um teste do sistema ClickFarma para o medicamento: " + agendamento.getProduto().getNome();

            if (agendamento.getUsuario().getTelegramId() != null) {
                try {
                    String respostaTelegram = telegramService.enviarMensagem(agendamento.getUsuario().getTelegramId(), mensagem);
                    return ResponseEntity.ok().body(respostaTelegram);
                } catch (Exception e) {
                    return ResponseEntity.internalServerError()
                            .body("Falha ao enviar mensagem de teste: " + e.getMessage());
                }
            }

            return ResponseEntity.badRequest().body("Usuário não possui Telegram ID vinculado.");
        }).orElse(ResponseEntity.notFound().build());
    }
}
