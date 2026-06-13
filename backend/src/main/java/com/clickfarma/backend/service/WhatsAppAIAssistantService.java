package com.clickfarma.backend.service;

import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.PedidoRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class WhatsAppAIAssistantService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppAIAssistantService.class);

    private final WhatsAppCloudService whatsAppCloudService;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final AiRouterService aiRouterService;

    public WhatsAppAIAssistantService(WhatsAppCloudService whatsAppCloudService,
                                       UsuarioRepository usuarioRepository,
                                       PedidoRepository pedidoRepository,
                                       AiRouterService aiRouterService) {
        this.whatsAppCloudService = whatsAppCloudService;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.aiRouterService = aiRouterService;
    }

    @Async
    public void processMessage(String from, String messageText) {
        try {
            String phoneClean = from.replaceAll("[^0-9]", "");
            String phoneSem55 = phoneClean.startsWith("55") ? phoneClean.substring(2) : phoneClean;
            log.info("WhatsApp AI assistant processando mensagem de {} (clean={}, sem55={})", from, phoneClean, phoneSem55);

            Usuario usuario = usuarioRepository.buscarPorTelefone(phoneClean);
            if (usuario == null) {
                usuario = usuarioRepository.buscarPorTelefone(phoneSem55);
            }

            List<Pedido> pedidos = new java.util.ArrayList<>();
            if (usuario != null) {
                pedidos.addAll(pedidoRepository.findByUsuarioId(usuario.getId()));
            }

            String response = generateAIResponse(messageText, usuario, pedidos);
            whatsAppCloudService.enviarTexto(from, response);
            log.info("Resposta enviada para {}: {}", phoneClean, response.substring(0, Math.min(100, response.length())));
        } catch (Exception e) {
            log.error("Erro ao processar mensagem WhatsApp de {}: {}", from, e.getMessage(), e);
            try {
                whatsAppCloudService.enviarTexto(from, "Desculpe, tive um erro ao processar sua mensagem. Tente novamente em instantes.");
            } catch (Exception ignored) {}
        }
    }

    private String generateAIResponse(String message, Usuario usuario, List<Pedido> pedidos) {
        StringBuilder context = new StringBuilder();

        if (usuario != null) {
            context.append("- Nome do cliente: ").append(usuario.getNome()).append("\n");

            if (!pedidos.isEmpty()) {
                context.append("- Total de pedidos: ").append(pedidos.size()).append("\n");
                context.append("- Pedidos recentes:\n");
                pedidos.stream().limit(3).forEach(p -> appendPedidoInfo(context, p));
            } else {
                context.append("- Cliente não possui pedidos no sistema.\n");
            }
        } else {
            context.append("- Cliente não encontrado no cadastro.\n");
        }

        String finalPrompt = """
            Você é o assistente virtual da ClickFarma no WhatsApp.
            Responda de forma SIMPÁTICA e CONCISA (máximo 4 linhas).

            INFORMAÇÕES REAIS DO SISTEMA (use apenas estes dados):
            %s

            REGRAS:
            - Se o cliente perguntar sobre pedido/rastreio, INFORME os dados reais acima
            - Se não houver pedidos, avise educadamente
            - Se for uma saudação, seja simpático e ofereça ajuda
            - Use emojis moderadamente
            - Responda em português brasileiro
            - NUNCA invente dados de pedidos. Use apenas as informações fornecidas

            Mensagem do cliente: %s
            """.formatted(context.toString().trim(), message);

        try {
            String aiResponse = aiRouterService.chat(finalPrompt).block();
            if (aiResponse != null && !aiResponse.isBlank() && !aiResponse.contains("não configurada")) {
                return aiResponse;
            }
        } catch (Exception e) {
            log.warn("IA indisponível, usando fallback: {}", e.getMessage());
        }

        return templateFallback(message, usuario, pedidos);
    }

    private void appendPedidoInfo(StringBuilder sb, Pedido p) {
        sb.append("  • Pedido #").append(p.getCodigoPedido());
        sb.append(" | Status: ").append(formatStatus(p.getStatus()));
        if (p.getRastreio() != null) {
            if (p.getRastreio().getUltimaLocalizacao() != null) {
                sb.append(" | Localização: ").append(p.getRastreio().getUltimaLocalizacao());
            }
            if (p.getRastreio().getDataPrevisaoEntrega() != null) {
                sb.append(" | Previsão: ").append(
                    p.getRastreio().getDataPrevisaoEntrega().format(DateTimeFormatter.ofPattern("dd/MM"))
                );
            }
        }
        if (p.getValorTotal() != null) {
            sb.append(" | Valor: R$ ").append(String.format("%.2f", p.getValorTotal()));
        }
        sb.append("\n");
    }

    private String templateFallback(String message, Usuario usuario, List<Pedido> pedidos) {
        String lower = message.toLowerCase();

        if (lower.contains("pedido") || lower.contains("código") || lower.contains("codigo") ||
            lower.contains("rastrear") || lower.contains("status") || lower.contains("entrega") ||
            lower.contains("localização") || lower.contains("localizacao") || lower.matches(".*\\bPED\\d+.*")) {
            return buildOrderResponse(usuario, pedidos);
        }

        if (lower.contains("oi") || lower.contains("olá") || lower.contains("ola") ||
            lower.contains("bom dia") || lower.contains("boa tarde") || lower.contains("boa noite") ||
            lower.contains("hey") || lower.contains("e aí")) {
            return buildGreeting(usuario);
        }

        return "Olá! 💚 Eu sou o assistente da ClickFarma. Posso consultar seus pedidos, " +
               "informar status de entrega e tirar dúvidas. É só me perguntar!";
    }

    private String buildOrderResponse(Usuario usuario, List<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            return "Não encontrei nenhum pedido associado a este número. 🧐 " +
                   "Verifique se o telefone cadastrado no site é este mesmo.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📦 *Seus Pedidos ClickFarma*\n\n");

        for (int i = 0; i < Math.min(pedidos.size(), 3); i++) {
            Pedido p = pedidos.get(i);
            sb.append("*#").append(p.getCodigoPedido()).append("*\n");
            sb.append("Status: ").append(formatStatusEmoji(p.getStatus())).append("\n");
            if (p.getRastreio() != null && p.getRastreio().getUltimaLocalizacao() != null) {
                sb.append("📍 ").append(p.getRastreio().getUltimaLocalizacao()).append("\n");
            }
            if (p.getRastreio() != null && p.getRastreio().getDataPrevisaoEntrega() != null) {
                sb.append("📅 Previsão: ").append(
                    p.getRastreio().getDataPrevisaoEntrega().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                ).append("\n");
            }
            if (p.getValorTotal() != null) {
                sb.append("💰 R$ ").append(String.format("%.2f", p.getValorTotal())).append("\n");
            }
            sb.append("\n");
        }

        if (pedidos.size() > 3) {
            sb.append("... e mais ").append(pedidos.size() - 3).append(" pedido(s).\n");
        }

        sb.append("\n💚 ClickFarma - Sua farmácia de confiança!");
        return sb.toString();
    }

    private String buildGreeting(Usuario usuario) {
        String nome = usuario != null ? usuario.getNome() : "";
        return "Olá" + (nome.isEmpty() ? "" : " " + nome) + "! 💚 Bem-vindo(a) à ClickFarma!\n\n" +
               "Digite o código do seu pedido ou me pergunte:\n" +
               "• \"Onde está meu pedido?\"\n" +
               "• \"Qual o status do pedido?\"\n" +
               "• \"Quero rastrear minha entrega\"";
    }

    private String formatStatus(Pedido.StatusPedido status) {
        if (status == null) return "Desconhecido";
        return switch (status) {
            case AGUARDANDO_PAGAMENTO -> "Aguardando Pagamento";
            case PAGO -> "Pago";
            case EM_PREPARACAO -> "Em Preparação";
            case ENVIADO -> "Enviado";
            case EM_TRANSITO -> "Em Trânsito";
            case ENTREGUE -> "Entregue";
            case CANCELADO -> "Cancelado";
        };
    }

    private String formatStatusEmoji(Pedido.StatusPedido status) {
        if (status == null) return "❓ Desconhecido";
        return switch (status) {
            case AGUARDANDO_PAGAMENTO -> "⏳ Aguardando Pagamento";
            case PAGO -> "✅ Pago";
            case EM_PREPARACAO -> "🔧 Em Preparação";
            case ENVIADO -> "📦 Enviado";
            case EM_TRANSITO -> "🚚 Em Trânsito";
            case ENTREGUE -> "🎉 Entregue";
            case CANCELADO -> "❌ Cancelado";
        };
    }
}
