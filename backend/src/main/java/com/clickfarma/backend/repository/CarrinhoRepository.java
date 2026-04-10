package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {
    
    List<Carrinho> findByUsuarioId(Long usuarioId);
    
    Optional<Carrinho> findByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);
    
    void deleteByUsuarioId(Long usuarioId);
}
