package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.RastreioResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RastreioStreamService {

    private static final long DEFAULT_TIMEOUT_MS = Duration.ofMinutes(30).toMillis();

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emittersByPedidoId = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long pedidoId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        emittersByPedidoId.computeIfAbsent(pedidoId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(pedidoId, emitter));
        emitter.onTimeout(() -> remove(pedidoId, emitter));
        emitter.onError(ex -> remove(pedidoId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("ok", MediaType.TEXT_PLAIN));
        } catch (IOException e) {
            remove(pedidoId, emitter);
        }

        return emitter;
    }

    public void publish(RastreioResponseDTO dto) {
        if (dto == null || dto.getPedidoId() == null) return;

        Long pedidoId = dto.getPedidoId();
        List<SseEmitter> emitters = emittersByPedidoId.get(pedidoId);
        if (emitters == null || emitters.isEmpty()) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("rastreio")
                        .data(dto, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                remove(pedidoId, emitter);
            }
        }
    }

    private void remove(Long pedidoId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByPedidoId.get(pedidoId);
        if (emitters == null) return;
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByPedidoId.remove(pedidoId);
        }
    }
}

