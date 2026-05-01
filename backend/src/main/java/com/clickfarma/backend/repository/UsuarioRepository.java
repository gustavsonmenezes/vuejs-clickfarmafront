package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findByTelegramLinkToken(String telegramLinkToken);

    Optional<Usuario> findByTelegramId(String telegramId);
}
