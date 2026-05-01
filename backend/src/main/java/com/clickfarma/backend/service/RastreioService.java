package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.AtualizacaoRastreioDTO;
import com.clickfarma.backend.dto.RastreioRequestDTO;
import com.clickfarma.backend.dto.RastreioResponseDTO;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Rastreio;
import com.clickfarma.backend.repository.PedidoRepository;
import com.clickfarma.backend.repository.RastreioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RastreioService {

    @Autowired
    private RastreioRepository rastreioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RastreioStreamService rastreioStreamService;

    // Criar novo rastreio
    @Transactional
    public RastreioResponseDTO criarRastreio(RastreioRequestDTO requestDTO) {
        // Verificar se pedido existe
        Pedido pedido = pedidoRepository.findById(requestDTO.getPedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + requestDTO.getPedidoId()));

        // Verificar se já existe rastreio para este pedido
        if (rastreioRepository.existsByPedidoId(requestDTO.getPedidoId())) {
            throw new RuntimeException("Já existe um rastreio para este pedido");
        }

        Rastreio rastreio = new Rastreio(pedido);
        rastreio.setTransportadora(requestDTO.getTransportadora());
        rastreio.setDataEnvio(LocalDateTime.now());
        rastreio.setDataPrevisaoEntrega(requestDTO.getDataPrevisaoEntrega());
        rastreio.setStatus("EM_TRANSITO");

        // Atualizar status do pedido
        pedido.setStatus(Pedido.StatusPedido.ENVIADO);
        pedidoRepository.save(pedido);

        Rastreio rastreioSalvo = rastreioRepository.save(rastreio);
        RastreioResponseDTO dto = new RastreioResponseDTO(rastreioSalvo);
        rastreioStreamService.publish(dto);
        return dto;
    }

    // Buscar rastreio por ID
    public RastreioResponseDTO buscarPorId(Long id) {
        Rastreio rastreio = rastreioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rastreio não encontrado: " + id));
        return new RastreioResponseDTO(rastreio);
    }

    // Buscar rastreio por código
    public RastreioResponseDTO buscarPorCodigo(String codigo) {
        Rastreio rastreio = rastreioRepository.findByCodigoRastreio(codigo)
                .orElseThrow(() -> new RuntimeException("Rastreio não encontrado com código: " + codigo));
        return new RastreioResponseDTO(rastreio);
    }

    // Buscar rastreio por pedido
    public RastreioResponseDTO buscarPorPedidoId(Long pedidoId) {
        Rastreio rastreio = rastreioRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Rastreio não encontrado para o pedido: " + pedidoId));
        return new RastreioResponseDTO(rastreio);
    }

    // Listar todos os rastreios
    public List<RastreioResponseDTO> listarTodos() {
        return rastreioRepository.findAll()
                .stream()
                .map(RastreioResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Atualizar informações do rastreio
    @Transactional
    public RastreioResponseDTO atualizarRastreio(Long id, AtualizacaoRastreioDTO atualizacao) {
        Rastreio rastreio = rastreioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rastreio não encontrado: " + id));

        rastreio.setStatus(atualizacao.getStatus());
        rastreio.setUltimaLocalizacao(atualizacao.getLocalizacao());
        rastreio.setUltimaAtualizacao(LocalDateTime.now());

        // Se status for ENTREGUE, atualizar data de entrega
        if ("ENTREGUE".equalsIgnoreCase(atualizacao.getStatus())) {
            rastreio.setDataEntregaReal(LocalDateTime.now());

            // Atualizar status do pedido
            Pedido pedido = rastreio.getPedido();
            pedido.setStatus(Pedido.StatusPedido.ENTREGUE);
            pedidoRepository.save(pedido);
        }

        Rastreio rastreioAtualizado = rastreioRepository.save(rastreio);
        RastreioResponseDTO dto = new RastreioResponseDTO(rastreioAtualizado);
        rastreioStreamService.publish(dto);
        return dto;
    }

    // Deletar rastreio
    @Transactional
    public void deletarRastreio(Long id) {
        if (!rastreioRepository.existsById(id)) {
            throw new RuntimeException("Rastreio não encontrado: " + id);
        }
        rastreioRepository.deleteById(id);
    }
}

