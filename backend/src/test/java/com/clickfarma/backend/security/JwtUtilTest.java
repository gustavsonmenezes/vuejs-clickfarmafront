package com.clickfarma.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "minhaChaveSecretaSuperSeguraCom256BitsParaJWTToken12345678");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);

        userDetails = new User("teste@email.com", "senha", Collections.emptyList());
    }

    @Test
    void deveGerarTokenValido() {
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(token.length() > 20);
    }

    @Test
    void deveExtrairUsernameDoToken() {
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractUsername(token);
        assertEquals("teste@email.com", username);
    }

    @Test
    void deveValidarTokenCorreto() {
        String token = jwtUtil.generateToken(userDetails);
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void deveRejeitarTokenInvalido() {
        assertFalse(jwtUtil.validateToken("token.invalido.aqui", userDetails));
    }

    @Test
    void deveRejeitarTokenParaOutroUsuario() {
        String token = jwtUtil.generateToken(userDetails);
        UserDetails outroUsuario = new User("outro@email.com", "senha", Collections.emptyList());
        assertFalse(jwtUtil.validateToken(token, outroUsuario));
    }
}
