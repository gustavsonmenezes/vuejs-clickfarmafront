package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.ItemPedidoRequestDTO;
import com.clickfarma.backend.dto.PedidoResponseDTO;
import com.clickfarma.backend.dto.UpdateCartItemDTO;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<PedidoResponseDTO> getCart() {
        Pedido cart = cartService.getCart();
        return ResponseEntity.ok(new PedidoResponseDTO(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<PedidoResponseDTO> addItemToCart(@Valid @RequestBody ItemPedidoRequestDTO itemDTO) {
        Pedido cart = cartService.addItemToCart(itemDTO);
        return ResponseEntity.ok(new PedidoResponseDTO(cart));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<PedidoResponseDTO> updateItemInCart(@PathVariable Long productId, @Valid @RequestBody UpdateCartItemDTO itemDTO) {
        Pedido cart = cartService.updateItemInCart(productId, itemDTO);
        return ResponseEntity.ok(new PedidoResponseDTO(cart));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long productId) {
        cartService.removeItemFromCart(productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
