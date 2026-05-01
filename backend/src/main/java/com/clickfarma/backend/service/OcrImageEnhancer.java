package com.clickfarma.backend.service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Pré-processamento para OCR de receitas manuscritas: contraste irregular, fotos escuras,
 * resolução baixa e traço fino.
 */
public final class OcrImageEnhancer {

    private static final int MIN_EDGE_BEFORE_UPSCALE = 1400;
    private static final int TARGET_MIN_LONG_EDGE = 1900;
    private static final int MAX_LONG_EDGE = 4000;

    private OcrImageEnhancer() {
    }

    /**
     * Corte simples para receitas: remove bordas e, opcionalmente, corta o rodapé (onde costuma vir
     * texto institucional tipo "fumar faz mal..."), melhorando a leitura dos itens prescritos.
     */
    public static BufferedImage cropForPrescription(BufferedImage original, float bottomFraction) {
        if (original == null) {
            return null;
        }
        BufferedImage rgb = normalizeToRgbWhiteBackground(original);
        BufferedImage gray = toGrayscale(rgb);
        BufferedImage cropped = cropToContent(gray, 12);
        if (cropped == null) {
            cropped = gray;
        }
        if (bottomFraction > 0.0f && bottomFraction < 1.0f) {
            int h = cropped.getHeight();
            int newH = Math.max(1, Math.min(h, Math.round(h * bottomFraction)));
            if (newH < h) {
                cropped = cropped.getSubimage(0, 0, cropped.getWidth(), newH);
            }
        }
        return grayscaleToRgb(cropped);
    }

    private static BufferedImage cropToContent(BufferedImage gray, int padding) {
        int w = gray.getWidth();
        int h = gray.getHeight();
        int minX = w, minY = h, maxX = -1, maxY = -1;

        // Threshold alto: fundo costuma ser claro; qualquer coisa mais escura vira "conteúdo".
        final int thr = 245;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int g = gray.getRGB(x, y) & 0xFF;
                if (g < thr) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX < 0 || maxY < 0) {
            return null;
        }

        minX = Math.max(0, minX - padding);
        minY = Math.max(0, minY - padding);
        maxX = Math.min(w - 1, maxX + padding);
        maxY = Math.min(h - 1, maxY + padding);

