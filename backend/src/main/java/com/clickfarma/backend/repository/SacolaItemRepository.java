package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.SacolaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SacolaItemRepository extends JpaRepository<SacolaItem, Long> {
    List<SacolaItem> findByUsuarioId(Long usuarioId);
    Optional<SacolaItem> findByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);
    void deleteByUsuarioId(Long usuarioId);
}
