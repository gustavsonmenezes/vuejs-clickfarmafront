package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.*;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.UsuarioRepository;
import com.clickfarma.backend.security.JwtUtil;
import com.clickfarma.backend.service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8082")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GoogleAuthService googleAuthService;

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
                    usuario.getEmail(),
                    usuario.getRole()
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

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequestDTO request) {
        try {
            GoogleAuthService.GoogleUserInfo googleUser = googleAuthService.verifyToken(request.getCredential());

            Usuario usuario = usuarioRepository.findByGoogleId(googleUser.getGoogleId())
                    .orElseGet(() -> {
                        Usuario existing = usuarioRepository.findByEmail(googleUser.getEmail())
                                .map(u -> {
                                    u.setGoogleId(googleUser.getGoogleId());
                                    u.setAvatarUrl(googleUser.getAvatarUrl());
                                    return usuarioRepository.save(u);
                                })
                                .orElseGet(() -> {
                                    Usuario novo = new Usuario();
                                    novo.setNome(googleUser.getName());
                                    novo.setEmail(googleUser.getEmail());
                                    novo.setGoogleId(googleUser.getGoogleId());
                                    novo.setAvatarUrl(googleUser.getAvatarUrl());
                                    novo.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
                                    return usuarioRepository.save(novo);
                                });
                        return existing;
                    });

            UserDetails userDetails = new User(usuario.getEmail(), usuario.getSenha(), new ArrayList<>());
            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new LoginResponseDTO(
                    token,
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getRole(),
                    usuario.getAvatarUrl()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensagemResponseDTO("Erro na autenticação com Google: " + e.getMessage(), false));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(new MensagemResponseDTO("Logout realizado com sucesso!", true));
    }
}