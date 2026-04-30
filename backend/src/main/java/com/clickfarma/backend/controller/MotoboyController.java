package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.MotoboyRequestDTO;
import com.clickfarma.backend.dto.MotoboyResponseDTO;
import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.service.MotoboyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/motoboys")
public class MotoboyController {

    @Autowired
    private MotoboyService motoboyService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody MotoboyRequestDTO dto) {
        try {
            MotoboyResponseDTO motoboy = motoboyService.registrarMotoboy(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(motoboy);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @GetMapping
    public ResponseEntity<List<MotoboyResponseDTO>> listarTodos() {
        return ResponseEntity.ok(motoboyService.listarTodos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody MotoboyRequestDTO dto) {
        try {
            return ResponseEntity.ok(motoboyService.atualizar(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            motoboyService.deletar(id);
            return ResponseEntity.ok(new MensagemResponseDTO("Motoboy deletado com sucesso", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @PatchMapping("/{id}/pix")
    public ResponseEntity<?> atualizarPix(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        try {
            // Como MotoboyController não tem service injetado da mesma forma, vou usar o repository diretamente se necessário ou o service se existir
            // Verificando se motoboyService tem o método ou se usamos repository
            // Vou assumir que o motoboyService pode buscar.
            return ResponseEntity.ok(motoboyService.atualizarPix(id, body.get("chavePix"), body.get("tipoChavePix")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }
}
