package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Método para buscar usuário por email
    Optional<Usuario> findByEmail(String email);

    // Método para verificar se email já existe
    boolean existsByEmail(String email);

    Optional<Usuario> findByTelegramLinkToken(String telegramLinkToken);

    Optional<Usuario> findByTelegramId(String telegramId);

    Optional<Usuario> findByGoogleId(String googleId);

    @Query(value = "SELECT * FROM usuarios WHERE telefone LIKE CONCAT('%', :telefone, '%') LIMIT 1", nativeQuery = true)
    Usuario buscarPorTelefone(@Param("telefone") String telefone);
}
