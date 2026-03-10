package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Método para buscar usuário por email
    Optional<Usuario> findByEmail(String email);

    // Método para verificar se email já existe
    boolean existsByEmail(String email);
}