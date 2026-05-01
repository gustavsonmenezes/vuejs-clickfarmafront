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
     * Processa uma receita médica extraindo medicamentos via IA Vision (Llama 3.2 90B Vision).
     * Envia a imagem diretamente para o modelo de visão, com OCR como fallback complementar.
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

        String imagemBase64 = request.getImagemBase64().contains(",")
                ? request.getImagemBase64().substring(request.getImagemBase64().indexOf(",") + 1)
                : request.getImagemBase64();

        return groqProcessadorReceitaService.processarReceitaComVision(imagemBase64)
                .flatMap(dto -> {
                    if (dto.getMedicamentos() == null || dto.getMedicamentos().isEmpty()) {
                        if (dto.getMensagemOrientacao() != null && dto.getMensagemOrientacao().toLowerCase().contains("não foi possível")) {
                            log.warn("Vision falhou, tentando fallback OCR...");
                            return tentarFallbackOcr(request.getImagemBase64());
                        }
                    }
                    Map<String, Object> response = new HashMap<>();
                    response.put("sucesso", true);
                    response.put("medicamentos", dto.getMedicamentos());
                    response.put("textoOriginal", dto.getTextoOriginal());
                    response.put("mensagemOrientacao", dto.getMensagemOrientacao());
                    return Mono.just(ResponseEntity.ok(response));
                })
                .onErrorResume(erro -> {
                    log.error("Erro no processamento Vision, tentando fallback OCR...", erro);
                    return tentarFallbackOcr(request.getImagemBase64());
                });
    }

    private Mono<ResponseEntity<Map<String, Object>>> tentarFallbackOcr(String imagemBase64) {
        Mono<String> ocrSpaceMono = ocrService.extrairTextoDeImagem(imagemBase64)
                .timeout(java.time.Duration.ofSeconds(35))
                .onErrorReturn("Erro OCR Space: timeout");

        Mono<String> tesseractMono = Mono.fromCallable(() -> tesseractOCRService.extrairTexto(imagemBase64))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());

        return Mono.zip(ocrSpaceMono, tesseractMono)
                .flatMap(tuple -> {
                    String textoExtraido = tuple.getT1();
                    String textoTesseract = tuple.getT2();

                    boolean ocrSpaceFalhou = textoInvalido(textoExtraido);
                    boolean tesseractFalhou = textoInvalido(textoTesseract);

                    if (ocrSpaceFalhou && tesseractFalhou) {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("sucesso", false);
                        errorResponse.put("erro", "Não foi possível extrair texto da imagem. Tente com uma imagem de melhor qualidade.");
                        return Mono.just(ResponseEntity.ok(errorResponse));
                    }

                    String melhor = ocrSpaceFalhou ? textoTesseract : textoExtraido;
                    int countA = groqProcessadorReceitaService.contarMedicamentosDeterministic(textoExtraido);
                    int countB = groqProcessadorReceitaService.contarMedicamentosDeterministic(textoTesseract);
                    if (countB > countA && !tesseractFalhou) {
                        melhor = textoTesseract;
                    }
                    return processarTextoComGroq(melhor);
                })
                .onErrorResume(erro -> {
                    log.error("Erro geral no fallback OCR", erro);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("sucesso", false);
                    errorResponse.put("erro", "Erro ao processar imagem: " + erro.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
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
        response.put("primaryEngine", "Groq Vision (Llama 4 Scout)");
        response.put("fallbackEngine", "OCR Space + Tesseract");
        response.put("groqConfigured", String.valueOf(groqProcessadorReceitaService != null && groqProcessadorReceitaService.isGroqConfigured()));
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }
}
