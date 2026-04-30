package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.FarmaciaRequestDTO;
import com.clickfarma.backend.dto.FarmaciaResponseDTO;
import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.model.Farmacia;
import com.clickfarma.backend.service.FarmaciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farmacias")
public class FarmaciaController {

    @Autowired
    private FarmaciaService farmaciaService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody FarmaciaRequestDTO dto) {
        try {
            FarmaciaResponseDTO farmacia = farmaciaService.registrarFarmacia(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(farmacia);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @GetMapping
    public ResponseEntity<List<FarmaciaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(farmaciaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(farmaciaService.buscarPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody FarmaciaRequestDTO dto) {
        try {
            FarmaciaResponseDTO farmacia = farmaciaService.atualizar(id, dto);
            return ResponseEntity.ok(farmacia);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            farmaciaService.deletar(id);
            return ResponseEntity.ok(new MensagemResponseDTO("Farmácia deletada com sucesso", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @PatchMapping("/{id}/pix")
    public ResponseEntity<?> atualizarPix(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        try {
            Farmacia f = farmaciaService.buscarEntidade(id);
            if (body.containsKey("chavePix")) f.setChavePix(body.get("chavePix"));
            if (body.containsKey("tipoChavePix")) f.setTipoChavePix(body.get("tipoChavePix"));
            return ResponseEntity.ok(farmaciaService.atualizarEntidade(id, f));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.clickfarma.backend.dto.MensagemResponseDTO(e.getMessage(), false));
        }
    }
}