        int cw = Math.max(1, maxX - minX + 1);
        int ch = Math.max(1, maxY - minY + 1);
        // Evita crop ridículo que provavelmente é ruído.
        if (cw < w * 0.35 || ch < h * 0.35) {
            return null;
        }
        return gray.getSubimage(minX, minY, cw, ch);
    }

    public static BufferedImage decodeBase64(String imagemBase64) throws IOException {
        String imagemLimpa = imagemBase64;
        if (imagemBase64 != null && imagemBase64.contains(",")) {
            imagemLimpa = imagemBase64.substring(imagemBase64.indexOf(",") + 1);
        }
        byte[] bytes = Base64.getDecoder().decode(imagemLimpa);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        return img;
    }

    public static String encodePngBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Pipeline usado antes do OCR.space: fundo claro, eventual inversão, nitidez por upscaling e contraste.
     */
    public static BufferedImage enhanceForHandwritingOcr(BufferedImage original) {
        if (original == null) {
            return null;
        }
        BufferedImage rgb = normalizeToRgbWhiteBackground(original);
        rgb = invertIfDarkPage(rgb);
        BufferedImage gray = toGrayscale(rgb);
        gray = upscaleIfTooSmall(gray);
        gray = applyContrast(gray, 1.42f, -10f);
        return grayscaleToRgb(gray);
    }

    public static BufferedImage normalizeToRgbWhiteBackground(BufferedImage original) {
        BufferedImage rgb = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return rgb;
    }

    public static BufferedImage toGrayscale(BufferedImage original) {
        BufferedImage gray = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return gray;
    }

    public static BufferedImage applyContrast(BufferedImage source, float factor, float offset) {
        BufferedImage adjusted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int value = source.getRGB(x, y) & 0xFF;
                int contrasted = Math.max(0, Math.min(255, Math.round((value - 128) * factor + 128 + offset)));
                int packed = (contrasted << 16) | (contrasted << 8) | contrasted;
                adjusted.setRGB(x, y, packed);
            }
        }
        return adjusted;
    }

    public static BufferedImage toBinary(BufferedImage source) {
        int threshold = estimateThreshold(source);
        BufferedImage binary = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int gray = source.getRGB(x, y) & 0xFF;
                int pixel = gray < threshold ? 0 : 255;
                int packed = (pixel << 16) | (pixel << 8) | pixel;
                binary.setRGB(x, y, packed);
            }
        }
        return binary;
    }

    /**
     * Binarização adaptativa (Sauvola-style com janela fixa): melhor para sombras e manuscrito sobre papel amarelado.
     */
    public static BufferedImage adaptiveMeanBinary(BufferedImage gray, int blockSize, int c) {
        int w = gray.getWidth();
        int h = gray.getHeight();
        if (blockSize < 3) {
            blockSize = 3;
        }
        if ((blockSize & 1) == 0) {
            blockSize++;
        }
        int half = blockSize / 2;
        int[][] integral = buildIntegralGray(gray);
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int x1 = Math.max(0, x - half);
                int y1 = Math.max(0, y - half);
                int x2 = Math.min(w - 1, x + half);
                int y2 = Math.min(h - 1, y + half);
                int count = (x2 - x1 + 1) * (y2 - y1 + 1);
                int sum = rectSum(integral, x1, y1, x2, y2);
                int mean = count == 0 ? 128 : sum / count;
                int pix = (gray.getRGB(x, y) & 0xFF) < mean - c ? 0 : 255;
                int packed = (pix << 16) | (pix << 8) | pix;
                out.setRGB(x, y, packed);
            }
        }
        return out;
    }

    private static BufferedImage grayscaleToRgb(BufferedImage gray) {
        BufferedImage out = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(gray, 0, 0, null);
        g.dispose();
        return out;
    }

    private static BufferedImage invertIfDarkPage(BufferedImage rgb) {
        long sum = 0;
        int n = rgb.getWidth() * rgb.getHeight();
        for (int y = 0; y < rgb.getHeight(); y++) {
            for (int x = 0; x < rgb.getWidth(); x++) {
                int p = rgb.getRGB(x, y);
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = p & 0xFF;
                sum += (r + g + b) / 3;
            }
        }
        int avg = n == 0 ? 255 : (int) (sum / n);
        if (avg >= 118) {
            return rgb;
        }
        BufferedImage inv = new BufferedImage(rgb.getWidth(), rgb.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < rgb.getHeight(); y++) {
            for (int x = 0; x < rgb.getWidth(); x++) {
                int p = rgb.getRGB(x, y);
                int r = 255 - ((p >> 16) & 0xFF);
                int g = 255 - ((p >> 8) & 0xFF);
                int b = 255 - (p & 0xFF);
                inv.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return inv;
    }

    private static BufferedImage upscaleIfTooSmall(BufferedImage gray) {
        int w = gray.getWidth();
        int h = gray.getHeight();
        int longEdge = Math.max(w, h);
        if (longEdge > MAX_LONG_EDGE) {
            double shrink = (double) MAX_LONG_EDGE / longEdge;
            return scaleGrayscaleToSize(gray, (int) Math.round(w * shrink), (int) Math.round(h * shrink));
        }
        if (longEdge >= MIN_EDGE_BEFORE_UPSCALE) {
            return gray;
        }
        double scale = (double) TARGET_MIN_LONG_EDGE / longEdge;
        int nw = Math.max(1, (int) Math.round(w * scale));
        int nh = Math.max(1, (int) Math.round(h * scale));
        if (Math.max(nw, nh) > MAX_LONG_EDGE) {
            double cap = (double) MAX_LONG_EDGE / Math.max(nw, nh);
            nw = Math.max(1, (int) Math.round(nw * cap));
            nh = Math.max(1, (int) Math.round(nh * cap));
        }
        return scaleGrayscaleToSize(gray, nw, nh);
    }

    public static BufferedImage upscaleByFactor(BufferedImage source, int factor) {
        int width = Math.max(1, source.getWidth() * factor);
        int height = Math.max(1, source.getHeight() * factor);
        if (source.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return scaleGrayscaleToSize(source, width, height);
        }
        if (source.getType() == BufferedImage.TYPE_BYTE_BINARY) {
            BufferedImage onWhite = normalizeToRgbWhiteBackground(source);
            return scaleRgbToSize(onWhite, width, height);
        }
        return scaleRgbToSize(source, width, height);
    }

    private static BufferedImage scaleGrayscaleToSize(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();
        return resized;
    }

    private static BufferedImage scaleRgbToSize(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(source.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();
        return resized;
    }

    private static int estimateThreshold(BufferedImage source) {
        long total = 0;
        long count = 0;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                total += source.getRGB(x, y) & 0xFF;
                count++;
            }
        }
        int average = count == 0 ? 160 : (int) (total / count);
        return Math.max(115, Math.min(190, average - 10));
    }

    private static int[][] buildIntegralGray(BufferedImage gray) {
        int w = gray.getWidth();
        int h = gray.getHeight();
        int[][] s = new int[h + 1][w + 1];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int g = gray.getRGB(x, y) & 0xFF;
                s[y + 1][x + 1] = g + s[y][x + 1] + s[y + 1][x] - s[y][x];
            }
        }
        return s;
    }

    private static int rectSum(int[][] integral, int x1, int y1, int x2, int y2) {
        return integral[y2 + 1][x2 + 1] - integral[y1][x2 + 1] - integral[y2 + 1][x1] + integral[y1][x1];
    }
}
