package com.clickfarma.backend.service;

import com.clickfarma.backend.model.AgendamentoRecompra;
import com.clickfarma.backend.model.ItemPedido;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Usuario;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.email.from:no-reply@clickfarma.local}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean enviarConfirmacaoPedido(Usuario usuario, Pedido pedido, String linkPagamento) {
        if (!podeEnviarPara(usuario)) {
            return false;
        }

        String itensHtml = buildTabelaItens(pedido.getItens());
        String endereco = formatarEndereco(pedido);

        String badgeHtml = "<div style=\"background:#15803D;color:#FFFFFF;padding:10px 24px;border-radius:24px;font-size:16px;font-weight:700;display:inline-block;margin:12px 0;\">Compra realizada com sucesso!</div>";

        String html = buildTemplate(
                "Compra Realizada",
                "Olá, " + usuario.getNome() + "!",
                "Sua compra <strong>#" + pedido.getCodigoPedido() + "</strong> foi confirmada.",
                badgeHtml,
                null,
                itensHtml,
                "Total",
                formatarMoeda(pedido.getValorTotal()),
                endereco,
                "Endereço de entrega"
        );

        return enviarEmail(usuario.getEmail(), "Compra #" + pedido.getCodigoPedido() + " confirmada - ClickFarma", html);
    }

        String itensHtml = buildTabelaItens(pedido.getItens());

        String pagamentoHtml = (linkPagamento != null && !linkPagamento.isBlank())
                ? "<table role=\"presentation\" width=\"100%%\" cellpadding=\"0\" cellspacing=\"0\">" +
                "<tr><td align=\"center\" style=\"padding:24px 0 8px;\">" +
                "<a href=\"" + linkPagamento + "\" " +
                "style=\"display:inline-block;padding:14px 32px;background:#15803D;color:#ffffff;text-decoration:none;border-radius:8px;font-weight:600;font-size:16px;\">" +
                "Finalizar pagamento</a></td></tr></table>"
                : "<div style=\"background:#F0FDF4;border:1px solid #BBF7D0;border-radius:8px;padding:16px;margin:16px 0;text-align:center;\">" +
                "<span style=\"color:#15803D;font-weight:600;\">Pagamento confirmado</span></div>";

        String endereco = formatarEndereco(pedido);

        String statusBadge = "PAGO".equalsIgnoreCase(pedido.getStatus() != null ? pedido.getStatus().name() : "")
                ? buildStatusBadgeComIcone(pedido.getStatus())
                : "<div style=\"display:inline-block;background:#15803D;color:#FFFFFF;padding:8px 20px;border-radius:20px;font-size:14px;font-weight:600;margin:8px 0 16px;\">Pedido confirmado</div>";

        String html = buildTemplate(
                "Pedido Confirmado",
                "Olá, " + usuario.getNome() + "!",
                "Seu pedido <strong>#" + pedido.getCodigoPedido() + "</strong> foi recebido com sucesso.",
                statusBadge,
                pagamentoHtml,
                itensHtml,
                "Total",
                formatarMoeda(pedido.getValorTotal()),
                endereco,
                "Endereço de entrega"
        );

        return enviarEmail(usuario.getEmail(), "Pedido #" + pedido.getCodigoPedido() + " confirmado - ClickFarma", html);
    }

    public boolean enviarAtualizacaoStatusPedido(Usuario usuario, Pedido pedido) {
        if (!podeEnviarPara(usuario)) {
            return false;
        }

        String statusHtml = buildStatusBadgeComIcone(pedido.getStatus());

        String html = buildTemplate(
                "Status Atualizado",
                "Olá, " + usuario.getNome() + "!",
                "Seu pedido <strong>#" + pedido.getCodigoPedido() + "</strong> foi atualizado.",
                statusHtml,
                "<p style=\"color:#6B7280;margin:16px 0;\">Acompanhe todos os detalhes do seu pedido clicando no botão abaixo.</p>",
                buildResumoItens(pedido.getItens()),
                null, null, null, null
        );

        return enviarEmail(usuario.getEmail(), "Pedido #" + pedido.getCodigoPedido() + " atualizado - ClickFarma", html);
    }

    public boolean enviarLembreteRecompra(AgendamentoRecompra agendamento) {
        Usuario usuario = agendamento.getUsuario();
        if (!podeEnviarPara(usuario)) {
            return false;
        }

        String html = buildTemplate(
                "Lembrete de Recompra",
                "Olá, " + usuario.getNome() + "!",
                "Percebemos que seu <strong>" + agendamento.getProduto().getNome() + "</strong> pode estar no fim.",
                "<div style=\"background:#FEF3C7;border:1px solid #FCD34D;border-radius:8px;padding:16px;text-align:center;margin:16px 0;\">" +
                "<span style=\"color:#92400E;font-weight:600;\">Hora de recomprar</span></div>",
                "<p style=\"color:#6B7280;margin:16px 0;\">Mantenha seu tratamento em dia repetindo seu pedido.</p>",
                null, null, null, null, null
        );

        return enviarEmail(usuario.getEmail(), "Hora de recomprar - ClickFarma", html);
    }

    public boolean enviarEmailTeste(String destinatario, String nome) {
        String html = buildTemplate(
                "Teste de E-mail",
                "Olá, " + (nome != null ? nome : "Cliente") + "!",
                "Este é um e-mail de teste da integração SMTP da ClickFarma.",
                "<div style=\"background:#F0FDF4;border:1px solid #BBF7D0;border-radius:8px;padding:16px;text-align:center;margin:16px 0;\">" +
                "<span style=\"color:#15803D;font-weight:600;\">E-mail funcionando</span></div>",
                null, null, null, null, null, null
        );
        return enviarEmail(destinatario, "Teste de e-mail - ClickFarma", html);
    }

    private String buildTemplate(String titulo, String saudacao, String subtitulo,
                                 String statusBlock, String ctaBlock,
                                 String itensBlock, String totalLabel, String totalValor,
                                 String enderecoText, String enderecoLabel) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">");
        sb.append("<style>@media only screen and (max-width:600px){.container{width:100%!important}.mobile-pad{padding-left:16px!important;padding-right:16px!important}}</style>");
        sb.append("</head><body style=\"margin:0;padding:0;background:#F3F4F6;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;color:#111827;\">");
        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#F3F4F6;padding:32px 0;\">");
        sb.append("<tr><td align=\"center\"><table role=\"presentation\" class=\"container\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#FFFFFF;border-radius:12px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,0.1);\">");

        // Header
        sb.append("<tr><td style=\"background:linear-gradient(135deg,#15803D 0%,#166534 100%);padding:32px;text-align:center;\">");
        sb.append("<h1 style=\"color:#FFFFFF;margin:0;font-size:24px;font-weight:700;\">").append(titulo).append("</h1>");
        sb.append("</td></tr>");

        // Body
        sb.append("<tr><td class=\"mobile-pad\" style=\"padding:32px;\">");
        sb.append("<p style=\"font-size:18px;font-weight:600;margin:0 0 8px;\">").append(saudacao).append("</p>");
        sb.append("<p style=\"color:#4B5563;margin:0 0 24px;line-height:1.5;\">").append(subtitulo).append("</p>");

        if (statusBlock != null) {
            sb.append(statusBlock);
        }

        if (ctaBlock != null) {
            sb.append(ctaBlock);
        }

        if (itensBlock != null) {
            sb.append("<div style=\"border-top:1px solid #E5E7EB;margin:24px 0 16px;\"></div>");
            sb.append("<p style=\"font-weight:600;margin:0 0 12px;\">Itens do pedido</p>");
            sb.append(itensBlock);
        }

        if (totalLabel != null && totalValor != null) {
            sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:16px 0;background:#F9FAFB;border-radius:8px;padding:16px;\">");
            sb.append("<tr><td style=\"font-weight:600;color:#374151;\">").append(totalLabel).append("</td>");
            sb.append("<td align=\"right\" style=\"font-size:20px;font-weight:700;color:#15803D;\">").append(totalValor).append("</td></tr></table>");
        }

        if (enderecoText != null && enderecoLabel != null) {
            sb.append("<div style=\"background:#F9FAFB;border-radius:8px;padding:16px;margin:16px 0;\">");
            sb.append("<p style=\"font-weight:600;margin:0 0 4px;color:#374151;\">").append(enderecoLabel).append("</p>");
            sb.append("<p style=\"color:#4B5563;margin:0;font-size:14px;line-height:1.5;\">").append(enderecoText).append("</p>");
            sb.append("</div>");
        }

        sb.append("</td></tr>");

        // Footer
        sb.append("<tr><td style=\"background:#F9FAFB;padding:24px;text-align:center;border-top:1px solid #E5E7EB;\">");
        sb.append("<p style=\"color:#6B7280;font-size:13px;margin:0;\">ClickFarma - Sua saúde em primeiro lugar</p>");
        sb.append("<p style=\"color:#9CA3AF;font-size:12px;margin:8px 0 0;\">Este e-mail foi enviado automaticamente. Não responda.</p>");
        sb.append("</td></tr>");

        sb.append("</table></td></tr></table></body></html>");
        return sb.toString();
    }

    private String buildStatusBadge(String status) {
        return "<div style=\"display:inline-block;background:#F3F4F6;padding:6px 16px;border-radius:20px;font-size:14px;font-weight:500;color:#374151;margin:8px 0 16px;\">" +
                status + "</div>";
    }

    private String buildStatusBadgeComIcone(Pedido.StatusPedido status) {
        String icon = switch (status) {
            case PAGO -> "✅";
            case EM_PREPARACAO -> "📦";
            case ENVIADO, EM_TRANSITO -> "🚚";
            case ENTREGUE -> "🎉";
            case CANCELADO -> "❌";
            default -> "📋";
        };
        String label = switch (status) {
            case PAGO -> "Pago";
            case EM_PREPARACAO -> "Em Preparação";
            case ENVIADO -> "Enviado";
            case EM_TRANSITO -> "Em Trânsito";
            case ENTREGUE -> "Entregue";
            case CANCELADO -> "Cancelado";
            case AGUARDANDO_PAGAMENTO -> "Aguardando Pagamento";
        };
        return "<div style=\"background:#F0FDF4;border:1px solid #BBF7D0;border-radius:8px;padding:16px;text-align:center;margin:16px 0;\">" +
                "<span style=\"font-size:20px;margin-right:8px;\">" + icon + "</span>" +
                "<span style=\"color:#15803D;font-weight:600;font-size:16px;\">" + label + "</span></div>";
    }

    private String buildTabelaItens(java.util.List<ItemPedido> itens) {
        if (itens == null || itens.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;\">");
        for (ItemPedido item : itens) {
            sb.append("<tr style=\"border-bottom:1px solid #F3F4F6;\">");
            sb.append("<td style=\"padding:12px 0;color:#374151;\">").append(item.getQuantidade()).append("x ").append(item.getProduto().getNome()).append("</td>");
            sb.append("<td align=\"right\" style=\"padding:12px 0;font-weight:600;color:#111827;\">R$ ").append(formatarMoeda(item.getSubtotal())).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private String buildResumoItens(java.util.List<ItemPedido> itens) {
        if (itens == null || itens.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (ItemPedido item : itens) {
            sb.append("<p style=\"color:#4B5563;margin:4px 0;font-size:14px;\">").append(item.getQuantidade()).append("x ").append(item.getProduto().getNome()).append("</p>");
        }
        return sb.toString();
    }

    private String formatarMoeda(java.math.BigDecimal valor) {
        if (valor == null) return "0,00";
        return String.format("%.2f", valor).replace('.', ',');
    }

    private String formatarEndereco(Pedido pedido) {
        String endereco = pedido.getEnderecoEntrega();
        if (endereco == null || endereco.isBlank()) return "Endereço não informado";
        return endereco;
    }

    private boolean enviarEmail(String destinatario, String assunto, String html) {
        if (!emailEnabled) {
            log.info("Envio de e-mail desabilitado. Ignorando mensagem para {}", destinatario);
            return false;
        }

        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(html, true);
            mailSender.send(mensagem);
            log.info("E-mail enviado com sucesso para {}", destinatario);
            return true;
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {}", destinatario, e);
            return false;
        }
    }

    private boolean podeEnviarPara(Usuario usuario) {
        return usuario != null && usuario.getEmail() != null && !usuario.getEmail().isBlank();
    }
}
