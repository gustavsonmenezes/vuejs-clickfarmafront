package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.DeliveryOptionDTO;
import com.clickfarma.backend.dto.DeliveryRequestDTO;
import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Entrega", description = "Cálculo de frete e opções de entrega")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @PostMapping("/calculate")
    @Operation(summary = "Calcular Frete", description = "Retorna as opções de entrega disponíveis para um determinado CEP, baseando-se no valor total do carrinho para conceder frete grátis.")
    public ResponseEntity<?> calculateDelivery(@Valid @RequestBody DeliveryRequestDTO request) {
        try {
            List<DeliveryOptionDTO> options = deliveryService.calculateDeliveryOptions(request);
            return ResponseEntity.ok(options);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MensagemResponseDTO(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MensagemResponseDTO("Erro ao calcular o frete", false));
        }
    }
}
