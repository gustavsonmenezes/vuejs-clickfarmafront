package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.TelegramLinkResponseDTO;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TelegramIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(TelegramIntegrationService.class);
    private static final String TELEGRAM_START_PREFIX = "connect_";

    private final UsuarioRepository usuarioRepository;
    private final TelegramService telegramService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public TelegramIntegrationService(UsuarioRepository usuarioRepository, TelegramService telegramService) {
        this.usuarioRepository = usuarioRepository;
        this.telegramService = telegramService;
    }

    public TelegramLinkResponseDTO gerarLinkVinculacao(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + usuarioId));

        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        usuario.setTelegramLinkToken(token);
        usuario.setTelegramLinkExpiresAt(expiresAt);
        usuarioRepository.save(usuario);

        String usernameNormalizado = normalizarBotUsername(botUsername);
        String payload = TELEGRAM_START_PREFIX + token;
        String deepLink = "https://t.me/" + usernameNormalizado + "?start=" + payload;

        return new TelegramLinkResponseDTO(usernameNormalizado, deepLink, payload, expiresAt, usuario.getTelegramId() != null);
    }

    public void processarUpdateTelegram(Map<String, Object> update) {
        Map<String, Object> message = asMap(update.get("message"));
        if (message == null) {
            return;
        }

        Map<String, Object> chat = asMap(message.get("chat"));
        if (chat == null || chat.get("id") == null) {
            return;
        }

        String chatId = String.valueOf(chat.get("id"));
        String nome = String.valueOf(chat.getOrDefault("first_name", "Cliente"));
        String texto = String.valueOf(message.getOrDefault("text", ""));

        if (texto.startsWith("/start")) {
            processarComandoStart(chatId, nome, texto);
        }
    }

    private void processarComandoStart(String chatId, String nome, String texto) {
        String[] partes = texto.split("\\s+", 2);
        if (partes.length < 2 || !partes[1].startsWith(TELEGRAM_START_PREFIX)) {
            telegramService.enviarMensagem(chatId,
                    "Olá, " + nome + "! Para vincular sua conta ClickFarma, abra o link de conexão gerado dentro do sistema.");
            return;
        }

        String token = partes[1].substring(TELEGRAM_START_PREFIX.length()).trim();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTelegramLinkToken(token);

        if (usuarioOpt.isEmpty()) {
            telegramService.enviarMensagem(chatId,
                    "Este link de vinculação é inválido ou já foi utilizado. Gere um novo link no sistema ClickFarma.");
            return;
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.getTelegramLinkExpiresAt() == null || usuario.getTelegramLinkExpiresAt().isBefore(LocalDateTime.now())) {
            telegramService.enviarMensagem(chatId,
                    "Este link de vinculação expirou. Gere um novo link no sistema ClickFarma.");
            return;
        }

        Optional<Usuario> usuarioComMesmoChat = usuarioRepository.findByTelegramId(chatId);
        if (usuarioComMesmoChat.isPresent() && !usuarioComMesmoChat.get().getId().equals(usuario.getId())) {
            telegramService.enviarMensagem(chatId,
                    "Este chat já está vinculado a outra conta ClickFarma.");
            return;
        }

        usuario.setTelegramId(chatId);
        usuario.setTelegramLinkToken(null);
        usuario.setTelegramLinkExpiresAt(null);
        usuarioRepository.save(usuario);

        telegramService.enviarMensagem(chatId,
                "Conta vinculada com sucesso, " + usuario.getNome() + "! Você agora receberá lembretes da ClickFarma por aqui.");
        log.info("Telegram vinculado automaticamente ao usuário {}", usuario.getId());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private String normalizarBotUsername(String username) {
        return username == null ? "" : username.replace("@", "").trim();
    }
}
