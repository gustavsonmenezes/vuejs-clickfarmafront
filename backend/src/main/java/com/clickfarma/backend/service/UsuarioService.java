package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.PedidoResponseDTO;
import com.clickfarma.backend.dto.TelegramLinkResponseDTO;
import com.clickfarma.backend.dto.UsuarioRequestDTO;
import com.clickfarma.backend.dto.UsuarioResponseDTO;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TelegramIntegrationService telegramIntegrationService;

    // Criar usuário
    public UsuarioResponseDTO criarUsuario(UsuarioRequestDTO usuarioDTO) {
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + usuarioDTO.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDTO.getNome());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setSenha(usuarioDTO.getSenha());
        usuario.setTelefone(usuarioDTO.getTelefone());
        usuario.setEndereco(usuarioDTO.getEndereco());

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return new UsuarioResponseDTO(usuarioSalvo);
    }

    // Listar todos
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Buscar por ID
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));
        return new UsuarioResponseDTO(usuario);
    }

    // Buscar por email
    public UsuarioResponseDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com email: " + email));
        return new UsuarioResponseDTO(usuario);
    }

    // Atualizar usuário
    public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioRequestDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        usuario.setNome(usuarioDTO.getNome());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setTelefone(usuarioDTO.getTelefone());
        usuario.setEndereco(usuarioDTO.getEndereco());

        if (usuarioDTO.getSenha() != null && !usuarioDTO.getSenha().isEmpty()) {
            usuario.setSenha(usuarioDTO.getSenha());
        }

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        return new UsuarioResponseDTO(usuarioAtualizado);
    }

    // Deletar usuário
    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // Buscar usuário com pedidos
    public Map<String, Object> buscarUsuarioComPedidos(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("usuario", new UsuarioResponseDTO(usuario));

        if (usuario.getPedidos() != null && !usuario.getPedidos().isEmpty()) {
            List<PedidoResponseDTO> pedidos = usuario.getPedidos()
                    .stream()
                    .map(PedidoResponseDTO::new)
                    .collect(Collectors.toList());
            resultado.put("pedidos", pedidos);
            resultado.put("totalPedidos", pedidos.size());
        } else {
            resultado.put("pedidos", List.of());
            resultado.put("totalPedidos", 0);
        }

        return resultado;
    }

    // Alterar senha
    public void alterarSenha(Long id, String senhaAtual, String senhaNova) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        // Verificar se a senha atual está correta
        if (!usuario.getSenha().equals(senhaAtual)) {
            throw new RuntimeException("Senha atual incorreta");
        }

        // Validar nova senha
        if (senhaNova == null || senhaNova.length() < 6) {
            throw new RuntimeException("Nova senha deve ter no mínimo 6 caracteres");
        }

        usuario.setSenha(senhaNova);
        usuarioRepository.save(usuario);
    }

    // Adicionar endereço
    public void adicionarEndereco(Long id, String endereco) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        // Aqui você pode implementar uma lógica para múltiplos endereços
        // Por enquanto, vamos apenas atualizar o endereço principal
        usuario.setEndereco(endereco);
        usuarioRepository.save(usuario);
    }

    public UsuarioResponseDTO vincularTelegram(Long id, String telegramId) {
        if (telegramId == null || telegramId.isBlank()) {
            throw new RuntimeException("Telegram ID é obrigatório");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        usuario.setTelegramId(telegramId.trim());
        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO removerTelegram(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        usuario.setTelegramId(null);
        usuario.setTelegramLinkToken(null);
        usuario.setTelegramLinkExpiresAt(null);
        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    public TelegramLinkResponseDTO gerarLinkTelegram(Long id) {
        return telegramIntegrationService.gerarLinkVinculacao(id);
    }

    // Gerar relatório de usuários
    public Map<String, Object> gerarRelatorioUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("totalUsuarios", usuarios.size());

        // Usuários com pedidos
        long usuariosComPedidos = usuarios.stream()
                .filter(u -> u.getPedidos() != null && !u.getPedidos().isEmpty())
                .count();
        relatorio.put("usuariosComPedidos", usuariosComPedidos);
        relatorio.put("usuariosSemPedidos", usuarios.size() - usuariosComPedidos);

        // Data do primeiro cadastro
        if (!usuarios.isEmpty()) {
            Usuario primeiroUsuario = usuarios.stream()
                    .min((u1, u2) -> u1.getDataCadastro().compareTo(u2.getDataCadastro()))
                    .orElse(null);
            if (primeiroUsuario != null) {
                relatorio.put("primeiroCadastro", primeiroUsuario.getDataCadastro());
                relatorio.put("primeiroUsuario", primeiroUsuario.getNome());
            }
        }

        // Últimos 5 usuários cadastrados
        List<UsuarioResponseDTO> ultimosUsuarios = usuarios.stream()
                .sorted((u1, u2) -> u2.getDataCadastro().compareTo(u1.getDataCadastro()))
                .limit(5)
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
        relatorio.put("ultimosCadastros", ultimosUsuarios);

        return relatorio;
    }
}
