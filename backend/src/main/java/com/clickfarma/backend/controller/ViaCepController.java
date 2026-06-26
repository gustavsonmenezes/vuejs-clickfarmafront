package com.clickfarma.backend.controller;

import com.clickfarma.backend.service.ViaCepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cep")
@RequiredArgsConstructor
public class ViaCepController {

    private final ViaCepService viaCepService;

    @GetMapping("/{cep}")
    public ResponseEntity<Map<String, String>> buscarCep(@PathVariable String cep) {
        Map<String, String> endereco = viaCepService.buscarEndereco(cep);
        if (endereco.containsKey("erro")) {
            return ResponseEntity.badRequest().body(endereco);
        }
        return ResponseEntity.ok(endereco);
    }
}
