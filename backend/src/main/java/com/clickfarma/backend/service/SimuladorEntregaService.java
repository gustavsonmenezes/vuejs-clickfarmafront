package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.RastreioResponseDTO;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Rastreio;
import com.clickfarma.backend.repository.PedidoRepository;
import com.clickfarma.backend.repository.RastreioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class SimuladorEntregaService {

    private static final Logger log = LoggerFactory.getLogger(SimuladorEntregaService.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> activeSimulation;

    @Autowired
    private RastreioRepository rastreioRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private RastreioStreamService rastreioStreamService;

    public void iniciarSimulacao(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        Rastreio rastreio = pedido.getRastreio();
        if (rastreio == null) throw new RuntimeException("Pedido sem rastreio");

        rastreio.setStatus("EM_TRANSITO");
        rastreio.setTransportadora("ClickFarma Express");
        rastreio.setDataEnvio(LocalDateTime.now());
        rastreio.setDataPrevisaoEntrega(LocalDateTime.now().plusMinutes(15));
        rastreioRepository.save(rastreio);

        double[] origem = { -8.6845, -35.5898 };
        double[] destino = extrairCoordenadas(pedido.getEnderecoEntrega());
        List<double[]> rota = gerarRota(origem, destino, 20);

        if (activeSimulation != null && !activeSimulation.isDone()) {
            activeSimulation.cancel(false);
        }

        activeSimulation = scheduler.scheduleAtFixedRate(new Runnable() {
            int step = 0;
            @Override
            public void run() {
                if (step >= rota.size()) {
                    rastreio.setStatus("ENTREGUE");
                    rastreio.setDataEntregaReal(LocalDateTime.now());
                    rastreio.setLatitude(destino[0]);
                    rastreio.setLongitude(destino[1]);
                    rastreio.setUltimaLocalizacao(pedido.getEnderecoEntrega());
                    rastreioRepository.save(rastreio);

                    pedido.setStatus(Pedido.StatusPedido.ENTREGUE);
                    pedido.setDataAtualizacao(LocalDateTime.now());
                    pedidoRepository.save(pedido);

                    rastreioStreamService.publish(new RastreioResponseDTO(rastreio));
                    activeSimulation.cancel(false);
                    log.info("Entrega simulada finalizada para pedido {}", pedidoId);
                    return;
                }

                double[] ponto = rota.get(step);
                rastreio.setLatitude(ponto[0]);
                rastreio.setLongitude(ponto[1]);
                rastreio.setUltimaLocalizacao(ponto[2] > 0
                        ? String.format("A %.0f metros do destino", ponto[2])
                        : "Saiu do centro de distribuição");
                rastreio.setUltimaAtualizacao(LocalDateTime.now());

                if (step == rota.size() / 2) {
                    rastreio.setStatus("PROXIMO_DA_ENTREGA");
                }

                rastreioRepository.save(rastreio);
                rastreioStreamService.publish(new RastreioResponseDTO(rastreio));
                step++;
            }
        }, 0, 3, TimeUnit.SECONDS);

        log.info("Simulacao iniciada para pedido {} com {} passos", pedidoId, rota.size());
    }

    private double[] extrairCoordenadas(String endereco) {
        if (endereco != null && endereco.toUpperCase().contains("PALMARES")) {
            return new double[]{ -8.6845, -35.5898 };
        }
        return new double[]{ -8.6845, -35.5898 };
    }

    private List<double[]> gerarRota(double[] origem, double[] destino, int passos) {
        List<double[]> rota = new ArrayList<>();
        for (int i = 0; i < passos; i++) {
            double t = (double) i / (passos - 1);
            double lat = origem[0] + (destino[0] - origem[0]) * t + (Math.random() - 0.5) * 0.002;
            double lng = origem[1] + (destino[1] - origem[1]) * t + (Math.random() - 0.5) * 0.002;
            double distanciaRestante = Math.sqrt(
                    Math.pow((destino[0] - lat) * 111000, 2) +
                    Math.pow((destino[1] - lng) * 111000 * Math.cos(Math.toRadians(lat)), 2)
            );
            rota.add(new double[]{ lat, lng, distanciaRestante });
        }
        return rota;
    }
}
