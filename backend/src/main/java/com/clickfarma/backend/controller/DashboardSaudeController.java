package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.DashboardSaudeResponseDTO;
import com.clickfarma.backend.service.DashboardSaudeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard-saude")
public class DashboardSaudeController {

    @Autowired
    private DashboardSaudeService dashboardSaudeService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<DashboardSaudeResponseDTO> getDashboard(@PathVariable Long usuarioId) {
        DashboardSaudeResponseDTO response = dashboardSaudeService.getDashboardSaude(usuarioId);
        return ResponseEntity.ok(response);
    }
}
