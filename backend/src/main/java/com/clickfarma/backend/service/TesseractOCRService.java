package com.clickfarma.backend.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.util.Base64;

@Service
@Slf4j
public class TesseractOCRService {

    private final Tesseract tesseract;

    public TesseractOCRService() {
        this.tesseract = new Tesseract();
        // Configurar idioma português
        this.tesseract.setLanguage("por");
        // Caminho do Tesseract
        this.tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata/");
        // Configurações para melhor leitura de letra médica
        this.tesseract.setPageSegMode(6); // Assume um único bloco de texto
        this.tesseract.setOcrEngineMode(1); // Modo LSTM mais preciso

        // Configurar variáveis do Tesseract para melhorar leitura
        this.tesseract.setTessVariable("tessedit_char_whitelist",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789mg/,:;.() -");
        this.tesseract.setTessVariable("classify_bln_numeric_mode", "0");
    }

    /**
     * Pré-processa a imagem para melhorar a precisão do OCR
     * Especialmente útil para letras de médico e imagens com baixa qualidade
     */
    private BufferedImage preprocessImage(BufferedImage original) {
        try {
            log.debug("Pré-processando imagem: {}x{}", original.getWidth(), original.getHeight());

            // 1. Converter para escala de cinza
            BufferedImage gray = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = gray.createGraphics();
            g.drawImage(original, 0, 0, null);
            g.dispose();

            // 2. Aumentar contraste e binarizar (preto e branco)
            BufferedImage binary = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D g2d = binary.createGraphics();
            g2d.drawImage(gray, 0, 0, null);
            g2d.dispose();

            // 3. Redimensionar para 2x (ajuda com letras pequenas e ilegíveis)
            int newWidth = binary.getWidth() * 2;
            int newHeight = binary.getHeight() * 2;
            BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D g3d = resized.createGraphics();
            g3d.drawImage(binary.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
            g3d.dispose();

            log.debug("✅ Imagem pré-processada: {}x{} -> {}x{}", original.getWidth(), original.getHeight(), newWidth, newHeight);
            return resized;

        } catch (Exception e) {
            log.warn("Erro no pré-processamento da imagem: {}", e.getMessage());
            return original; // Retorna a imagem original se o pré-processamento falhar
        }
    }

    public String extrairTexto(String imagemBase64) {
        log.info("Iniciando Tesseract OCR");

        try {
            // Limpar o Base64
            String imagemLimpa = imagemBase64;
            if (imagemBase64.contains(",")) {
                imagemLimpa = imagemBase64.substring(imagemBase64.indexOf(",") + 1);
            }

            // Converter Base64 para imagem
            byte[] imageBytes = Base64.getDecoder().decode(imagemLimpa);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);

            if (image == null) {
                log.error("Não foi possível ler a imagem");
                return "Erro: Não foi possível ler a imagem";
            }

            // APLICAR PRÉ-PROCESSAMENTO NA IMAGEM
            BufferedImage processedImage = preprocessImage(image);

            // Tentativa 1: OCR com imagem pré-processada
            log.info("Executando OCR na imagem pré-processada...");
            String texto = tesseract.doOCR(processedImage);

            // Se não encontrou texto ou texto muito curto, tenta com imagem original
            if (texto == null || texto.trim().isEmpty() || texto.length() < 10) {
                log.warn("Pouco texto encontrado na imagem pré-processada, tentando com imagem original...");
                texto = tesseract.doOCR(image);
            }

            // Se ainda não encontrou texto, tenta com configurações diferentes
            if (texto == null || texto.trim().isEmpty() || texto.length() < 10) {
                log.warn("Tentando OCR com configurações alternativas...");
                this.tesseract.setPageSegMode(3); // Modo automático
                texto = tesseract.doOCR(processedImage);
                this.tesseract.setPageSegMode(6); // Voltar ao modo original
            }

            if (texto == null || texto.trim().isEmpty()) {
                log.warn("Nenhum texto encontrado na imagem");
                return "Nenhum texto encontrado na imagem";
            }

            // Limpar o texto extraído (remover caracteres estranhos)
            texto = limparTextoExtraido(texto);

            log.info("Texto extraído com sucesso: {} caracteres", texto.length());
            log.debug("Conteúdo extraído: {}", texto);

            return texto;

        } catch (TesseractException e) {
            log.error("Erro Tesseract: {}", e.getMessage());
            return "Erro ao processar OCR: " + e.getMessage();
        } catch (Exception e) {
            log.error("Erro geral: {}", e.getMessage());
            return "Erro ao processar imagem: " + e.getMessage();
        }
    }

    /**
     * Limpa o texto extraído, removendo caracteres que podem atrapalhar a IA
     */
    private String limparTextoExtraido(String texto) {
        if (texto == null) return "";

        // Remover múltiplos espaços
        String limpo = texto.replaceAll("\\s+", " ");

        // Remover caracteres muito estranhos (mantém letras, números, pontuação básica)
        limpo = limpo.replaceAll("[^\\p{L}\\p{N}\\s\\.,;:()/-]", "");

        // Remover linhas muito curtas (possíveis erros)
        String[] linhas = limpo.split("\n");
        StringBuilder resultado = new StringBuilder();
        for (String linha : linhas) {
            if (linha.trim().length() > 3) { // Mantém apenas linhas com mais de 3 caracteres
                resultado.append(linha).append("\n");
            }
        }

        return resultado.toString().trim();
    }
}
