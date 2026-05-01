package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.UsuarioRepository;
import com.clickfarma.backend.service.EmailNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:8081", "http://localhost:8082"})
public class EmailController {

    private final EmailNotificationService emailNotificationService;
    private final UsuarioRepository usuarioRepository;

    public EmailController(EmailNotificationService emailNotificationService, UsuarioRepository usuarioRepository) {
        this.emailNotificationService = emailNotificationService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/teste/{usuarioId}")
    public ResponseEntity<?> enviarTeste(@PathVariable Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + usuarioId));

        boolean enviado = emailNotificationService.enviarEmailTeste(usuario.getEmail(), usuario.getNome());
        if (!enviado) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO("Não foi possível enviar o e-mail de teste. Verifique a configuração SMTP.", false));
        }

        return ResponseEntity.ok(new MensagemResponseDTO("E-mail de teste enviado com sucesso!", true));
    }
}
