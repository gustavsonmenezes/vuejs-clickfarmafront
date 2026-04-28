package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.ReceitaRequestDTO;
import com.clickfarma.backend.service.TesseractOCRService;
import com.clickfarma.backend.service.OCRService;
import com.clickfarma.backend.service.GroqProcessadorReceitaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/receita")
@CrossOrigin(origins = {"http://localhost", "http://localhost:8080", "http://localhost:8081", "http://localhost:8082"})
public class ReceitaController {

    private static final Logger log = LoggerFactory.getLogger(ReceitaController.class);

    @Autowired
    private TesseractOCRService tesseractOCRService;

    @Autowired
    private OCRService ocrService;

    @Autowired
    private GroqProcessadorReceitaService groqProcessadorReceitaService;

    /**
     * Processa uma receita médica extraindo medicamentos via IA
     * Tenta OCR Space primeiro (mais preciso), depois Tesseract como fallback
     */
    @PostMapping("/processar")
    public Mono<ResponseEntity<Map<String, Object>>> processarReceita(@RequestBody ReceitaRequestDTO request) {
        log.info("Recebida requisição para processar receita: {}", request.getNomeArquivo());

        if (request.getImagemBase64() == null || request.getImagemBase64().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("sucesso", false);
            errorResponse.put("erro", "Imagem não fornecida");
            return Mono.just(ResponseEntity.badRequest().body(errorResponse));
        }

        // OCR Space (rede) + Tesseract (CPU/bloqueante). Rodamos em paralelo e tiramos o Tesseract do thread de I/O.
        log.info("Tentando OCR Space API...");

        Mono<String> ocrSpaceMono = ocrService.extrairTextoDeImagem(request.getImagemBase64())
                .timeout(Duration.ofSeconds(35))
                .onErrorReturn("Erro OCR Space: timeout");

        Mono<String> tesseractMono = Mono.fromCallable(() -> tesseractOCRService.extrairTexto(request.getImagemBase64()))
                .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(ocrSpaceMono, tesseractMono)
                .flatMap(tuple -> {
                    String textoExtraido = tuple.getT1();
                    String textoTesseract = tuple.getT2();

                    boolean ocrSpaceFalhou = textoInvalido(textoExtraido);
                    boolean tesseractFalhou = textoInvalido(textoTesseract);

                    if (ocrSpaceFalhou && tesseractFalhou) {
                        log.error("Ambos OCR Space e Tesseract falharam");
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("sucesso", false);
                        errorResponse.put("erro", "Não foi possível extrair texto da imagem. Tente com uma imagem de melhor qualidade.");
                        return Mono.just(ResponseEntity.ok(errorResponse));
                    }

                    if (ocrSpaceFalhou) {
                        log.warn("OCR Space falhou, tentando Tesseract...");
                        return processarTextoComGroq(textoTesseract);
                    }

                    if (tesseractFalhou) {
                        log.info("✅ OCR Space bem-sucedido; Tesseract não trouxe complemento utilizável");
                        return processarTextoComGroq(textoExtraido);
                    }

	                    String melhor = escolherMelhorLeituraOcr(textoExtraido, textoTesseract);
	                    // Decide pela leitura que rende mais itens determinísticos (nomes), não só pelo tamanho do texto.
	                    int countA = groqProcessadorReceitaService.contarMedicamentosDeterministic(textoExtraido);
	                    int countB = groqProcessadorReceitaService.contarMedicamentosDeterministic(textoTesseract);
	                    if (countB > countA) {
	                        melhor = textoTesseract;
	                    } else if (countA > countB) {
	                        melhor = textoExtraido;
	                    }
	                    log.info("✅ OCR Space e Tesseract processados; usando '{}' para extração (itens: ocrSpace={}, tesseract={})",
	                            melhor == textoTesseract ? "Tesseract" : "OCR Space", countA, countB);
                    return processarTextoComGroq(melhor);
                })
                .onErrorResume(erro -> {
                    log.error("Erro geral no processamento da receita", erro);

                    // Fallback para Tesseract se OCR Space falhar completamente
                    log.info("Tentando Tesseract como último recurso...");
                    return Mono.fromCallable(() -> tesseractOCRService.extrairTexto(request.getImagemBase64()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(textoTesseract -> {
                                if (textoTesseract.startsWith("Erro")) {
                                    Map<String, Object> errorResponse = new HashMap<>();
                                    errorResponse.put("sucesso", false);
                                    errorResponse.put("erro", "Erro ao processar imagem: " + erro.getMessage());
                                    return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
                                }
                                return processarTextoComGroq(textoTesseract);
                            });
                });
    }

    private String escolherMelhorLeituraOcr(String textoOcrSpace, String textoTesseract) {
        boolean aOk = !textoInvalido(textoOcrSpace);
        boolean bOk = !textoInvalido(textoTesseract);
        if (aOk && !bOk) return textoOcrSpace;
        if (!aOk && bOk) return textoTesseract;
        if (!aOk) return textoOcrSpace;
        // Heurística simples: usar a leitura mais longa tende a preservar mais itens/linhas.
        return textoTesseract != null && textoOcrSpace != null && textoTesseract.length() >= textoOcrSpace.length()
                ? textoTesseract
                : textoOcrSpace;
    }

    private boolean textoInvalido(String texto) {
        return texto == null
                || texto.isBlank()
                || texto.startsWith("Erro")
                || texto.equals("Nenhum texto encontrado na imagem")
                || texto.equals("Nenhum texto identificado na imagem.");
    }

    private String combinarLeiturasOCR(String textoOcrSpace, String textoTesseract) {
        if (textoOcrSpace.equalsIgnoreCase(textoTesseract)) {
            return textoOcrSpace;
        }

        return """
                [LEITURA_OCR_SPACE]
                %s

                [LEITURA_TESSERACT]
                %s
                """.formatted(textoOcrSpace, textoTesseract);
    }

    /**
     * Processa o texto extraído com o Groq para identificar medicamentos
     */
    private Mono<ResponseEntity<Map<String, Object>>> processarTextoComGroq(String textoExtraido) {
        return groqProcessadorReceitaService.processarReceita(textoExtraido)
                .map(medicamentos -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sucesso", true);
                    response.put("medicamentos", medicamentos.getMedicamentos());
                    response.put("textoOriginal", medicamentos.getTextoOriginal());
                    response.put("mensagemOrientacao", medicamentos.getMensagemOrientacao());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(erro -> {
                    log.error("Erro no processamento da receita com Groq", erro);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("sucesso", false);
                    errorResponse.put("erro", "Erro ao processar receita: " + erro.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
                });
    }

    /**
     * Verifica a saúde do serviço de receita
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("ocrService", "OCR Space + Tesseract");
        response.put("aiService", "Groq LLM");
        response.put("groqConfigured", String.valueOf(groqProcessadorReceitaService != null && groqProcessadorReceitaService.isGroqConfigured()));
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }
}
