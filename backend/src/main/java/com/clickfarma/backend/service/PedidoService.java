package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.*;
import com.clickfarma.backend.model.*;
import com.clickfarma.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

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

    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO pedidoDTO) {
        // Buscar usuário
        Usuario usuario = usuarioRepository.findById(pedidoDTO.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Criar pedido
        Pedido pedido = new Pedido(usuario);
        pedido.setMetodoPagamento(pedidoDTO.getMetodoPagamento());
        pedido.setEnderecoEntrega(pedidoDTO.getEnderecoEntrega() != null ?
                pedidoDTO.getEnderecoEntrega() : usuario.getEndereco());
        pedido.setObservacoes(pedidoDTO.getObservacoes());

        // Salvar pedido primeiro
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Processar itens
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemPedidoRequestDTO itemDTO : pedidoDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.getProdutoId()));

            // Verificar estoque
            if (produto.getEstoque() < itemDTO.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
            }

            // Criar item
            ItemPedido item = new ItemPedido(produto, itemDTO.getQuantidade());
            item.setPedido(pedidoSalvo);

            // Atualizar estoque
            produto.setEstoque(produto.getEstoque() - itemDTO.getQuantidade());
            produtoRepository.save(produto);

            itemPedidoRepository.save(item);
            valorTotal = valorTotal.add(item.getSubtotal());
        }

        // Atualizar valor total do pedido
        pedidoSalvo.setValorTotal(valorTotal);
        pedidoSalvo = pedidoRepository.save(pedidoSalvo);

        return new PedidoResponseDTO(pedidoSalvo);
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

        // Se status for ENVIADO, criar rastreio automaticamente
        if (status.equals("ENVIADO") && pedido.getRastreio() == null) {
            Rastreio rastreio = new Rastreio(pedido);
            rastreio.setTransportadora("ClickFarma Express");
            rastreio.setDataEnvio(LocalDateTime.now());
            rastreio.setDataPrevisaoEntrega(LocalDateTime.now().plusDays(5));
            rastreio.setStatus("EM_TRANSITO");

            rastreioRepository.save(rastreio);
        }

        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        return new PedidoResponseDTO(pedidoAtualizado);
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
    public RastreioResponseDTO atualizarRastreio(Long pedidoId, String localizacao, String status) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getRastreio() == null) {
            throw new RuntimeException("Pedido não possui rastreio");
        }

        Rastreio rastreio = pedido.getRastreio();
        rastreio.setUltimaLocalizacao(localizacao);
        rastreio.setStatus(status);
        rastreio.setUltimaAtualizacao(LocalDateTime.now());

        if (status.equals("ENTREGUE")) {
            rastreio.setDataEntregaReal(LocalDateTime.now());
            pedido.setStatus(Pedido.StatusPedido.ENTREGUE);
        }

        rastreioRepository.save(rastreio);
        pedidoRepository.save(pedido);

        return new RastreioResponseDTO(rastreio);
    }
}

