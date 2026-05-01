package com.clickfarma.backend.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PagamentoService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);

    @Value("${MERCADOPAGO_ACCESS_TOKEN:}")
    private String accessToken;

    @Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

    @Value("${app.webhook.url:}")
    private String webhookUrl;

    @PostConstruct
    public void init() {
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("MERCADOPAGO_ACCESS_TOKEN nao configurado. O backend iniciara, mas a criacao de links de pagamento ficara desabilitada.");
            return;
        }

        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("✅ Mercado Pago configurado com sucesso!");
    }

    public String criarLinkPagamento(Double valorTotal, Long pedidoId) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("Mercado Pago nao configurado. Defina MERCADOPAGO_ACCESS_TOKEN para habilitar pagamentos.");
        }

        try {
            log.info("💰 Criando pagamento para pedido: {} - Valor: R$ {}", pedidoId, valorTotal);

    @Transactional
    public Map<String, Object> gerarPagamentoFarmacia(Long farmaciaId, String periodo) {
        Farmacia farmacia = farmaciaRepository.findById(farmaciaId)
                .orElseThrow(() -> new RuntimeException("Farmácia não encontrada"));

        YearMonth ym = YearMonth.parse(periodo, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime inicio = ym.atDay(1).atStartOfDay();
        LocalDateTime fim = ym.atEndOfMonth().atTime(23, 59, 59);

            // Configurar URLs de retorno
            String baseFront = frontendUrl != null ? frontendUrl.replaceAll("/+$", "") : "http://localhost:8081";
            String successUrl = baseFront + "/sucesso-pagamento";
            String pendingUrl = baseFront + "/order-confirmation";
            String failureUrl = baseFront + "/checkout";

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .pending(pendingUrl)
                    .failure(failureUrl)
                    .build();

            // Mercado Pago costuma exigir notification_url publicamente acessível (normalmente https).
            // Em desenvolvimento local, evitar setar localhost aqui para não quebrar a criação da preference.
            String notification = normalizeNotificationUrl(webhookUrl);

            // auto_return costuma exigir back_urls.success válido (e, em alguns ambientes, https).
            // Em desenvolvimento local (http://localhost) desativamos auto_return para evitar 400 invalid_auto_return.
            String autoReturn = (successUrl != null && successUrl.toLowerCase().startsWith("https://"))
                    ? "approved"
                    : null;

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(pedidoId.toString())
                    .backUrls(backUrls)
                    .autoReturn(autoReturn)
                    .notificationUrl(notification)
                    .build();

        Pagamento p = new Pagamento();
        p.setTipo(Pagamento.TipoPagamento.FARMACIA);
        p.setFarmacia(farmacia);
        p.setValorBruto(valorBruto);
        p.setValorTaxa(taxa);
        p.setValorLiquido(liquido);
        p.setChavePix(farmacia.getChavePix());
        p.setTipoChavePix(farmacia.getTipoChavePix());
        p.setReferenciaPeriodo(periodo);
        p.setStatus(Pagamento.StatusPagamento.PENDENTE);

        return toMap(pagamentoRepository.save(p));
    }

    @Transactional
    public Map<String, Object> gerarPagamentoMotoboy(Long motoboyId, String periodo) {
        Motoboy motoboy = motoboyRepository.findById(motoboyId)
                .orElseThrow(() -> new RuntimeException("Motoboy não encontrado"));

        } catch (MPApiException e) {
            log.error("❌ Erro ao criar pagamento (MPApiException). status={} content={}",
                    e.getStatusCode(), e.getApiResponse() != null ? e.getApiResponse().getContent() : "null");
            throw new RuntimeException("Erro ao gerar link de pagamento (Mercado Pago): " +
                    (e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage()));
        } catch (MPException e) {
            log.error("❌ Erro ao criar pagamento (MPException): {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar link de pagamento (Mercado Pago): " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Erro ao criar pagamento: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar link de pagamento: " + e.getMessage());
        }
        return m;
    }

    private String normalizeNotificationUrl(String raw) {
        if (raw == null) return null;
        String url = raw.trim();
        if (url.isBlank()) return null;
        // Garante https ou uma URL pública; evita localhost por padrão.
        String lower = url.toLowerCase();
        if (lower.startsWith("http://localhost")
                || lower.startsWith("http://127.0.0.1")
                || lower.startsWith("http://0.0.0.0")
                || lower.startsWith("https://localhost")
                || lower.startsWith("https://127.0.0.1")
                || lower.startsWith("https://0.0.0.0")) {
            return null;
        }
        if (!lower.startsWith("https://")) {
            // MP normalmente espera https; deixe nulo se não for.
            return null;
        }
        return url;
    }
}
