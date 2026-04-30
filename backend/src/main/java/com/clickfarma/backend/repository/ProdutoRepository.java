package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;  // ← ADICIONE ESTA IMPORT

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByCategoriaId(Long categoriaId);

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findByPrecoBetween(BigDecimal precoMin, BigDecimal precoMax);

    List<Produto> findByEstoqueLessThan(Integer limite);

    @Query("SELECT p FROM Produto p WHERE " +
            "(:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
            "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
            "(:precoMin IS NULL OR p.preco >= :precoMin) AND " +
            "(:precoMax IS NULL OR p.preco <= :precoMax)")
    List<Produto> buscarProdutosFiltrados(
            @Param("nome") String nome,
            @Param("categoriaId") Long categoriaId,
            @Param("precoMin") BigDecimal precoMin,
            @Param("precoMax") BigDecimal precoMax);

    // ========== MÉTODOS ADICIONAIS ==========

    List<Produto> findByNomeStartingWithIgnoreCase(String nome);

    @Query("SELECT p FROM Produto p WHERE " +
            "LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Produto> buscarPorNomeOuDescricao(@Param("termo") String termo);

    Optional<Produto> findByNomeIgnoreCase(String nome);

    List<Produto> findByFarmaciaId(Long farmaciaId);
}