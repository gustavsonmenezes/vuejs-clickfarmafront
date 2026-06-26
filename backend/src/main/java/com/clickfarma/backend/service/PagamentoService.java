package com.clickfarma.backend.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PagamentoService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);

    @Value("${MERCADOPAGO_ACCESS_TOKEN:}")
    private String accessToken;

    @Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

    @Value("${app.webhook.url:}")
    private String webhookUrl;

    @Value("${app.pix.chave:}")
    private String pixChave;

    @Value("${app.pix.nome:ClickFarma LTDA}")
    private String pixNome;

    @Value("${app.pix.cidade:SAO PAULO}")
    private String pixCidade;

    private final Map<String, SimulatedPix> simulatedPayments = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("MERCADOPAGO_ACCESS_TOKEN nao configurado. O backend iniciara, mas a criacao de links de pagamento ficara desabilitada.");
            return;
        }

        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("Mercado Pago configurado com sucesso!");
    }

    public String criarLinkPagamento(Double valorTotal, Long pedidoId) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("Mercado Pago nao configurado. Defina MERCADOPAGO_ACCESS_TOKEN para habilitar pagamentos.");
        }

        try {
            log.info("Criando pagamento para pedido: {} - Valor: R$ {}", pedidoId, valorTotal);

            List<PreferenceItemRequest> items = new ArrayList<>();
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Pedido ClickFarma #" + pedidoId)
                    .description("Produtos da ClickFarma")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(valorTotal))
                    .currencyId("BRL")
                    .build();
            items.add(item);

            String baseFront = frontendUrl != null ? frontendUrl.replaceAll("/+$", "") : "http://localhost:8081";
            String successUrl = baseFront + "/sucesso-pagamento";
            String pendingUrl = baseFront + "/order-confirmation";
            String failureUrl = baseFront + "/checkout";

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .pending(pendingUrl)
                    .failure(failureUrl)
                    .build();

            String notification = normalizeNotificationUrl(webhookUrl);

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

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            log.info("Link gerado: {}", preference.getInitPoint());
            return preference.getInitPoint();

        } catch (MPApiException e) {
            log.error("Erro ao criar pagamento (MPApiException). status={} content={}",
                    e.getStatusCode(), e.getApiResponse() != null ? e.getApiResponse().getContent() : "null");
            throw new RuntimeException("Erro ao gerar link de pagamento (Mercado Pago): " +
                    (e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage()));
        } catch (MPException e) {
            log.error("Erro ao criar pagamento (MPException): {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar link de pagamento (Mercado Pago): " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao criar pagamento: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar link de pagamento: " + e.getMessage());
        }
    }

    public CriarPixResponse criarPagamentoPix(Double valorTotal, Long pedidoId, String email) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("Mercado Pago nao configurado.");
        }

        try {
            log.info("Criando PIX para pedido: {} - Valor: R$ {}", pedidoId, valorTotal);

            OffsetDateTime expiracao = OffsetDateTime.now().plusMinutes(30);

            PaymentCreateRequest request = PaymentCreateRequest.builder()
                    .transactionAmount(BigDecimal.valueOf(valorTotal))
                    .paymentMethodId("pix")
                    .payer(PaymentPayerRequest.builder()
                            .email(email)
                            .build())
                    .externalReference(pedidoId.toString())
                    .dateOfExpiration(expiracao)
                    .build();

            PaymentClient client = new PaymentClient();
            Payment payment = client.create(request);

            String qrBase64 = payment.getPointOfInteraction().getTransactionData().getQrCodeBase64();
            String copiaECola = payment.getPointOfInteraction().getTransactionData().getQrCode();

            log.info("PIX gerado. Payment ID: {}", payment.getId());
            return new CriarPixResponse(qrBase64, copiaECola, expiracao.toString(), payment.getId());

        } catch (MPApiException e) {
            int statusCode = e.getStatusCode();
            String content = e.getApiResponse() != null ? e.getApiResponse().getContent() : "";

            if (statusCode == 401 || statusCode == 403 || content.contains("unauthorized") || content.contains("credentials")) {
                log.warn("Token nao autorizado para PIX direto (status={}). Usando modo simulado.", statusCode);
                return gerarPixSimulado(valorTotal, pedidoId, email);
            }

            log.error("Erro PIX (MPApiException). status={} content={}", statusCode, content);
            throw new RuntimeException("Erro ao gerar PIX: " + content);
        } catch (MPException e) {
            log.error("Erro PIX (MPException): {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PIX: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao gerar PIX: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PIX: " + e.getMessage());
        }
    }

    private CriarPixResponse gerarPixSimulado(Double valorTotal, Long pedidoId, String email) {
        try {
            String chave = (pixChave != null && !pixChave.isBlank()) ? pixChave : email;
            String nome = pixNome != null ? pixNome : "ClickFarma LTDA";
            String cidade = pixCidade != null ? pixCidade : "SAO PAULO";

            chave = chave.replaceAll("[^a-zA-Z0-9.@\\-]", "");
            String txid = UUID.randomUUID().toString().replace("-", "").substring(0, 25).toUpperCase();
            String valorStr = String.format("%.2f", valorTotal);

            StringBuilder sb = new StringBuilder();
            appendTlv(sb, "00", "01");
            appendTlv(sb, "01", "12");

            String merchantInfo = emv("00", "br.gov.bcb.pix") + emv("01", chave);
            appendTlv(sb, "26", merchantInfo);

            appendTlv(sb, "52", "0000");
            appendTlv(sb, "53", "986");
            appendTlv(sb, "54", valorStr);
            appendTlv(sb, "58", "BR");
            appendTlv(sb, "59", nome);
            appendTlv(sb, "60", cidade);

            String additional = emv("05", txid);
            appendTlv(sb, "62", additional);

            String payloadSemCRC = sb.toString();
            String crc = calcularCRC16(payloadSemCRC + "6304");
            sb.append("6304").append(crc);

            String copiaECola = sb.toString();

            QRCodeWriter qrWriter = new QRCodeWriter();
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(
                    qrWriter.encode(copiaECola, BarcodeFormat.QR_CODE, 300, 300)
            );

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            String qrBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            long simulatedId = -1 * pedidoId;
            OffsetDateTime expiracao = OffsetDateTime.now().plusMinutes(30);

            simulatedPayments.put(String.valueOf(simulatedId),
                    new SimulatedPix(pedidoId, System.currentTimeMillis() + 20_000));

            log.info("PIX SIMULADO gerado para pedido {} (chave={})", pedidoId, chave);
            return new CriarPixResponse(qrBase64, copiaECola, expiracao.toString(), simulatedId);

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Erro ao gerar QR Code: " + e.getMessage(), e);
        }
    }

    private static void appendTlv(StringBuilder sb, String id, String value) {
        sb.append(id);
        sb.append(String.format("%02d", value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length));
        sb.append(value);
    }

    private static String emv(String id, String value) {
        return id + String.format("%02d", value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length) + value;
    }

    private static String calcularCRC16(String payload) {
        int crc = 0xFFFF;
        for (byte b : payload.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }
        return String.format("%04X", crc & 0xFFFF);
    }

    public String consultarStatusPagamento(Long paymentId) {
        if (paymentId != null && paymentId < 0) {
            return consultarStatusSimulado(String.valueOf(paymentId));
        }
        try {
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(paymentId);
            return payment.getStatus();
        } catch (MPApiException e) {
            if (e.getStatusCode() == 401 || e.getStatusCode() == 404) {
                return consultarStatusSimulado(String.valueOf(paymentId));
            }
            log.error("Erro ao consultar pagamento {}: {}", paymentId, e.getMessage());
            return "unknown";
        } catch (Exception e) {
            log.error("Erro ao consultar pagamento {}: {}", paymentId, e.getMessage());
            return "unknown";
        }
    }

    private String consultarStatusSimulado(String paymentIdStr) {
        SimulatedPix sim = simulatedPayments.get(paymentIdStr);
        if (sim == null) return "unknown";
        if (System.currentTimeMillis() >= sim.aprovaEm()) {
            log.info("PIX simulado aprovado! paymentId={}", paymentIdStr);
            return "approved";
        }
        return "pending";
    }

    public boolean isSimulado(Long paymentId) {
        return paymentId != null && paymentId < 0;
    }

    public record CriarPixResponse(String qrCodeBase64, String copiaECola, String expiracao, Long pagamentoId) {}
    private record SimulatedPix(Long pedidoId, long aprovaEm) {}

    private String normalizeNotificationUrl(String raw) {
        if (raw == null) return null;
        String url = raw.trim();
        if (url.isBlank()) return null;
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
            return null;
        }
        return url;
    }
}
