package com.clickfarma.backend.controller;

import com.clickfarma.backend.model.AgendamentoRecompra;
import com.clickfarma.backend.repository.AgendamentoRecompraRepository;
import com.clickfarma.backend.service.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recompra" )
public class AgendamentoRecompraController {

    @Autowired
    private AgendamentoRecompraRepository repository;

    @Autowired
    private TelegramService telegramService;

    // Listar agendamentos de um usuário
    @GetMapping("/usuario/{usuarioId}")
    public List<AgendamentoRecompra> listarPorUsuario(@PathVariable Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
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
