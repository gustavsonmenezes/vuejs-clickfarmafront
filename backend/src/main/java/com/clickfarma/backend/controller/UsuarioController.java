package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.UsuarioRequestDTO;
import com.clickfarma.backend.dto.UsuarioResponseDTO;
import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.dto.PedidoResponseDTO;
import com.clickfarma.backend.dto.TelegramLinkResponseDTO;
import com.clickfarma.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:8082")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // POST - Criar novo usuário
    @PostMapping
    public ResponseEntity<?> criarUsuario(@Valid @RequestBody UsuarioRequestDTO usuarioDTO) {
        try {
            UsuarioResponseDTO novoUsuario = usuarioService.criarUsuario(usuarioDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MensagemResponseDTO(
                            "Usuário criado com sucesso!",
                            true,
                            novoUsuario
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Listar todos os usuários
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // GET - Buscar usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Buscar usuário por email
    @GetMapping("/email/{email}")
    public ResponseEntity<?> buscarPorEmail(@PathVariable String email) {
        try {
            UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(email);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Buscar usuário com seus pedidos
    @GetMapping("/{id}/pedidos")
    public ResponseEntity<?> buscarUsuarioComPedidos(@PathVariable Long id) {
        try {
            Map<String, Object> resultado = usuarioService.buscarUsuarioComPedidos(id);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Relatório de usuários (admin)
    @GetMapping("/relatorio")
    public ResponseEntity<?> gerarRelatorio() {
        try {
            Map<String, Object> relatorio = usuarioService.gerarRelatorioUsuarios();
            return ResponseEntity.ok(relatorio);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // PUT - Atualizar usuário completo
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO usuarioDTO) {
        try {
            UsuarioResponseDTO usuarioAtualizado = usuarioService.atualizarUsuario(id, usuarioDTO);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Usuário atualizado com sucesso!",
                    true,
                    usuarioAtualizado
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // PATCH - Alterar senha
    @PatchMapping("/{id}/senha")
    public ResponseEntity<?> alterarSenha(
            @PathVariable Long id,
            @RequestParam String senhaAtual,
            @RequestParam String senhaNova) {
        try {
            usuarioService.alterarSenha(id, senhaAtual, senhaNova);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Senha alterada com sucesso!",
                    true
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // POST - Adicionar endereço adicional
    @PostMapping("/{id}/endereco")
    public ResponseEntity<?> adicionarEndereco(
            @PathVariable Long id,
            @RequestParam String endereco) {
        try {
            usuarioService.adicionarEndereco(id, endereco);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Endereço adicionado com sucesso!",
                    true
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @PatchMapping("/{id}/telegram")
    public ResponseEntity<?> vincularTelegram(
            @PathVariable Long id,
            @RequestParam String telegramId) {
        try {
            UsuarioResponseDTO usuarioAtualizado = usuarioService.vincularTelegram(id, telegramId);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Telegram vinculado com sucesso!",
                    true,
                    usuarioAtualizado
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @PostMapping("/{id}/telegram/link")
    public ResponseEntity<?> gerarLinkTelegram(@PathVariable Long id) {
        try {
            TelegramLinkResponseDTO response = usuarioService.gerarLinkTelegram(id);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Link de vinculação Telegram gerado com sucesso!",
                    true,
                    response
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    @DeleteMapping("/{id}/telegram")
    public ResponseEntity<?> removerTelegram(@PathVariable Long id) {
        try {
            UsuarioResponseDTO usuarioAtualizado = usuarioService.removerTelegram(id);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Telegram removido com sucesso!",
                    true,
                    usuarioAtualizado
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // DELETE - Deletar usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id) {
        try {
            usuarioService.deletarUsuario(id);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Usuário deletado com sucesso!",
                    true
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }
}
