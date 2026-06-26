package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.*;
import com.clickfarma.backend.service.UberDirectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/uber-direct")
public class UberDirectController {

    @Autowired
    private UberDirectService uberDirectService;

    @PostMapping("/quote")
    public ResponseEntity<?> getQuote(@Valid @RequestBody UberDirectQuoteRequestDTO request) {
        try {
            UberDirectQuoteResponseDTO quote = uberDirectService.getQuote(request.getDropoffAddress());
            return ResponseEntity.ok(quote);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensagemResponseDTO("Erro ao calcular frete: " + e.getMessage(), false));
        }
    }

    @PostMapping("/delivery")
    public ResponseEntity<?> createDelivery(@Valid @RequestBody UberDirectDeliveryRequestDTO request) {
        try {
            UberDirectDeliveryResponseDTO delivery = uberDirectService.createDelivery(
                    request.getQuoteId(),
                    request.getDropoff(),
                    request.getManifestItems(),
                    request.getExternalOrderId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(delivery);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensagemResponseDTO("Erro ao criar entrega: " + e.getMessage(), false));
        }
    }

    @GetMapping("/delivery/{deliveryId}")
    public ResponseEntity<?> getDeliveryStatus(@PathVariable String deliveryId) {
        try {
            UberDirectDeliveryResponseDTO delivery = uberDirectService.getDeliveryStatus(deliveryId);
            return ResponseEntity.ok(delivery);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO("Entrega nao encontrada", false));
        }
    }

    @PostMapping("/delivery/{deliveryId}/cancel")
    public ResponseEntity<?> cancelDelivery(@PathVariable String deliveryId) {
        try {
            uberDirectService.cancelDelivery(deliveryId);
            return ResponseEntity.ok(new MensagemResponseDTO("Entrega cancelada com sucesso", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO("Erro ao cancelar entrega: " + e.getMessage(), false));
        }
    }
}
