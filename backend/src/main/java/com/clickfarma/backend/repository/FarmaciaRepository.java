package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Farmacia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FarmaciaRepository extends JpaRepository<Farmacia, Long> {
    Optional<Farmacia> findByCnpj(String cnpj);
    Optional<Farmacia> findByUsuarioId(Long usuarioId);
}
