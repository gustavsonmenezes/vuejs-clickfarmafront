package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.RastreioResponseDTO;
import com.clickfarma.backend.model.Entrega;
import com.clickfarma.backend.model.Entrega.StatusEntrega;
import com.clickfarma.backend.model.Rastreio;
import com.clickfarma.backend.repository.EntregaRepository;
import com.clickfarma.backend.repository.RastreioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EntregaTrackingService {

    private static final Logger log = LoggerFactory.getLogger(EntregaTrackingService.class);

    private final EntregaRepository entregaRepository;
    private final RastreioRepository rastreioRepository;
    private final RastreioStreamService rastreioStreamService;

    public EntregaTrackingService(EntregaRepository entregaRepository,
                                   RastreioRepository rastreioRepository,
                                   RastreioStreamService rastreioStreamService) {
        this.entregaRepository = entregaRepository;
        this.rastreioRepository = rastreioRepository;
        this.rastreioStreamService = rastreioStreamService;
    }

    @Transactional
    public void atualizarPosicao(Long entregadorId, Double latitude, Double longitude) {
        Entrega entregaAtiva = entregaRepository
                .findByEntregadorIdAndStatusInOrderByCriadoEmDesc(
                        entregadorId,
                        java.util.List.of(StatusEntrega.EM_ROTA, StatusEntrega.RETIRADA)
                ).stream().findFirst().orElse(null);

        if (entregaAtiva == null) return;

        Rastreio rastreio = rastreioRepository.findByPedidoId(entregaAtiva.getPedidoId()).orElse(null);
        if (rastreio == null) return;

        rastreio.setLatitude(latitude);
        rastreio.setLongitude(longitude);
        rastreio.setUltimaLocalizacao(String.format("%.6f, %.6f", latitude, longitude));
        rastreio.setUltimaAtualizacao(LocalDateTime.now());
        rastreioRepository.save(rastreio);

        rastreioStreamService.publish(new RastreioResponseDTO(rastreio));

        entregaAtiva.setLatitudeOrigem(latitude);
        entregaAtiva.setLongitudeOrigem(longitude);
        entregaRepository.save(entregaAtiva);

        log.debug("Tracking: entregador {} atualizou posição para {},{}", entregadorId, latitude, longitude);
    }
}
