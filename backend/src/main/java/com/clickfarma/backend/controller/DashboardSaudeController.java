package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.DashboardSaudeResponseDTO;
import com.clickfarma.backend.dto.TimelinePrevisaoDTO;
import com.clickfarma.backend.service.DashboardSaudeService;
import com.clickfarma.backend.service.TimelinePredicaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard-saude")
public class DashboardSaudeController {

    @Autowired
    private DashboardSaudeService dashboardSaudeService;

    @Autowired
    private TimelinePredicaoService timelinePredicaoService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<DashboardSaudeResponseDTO> getDashboard(@PathVariable Long usuarioId) {
        DashboardSaudeResponseDTO response = dashboardSaudeService.getDashboardSaude(usuarioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/timeline/{usuarioId}")
    public ResponseEntity<TimelinePrevisaoDTO> getTimeline(@PathVariable Long usuarioId) {
        TimelinePrevisaoDTO timeline = timelinePredicaoService.preverTimeline(usuarioId);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/timeline/{usuarioId}/produto/{produtoId}")
    public ResponseEntity<Map<String, Object>> getPrevisaoProduto(
            @PathVariable Long usuarioId,
            @PathVariable Long produtoId) {
        Map<String, Object> previsao = timelinePredicaoService.preverPorProduto(usuarioId, produtoId);
        return ResponseEntity.ok(previsao);
    }
}
