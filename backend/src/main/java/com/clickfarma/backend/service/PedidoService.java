package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.MedicamentoExtraidoDTO;
import com.clickfarma.backend.dto.PedidoRequestDTO;
import com.clickfarma.backend.dto.PedidoResponseDTO;
import com.clickfarma.backend.dto.ItemPedidoRequestDTO;
import com.clickfarma.backend.dto.RastreioResponseDTO;
import com.clickfarma.backend.dto.UberDirectContactDTO;
import com.clickfarma.backend.dto.UberDirectDeliveryResponseDTO;
import com.clickfarma.backend.dto.UberDirectManifestItemDTO;
import com.clickfarma.backend.model.AgendamentoRecompra;
import com.clickfarma.backend.model.ItemPedido;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.model.Rastreio;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.AgendamentoRecompraRepository;
import com.clickfarma.backend.repository.ItemPedidoRepository;
import com.clickfarma.backend.repository.PedidoRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.clickfarma.backend.repository.RastreioRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PedidoService.class);

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private RastreioRepository rastreioRepository;

    @Autowired
    private RastreioStreamService rastreioStreamService;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private AgendamentoRecompraRepository agendamentoRecompraRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private TelegramService telegramService;

    @Autowired
    private WhatsAppCloudService whatsAppCloudService;

    @Autowired
    private UberDirectService uberDirectService;

    @Autowired
    private EntregaService entregaService;

    @Value("${telegram.entregador.chat-id}")
    private String entregadorChatId;

    @Value("${farmacia.nome:ClickFarma - Matriz}")
    private String farmaciaNome;

    /**
     * Cria o pedido normal (sem considerar agendamento de recompra).
     * Se quiser já criar os agendamentos, use o método
     * criarPedidoComAgendamentos abaixo.
     */
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO pedidoDTO) {
        Usuario usuario = usuarioRepository.findById(pedidoDTO.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Pedido pedido = new Pedido(usuario);
        pedido.setMetodoPagamento(pedidoDTO.getMetodoPagamento());
        pedido.setEnderecoEntrega(pedidoDTO.getEnderecoEntrega() != null ?
                pedidoDTO.getEnderecoEntrega() : usuario.getEndereco());
        pedido.setObservacoes(pedidoDTO.getObservacoes());
        pedido.setFarmaciaId(pedidoDTO.getFarmaciaId());
        if (pedidoDTO.getValorFrete() != null) {
            pedido.setValorFrete(BigDecimal.valueOf(pedidoDTO.getValorFrete()));
        }
        pedido.setStatus(Pedido.StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setDataAtualizacao(LocalDateTime.now());

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemPedidoRequestDTO itemDTO : pedidoDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.getProdutoId()));

            if (produto.getEstoque() < itemDTO.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
            }

            ItemPedido item = new ItemPedido(produto, itemDTO.getQuantidade());
            item.setPedido(pedidoSalvo);

            produto.setEstoque(produto.getEstoque() - itemDTO.getQuantidade());
            produtoRepository.save(produto);

            itemPedidoRepository.save(item);
            valorTotal = valorTotal.add(item.getSubtotal());
        }

        pedidoSalvo.setValorTotal(valorTotal);
        pedidoSalvo = pedidoRepository.save(pedidoSalvo);

        String metodo = pedidoDTO.getMetodoPagamento() != null
                ? pedidoDTO.getMetodoPagamento().trim().toUpperCase(Locale.ROOT)
                : "";

        String linkPagamento = null;
        String pixQrCodeBase64 = null;
        String pixCopiaECola = null;
        String pixExpiracao = null;

        if ("MERCADO_PAGO".equals(metodo)) {
            linkPagamento = pagamentoService.criarLinkPagamento(valorTotal.doubleValue(), pedidoSalvo.getId());
        } else if ("PIX".equals(metodo)) {
            PagamentoService.CriarPixResponse pix = pagamentoService.criarPagamentoPix(
                    valorTotal.doubleValue(),
                    pedidoSalvo.getId(),
                    usuario.getEmail());
            pixQrCodeBase64 = pix.qrCodeBase64();
            pixCopiaECola = pix.copiaECola();
            pixExpiracao = pix.expiracao();
            pedidoSalvo.setPagamentoMpId(pix.pagamentoId());
            pedidoRepository.save(pedidoSalvo);
        } else {
            finalizarPedidoSimulado(pedidoSalvo, pedidoDTO.getTipoEntrega(), pedidoDTO.getUberQuoteId());
        }

        PedidoResponseDTO responseDTO = new PedidoResponseDTO(pedidoSalvo);
        responseDTO.setLinkPagamento(linkPagamento);
        responseDTO.setPixQrCodeBase64(pixQrCodeBase64);
        responseDTO.setPixCopiaECola(pixCopiaECola);
        responseDTO.setPixExpiracao(pixExpiracao);
        responseDTO.setWhatsappLink(whatsAppService.gerarLinkCompartilhar(pedidoSalvo));

        emailNotificationService.enviarConfirmacaoPedido(usuario, pedidoSalvo, linkPagamento);

        return responseDTO;
    }

    private void finalizarPedidoSimulado(Pedido pedido, String tipoEntrega, String uberQuoteId) {
        if (pedido == null) return;

        pedido.setStatus(Pedido.StatusPedido.PAGO);
        pedido.setDataAtualizacao(LocalDateTime.now());
        pedidoRepository.save(pedido);

        if (pedido.getRastreio() == null) {
            Rastreio rastreio = new Rastreio(pedido);

            if ("uber_direct".equals(tipoEntrega) && uberQuoteId != null) {
                rastreio.setTransportadora("Uber Direct");
                rastreio.setStatus("SAIU_PARA_ENTREGA");
                rastreio.setUltimaLocalizacao("Farmácia " + farmaciaNome);
            } else {
                rastreio.setTransportadora("ClickFarma Express");
                rastreio.setStatus("EM_TRANSITO");
                rastreio.setUltimaLocalizacao("Centro de Distribuição");
            }

            rastreio.setDataEnvio(LocalDateTime.now());
            rastreio.setDataPrevisaoEntrega(LocalDateTime.now().plusDays(5));
            rastreio.setUltimaAtualizacao(LocalDateTime.now());
            rastreio = rastreioRepository.save(rastreio);
            pedido.setRastreio(rastreio);

            pedido.setStatus(Pedido.StatusPedido.ENVIADO);
            pedido.setDataAtualizacao(LocalDateTime.now());
            pedidoRepository.save(pedido);
        }

        if ("uber_direct".equals(tipoEntrega) && uberQuoteId != null) {
            criarEntregaUberDirect(pedido, uberQuoteId);
        } else {
            notificarEntregador(pedido);
        }
    }

    private void criarEntregaUberDirect(Pedido pedido, String uberQuoteId) {
        try {
            List<UberDirectManifestItemDTO> itens = pedido.getItens().stream().map(item -> {
                UberDirectManifestItemDTO dto = new UberDirectManifestItemDTO();
                dto.setName(item.getProduto().getNome());
                dto.setQuantity(item.getQuantidade());
                dto.setSize("small");
                return dto;
            }).toList();

            UberDirectContactDTO dropoff = new UberDirectContactDTO();
            dropoff.setName(pedido.getUsuario().getNome());
            dropoff.setAddress(pedido.getEnderecoEntrega());
            dropoff.setPhone(pedido.getUsuario().getTelefone());

            UberDirectDeliveryResponseDTO delivery = uberDirectService.createDelivery(
                    uberQuoteId, dropoff, itens, String.valueOf(pedido.getId()));

            String obs = pedido.getObservacoes() != null ? pedido.getObservacoes() : "";
            obs += " [UberDirect: id=" + delivery.getId();
            if (delivery.getTrackingUrl() != null) {
                obs += " tracking=" + delivery.getTrackingUrl();
            }
            obs += "]";
            pedido.setObservacoes(obs);
            pedidoRepository.save(pedido);

            log.info("Entrega Uber Direct criada para pedido {}: {}", pedido.getCodigoPedido(), delivery.getId());
        } catch (Exception e) {
            log.error("Erro ao criar entrega Uber Direct para pedido {}", pedido.getCodigoPedido(), e);
        }
    }

    @Transactional
    public void processarPagamentoAprovado(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
        if (pedido == null) return;

        pedido.setStatus(Pedido.StatusPedido.PAGO);
        pedido.setDataAtualizacao(LocalDateTime.now());
        pedidoRepository.save(pedido);

        notificarEntregador(pedido);
    }

    private void notificarEntregador(Pedido pedido) {
        try {
            String itensStr = pedido.getItens().stream()
                    .map(i -> i.getProduto().getNome() + " x" + i.getQuantidade())
                    .collect(java.util.stream.Collectors.joining(", "));

            entregaService.criarEntrega(pedido.getId(), pedido.getCodigoPedido(), itensStr);

            StringBuilder msg = new StringBuilder();
            msg.append("Novo Pedido para Entrega!\n\n");
            msg.append("Pedido: #").append(pedido.getCodigoPedido()).append("\n");
            msg.append("Cliente: ").append(pedido.getUsuario().getNome()).append("\n");
            msg.append("Endereço: ").append(pedido.getEnderecoEntrega()).append("\n");
            msg.append("Valor: R$ ").append(pedido.getValorTotal()).append("\n\n");
            msg.append("✅ Prepare o pedido para entrega!");

            telegramService.enviarMensagem(entregadorChatId, msg.toString());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(PedidoService.class).warn("Erro ao notificar entregador via Telegram", e);
        }
    }

    /**
     * Versão de criação de pedido que já recebe os medicamentos extraídos
     * da receita e cria automaticamente os agendamentos de recompra.
     */
    @Transactional
    public PedidoResponseDTO criarPedidoComAgendamentos(
            PedidoRequestDTO pedidoDTO,
            List<MedicamentoExtraidoDTO.MedicamentoItem> itensExtraidos
    ) {
        // Reaproveita toda a lógica de criação do pedido normal
        PedidoResponseDTO responseDTO = criarPedido(pedidoDTO);

        // Busca o pedido recém-criado (já vem no DTO, mas pegamos do banco para garantir consistência)
        Pedido pedido = pedidoRepository.findById(responseDTO.getId())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado após criação"));

        // Cria os agendamentos de recompra com base nos itens extraídos
        finalizarPedido(pedido, itensExtraidos);

        return responseDTO;
    }

    /**
     * Lógica para salvar automaticamente os agendamentos de recompra
     * no momento da compra.
     */
    @Transactional
    public void finalizarPedido(Pedido pedido, List<MedicamentoExtraidoDTO.MedicamentoItem> itensExtraidos) {
        if (itensExtraidos == null || itensExtraidos.isEmpty()) {
            return;
        }

        for (MedicamentoExtraidoDTO.MedicamentoItem item : itensExtraidos) {
            if (item.getDiasDuracao() != null && item.getProdutoId() != null) {
                AgendamentoRecompra agendamento = new AgendamentoRecompra();
                agendamento.setUsuario(pedido.getUsuario());
                agendamento.setProduto(
                        produtoRepository.findById(item.getProdutoId())
                                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + item.getProdutoId()))
                );
                agendamento.setPedido(pedido);
                agendamento.setPosologiaTexto(item.getPosologia());
                agendamento.setDiasDuracao(item.getDiasDuracao());
                agendamento.setStatus("PENDENTE");

                // Calcula a data: Hoje + Dias de Duração - 3 dias de antecedência
                agendamento.setDataProximaNotificacao(
                        LocalDateTime.now().plusDays(item.getDiasDuracao()).minusDays(3)
                );

                agendamentoRecompraRepository.save(agendamento);
            }
        }
    }

    public List<PedidoResponseDTO> listarTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public PedidoResponseDTO buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
        return new PedidoResponseDTO(pedido);
    }

    public List<PedidoResponseDTO> buscarPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<Pedido> findPedidosComItensParaWellness(Long usuarioId) {
        return pedidoRepository.findPedidosComItensByUsuario(usuarioId);
    }

    public PedidoResponseDTO buscarPorCodigo(String codigo) {
        Pedido pedido = pedidoRepository.findByCodigoPedidoWithRastreio(codigo);
        if (pedido == null) {
            throw new RuntimeException("Pedido não encontrado com código: " + codigo);
        }
        return new PedidoResponseDTO(pedido);
    }

    @Transactional
    public PedidoResponseDTO atualizarStatus(Long id, String status) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        try {
            pedido.setStatus(Pedido.StatusPedido.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status inválido: " + status);
        }

        pedido.setDataAtualizacao(LocalDateTime.now());

        if (status.equals("ENVIADO") && pedido.getRastreio() == null) {
            Rastreio rastreio = new Rastreio(pedido);
            rastreio.setTransportadora("ClickFarma Express");
            rastreio.setDataEnvio(LocalDateTime.now());
            rastreio.setDataPrevisaoEntrega(LocalDateTime.now().plusDays(5));
            rastreio.setStatus("EM_TRANSITO");
            rastreio.setLatitude(-8.047562);
            rastreio.setLongitude(-34.877003);
            rastreio.setUltimaLocalizacao("Centro de Distribuição ClickFarma - Recife");

            Rastreio rastreioSalvo = rastreioRepository.save(rastreio);
            rastreioStreamService.publish(new RastreioResponseDTO(rastreioSalvo));
        }

        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        emailNotificationService.enviarAtualizacaoStatusPedido(pedidoAtualizado.getUsuario(), pedidoAtualizado);

        PedidoResponseDTO responseDTO = new PedidoResponseDTO(pedidoAtualizado);

        try {
            WhatsAppCloudService.EnvioResult whatsResult = whatsAppCloudService
                .enviarNotificacaoStatus(pedidoAtualizado);
            if (whatsResult.isSucesso()) {
                log.info("WhatsApp status enviado para {}", pedidoAtualizado.getUsuario().getTelefone());
            }
            responseDTO.setWhatsappEnviado(whatsResult.isSucesso());
            responseDTO.setWhatsappMensagem(whatsResult.getMensagem());
        } catch (Exception e) {
            log.warn("Erro ao enviar WhatsApp status: {}", e.getMessage());
            responseDTO.setWhatsappEnviado(false);
            responseDTO.setWhatsappMensagem("Erro: " + e.getMessage());
        }

        responseDTO.setWhatsappLink(whatsAppService.gerarLinkStatusPedido(pedidoAtualizado));
        return responseDTO;
    }

    @Transactional
    public RastreioResponseDTO rastrearPedido(String codigoPedido) {
        Pedido pedido = pedidoRepository.findByCodigoPedidoWithRastreio(codigoPedido);
        if (pedido == null) {
            throw new RuntimeException("Pedido não encontrado");
        }

        if (pedido.getRastreio() == null) {
            throw new RuntimeException("Pedido ainda não foi enviado");
        }

        return new RastreioResponseDTO(pedido.getRastreio());
    }

    @Transactional
    public RastreioResponseDTO atualizarRastreio(Long pedidoId, String localizacao, String status,
                                                  Double latitude, Double longitude) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getRastreio() == null) {
            throw new RuntimeException("Pedido não possui rastreio");
        }

        Rastreio rastreio = pedido.getRastreio();
        rastreio.setUltimaLocalizacao(localizacao);
        rastreio.setStatus(status);
        rastreio.setUltimaAtualizacao(LocalDateTime.now());
        if (latitude != null) rastreio.setLatitude(latitude);
        if (longitude != null) rastreio.setLongitude(longitude);

        if (status.equals("ENTREGUE")) {
            rastreio.setDataEntregaReal(LocalDateTime.now());
            pedido.setStatus(Pedido.StatusPedido.ENTREGUE);
        }

        rastreioRepository.save(rastreio);
        pedidoRepository.save(pedido);

        RastreioResponseDTO dto = new RastreioResponseDTO(rastreio);
        rastreioStreamService.publish(dto);
        return dto;
    }

    public List<PedidoResponseDTO> buscarPorStatus(String status) {
        try {
            Pedido.StatusPedido statusEnum = Pedido.StatusPedido.valueOf(status);
            return pedidoRepository.findByStatus(statusEnum)
                    .stream()
                    .map(PedidoResponseDTO::new)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status inválido: " + status);
        }
    }

    public List<PedidoResponseDTO> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return pedidoRepository.findByDataPedidoBetween(inicio, fim)
                .stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<PedidoResponseDTO> buscarRecentes() {
        return pedidoRepository.findTop10ByOrderByDataPedidoDesc()
                .stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getStatus() == Pedido.StatusPedido.ENTREGUE) {
            throw new RuntimeException("Pedido já entregue não pode ser cancelado");
        }

        if (pedido.getStatus() == Pedido.StatusPedido.CANCELADO) {
            throw new RuntimeException("Pedido já está cancelado");
        }

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produto.setEstoque(produto.getEstoque() + item.getQuantidade());
            produtoRepository.save(produto);
        }

        pedido.setStatus(Pedido.StatusPedido.CANCELADO);
        pedido.setDataAtualizacao(LocalDateTime.now());
        pedidoRepository.save(pedido);
    }

    public Map<String, Object> gerarRelatorio(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null) {
            inicio = LocalDateTime.now().minusMonths(1);
        }
        if (fim == null) {
            fim = LocalDateTime.now();
        }

        List<Pedido> pedidos = pedidoRepository.findByDataPedidoBetween(inicio, fim);

        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("periodo", inicio + " até " + fim);
        relatorio.put("totalPedidos", pedidos.size());

        BigDecimal valorTotal = pedidos.stream()
                .map(Pedido::getValorTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        relatorio.put("valorTotal", valorTotal);

        Map<String, Long> contagemStatus = pedidos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStatus() != null ? p.getStatus().name() : "SEM_STATUS",
                        Collectors.counting()
                ));
        relatorio.put("contagemStatus", contagemStatus);

        if (!pedidos.isEmpty()) {
            BigDecimal media = valorTotal.divide(BigDecimal.valueOf(pedidos.size()), 2, BigDecimal.ROUND_HALF_UP);
            relatorio.put("mediaValor", media);
        }

        return relatorio;
    }
}
