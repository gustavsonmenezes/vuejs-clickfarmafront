package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.*;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.UsuarioRepository;
import com.clickfarma.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // 👈 CORRIGIDO: Era 8082, agora é 3000
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            return ResponseEntity.ok(new LoginResponseDTO(
                    token,
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensagemResponseDTO("Email ou senha inválidos", false));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            // Verificar se email já existe
            if (usuarioRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new MensagemResponseDTO("Email já cadastrado", false));
            }

            // Criar novo usuário
            Usuario usuario = new Usuario();
            usuario.setNome(registerRequest.getNome());
            usuario.setEmail(registerRequest.getEmail());
            usuario.setSenha(passwordEncoder.encode(registerRequest.getSenha()));
            usuario.setTelefone(registerRequest.getTelefone());
            usuario.setEndereco(registerRequest.getEndereco());

            usuarioRepository.save(usuario);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MensagemResponseDTO("Usuário registrado com sucesso!", true));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO("Erro ao registrar usuário: " + e.getMessage(), false));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Como estamos usando JWT, o logout é feito no cliente
        // Mas podemos retornar uma mensagem de sucesso
        return ResponseEntity.ok(new MensagemResponseDTO("Logout realizado com sucesso!", true));
    }
}
