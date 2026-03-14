package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.ItemPedidoRequestDTO;
import com.clickfarma.backend.dto.UpdateCartItemDTO;
import com.clickfarma.backend.model.*;
import com.clickfarma.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Pedido getCart() {
        Usuario usuario = getCurrentUser();
        return pedidoRepository.findFirstByUsuarioAndStatusOrderByDataPedidoDesc(usuario, Pedido.StatusPedido.CARRINHO)
                .orElseGet(() -> {
                    Pedido newCart = new Pedido(usuario);
                    return pedidoRepository.save(newCart);
                });
    }

    @Transactional
    public Pedido addItemToCart(ItemPedidoRequestDTO itemDTO) {
        Usuario usuario = getCurrentUser();
        Pedido cart = getCart();

        Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Optional<ItemPedido> existingItem = itemPedidoRepository.findByPedidoAndProdutoId(cart, produto.getId());

        if (existingItem.isPresent()) {
            ItemPedido item = existingItem.get();
            item.setQuantidade(item.getQuantidade() + itemDTO.getQuantidade());
            itemPedidoRepository.save(item);
        } else {
            ItemPedido newItem = new ItemPedido(produto, itemDTO.getQuantidade());
            cart.adicionarItem(newItem);
        }

        return pedidoRepository.save(cart);
    }

    @Transactional
    public Pedido updateItemInCart(Long produtoId, UpdateCartItemDTO itemDTO) {
        Usuario usuario = getCurrentUser();
        Pedido cart = getCart();

        ItemPedido item = itemPedidoRepository.findByPedidoAndProdutoId(cart, produtoId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado no carrinho"));

        item.setQuantidade(itemDTO.getQuantidade());
        return pedidoRepository.save(cart);
    }

    @Transactional
    public void removeItemFromCart(Long produtoId) {
        Usuario usuario = getCurrentUser();
        Pedido cart = getCart();

        ItemPedido item = itemPedidoRepository.findByPedidoAndProdutoId(cart, produtoId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado no carrinho"));

        cart.removerItem(item);
        itemPedidoRepository.delete(item);
        pedidoRepository.save(cart);
    }

    @Transactional
    public void clearCart() {
        Usuario usuario = getCurrentUser();
        Pedido cart = getCart();
        pedidoRepository.delete(cart);
    }

    private Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Nenhum usuário autenticado encontrado.");
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + email));
    }
}
