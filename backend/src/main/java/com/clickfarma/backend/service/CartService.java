package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.ItemPedidoRequestDTO;
import com.clickfarma.backend.dto.UpdateCartItemDTO;
import com.clickfarma.backend.model.Carrinho;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.CarrinhoRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Carrinho> getCart() {
        Usuario usuario = getCurrentUser();
        return carrinhoRepository.findByUsuarioId(usuario.getId());
    }

    @Transactional
    public List<Carrinho> addItemToCart(ItemPedidoRequestDTO itemDTO) {
        Usuario usuario = getCurrentUser();

        Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Optional<Carrinho> existingItem = carrinhoRepository.findByUsuarioIdAndProdutoId(usuario.getId(), produto.getId());

        if (existingItem.isPresent()) {
            Carrinho item = existingItem.get();
            item.setQuantidade(item.getQuantidade() + itemDTO.getQuantidade());
            carrinhoRepository.save(item);
        } else {
            Carrinho newItem = new Carrinho(usuario, produto, itemDTO.getQuantidade());
            carrinhoRepository.save(newItem);
        }

        return getCart();
    }

    @Transactional
    public List<Carrinho> updateItemInCart(Long produtoId, UpdateCartItemDTO itemDTO) {
        Usuario usuario = getCurrentUser();

        Carrinho item = carrinhoRepository.findByUsuarioIdAndProdutoId(usuario.getId(), produtoId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado no carrinho"));

        item.setQuantidade(itemDTO.getQuantidade());
        carrinhoRepository.save(item);
        
        return getCart();
    }

    @Transactional
    public void removeItemFromCart(Long produtoId) {
        Usuario usuario = getCurrentUser();

        Carrinho item = carrinhoRepository.findByUsuarioIdAndProdutoId(usuario.getId(), produtoId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado no carrinho"));

        carrinhoRepository.delete(item);
    }

    @Transactional
    public void clearCart() {
        Usuario usuario = getCurrentUser();
        carrinhoRepository.deleteByUsuarioId(usuario.getId());
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
