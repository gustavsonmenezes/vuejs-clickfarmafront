package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Motoboy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotoboyRepository extends JpaRepository<Motoboy, Long> {
    Optional<Motoboy> findByCpf(String cpf);
    Optional<Motoboy> findByUsuarioId(Long usuarioId);
}
