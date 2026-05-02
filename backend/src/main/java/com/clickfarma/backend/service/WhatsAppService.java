package com.clickfarma.backend.service;

import com.clickfarma.backend.model.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class WhatsAppService {

    private static final String WHATSAPP_API = "https://wa.me/";

    @Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

    public String gerarLinkStatusPedido(Pedido pedido) {
        String telefone = pedido.getUsuario().getTelefone();
        if (telefone == null || telefone.isBlank()) return null;

        String phone = limparTelefone(telefone);
        String message = montarMensagemStatus(pedido);
        return WHATSAPP_API + phone + "?text=" + encode(message);
    }

    public String gerarLinkRastreio(Pedido pedido) {
        String telefone = pedido.getUsuario().getTelefone();
        if (telefone == null || telefone.isBlank()) return null;

        String phone = limparTelefone(telefone);
        String message = montarMensagemRastreio(pedido);
        return WHATSAPP_API + phone + "?text=" + encode(message);
    }

    public String gerarLinkCompartilhar(Pedido pedido) {
        String message = "Ola! Fiz um pedido na ClickFarma (codigo: " + pedido.getCodigoPedido()
                + ") no valor de R$ " + formatarValor(pedido.getValorTotal())
                + ". Status: " + formatarStatus(pedido.getStatus());
        return WHATSAPP_API + "?text=" + encode(message);
    }

    private String limparTelefone(String telefone) {
        String cleaned = telefone.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("55") && cleaned.length() >= 13) return cleaned;
        if (cleaned.startsWith("0") && cleaned.length() >= 12) return "55" + cleaned.substring(1);
        if (cleaned.length() >= 11) return "55" + cleaned;
        return cleaned;
    }

    private String montarMensagemStatus(Pedido pedido) {
        String base = "Pedido *#" + pedido.getCodigoPedido() + "* - ClickFarma\n\n"
                + "Status: *" + formatarStatus(pedido.getStatus()) + "*\n";

        if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
            base += "Itens: " + pedido.getItens().size() + " produto(s)\n";
        }
        base += "Total: R$ " + formatarValor(pedido.getValorTotal()) + "\n\n";
        base += "Acompanhe seu pedido em: " + frontendUrl + "/rastrear";

        return base;
    }

    private String montarMensagemRastreio(Pedido pedido) {
        String base = "Rastreio do Pedido *#" + pedido.getCodigoPedido() + "*\n\n"
                + "Status: *" + formatarStatus(pedido.getStatus()) + "*\n";

        if (pedido.getRastreio() != null) {
            if (pedido.getRastreio().getUltimaLocalizacao() != null) {
                base += "Ultima localizacao: " + pedido.getRastreio().getUltimaLocalizacao() + "\n";
            }
            if (pedido.getRastreio().getTransportadora() != null) {
                base += "Transportadora: " + pedido.getRastreio().getTransportadora() + "\n";
            }
        }

        base += "\nAcompanhe em tempo real: " + frontendUrl + "/rastrear?pedido=" + pedido.getCodigoPedido();
        return base;
    }

    private String formatarStatus(Pedido.StatusPedido status) {
        if (status == null) return "Desconhecido";
        return switch (status) {
            case AGUARDANDO_PAGAMENTO -> "Aguardando Pagamento";
            case PAGO -> "Pago";
            case EM_PREPARACAO -> "Em Preparacao";
            case ENVIADO -> "Enviado";
            case EM_TRANSITO -> "Em Transito";
            case ENTREGUE -> "Entregue";
            case CANCELADO -> "Cancelado";
        };
    }

    private String formatarValor(java.math.BigDecimal valor) {
        if (valor == null) return "0,00";
        return String.format("%.2f", valor).replace('.', ',');
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
