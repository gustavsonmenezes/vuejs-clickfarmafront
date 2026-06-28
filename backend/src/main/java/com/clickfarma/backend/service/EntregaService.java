package com.clickfarma.backend.service;

import com.clickfarma.backend.model.Entrega;
import com.clickfarma.backend.model.Entrega.StatusEntrega;
import com.clickfarma.backend.model.Entregador;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.repository.EntregaRepository;
import com.clickfarma.backend.repository.EntregadorRepository;
import com.clickfarma.backend.repository.PedidoRepository;
import com.clickfarma.backend.service.CorridaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class EntregaService {

    private static final Logger log = LoggerFactory.getLogger(EntregaService.class);

    private final EntregaRepository entregaRepository;
    private final EntregadorRepository entregadorRepository;
    private final PedidoRepository pedidoRepository;
    private final TelegramService telegramService;
    private final CorridaService corridaService;

    @Value("${farmacia.nome:ClickFarma}")
    private String farmaciaNome;

    @Value("${farmacia.endereco:}")
    private String farmaciaEndereco;

    @Value("${farmacia.latitude:}")
    private Double farmaciaLatitude;

    @Value("${farmacia.longitude:}")
    private Double farmaciaLongitude;

    @Value("${uber.direct.taxa-entrega:10.00}")
    private java.math.BigDecimal taxaEntregaPadrao;

    public EntregaService(EntregaRepository entregaRepository,
                          EntregadorRepository entregadorRepository,
                          PedidoRepository pedidoRepository,
                          TelegramService telegramService,
                          CorridaService corridaService) {
        this.entregaRepository = entregaRepository;
        this.entregadorRepository = entregadorRepository;
        this.pedidoRepository = pedidoRepository;
        this.telegramService = telegramService;
        this.corridaService = corridaService;
    }

    @Transactional
    public Entrega criarEntrega(Long pedidoId, String codigoPedido, String mensagem) {
        Entrega nova = new Entrega(pedidoId, codigoPedido);
        nova.setMensagemTelegram(mensagem);

        pedidoRepository.findById(pedidoId).ifPresent(pedido -> {
            nova.setClienteNome(pedido.getUsuario().getNome());
            nova.setClienteTelefone(pedido.getUsuario().getTelefone());
            nova.setEnderecoDestino(pedido.getEnderecoEntrega());
            nova.setTaxaEntrega(taxaEntregaPadrao);
            if (pedido.getValorTotal() != null) {
                nova.setDistanciaKm(java.math.BigDecimal.ZERO);
            }
        });

        nova.setEnderecoOrigem(farmaciaEndereco);
        nova.setLatitudeOrigem(farmaciaLatitude);
        nova.setLongitudeOrigem(farmaciaLongitude);

        Entrega salva = entregaRepository.save(nova);

        corridaService.criarCorrida(salva);

        return salva;
    }

    public void broadcastEntrega(Entrega entrega, String endereco, String farmaciaNome, String farmaciaEndereco) {
        List<Entregador> entregadores = entregadorRepository.findByAtivoTrue().stream()
                .filter(e -> e.getTelegramChatId() != null)
                .toList();

        if (entregadores.isEmpty()) {
            log.warn("Nenhum entregador ativo com Telegram cadastrado para broadcast da entrega {}", entrega.getCodigoPedido());
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
                .filter(e -> Objects.equals(e.getTelegramChatId(), telegramChatId))
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
                .filter(e -> e.getTelegramChatId() != null)
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
                .anyMatch(e -> Objects.equals(e.getTelegramChatId(), telegramChatId))) {
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
                .anyMatch(e -> Objects.equals(e.getTelegramChatId(), chatId));
        if (!jaExiste) {
            Entregador e = new Entregador(nome, chatId);
            entregadorRepository.save(e);
            log.info("Entregador inicial cadastrado a partir da config: {} (chat={})", nome, chatId);
        }
    }
}
