package com.clickfarma.backend.service;

import com.clickfarma.backend.repository.AgendamentoRecompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class RecompraScheduler {

    @Autowired
    private AgendamentoRecompraRepository repository;

    @Autowired
    private TelegramService telegramService;

    @Autowired
    private EmailNotificationService emailNotificationService;

    // Roda todo dia às 09:00 da manhã
    @Scheduled(cron = "0 0 9 * * *")
    public void verificarRecompras() {
        var hoje = LocalDateTime.now();
        var agendamentos = repository.findByDataProximaNotificacaoBeforeAndStatus(hoje, "PENDENTE");

        for (var agendamento : agendamentos) {
            String mensagem = String.format(
                    "*Olá, %s!* 💊\n\nVimos que sua caixa de *%s* está no fim. " +
                            "Queremos garantir que seu tratamento não pare.\n\n" +
                            "Clique aqui para repetir o pedido com *10%% de desconto*! 🚀",
                    agendamento.getUsuario().getNome(),
                    agendamento.getProduto().getNome()
            );

            boolean notificou = false;

            if (agendamento.getUsuario().getTelegramId() != null) {
                try {
                    telegramService.enviarMensagem(agendamento.getUsuario().getTelegramId(), mensagem);
                    notificou = true;
                } catch (Exception e) {
                    // Mantém PENDENTE para nova tentativa no próximo ciclo.
                }
            }

            if (emailNotificationService.enviarLembreteRecompra(agendamento)) {
                notificou = true;
            }

            if (notificou) {
                agendamento.setStatus("ENVIADO");
                repository.save(agendamento);
            }
        }
    }
}
