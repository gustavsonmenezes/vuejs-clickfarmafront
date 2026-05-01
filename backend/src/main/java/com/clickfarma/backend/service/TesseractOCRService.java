package com.clickfarma.backend.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
<<<<<<< main
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
=======

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
>>>>>>> main
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TesseractOCRService {
    private static final Logger log = LoggerFactory.getLogger(TesseractOCRService.class);

    private static final Logger log = LoggerFactory.getLogger(TesseractOCRService.class);

    /**
     * PSM: bloco uniforme, texto esparso (manuscrito), OSD+esparso, página automática.
     */
    private static final List<Integer> PAGE_SEG_MODES = List.of(6, 11, 12, 3);

    @Value("${receita.ocr.cropBottomFraction:0.78}")
    private float cropBottomFraction;

    /**
     * Lista de palavras para o Tesseract reconhecer melhor (sem "pós-correção" no output).
     * Pode ser desligado setando `receita.tesseract.useUserWords=false`.
     */
    @Value("${receita.tesseract.useUserWords:true}")
    private boolean useUserWords;

    private static volatile Path userWordsTempFile;

    public String extrairTexto(String imagemBase64) {
        log.info("Iniciando Tesseract OCR");

        try {
            String imagemLimpa = imagemBase64;
            if (imagemBase64.contains(",")) {
                imagemLimpa = imagemBase64.substring(imagemBase64.indexOf(",") + 1);
            }

            byte[] imageBytes = Base64.getDecoder().decode(imagemLimpa);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);

            if (image == null) {
                log.error("Não foi possível ler a imagem");
                return "Erro: Não foi possível ler a imagem";
            }

            String texto = extractBestText(image);
            if (texto == null || texto.isBlank()) {
                log.warn("Nenhum texto encontrado na imagem");
                return "Nenhum texto encontrado na imagem";
            }

            log.info("Texto extraído com sucesso: {} caracteres", texto.length());
            log.debug("Conteúdo extraído: {}", texto);
            return texto;

        } catch (TesseractException e) {
            log.error("Erro Tesseract", e);
            return "Erro ao processar OCR: " + e.getMessage();
        } catch (Exception e) {
            log.error("Erro geral ao processar imagem", e);
            return "Erro ao processar imagem: " + e.getMessage();
        }
    }

    private String extractBestText(BufferedImage image) throws TesseractException {
        Map<String, BufferedImage> variants = preprocessVariants(image);
        String bestText = "";
        int bestScore = Integer.MIN_VALUE;
        String bestAttempt = "none";

        for (Map.Entry<String, BufferedImage> entry : variants.entrySet()) {
            for (Integer pageSegMode : PAGE_SEG_MODES) {
                String attemptName = entry.getKey() + "-psm" + pageSegMode;
                String rawText = createTesseract(pageSegMode).doOCR(entry.getValue());
                String cleanedText = limparTextoExtraido(rawText);
                int score = scoreText(cleanedText);

                log.debug("Tentativa OCR {}: score={}, chars={}", attemptName, score, cleanedText.length());

                if (score > bestScore) {
                    bestScore = score;
                    bestText = cleanedText;
                    bestAttempt = attemptName;
                }
            }
        }

        log.info("Melhor leitura do Tesseract: {} (score={}, chars={})", bestAttempt, bestScore, bestText.length());
        return bestText;
    }

    private Tesseract createTesseract(int pageSegMode) {
        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage("por");
        tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata/");
        tesseract.setPageSegMode(pageSegMode);
        tesseract.setOcrEngineMode(1);
        tesseract.setTessVariable("classify_bln_numeric_mode", "0");
        tesseract.setTessVariable("preserve_interword_spaces", "1");
        if (useUserWords) {
            Path words = resolveUserWordsFile();
            if (words != null) {
                // Tesseract expects a filesystem path.
                tesseract.setTessVariable("user_words_file", words.toAbsolutePath().toString());
            }
        }
        return tesseract;
    }

    private Path resolveUserWordsFile() {
        if (userWordsTempFile != null) {
            return userWordsTempFile;
        }
        synchronized (TesseractOCRService.class) {
            if (userWordsTempFile != null) {
                return userWordsTempFile;
            }
            try (InputStream in = TesseractOCRService.class.getClassLoader()
                    .getResourceAsStream("tesseract/user_words_receita.txt")) {
                if (in == null) {
                    return null;
                }
                Path tmp = Files.createTempFile("clickfarma-user-words-", ".txt");
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                tmp.toFile().deleteOnExit();
                userWordsTempFile = tmp;
                return tmp;
            } catch (Exception e) {
                log.warn("Falha ao carregar user_words do Tesseract: {}", e.getMessage());
                return null;
            }
        }
    }

    private Map<String, BufferedImage> preprocessVariants(BufferedImage original) {
        Map<String, BufferedImage> variants = new LinkedHashMap<>();

        try {
            BufferedImage normalized = OcrImageEnhancer.normalizeToRgbWhiteBackground(original);
            BufferedImage cropped = OcrImageEnhancer.cropForPrescription(normalized, cropBottomFraction);
            if (cropped == null) {
                cropped = normalized;
            }
            BufferedImage enhanced = OcrImageEnhancer.enhanceForHandwritingOcr(cropped);
            BufferedImage gray = OcrImageEnhancer.toGrayscale(cropped);
            BufferedImage adaptive = OcrImageEnhancer.adaptiveMeanBinary(gray, 43, 11);
            BufferedImage adaptive2 = OcrImageEnhancer.adaptiveMeanBinary(gray, 31, 9);

            variants.put("original", OcrImageEnhancer.upscaleByFactor(cropped, 2));
            variants.put("handwriting", OcrImageEnhancer.upscaleByFactor(enhanced != null ? enhanced : cropped, 3));
            variants.put("grayscale", OcrImageEnhancer.upscaleByFactor(gray, 3));
            variants.put("high-contrast", OcrImageEnhancer.upscaleByFactor(OcrImageEnhancer.applyContrast(gray, 1.55f, -18f), 3));
            variants.put("binary", OcrImageEnhancer.upscaleByFactor(OcrImageEnhancer.toBinary(gray), 3));
            variants.put("adaptive", OcrImageEnhancer.upscaleByFactor(adaptive, 2));
            variants.put("adaptive2", OcrImageEnhancer.upscaleByFactor(adaptive2, 2));
        } catch (Exception e) {
            log.warn("Erro ao gerar variantes da imagem para OCR: {}", e.getMessage());
            variants.clear();
            variants.put("original", original);
        }

        return variants;
    }

    private int scoreText(String text) {
        if (text == null || text.isBlank()) {
            return Integer.MIN_VALUE / 4;
        }

        int score = 0;
        int letters = 0;
        int digits = 0;
        int strangeChars = 0;
        int lines = 0;

        for (char ch : text.toCharArray()) {
            if (Character.isLetter(ch)) {
                letters++;
            } else if (Character.isDigit(ch)) {
                digits++;
            } else if (ch == '\n') {
                lines++;
            } else if (!Character.isWhitespace(ch) && "/,:;.()%-+×x".indexOf(ch) < 0) {
                strangeChars++;
            }
        }

        score += Math.min(text.length(), 240);
        score += Math.min(letters, 160);
        score += Math.min(digits * 2, 60);
        score += Math.min(lines * 6, 48);
        score -= strangeChars * 4;

        String normalized = text.toLowerCase();
        if (normalized.contains("mg")) score += 20;
        if (normalized.contains("ml")) score += 16;
        if (normalized.contains("comprim")) score += 18;
        if (normalized.contains("capsul")) score += 18;
        if (normalized.contains("tomar")) score += 12;
        if (normalized.contains("8/8")) score += 16;
        if (normalized.contains("12/12")) score += 16;
        if (normalized.contains("1x")) score += 8;
        if (normalized.contains("ao dia")) score += 8;
        if (normalized.contains("receitu") || normalized.contains("uso ")) score += 10;

        return score;
    }

    private String limparTextoExtraido(String texto) {
        if (texto == null) {
            return "";
        }

        String normalized = texto
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\t', ' ');

        String[] linhas = normalized.split("\n");
        StringBuilder resultado = new StringBuilder();

        for (String linha : linhas) {
            String limpa = linha
                    .replaceAll("\\s+", " ")
                    .replaceAll("[^\\p{L}\\p{N}\\s\\.,;:()/%+\\-×x°º]", "")
                    .trim();

            if (limpa.length() >= 2) {
                resultado.append(limpa).append('\n');
            }
        }

        return resultado.toString().trim();
    }
}
