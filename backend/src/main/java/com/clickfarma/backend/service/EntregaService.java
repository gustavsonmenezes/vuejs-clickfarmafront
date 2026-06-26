package com.clickfarma.backend.service;

import com.clickfarma.backend.model.Entrega;
import com.clickfarma.backend.model.Entrega.StatusEntrega;
import com.clickfarma.backend.model.Entregador;
import com.clickfarma.backend.repository.EntregaRepository;
import com.clickfarma.backend.repository.EntregadorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EntregaService {

    private static final Logger log = LoggerFactory.getLogger(EntregaService.class);

    private final EntregaRepository entregaRepository;
    private final EntregadorRepository entregadorRepository;
    private final TelegramService telegramService;

    public EntregaService(EntregaRepository entregaRepository,
                          EntregadorRepository entregadorRepository,
                          TelegramService telegramService) {
        this.entregaRepository = entregaRepository;
        this.entregadorRepository = entregadorRepository;
        this.telegramService = telegramService;
    }

    @Transactional
    public Entrega criarEntrega(Long pedidoId, String codigoPedido, String mensagem) {
        Entrega entrega = new Entrega(pedidoId, codigoPedido);
        entrega.setMensagemTelegram(mensagem);
        return entregaRepository.save(entrega);
    }

    public void broadcastEntrega(Entrega entrega, String endereco, String farmaciaNome, String farmaciaEndereco) {
        List<Entregador> entregadores = entregadorRepository.findByAtivoTrue();

        if (entregadores.isEmpty()) {
            log.warn("Nenhum entregador ativo cadastrado para broadcast da entrega {}", entrega.getCodigoPedido());
            return;
        }

        String msg = montarMensagemBroadcast(entrega, endereco, farmaciaNome, farmaciaEndereco);

        for (Entregador e : entregadores) {
            try {
                telegramService.enviarMensagem(e.getTelegramChatId(), msg);
                log.info("Broadcast enviado para entregador {} (chat={})", e.getNome(), e.getTelegramChatId());
            } catch (Exception ex) {
                log.error("Erro ao enviar broadcast para entregador {}: {}", e.getNome(), ex.getMessage());
            }
        }
    }

    private String montarMensagemBroadcast(Entrega entrega, String endereco, String farmaciaNome, String farmaciaEndereco) {
        StringBuilder msg = new StringBuilder();
        msg.append("NOVA ENTREGA UBER DIRECT\n");
        msg.append("========================\n\n");
        msg.append("Pedido: #").append(entrega.getCodigoPedido()).append("\n");
        msg.append("Endereço: ").append(endereco).append("\n\n");
        msg.append("Retirar na farmácia: ").append(farmaciaNome).append("\n");
        msg.append("Endereço: ").append(farmaciaEndereco).append("\n\n");
        msg.append("Responda:\n");
        msg.append("/aceitar ").append(entrega.getId()).append(" - Aceitar entrega\n");
        msg.append("/recusar ").append(entrega.getId()).append(" - Recusar entrega");
        return msg.toString();
    }

    @Transactional
    public synchronized String aceitar(Long entregaId, String telegramChatId) {
        Entregador entregador = entregadorRepository.findByAtivoTrue().stream()
                .filter(e -> e.getTelegramChatId().equals(telegramChatId))
                .findFirst()
                .orElse(null);

        if (entregador == null) {
            return "Você não está cadastrado como entregador. Envie /registrar SeuNome";
        }

        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        if (entrega == null) {
            return "Entrega não encontrada.";
        }

        if (entrega.getStatus() != StatusEntrega.PENDENTE) {
            return "Esta entrega já foi " + entrega.getStatus().name().toLowerCase() + " por outro entregador.";
        }

        entrega.setEntregador(entregador);
        entrega.setStatus(StatusEntrega.ACEITA);
        entrega.setAceitoEm(LocalDateTime.now());
        entregaRepository.save(entrega);

        notificarOutrosEntregadores(entrega, entregador);

        log.info("Entrega {} aceita pelo entregador {} (chat={})", entrega.getCodigoPedido(), entregador.getNome(), telegramChatId);
        return "✅ Você aceitou a entrega #" + entrega.getCodigoPedido() + "! Retire na farmácia e faça a entrega.";
    }

    @Transactional
    public String recusar(Long entregaId, String telegramChatId) {
        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        if (entrega == null) {
            return "Entrega não encontrada.";
        }
        if (entrega.getStatus() != StatusEntrega.PENDENTE) {
            return "Esta entrega já foi " + entrega.getStatus().name().toLowerCase() + ".";
        }
        log.info("Entregador {} recusou entrega {}", telegramChatId, entrega.getCodigoPedido());
        return "✅ Você recusou a entrega #" + entrega.getCodigoPedido() + ".";
    }

    private void notificarOutrosEntregadores(Entrega entrega, Entregador aceitou) {
        List<Entregador> outros = entregadorRepository.findByAtivoTrue().stream()
                .filter(e -> !e.getId().equals(aceitou.getId()))
                .toList();

        String msg = "⚠️ Entrega #" + entrega.getCodigoPedido()
                + " já foi aceita por " + aceitou.getNome() + ".";

        for (Entregador e : outros) {
            try {
                telegramService.enviarMensagem(e.getTelegramChatId(), msg);
            } catch (Exception ex) {
                log.error("Erro ao notificar recusa para {}", e.getNome(), ex);
            }
        }
    }

    @Transactional
    public Entregador registrarEntregador(String nome, String telegramChatId) {
        if (entregadorRepository.findByAtivoTrue().stream()
                .anyMatch(e -> e.getTelegramChatId().equals(telegramChatId))) {
            return null;
        }

        Entregador entregador = new Entregador(nome, telegramChatId);
        entregador = entregadorRepository.save(entregador);
        telegramService.enviarMensagem(telegramChatId,
                "✅ Cadastro realizado, " + nome + "! Você agora receberá pedidos de entrega da ClickFarma.");
        log.info("Novo entregador cadastrado: {} (chat={})", nome, telegramChatId);
        return entregador;
    }

    @Transactional
    public void seedFromConfig(String chatId, String nome) {
        if (chatId == null || chatId.isEmpty()) return;
        boolean jaExiste = entregadorRepository.findByAtivoTrue().stream()
                .anyMatch(e -> e.getTelegramChatId().equals(chatId));
        if (!jaExiste) {
            Entregador e = new Entregador(nome, chatId);
            entregadorRepository.save(e);
            log.info("Entregador inicial cadastrado a partir da config: {} (chat={})", nome, chatId);
        }
    }
}
