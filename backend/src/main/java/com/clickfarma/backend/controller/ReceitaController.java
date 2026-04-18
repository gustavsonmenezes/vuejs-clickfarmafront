package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.ReceitaRequestDTO;
import com.clickfarma.backend.service.TesseractOCRService;
import com.clickfarma.backend.service.OCRService;
import com.clickfarma.backend.service.GroqProcessadorReceitaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/receita")
@CrossOrigin(origins = {"http://localhost", "http://localhost:8080", "http://localhost:8081", "http://localhost:8082"})
@Slf4j
public class ReceitaController {

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

        // Tentar OCR Space primeiro (mais preciso para letras manuscritas)
        log.info("Tentando OCR Space API...");
        return ocrService.extrairTextoDeImagem(request.getImagemBase64())
                .flatMap(textoExtraido -> {
                    // Se OCR Space falhar, tentar Tesseract
                    if (textoExtraido.startsWith("Erro") || textoExtraido.equals("Nenhum texto encontrado na imagem")) {
                        log.warn("OCR Space falhou, tentando Tesseract...");
                        String textoTesseract = tesseractOCRService.extrairTexto(request.getImagemBase64());

                        if (textoTesseract.startsWith("Erro") || textoTesseract.equals("Nenhum texto encontrado na imagem")) {
                            log.error("Ambos OCR Space e Tesseract falharam");
                            Map<String, Object> errorResponse = new HashMap<>();
                            errorResponse.put("sucesso", false);
                            errorResponse.put("erro", "Não foi possível extrair texto da imagem. Tente com uma imagem de melhor qualidade.");
                            return Mono.just(ResponseEntity.ok(errorResponse));
                        }

                        return processarTextoComGroq(textoTesseract);
                    }

                    log.info("✅ OCR Space bem-sucedido");
                    return processarTextoComGroq(textoExtraido);
                })
                .onErrorResume(erro -> {
                    log.error("Erro geral no processamento da receita", erro);

                    // Fallback para Tesseract se OCR Space falhar completamente
                    log.info("Tentando Tesseract como último recurso...");
                    String textoTesseract = tesseractOCRService.extrairTexto(request.getImagemBase64());

                    if (textoTesseract.startsWith("Erro")) {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("sucesso", false);
                        errorResponse.put("erro", "Erro ao processar imagem: " + erro.getMessage());
                        return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
                    }

                    return processarTextoComGroq(textoTesseract);
                });
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
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }
}