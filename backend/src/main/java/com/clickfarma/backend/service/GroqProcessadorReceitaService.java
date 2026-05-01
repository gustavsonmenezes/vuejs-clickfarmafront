package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.AlternativaSugeridaDTO;
import com.clickfarma.backend.dto.MedicamentoExtraidoDTO;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroqProcessadorReceitaService {

    private static final Logger log = LoggerFactory.getLogger(GroqProcessadorReceitaService.class);
    /**
     * Termos típicos do "miolo" da receita (cabeçalho/rodapé/orientação) que não devem virar item de medicamento.
     */
    private static final Set<String> RECEITA_NOISE_TOKENS = Set.of(
            "leitura", "ocr", "space",
            "receita", "receituario", "receituário",
            "paciente", "medico", "médico", "crm", "carimbo", "assinatura", "assin",
            "prescricao", "prescrição", "prescrito", "posologia",
            "uso", "oral", "via", "geral", "use", "eral",
            "fumar", "faz", "mal", "saude", "saúde"
    );

    /**
     * Produtos comuns do catálogo que não são medicamentos e não devem aparecer no fluxo de receita.
     */
    private static final Set<String> NAO_MEDICAMENTO_TOKENS = Set.of(
            "lenco", "lenço", "lencos", "lenços", "umedecido", "umedecidos", "huggies",
            "fralda", "fraldas",
            "sabonete", "shampoo", "condicionador", "hidratante", "protetor", "desodorante",
            "absorvente", "preservativo", "algodao", "algodão", "cotton"
    );

    private static final List<String> MEDICAMENTOS_CANONICOS = List.of(
            "azitromicina",
            "dipirona",
            "floratil",
            "amoxicilina",
            "paracetamol",
            "losartana",
            "prednisolona",
            "xarelto"
    );

    @Autowired
    private GroqService groqService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Value("${receita.ai.enabled:false}")
    private boolean receitaAiEnabled;

    @Value("${receita.ocr.autocorrect:false}")
    private boolean receitaOcrAutocorrect;

    @Value("${receita.ocr.lexicon.enabled:true}")
    private boolean receitaOcrLexiconEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isGroqConfigured() {
        return groqService != null && groqService.isConfigured();
    }

    public Mono<MedicamentoExtraidoDTO> processarReceitaComVision(String imagemBase64) {
        log.info("Processando receita com Groq Vision (Llama 4 Scout)");

        if (!isGroqConfigured()) {
            log.warn("Groq não configurada; pulando Vision.");
            MedicamentoExtraidoDTO dto = new MedicamentoExtraidoDTO();
            dto.setTextoOriginal("Vision não disponível - Groq não configurada");
            dto.setMedicamentos(Collections.emptyList());
            dto.setMensagemOrientacao("Serviço de IA não configurado. Contate o administrador.");
            return Mono.just(dto);
        }

        String prompt = """
                Você é um assistente farmacêutico especialista em interpretar RECEITAS MÉDICAS MANUSCRITAS e IMPRESSAS do Brasil.

                ANALISE A IMAGEM DA RECEITA e extraia TODOS os medicamentos prescritos.

                REGRAS CRÍTICAS:
                1. Extraia SOMENTE medicamentos que estão visíveis na imagem. Não invente.
                2. Para cada medicamento, identifique: nome, dosagem (ex: 500mg), posologia (ex: 1 comprimido 8/8h), duração estimada.
                3. Ignore cabeçalho (nome do médico, CRM, clínica) e rodapé ("fumar faz mal à saúde", assinaturas, carimbos).
                4. Não repita o mesmo medicamento.
                5. Se a handwriting for difícil, use contexto farmacêutico brasileiro para inferir o nome correto.

                EXEMPLOS DE CORREÇÃO de handwriting:
                - "azitromicna" -> "Azitromicina"
                - "amoxlcllna" -> "Amoxicilina"
                - "dlpirona" -> "Dipirona"
                - "paracetamol 750" -> "Paracetamol 750mg"

                Responda SOMENTE com JSON válido neste formato:
                {
                  "medicamentos": [
                    {
                      "nome": "nome_corrigido_do_medicamento",
                      "quantidade": 1,
                      "dosagem": "ex: 500mg",
                      "posologia": "ex: 1 comprimido a cada 8 horas",
                      "diasDuracao": 10,
                      "descricao": "descrição breve do uso"
                    }
                  ]
                }

                Se não encontrar nenhum medicamento na imagem, retorne: {"medicamentos": []}
                IMPORTANTE: Retorne apenas JSON válido, sem texto antes ou depois.
                """;

        List<Produto> cacheProdutos = produtoRepository.findAll();
        log.info("Cache de {} produtos carregado para matching", cacheProdutos.size());

        return groqService.chatWithVision(imagemBase64, prompt)
                .map(resposta -> {
                    log.info("Resposta Vision recebida: {} caracteres", resposta.length());
                    return parseRespostaVision(resposta, cacheProdutos);
                })
                .map(this::filtrarItensIrrelevantes);
    }

    private MedicamentoExtraidoDTO parseRespostaVision(String respostaGroq, List<Produto> cacheProdutos) {
        MedicamentoExtraidoDTO dto = new MedicamentoExtraidoDTO();
        dto.setTextoOriginal("EXTRACAO_VISION_LLAMA_4_SCOUT");

        if (respostaGroq == null || respostaGroq.startsWith("Erro")) {
            log.warn("Vision retornou erro: {}", respostaGroq);
            dto.setMedicamentos(Collections.emptyList());
            dto.setMensagemOrientacao("Não foi possível ler a receita com IA. Tente com uma imagem de melhor qualidade.");
            return dto;
        }

        try {
            String respostaLimpa = limparJson(respostaGroq);
            Map<String, Object> resultado = objectMapper.readValue(respostaLimpa, Map.class);

            List<MedicamentoExtraidoDTO.MedicamentoItem> medicamentos = new ArrayList<>();
            Set<String> nomesNormalizados = new HashSet<>();

            if (resultado.containsKey("medicamentos")) {
                List<Map<String, Object>> medsMap = (List<Map<String, Object>>) resultado.get("medicamentos");
                for (Map<String, Object> med : medsMap) {
                    MedicamentoExtraidoDTO.MedicamentoItem item = new MedicamentoExtraidoDTO.MedicamentoItem();
                    item.setNome(sanitizarNomeMedicamento(med.get("nome")));
                    String nomeNorm = normalizarTexto(item.getNome());
                    if (nomeNorm.isBlank() || isNoisePhrase(nomeNorm)) {
                        continue;
                    }
                    if (!nomesNormalizados.add(nomeNorm)) {
                        continue;
                    }
                    item.setQuantidade(parseQuantidade(med.get("quantidade")));
                    item.setDosagem((String) med.get("dosagem"));
                    item.setPosologia((String) med.get("posologia"));
                    item.setDiasDuracao(parseQuantidade(med.get("diasDuracao")));
                    item.setDescricaoIA("EXTRACAO_VISION_LLAMA_4_SCOUT");

                    buscarProdutoNoBanco(item, cacheProdutos);
                    definirSituacaoCatalogo(item);
                    medicamentos.add(item);
                }
            }

            dto.setMedicamentos(medicamentos);
            if (medicamentos.isEmpty()) {
                dto.setMensagemOrientacao(
                    "Não identificamos medicamentos na imagem. Tente uma foto com melhor foco e iluminação.");
            } else {
                dto.setMensagemOrientacao(mensagemOrientacaoFallback(dto));
            }
            return dto;
        } catch (Exception e) {
            log.error("Erro ao processar resposta Vision", e);
            dto.setMedicamentos(Collections.emptyList());
            dto.setMensagemOrientacao("Erro ao interpretar a receita com IA. Tente novamente com outra imagem.");
            return dto;
        }
    }

    public Mono<MedicamentoExtraidoDTO> processarReceita(String textoReceita) {
        log.info("Processando receita com Groq para extração e cálculo de recompra.");

        if (textoReceita == null || textoReceita.length() < 10 || textoReceita.startsWith("Erro")) {
            MedicamentoExtraidoDTO dto = new MedicamentoExtraidoDTO();
            dto.setTextoOriginal(textoReceita);
            return Mono.just(dto);
        }

        // Primeiro tenta extração determinística por padrão de receita (ex.: "1) Floratil 200mg").
        // Isso evita alucinações do LLM e ignora rodapés como "fumar faz mal a saúde".

        MedicamentoExtraidoDTO porPadrao = processarReceitaPorPadrao(textoReceita);
        if (porPadrao.getMedicamentos() != null && !porPadrao.getMedicamentos().isEmpty()) {
            return Mono.just(filtrarItensIrrelevantes(porPadrao));
        }

        if (!receitaAiEnabled) {
            log.info("receita.ai.enabled=false; pulando IA e mantendo apenas extração determinística.");
            return Mono.just(filtrarItensIrrelevantes(processarReceitaSemIA(textoReceita)));
        }

        if (!isGroqConfigured()) {
            log.warn("Groq não configurada; usando extração heurística baseada no OCR + matching no catálogo.");
            return Mono.just(filtrarItensIrrelevantes(processarReceitaSemIA(textoReceita)));
        }

        String textoPreprocessado = preprocessarOCR(textoReceita);

	        String prompt = String.format("""
	                Você é um assistente farmacêutico especialista em interpretar OCR de RECEITAS MANUSCRITAS e IMPRESSAS de farmácia.
	                
	                REGRAS CRÍTICAS:
	                1. O texto OCR pode ter ERROS GRAVES. Use contexto farmacêutico para corrigir nomes de medicamentos.
	                2. Extraia SOMENTE medicamentos que tenham evidência no texto OCR abaixo (ou correção muito próxima). Não invente.
	                3. Não repita o mesmo medicamento; não "complete" quantidade de itens com chutes.
	                4. Ignore frases como "receituário", "uso oral", "via", "paciente", "CRM", e qualquer texto de debug tipo "leitura ocr".
	                5. Para cada medicamento, identifique: nome, dosagem (ex: 500mg), posologia (ex: 1 comprimido 8/8h), duração estimada.
                
	                TEXTO OCR DA RECEITA (PODE CONTER ERROS; preserve as linhas):
	                ---
	                %s
	                ---
                
                EXEMPLOS DE CORREÇÃO:
                - "azitromicna" -> "Azitromicina"
                - "amoxlcllna" -> "Amoxicilina"  
                - "dipirona sodica" -> "Dipirona Sódica"
                - "paracetamol 750" -> "Paracetamol 750mg"
                
                Responda SOMENTE com JSON neste formato:
                {
                  "medicamentos": [
                    {
                      "nome": "nome_corrigido_do_medicamento",
                      "quantidade": 1,
                      "dosagem": "ex: 500mg",
                      "posologia": "ex: 1 comprimido a cada 8 horas",
                      "diasDuracao": 10,
                      "descricao": "descrição breve do uso"
                    }
                  ]
                }
                
	                IMPORTANTE: Retorne apenas os itens que você realmente identificou. Se não tiver certeza, omita.
	                """, textoPreprocessado);

        return groqService.chat(prompt)
                .flatMap(respostaGroq -> {
                    if (respostaGroq == null || respostaGroq.isBlank()) {
                        return Mono.error(new IllegalStateException("Resposta vazia da IA (Groq)"));
                    }
                    if (respostaGroq.startsWith("Erro")) {
                        return Mono.error(new IllegalStateException(respostaGroq));
                    }
                    return Mono.just(respostaGroq);
                })
                .map(respostaGroq -> parsePrimeiraExtracao(respostaGroq, textoReceita))
                .map(this::filtrarItensIrrelevantes);
    }

    private MedicamentoExtraidoDTO processarReceitaPorPadrao(String textoReceita) {
        MedicamentoExtraidoDTO dto = new MedicamentoExtraidoDTO();
        dto.setTextoOriginal(textoReceita);

        List<MedicamentoExtraidoDTO.MedicamentoItem> itens = extrairItensEnumerados(textoReceita);
        if (itens.isEmpty()) {
            itens = extrairItensPorDosagem(textoReceita);
        }
        if (itens.isEmpty() && receitaOcrLexiconEnabled) {
            itens = extrairItensPorLexicon(textoReceita);
        }
        if (!itens.isEmpty()) {
            dto.setMedicamentos(itens);
            dto.setMensagemOrientacao(null);
        } else {
            dto.setMedicamentos(Collections.emptyList());
        }
        return dto;
    }

    /**
     * Contagem rápida para decidir entre OCR Space vs Tesseract sem depender de IA.
     */
    public int contarMedicamentosDeterministic(String textoReceita) {
        try {
            MedicamentoExtraidoDTO dto = processarReceitaPorPadrao(textoReceita);
            return dto.getMedicamentos() == null ? 0 : dto.getMedicamentos().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private List<MedicamentoExtraidoDTO.MedicamentoItem> extrairItensEnumerados(String textoReceita) {
        if (textoReceita == null || textoReceita.isBlank()) {
            return Collections.emptyList();
        }

        String normalized = textoReceita
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        List<MedicamentoExtraidoDTO.MedicamentoItem> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String rawLine : normalized.split("\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            // Formatos comuns: "1) ...", "1. ...", "- ...", "• ..."
            java.util.regex.Matcher m = java.util.regex.Pattern
                    // Alguns OCRs trocam "1." por "O." ou "0." quando a escrita é arredondada.
                    .compile("^(?:(?:\\d{1,2}|[oO0])\\s*[\\)\\.:\\-º°]\\s*|[-•]\\s+)(.+)$")
                    .matcher(line);
            if (!m.find()) {
                continue;
            }

            String content = m.group(1).trim();
            // Corta posologia para manter só o nome/dosagem da primeira linha.
            content = content.split("(?i)\\b(tomar|usar|use|via|a cada|de \\d+ em \\d+|por \\d+|durante|ao dia|x\\s*\\d+)\\b", 2)[0].trim();

            String dosage = extractDosageLoose(content);

            String nome = content
                    .replaceAll("(?i)\\b[0-9oO]{2,5}(?:[\\.,][0-9oO])?\\s*(mg|mq|g|mcg|ml|ui|%)?\\b", " ")
                    .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            if (!nomePareceMedicamento(nome)) {
                continue;
            }

            if (receitaOcrAutocorrect) {
                nome = corrigirNomeReceita(nome, dosage);
            }
            String key = normalizarTexto(nome);
            if (key.isBlank() || !seen.add(key)) {
                continue;
            }

            MedicamentoExtraidoDTO.MedicamentoItem item = new MedicamentoExtraidoDTO.MedicamentoItem();
            item.setNome(nome);
            item.setQuantidade(1);
            item.setDosagem(dosage);
            item.setDescricaoIA("EXTRACAO_OCR_PADRAO");
            result.add(item);
        }

        return result;
    }

    private List<MedicamentoExtraidoDTO.MedicamentoItem> extrairItensPorDosagem(String textoReceita) {
        if (textoReceita == null || textoReceita.isBlank()) {
            return Collections.emptyList();
        }

        String normalized = textoReceita
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        List<MedicamentoExtraidoDTO.MedicamentoItem> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // Captura "<nome> 200mg" / "<nome> 5 ml" etc. Mantém robusto contra OCR ruim.
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "(?i)\\b([\\p{L}][\\p{L}\\p{N}\\- ]{2,40}?)\\s*([0-9oO]{2,5}(?:[\\.,][0-9oO])?)(?:\\s*(mg|mq|g|mcg|ml|ui|%))?\\b");
        java.util.regex.Matcher m = p.matcher(normalized);
        while (m.find()) {
            String nomeBruto = m.group(1) == null ? "" : m.group(1).trim();
            String numero = m.group(2) == null ? null : m.group(2).trim();
            String unidade = m.group(3) == null ? null : m.group(3).trim();
            String dosagemBruta = (numero == null ? "" : numero) + (unidade == null ? "" : unidade);

            String nome = nomeBruto
                    .replaceAll("(?i)\\b(tomar|usar|use|via|a cada|por|durante|ao dia)\\b.*$", "")
                    .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            if (!nomePareceMedicamento(nome)) {
                continue;
            }

            String dosagem = normalizeDosage(dosagemBruta);
            if (receitaOcrAutocorrect) {
                nome = corrigirNomeReceita(nome, dosagem);
            }
            String key = normalizarTexto(nome);
            if (key.isBlank() || !seen.add(key)) {
                continue;
            }

            MedicamentoExtraidoDTO.MedicamentoItem item = new MedicamentoExtraidoDTO.MedicamentoItem();
            item.setNome(nome);
            item.setQuantidade(1);
            item.setDosagem(dosagem);
            item.setDescricaoIA("EXTRACAO_OCR_PADRAO");
            result.add(item);
        }

        return result;
    }

    private List<MedicamentoExtraidoDTO.MedicamentoItem> extrairItensPorLexicon(String textoReceita) {
        if (textoReceita == null || textoReceita.isBlank()) {
            return Collections.emptyList();
        }

        String ocr = normalizarTexto(textoReceita);
        if (ocr.isBlank()) {
            return Collections.emptyList();
        }

        List<String> tokens = Arrays.stream(ocr.split("\\s+"))
                .filter(t -> t.length() >= 3)
                .collect(Collectors.toList());

        Map<String, Integer> bestDist = new HashMap<>();
        for (String med : MEDICAMENTOS_CANONICOS) {
            bestDist.put(med, Integer.MAX_VALUE);
        }

        for (int i = 0; i < tokens.size(); i++) {
            String w1 = tokens.get(i);
            String w2 = (i + 1 < tokens.size()) ? (w1 + tokens.get(i + 1)) : null;
            String w3 = (i + 2 < tokens.size()) ? (w2 + tokens.get(i + 2)) : null;

            for (String cand : MEDICAMENTOS_CANONICOS) {
                String c = cand.replace(" ", "");
                considerLexicon(bestDist, cand, w1, c);
                if (w2 != null) considerLexicon(bestDist, cand, w2, c);
                if (w3 != null) considerLexicon(bestDist, cand, w3, c);
            }
        }

        List<MedicamentoExtraidoDTO.MedicamentoItem> result = new ArrayList<>();
        for (String cand : MEDICAMENTOS_CANONICOS) {
            int d = bestDist.getOrDefault(cand, Integer.MAX_VALUE);
            if (d == Integer.MAX_VALUE) continue;

            String compact = cand.replace(" ", "");
            int maxLen = Math.max(ocr.length(), compact.length());
            int limit = lexiconLimit(compact.length(), d);
            if (d > limit) {
                continue;
            }

            MedicamentoExtraidoDTO.MedicamentoItem item = new MedicamentoExtraidoDTO.MedicamentoItem();
            item.setNome(cand.substring(0, 1).toUpperCase(Locale.ROOT) + cand.substring(1));
            item.setQuantidade(1);
            item.setDescricaoIA("EXTRACAO_OCR_LEXICON");
            // Tenta vincular no catálogo para permitir adicionar ao carrinho e para o filtro não descartar.
            buscarProdutoNoBanco(item);
            if (item.getProdutoId() != null && item.getNomeCompleto() != null && !item.getNomeCompleto().isBlank()) {
                item.setNome(item.getNomeCompleto());
            }
            definirSituacaoCatalogo(item);
            result.add(item);
        }

        return result;
    }

    private void considerLexicon(Map<String, Integer> bestDist, String candKey, String token, String candCompact) {
        if (token == null || token.isBlank()) return;
        if (token.length() < 4) return;
        // Evita comparar token com começo muito diferente.
        if (Character.toLowerCase(token.charAt(0)) != Character.toLowerCase(candCompact.charAt(0))) {
            return;
        }
        int d = levenshtein(token, candCompact);
        int prev = bestDist.getOrDefault(candKey, Integer.MAX_VALUE);
        if (d < prev) {
            bestDist.put(candKey, d);
        }
    }

    private int lexiconLimit(int len, int dist) {
        // Limite conservador: só aceita bem próximo.
        if (len <= 7) return 1;
        if (len <= 10) return 2;
        if (len <= 14) return 3;
        return 4;
    }

    private boolean nomePareceMedicamento(String nome) {
        if (nome == null) return false;
        String n = nome.trim();
        if (n.length() < 4) return false;

        String norm = normalizarTexto(n);
        if (norm.isBlank() || isNoisePhrase(norm)) return false;

        for (String t : NAO_MEDICAMENTO_TOKENS) {
            if (norm.contains(normalizarTexto(t))) {
                return false;
            }
        }

        int letters = 0;
        int other = 0;
        for (char ch : n.toCharArray()) {
            if (Character.isLetter(ch)) letters++;
            else if (!Character.isWhitespace(ch) && !Character.isDigit(ch)) other++;
        }

        // Evita "gibberish": precisa ter letras suficientes e poucos símbolos estranhos.
        if (letters < 4) return false;
        if (other > 3) return false;

        // Pelo menos um token de 4+ letras (marca/medicamento)
        boolean hasLongToken = Arrays.stream(norm.split("\\s+")).anyMatch(tok -> tok.length() >= 4 && !RECEITA_NOISE_TOKENS.contains(tok));
        return hasLongToken;
    }

    private String extractDosageLoose(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        java.util.regex.Matcher dm = java.util.regex.Pattern
                .compile("(?i)\\b([0-9oO]{2,5}(?:[\\.,][0-9oO])?)\\s*(mg|mq|g|mcg|ml|ui|%)?\\b")
                .matcher(text);
        if (!dm.find()) {
            return null;
        }
        String number = dm.group(1);
        String unit = dm.groupCount() >= 2 ? dm.group(2) : null;
        return normalizeDosage(number + (unit == null ? "" : unit));
    }

    private String normalizeDosage(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase(Locale.ROOT);
        s = s.replaceAll("\\s+", "");
        s = s.replace("mq", "mg");
        s = s.replace('o', '0').replace('O', '0');

        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^([0-9]+(?:[\\.,][0-9]+)?)(mg|g|mcg|ml|ui|%)?$")
                .matcher(s);
        if (m.find()) {
            String numero = m.group(1).replace(',', '.');
            String unidade = m.group(2);
            if (unidade == null || unidade.isBlank()) {
                // Se veio só número (ex.: "500"), assume mg para faixas comuns de dosagem.
                try {
                    double val = Double.parseDouble(numero);
                    if (val >= 10 && val <= 2000) {
                        unidade = "mg";
                    } else {
                        return numero;
                    }
                } catch (Exception e) {
                    return numero;
                }
            }
            return numero + unidade;
        }
        return s;
    }

    private String corrigirNomeReceita(String nome, String dosagem) {
        if (nome == null) {
            return "";
        }
        String original = nome.trim();
        String norm = normalizarTexto(original);
        if (norm.isBlank()) {
            return original;
        }

        // 1) Correção guiada pelo catálogo: se o OCR veio "perto" de um produto real (e medicamento),
        // usamos o nome do produto como correção. Isso aumenta bastante o acerto em manuscrito sem LLM.
        try {
            String semDosagem = removerDosagem(original);
            String normBusca = normalizarTexto(semDosagem);
            Optional<Produto> melhorProduto = encontrarMelhorProduto(original, semDosagem, normBusca);
            if (melhorProduto.isPresent()) {
                Produto p = melhorProduto.get();
                if (!produtoClaramenteNaoMedicamento(p)) {
                    int score = calcularScoreProduto(p, normBusca);

                    // Se o OCR trouxe dosagem, favorece produto cuja string também contenha essa dosagem
                    // (ex.: "500mg"). Isso reduz trocas erradas.
                    if (dosagem != null && !dosagem.isBlank()) {
                        String dp = dosagem.toLowerCase(Locale.ROOT).replace(" ", "");
                        String nomeProdNorm = normalizarTexto(p.getNome()).replace(" ", "");
                        if (nomeProdNorm.contains(dp)) {
                            score += 35;
                        }
                    }

                    // Limiar: alto o suficiente para evitar "puxar" coisas erradas, mas permitindo correção.
                    if (score >= 140) {
                        return p.getNome();
                    }
                }
            }
        } catch (Exception e) {
            // Correção por catálogo é best-effort; nunca deve quebrar o fluxo.
        }

        // Heurísticas específicas de receita (sem "sugerir", apenas corrigir OCR comum).
        if (dosagem != null) {
            if (dosagem.startsWith("500") && norm.startsWith("az")) {
                return "Azitromicina";
            }
            if (dosagem.startsWith("500") && (norm.startsWith("di") || norm.contains("dip"))) {
                return "Dipirona";
            }
            if (dosagem.startsWith("200") && (norm.startsWith("flo") || norm.contains("flor"))) {
                return "Floratil";
            }
        }

        String compact = norm.replace(" ", "");
        String melhor = original;
        int melhorDist = Integer.MAX_VALUE;
        for (String candidato : MEDICAMENTOS_CANONICOS) {
            String cand = normalizarTexto(candidato).replace(" ", "");
            int d = levenshtein(compact, cand);
            if (d < melhorDist) {
                melhorDist = d;
                melhor = candidato;
            }
        }

        int limite = Math.max(4, (int) Math.ceil(compact.length() * 0.45));
        if (melhorDist <= limite) {
            // Capitaliza primeira letra para exibição.
            return melhor.substring(0, 1).toUpperCase(Locale.ROOT) + melhor.substring(1);
        }

        return original;
    }

    private MedicamentoExtraidoDTO processarReceitaSemIA(String textoReceita) {
        MedicamentoExtraidoDTO dto = new MedicamentoExtraidoDTO();
        dto.setTextoOriginal(textoReceita);

        List<MedicamentoExtraidoDTO.MedicamentoItem> medicamentos = new ArrayList<>();
        adicionarItensPerdidosPorLinha(textoReceita, medicamentos);
        dto.setMedicamentos(medicamentos);

        if (dto.getMedicamentos() == null || dto.getMedicamentos().isEmpty()) {
            dto.setMensagemOrientacao(
                    "Não identificamos itens de medicamento claros nesta receita. Tente uma foto com melhor foco e iluminação.");
        } else {
            dto.setMensagemOrientacao(mensagemOrientacaoFallback(dto));
        }
        return dto;
    }

    private MedicamentoExtraidoDTO filtrarItensIrrelevantes(MedicamentoExtraidoDTO dto) {
        if (dto == null || dto.getMedicamentos() == null || dto.getMedicamentos().isEmpty()) {
            return dto;
        }

        List<MedicamentoExtraidoDTO.MedicamentoItem> filtrados = dto.getMedicamentos().stream()
                .filter(this::itemPareceMedicamento)
                .collect(Collectors.toList());

        if (filtrados.size() != dto.getMedicamentos().size()) {
            log.info("Filtrando itens irrelevantes: {} -> {}", dto.getMedicamentos().size(), filtrados.size());
        }

        dto.setMedicamentos(filtrados);
        return dto;
    }

    private boolean itemPareceMedicamento(MedicamentoExtraidoDTO.MedicamentoItem item) {
        if (item == null) {
            return false;
        }

        String nome = item.getNome() == null ? "" : item.getNome().trim();
        if (nome.isEmpty()) {
            return false;
        }

        String normalizado = normalizarTexto(nome);
        if (normalizado.isBlank()) {
            return false;
        }

        if (isNoisePhrase(normalizado)) {
            return false;
        }

        for (String t : NAO_MEDICAMENTO_TOKENS) {
            if (normalizado.contains(normalizarTexto(t))) {
                return false;
            }
        }

        boolean temIndicadorForte = contemIndicadorMedicamentoNoTexto(nome)
                || contemIndicadorMedicamentoNoTexto(item.getDosagem())
                || contemIndicadorMedicamentoNoTexto(item.getPosologia());

        if ("EXTRACAO_OCR_PADRAO".equals(item.getDescricaoIA())) {
            // Extração determinística: permite itens mais curtos se houver evidência forte (ex.: "500mg", "cápsula", etc.).
            return hasLongToken(normalizado) || temIndicadorForte;
        }
        if ("EXTRACAO_OCR_LEXICON".equals(item.getDescricaoIA())) {
            // Extração por léxico: geralmente não temos posologia/dosagem preenchidas; não podemos derrubar por isso.
            // Se casou no catálogo, já vale; senão, exige ao menos um token longo (nome de medicamento).
            return item.getProdutoId() != null || hasLongToken(normalizado) || temIndicadorForte;
        }

        // Se casou no catálogo, já passou pelo filtro de "não-medicamento" no matching.
        if (item.getProdutoId() != null) {
            return true;
        }

        // Sem produto no catálogo: só exibe se houver evidência forte de medicamento.
        return temIndicadorForte;
    }

    private boolean hasLongToken(String normalizado) {
        return Arrays.stream(normalizado.split("\\s+"))
                .anyMatch(tok -> tok.length() >= 5 && !RECEITA_NOISE_TOKENS.contains(tok));
    }

    private boolean isNoisePhrase(String normalizado) {
        String[] tokens = normalizado.split("\\s+");
        int relevantes = 0;
        for (String tok : tokens) {
            if (tok.isBlank()) continue;
            if (tok.length() <= 2) continue;
            if (RECEITA_NOISE_TOKENS.contains(tok)) continue;
            relevantes++;
        }
        if (relevantes == 0) {
            return true;
        }
        if (normalizado.contains("leitura") && normalizado.contains("ocr")) {
            return true;
        }
        if (normalizado.startsWith("uso ") || normalizado.startsWith("via ")) {
            return true;
        }
        return false;
    }

    private boolean contemIndicadorMedicamentoNoTexto(String texto) {
        if (texto == null || texto.isBlank()) return false;
        String t = texto.toLowerCase(Locale.ROOT);
        return t.matches(".*\\b\\d+[\\.,]?\\d*\\s*(mg|g|mcg|ml|ui|%)\\b.*")
                || t.contains("comprim")
                || t.contains("capsul")
                || t.contains("cápsul")
                || t.contains("gotas")
                || t.contains("ampola")
                || t.contains("xarope");
    }

    private String preprocessarOCR(String textoOCR) {
        if (textoOCR == null || textoOCR.isBlank()) return "";

        Map<String, String> correcoes = new LinkedHashMap<>();
        correcoes.put("(?i)\\bazitromicna\\b", "Azitromicina");
        correcoes.put("(?i)\\bazitromlcina\\b", "Azitromicina");
        correcoes.put("(?i)\\bazitromlclna\\b", "Azitromicina");
        correcoes.put("(?i)\\bazitro\\w*\\b", "Azitromicina");
        correcoes.put("(?i)\\bazlthromlcln\\b", "Azitromicina");
        correcoes.put("(?i)\\bamoxlcllna\\b", "Amoxicilina");
        correcoes.put("(?i)\\bamoxl\\w*\\b", "Amoxicilina");
        correcoes.put("(?i)\\bparacetamol\\b", "Paracetamol");
        correcoes.put("(?i)\\bdlpirona\\b", "Dipirona");
        correcoes.put("(?i)\\bdlplrona\\b", "Dipirona");
        correcoes.put("(?i)\\blosartana\\b", "Losartana");
        correcoes.put("(?i)\\bmedlcamento\\b", "medicamento");
        correcoes.put("(?i)\\bcomprlmldo\\b", "comprimido");
        correcoes.put("(?i)\\bcapsula\\b", "cápsula");

        String corrigido = textoOCR
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        for (Map.Entry<String, String> entry : correcoes.entrySet()) {
            corrigido = corrigido.replaceAll(entry.getKey(), entry.getValue());
        }

        corrigido = corrigido.replaceAll("(?m)^[\\d\\s\\-•_|]+", "");
        corrigido = corrigido.replaceAll("[^\\p{L}\\p{N}\\s\\.,;:()\\-/%mgMG]", " ");
        // Mantém quebras de linha; normaliza apenas espaços dentro de cada linha.
        String[] linhas = corrigido.split("\\n");
        StringBuilder sb = new StringBuilder();
        for (String linha : linhas) {
            String limpa = linha.replaceAll("[ \\t\\f\\u000B]+", " ").trim();
            if (limpa.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(limpa);
        }
        corrigido = sb.toString().trim();

        log.debug("OCR pré-processado: {}", corrigido);
        return corrigido;
    }

    private MedicamentoExtraidoDTO parsePrimeiraExtracao(String respostaGroq, String textoReceita) {
        try {
            String respostaLimpa = limparJson(respostaGroq);
            Map<String, Object> resultado = objectMapper.readValue(respostaLimpa, Map.class);

            MedicamentoExtraidoDTO dto = new MedicamentoExtraidoDTO();
            dto.setTextoOriginal(textoReceita);
            List<MedicamentoExtraidoDTO.MedicamentoItem> medicamentos = new ArrayList<>();
            Set<String> nomesNormalizados = new HashSet<>();

            if (resultado.containsKey("medicamentos")) {
                List<Map<String, Object>> medsMap = (List<Map<String, Object>>) resultado.get("medicamentos");
                for (Map<String, Object> med : medsMap) {
                    MedicamentoExtraidoDTO.MedicamentoItem item = new MedicamentoExtraidoDTO.MedicamentoItem();
                    item.setNome(sanitizarNomeMedicamento(med.get("nome")));
                    String nomeNorm = normalizarTexto(item.getNome());
                    if (nomeNorm.isBlank() || isNoisePhrase(nomeNorm) || !temEvidenciaNoOcr(textoReceita, item.getNome())) {
                        continue;
                    }
                    if (!nomesNormalizados.add(nomeNorm)) {
                        continue;
                    }
                    item.setQuantidade(parseQuantidade(med.get("quantidade")));
                    item.setDosagem((String) med.get("dosagem"));
                    item.setPosologia((String) med.get("posologia"));
                    item.setDiasDuracao(parseQuantidade(med.get("diasDuracao")));
                    item.setDescricaoIA((String) med.get("descricao"));

                    buscarProdutoNoBanco(item);
                    definirSituacaoCatalogo(item);
                    medicamentos.add(item);
                }
            }

            adicionarItensPerdidosPorLinha(textoReceita, medicamentos);

            dto.setMedicamentos(medicamentos);
            return dto;
        } catch (Exception e) {
            log.error("Erro ao processar resposta da IA", e);
            MedicamentoExtraidoDTO dto = new MedicamentoExtraidoDTO();
            dto.setTextoOriginal(textoReceita);

            // Fallback "best-effort": tenta ao menos sugerir linhas que parecem medicamentos a partir do OCR.
            List<MedicamentoExtraidoDTO.MedicamentoItem> candidatos = new ArrayList<>();
            adicionarItensPerdidosPorLinha(textoReceita, candidatos);
            dto.setMedicamentos(candidatos);

            dto.setMensagemOrientacao(
                    "Tivemos dificuldade em interpretar a resposta da IA. " +
                            "Mostramos uma extração básica do texto OCR; pode ser necessário ajustar o nome manualmente.");
            return dto;
        }
    }

    private boolean temEvidenciaNoOcr(String textoReceita, String nomeMedicamento) {
        if (textoReceita == null || textoReceita.isBlank() || nomeMedicamento == null || nomeMedicamento.isBlank()) {
            return false;
        }
        String ocr = normalizarTexto(textoReceita);
        String nome = normalizarTexto(nomeMedicamento);
        if (nome.isBlank() || ocr.isBlank()) {
            return false;
        }
        if (ocr.contains(nome)) {
            return true;
        }
        // Evidência por token relevante (>=4 chars) para tolerar pequenas correções de OCR.
        for (String tok : nome.split("\\s+")) {
            if (tok.length() < 4) continue;
            if (STOP_TOKENS_BUSCA.contains(tok)) continue;
            if (ocr.contains(tok)) {
                return true;
            }
        }
        return false;
    }

    private void adicionarItensPerdidosPorLinha(String textoOCR, List<MedicamentoExtraidoDTO.MedicamentoItem> medicamentos) {
        if (medicamentos == null) return;

        String[] linhas = textoOCR.split("\\n");
        Set<String> nomesExistentes = medicamentos.stream()
                .map(m -> m.getNome() != null ? m.getNome().toLowerCase() : "")
                .collect(Collectors.toSet());

        for (String linha : linhas) {
            String linhaTrim = linha.trim().toLowerCase();
            if (linhaTrim.length() < 3) continue;

            // Evita "lixo" comum de cabeçalho/rodapé de receita e de debug de OCR.
            if (linhaTrim.contains("receitu") || linhaTrim.contains("paciente") || linhaTrim.contains("crm")
                    || linhaTrim.contains("carimbo") || linhaTrim.contains("assin") || linhaTrim.contains("prescr")
                    || linhaTrim.contains("leitura") || linhaTrim.contains("ocr") || linhaTrim.contains("space")
                    || linhaTrim.contains("uso") || linhaTrim.contains("oral") || linhaTrim.contains("via") || linhaTrim.contains("geral")) {
                continue;
            }

            boolean indicadorForte =
                    linhaTrim.matches(".*\\d+\\s*(mg|g|mcg|ml|ui|%).*") ||
                            linhaTrim.matches(".*(comprimido|capsula|cápsula|cp|cps|drágea|solução|ampola).*");

            String nomeCandidato = extrairNomeProvavel(linhaTrim);
            if (nomeCandidato.length() <= 3 || nomesExistentes.contains(nomeCandidato)) {
                continue;
            }
            if (isNoisePhrase(normalizarTexto(nomeCandidato))) {
                continue;
            }

            MedicamentoExtraidoDTO.MedicamentoItem item = new MedicamentoExtraidoDTO.MedicamentoItem();
            item.setNome(nomeCandidato);
            item.setDescricaoIA("OCR com baixa qualidade - verifique o nome na receita original");
            if (item.getQuantidade() == null) {
                item.setQuantidade(1);
            }

            // Sem "indicador forte" só aceitamos se casar com algum produto do catálogo.
            buscarProdutoNoBanco(item);
            // Se conseguimos vincular um produto, preferimos mostrar o nome do catálogo como "nome do item".
            // Isso aumenta a taxa de acerto percebida pelo usuário e facilita adicionar ao carrinho.
            if (item.getProdutoId() != null && item.getNomeCompleto() != null && !item.getNomeCompleto().isBlank()) {
                item.setNome(item.getNomeCompleto());
            }
            definirSituacaoCatalogo(item);
            if (!indicadorForte && item.getProdutoId() == null) {
                continue;
            }

            log.warn("Item potencial não extraído pela IA: '{}' (indicadorForte={}, produtoId={})",
                    nomeCandidato, indicadorForte, item.getProdutoId());
            medicamentos.add(item);
            nomesExistentes.add(nomeCandidato);
        }
    }

    private String extrairNomeProvavel(String linha) {
        String nome = linha.replaceAll("\\b\\d+[\\.,]?\\d*\\s*(mg|g|mcg|ml|ui|%|cp|cps|comprimido)\\b", "")
                .replaceAll("\\b(de|da|do|para|por|uso|oral|via|tomar|cada)\\b", "")
                .replaceAll("[^a-zA-ZÀ-ÿ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (nome.isEmpty()) return linha.substring(0, Math.min(20, linha.length()));
        return nome.substring(0, Math.min(30, nome.length()));
    }

    private void definirSituacaoCatalogo(MedicamentoExtraidoDTO.MedicamentoItem item) {
        if (item.getProdutoId() == null) {
            item.setSituacaoCatalogo("NAO_ENCONTRADO");
        } else if (item.getEstoque() != null && item.getEstoque() == 0) {
            item.setSituacaoCatalogo("SEM_ESTOQUE");
        } else {
            item.setSituacaoCatalogo("DISPONIVEL");
        }
    }

    private Mono<MedicamentoExtraidoDTO> enriquecerComCatalogoClickfarma(MedicamentoExtraidoDTO dto) {
        if (dto.getMedicamentos() == null || dto.getMedicamentos().isEmpty()) {
            dto.setMensagemOrientacao(
                    "Não identificamos itens de medicamento claros nesta receita. Tente uma foto com melhor foco e iluminação.");
            return Mono.just(dto);
        }

        if (!isGroqConfigured()) {
            dto.setMensagemOrientacao(mensagemOrientacaoFallback(dto));
            return Mono.just(dto);
        }

        try {
            Map<String, Object> contexto = new LinkedHashMap<>();
            contexto.put("medicamentos_lidos_na_receita", dto.getMedicamentos().stream()
                    .map(this::mapItemParaContexto)
                    .collect(Collectors.toList()));
            contexto.put("catalogo_produtos_com_estoque",
                    produtoRepository.findAll().stream()
                            .filter(p -> p.getEstoque() != null && p.getEstoque() > 0)
                            .sorted(Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                            .limit(150)
                            .map(p -> {
                                Map<String, Object> linha = new LinkedHashMap<>();
                                linha.put("id", p.getId());
                                linha.put("nome", p.getNome());
                                linha.put("estoque", p.getEstoque());
                                return linha;
                            })
                            .collect(Collectors.toList()));

            String jsonContexto = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(contexto);
            String promptEnriquecimento = montarPromptEnriquecimentoCatalogo(jsonContexto);

            return groqService.chat(promptEnriquecimento, 0.25)
                    .map(resp -> {
                        aplicarRespostaEnriquecimento(dto, resp);
                        if (dto.getMensagemOrientacao() == null || dto.getMensagemOrientacao().isBlank()) {
                            dto.setMensagemOrientacao(mensagemOrientacaoFallback(dto));
                        }
                        return dto;
                    })
                    .onErrorResume(e -> {
                        log.warn("Enriquecimento com catálogo falhou: {}", e.getMessage());
                        dto.setMensagemOrientacao(mensagemOrientacaoFallback(dto));
                        return Mono.just(dto);
                    });
        } catch (Exception e) {
            log.warn("Montagem do contexto de catálogo falhou: {}", e.getMessage());
            dto.setMensagemOrientacao(mensagemOrientacaoFallback(dto));
            return Mono.just(dto);
        }
    }

    private Map<String, Object> mapItemParaContexto(MedicamentoExtraidoDTO.MedicamentoItem item) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nome_prescrito", item.getNome());
        m.put("dosagem", item.getDosagem());
        m.put("posologia", item.getPosologia());
        m.put("situacao_no_clickfarma", item.getSituacaoCatalogo());
        m.put("produto_id_vinculado", item.getProdutoId());
        m.put("nome_produto_catalogo", item.getNomeCompleto());
        m.put("estoque", item.getEstoque());
        return m;
    }

    private String montarPromptEnriquecimentoCatalogo(String jsonContexto) {
        return """
                Você é farmacêutico orientador do e-commerce ClickFarma (Brasil).

                DADOS (JSON abaixo):
                - medicamentos_lidos_na_receita: o que foi extraído da receita e a situação no nosso sistema (DISPONIVEL, SEM_ESTOQUE, NAO_ENCONTRADO).
                - catalogo_produtos_com_estoque: lista REAL de produtos que temos em estoque (id, nome, estoque). Use APENAS estes ids para alternativas.

                """ + jsonContexto + """

                TAREFA:
                1) Escreva "mensagem_orientacao": um texto claro em português (3 a 6 frases) para o paciente, no estilo:
                   explicar que lemos a receita; o que o ClickFarma tem em catálogo para cada caso; quando não temos o mesmo medicamento, indicar que há opção similar ou da mesma classe/efeito no nosso sistema quando existir no catálogo.
                   Inclua aviso de que troca ou substituição de medicamento deve ser confirmada com médico ou farmacêutico quando não for o mesmo produto.
                2) Para cada item em medicamentos_lidos_na_receita cujo "situacao_no_clickfarma" seja NAO_ENCONTRADO ou SEM_ESTOQUE, preencha até 2 alternativas escolhendo APENAS produto_id que existam no catálogo e com estoque > 0.
                   Para DISPONIVEL, alternativas pode ser [].
                   Motivo deve ser curto (efeito similar, mesma classe, sintoma, etc.).

                Retorne SOMENTE JSON válido neste formato:
                {
                  "mensagem_orientacao": "texto",
                  "indicacoes": [
                    {
                      "nome_medicamento_prescrito": "igual ao campo nome_prescrito do item",
                      "alternativas": [ { "produto_id": 0, "motivo": "texto curto" } ]
                    }
                  ]
                }
                """;
    }

    private void aplicarRespostaEnriquecimento(MedicamentoExtraidoDTO dto, String respostaGroq) {
        try {
            String limpo = limparJson(respostaGroq);
            JsonNode root = objectMapper.readTree(limpo);
            if (root.hasNonNull("mensagem_orientacao")) {
                dto.setMensagemOrientacao(root.get("mensagem_orientacao").asText().trim());
            }
            JsonNode indicacoes = root.get("indicacoes");
            if (indicacoes == null || !indicacoes.isArray()) {
                return;
            }
            for (JsonNode bloco : indicacoes) {
                String nomeRef = bloco.path("nome_medicamento_prescrito").asText("").trim();
                MedicamentoExtraidoDTO.MedicamentoItem item = encontrarItemPorNomePrescrito(dto.getMedicamentos(), nomeRef);
                if (item == null) {
                    continue;
                }
                List<AlternativaSugeridaDTO> lista = new ArrayList<>();
                JsonNode alts = bloco.get("alternativas");
                if (alts != null && alts.isArray()) {
                    for (JsonNode a : alts) {
                        if (!a.has("produto_id")) {
                            continue;
                        }
                        long pid = a.get("produto_id").asLong(0L);
                        if (pid <= 0) {
                            continue;
                        }
                        final long produtoId = pid;
                        produtoRepository.findById(produtoId).ifPresent(p -> {
                            if (p.getEstoque() != null && p.getEstoque() > 0) {
                                lista.add(new AlternativaSugeridaDTO(
                                        p.getId(),
                                        p.getNome(),
                                        p.getPreco().doubleValue(),
                                        p.getEstoque(),
                                        a.path("motivo").asText("").trim()
                                ));
                            }
                        });
                    }
                }
                item.setAlternativasSugeridas(lista.stream().limit(2).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.warn("Não foi possível aplicar enriquecimento do catálogo: {}", e.getMessage());
        }
    }

    private MedicamentoExtraidoDTO.MedicamentoItem encontrarItemPorNomePrescrito(
            List<MedicamentoExtraidoDTO.MedicamentoItem> itens,
            String nomeGroq) {
        if (nomeGroq == null || nomeGroq.isBlank()) {
            return null;
        }
        String alvo = normalizarTexto(nomeGroq);
        for (MedicamentoExtraidoDTO.MedicamentoItem item : itens) {
            if (normalizarTexto(item.getNome()).equals(alvo)) {
                return item;
            }
        }
        for (MedicamentoExtraidoDTO.MedicamentoItem item : itens) {
            String n = normalizarTexto(item.getNome());
            if (n.contains(alvo) || alvo.contains(n)) {
                return item;
            }
        }
        return null;
    }

    private String mensagemOrientacaoFallback(MedicamentoExtraidoDTO dto) {
        long comEstoque = dto.getMedicamentos().stream()
                .filter(m -> "DISPONIVEL".equals(m.getSituacaoCatalogo()))
                .count();
        long precisamAlt = dto.getMedicamentos().stream()
                .filter(m -> "NAO_ENCONTRADO".equals(m.getSituacaoCatalogo()) || "SEM_ESTOQUE".equals(m.getSituacaoCatalogo()))
                .count();
        return String.format(
                "Lemos sua receita e identificamos %d item(ns). No ClickFarma, %d estão disponíveis para compra agora. "
                        + "Para %d item(ns) verificamos o catálogo: confira abaixo sugestões quando houver equivalente em estoque. "
                        + "Substituição de medicamento deve ser confirmada com seu médico ou farmacêutico.",
                dto.getMedicamentos().size(), comEstoque, precisamAlt);
    }

    private String limparJson(String json) {
        if (json.contains("{")) json = json.substring(json.indexOf("{"));
        if (json.contains("}")) json = json.substring(0, json.lastIndexOf("}") + 1);
        return json.replace("```json", "").replace("```", "").trim();
    }

    private Integer parseQuantidade(Object qtd) {
        if (qtd instanceof Number) return ((Number) qtd).intValue();
        try { return Integer.parseInt(qtd.toString()); } catch (Exception e) { return 1; }
    }

    private void buscarProdutoNoBanco(MedicamentoExtraidoDTO.MedicamentoItem item) {
        buscarProdutoNoBanco(item, null);
    }

    private void buscarProdutoNoBanco(MedicamentoExtraidoDTO.MedicamentoItem item, List<Produto> cacheProdutos) {
        String nomeBuscaOriginal = item.getNome() == null ? "" : item.getNome().trim();
        String nomeBuscaSemDosagem = removerDosagem(nomeBuscaOriginal);
        String nomeBuscaNormalizado = normalizarTexto(nomeBuscaSemDosagem);

        Optional<Produto> produto = cacheProdutos != null
                ? encontrarMelhorProdutoComCache(nomeBuscaOriginal, nomeBuscaSemDosagem, nomeBuscaNormalizado, cacheProdutos)
                : encontrarMelhorProduto(nomeBuscaOriginal, nomeBuscaSemDosagem, nomeBuscaNormalizado);

        if (produto.isPresent()) {
            Produto p = produto.get();
            if (produtoClaramenteNaoMedicamento(p)) {
                item.setDescricaoProduto(item.getDescricaoIA());
                item.setPreco(0.0);
                item.setEstoque(null);
                log.info("Produto ignorado (não-medicamento) para '{}': '{}'", nomeBuscaOriginal, p.getNome());
                return;
            }
            item.setProdutoId(p.getId());
            item.setPreco(p.getPreco().doubleValue());
            item.setNomeCompleto(p.getNome());
            item.setEstoque(p.getEstoque() != null ? p.getEstoque() : 0);
            item.setDescricaoProduto(p.getDescricao());
            log.info("Produto associado via matching flexível: '{}' -> '{}' (estoque={})", nomeBuscaOriginal, p.getNome(), p.getEstoque());
        } else {
            item.setDescricaoProduto(item.getDescricaoIA());
            item.setPreco(0.0);
            item.setEstoque(null);
            log.warn("Nenhum produto encontrado para '{}'", nomeBuscaOriginal);
        }
    }

    private boolean produtoClaramenteNaoMedicamento(Produto p) {
        if (p == null) return true;

        String nome = normalizarTexto(p.getNome());
        if (!nome.isBlank()) {
            for (String t : NAO_MEDICAMENTO_TOKENS) {
                if (nome.contains(normalizarTexto(t))) {
                    return true;
                }
            }
        }

        if (p.getCategoria() != null && p.getCategoria().getNome() != null) {
            String cat = normalizarTexto(p.getCategoria().getNome());
            // Rejeita apenas categorias claramente não-medicamento (evita bloquear farmácia/medicamentos).
            if (cat.contains("higiene") || cat.contains("perfum") || cat.contains("cosmet")
                    || cat.contains("infantil") || cat.contains("beleza") || cat.contains("acessor")
                    || cat.contains("cuidado") || cat.contains("dermo")) {
                return true;
            }
        }

        return false;
    }

    private String sanitizarNomeMedicamento(Object nomeBruto) {
        if (nomeBruto == null) {
            return "";
        }
        String s = nomeBruto.toString().trim().toLowerCase(Locale.ROOT);
        s = s.replaceAll("^[\"'`´]+|[\"'`´]+$", "");
        s = s.replaceAll("(?m)^\\s*\\d+[\\).\\-]\\s*", "");
        s = removerPrefixosFarmaceuticos(s).trim();
        return s;
    }

    private String removerPrefixosFarmaceuticos(String s) {
        if (s.isEmpty()) {
            return s;
        }
        String lower = s.toLowerCase(Locale.ROOT);
        String[] prefixes = {
                "cloridrato de ", "dicloridrato de ", "hemi-hidratado de ", "besilato de ",
                "sulfato de ", "maleato de ", "succinato de ", "fumarato de ", "mesilato de "
        };
        for (String p : prefixes) {
            if (lower.startsWith(p)) {
                return s.substring(p.length()).trim();
            }
        }
        return s;
    }

    private static final Set<String> STOP_TOKENS_BUSCA = Set.of(
            "de", "da", "do", "das", "dos", "com", "para", "por", "o", "a", "os", "as",
            "e", "ao", "aos", "na", "no", "nas", "nos", "em", "um", "uma", "uns", "umas",
            "uso", "oral", "via", "tomar", "cada", "se", "que"
    );

    private Optional<Produto> encontrarMelhorProduto(String nomeOriginal, String nomeSemDosagem, String nomeNormalizado) {
        if (nomeNormalizado.isBlank() || nomeSemDosagem.length() < 2) {
            return Optional.empty();
        }

        LinkedHashMap<Long, Produto> candidatos = new LinkedHashMap<>();

        produtoRepository.findByNomeIgnoreCase(nomeOriginal).ifPresent(p -> candidatos.put(p.getId(), p));
        produtoRepository.findByNomeIgnoreCase(nomeSemDosagem).ifPresent(p -> candidatos.put(p.getId(), p));

        adicionarPorNomeContendo(candidatos, nomeOriginal);
        adicionarPorNomeContendo(candidatos, nomeSemDosagem);
        adicionarPorNomeComecando(candidatos, nomeSemDosagem);
        adicionarPorNomeOuDescricao(candidatos, nomeSemDosagem);

        for (String token : extrairTokensRelevantes(nomeNormalizado)) {
            adicionarPorNomeContendo(candidatos, token);
            adicionarPorNomeOuDescricao(candidatos, token);
        }

        if (candidatos.isEmpty()) {
            String nomeFonetico = simplificarParaFonetica(nomeNormalizado);
            List<Produto> porFonetica = produtoRepository.findAll().stream()
                    .filter(p -> simplificarParaFonetica(normalizarTexto(p.getNome())).equals(nomeFonetico))
                    .collect(Collectors.toList());
            adicionarCandidatos(candidatos, porFonetica);
        }

        if (candidatos.isEmpty()) {
            Set<String> tokens = extrairTokensRelevantes(nomeNormalizado);
            List<Produto> porTokens = produtoRepository.findAll().stream()
                    .filter(p -> {
                        String nomeProd = normalizarTexto(p.getNome());
                        return tokens.stream().anyMatch(token -> nomeProd.contains(token) && token.length() > 3);
                    })
                    .collect(Collectors.toList());
            adicionarCandidatos(candidatos, porTokens);
        }

        Comparator<Produto> melhor = Comparator
                .<Produto>comparingInt(p -> calcularScoreProduto(p, nomeNormalizado))
                .thenComparingInt(p -> p.getEstoque() != null ? p.getEstoque() : 0);

        return candidatos.values().stream().max(melhor);
    }

    private Optional<Produto> encontrarMelhorProdutoComCache(String nomeOriginal, String nomeSemDosagem, String nomeNormalizado, List<Produto> cacheProdutos) {
        if (nomeNormalizado.isBlank() || nomeSemDosagem.length() < 2) {
            return Optional.empty();
        }

        LinkedHashMap<Long, Produto> candidatos = new LinkedHashMap<>();

        for (Produto p : cacheProdutos) {
            String nomeProd = normalizarTexto(p.getNome());
            if (nomeProd.equals(nomeNormalizado) || nomeProd.equals(normalizarTexto(nomeOriginal)) || nomeProd.equals(normalizarTexto(nomeSemDosagem))) {
                candidatos.put(p.getId(), p);
            }
        }

        for (Produto p : cacheProdutos) {
            String nomeProd = normalizarTexto(p.getNome());
            String descProd = normalizarTexto(p.getDescricao() != null ? p.getDescricao() : "");
            if (nomeProd.contains(nomeNormalizado) || nomeProd.contains(normalizarTexto(nomeSemDosagem))
                    || descProd.contains(nomeNormalizado) || descProd.contains(normalizarTexto(nomeSemDosagem))) {
                candidatos.putIfAbsent(p.getId(), p);
            }
        }

        Set<String> tokens = extrairTokensRelevantes(nomeNormalizado);
        for (Produto p : cacheProdutos) {
            String nomeProd = normalizarTexto(p.getNome());
            String descProd = normalizarTexto(p.getDescricao() != null ? p.getDescricao() : "");
            for (String token : tokens) {
                if (nomeProd.contains(token) || descProd.contains(token)) {
                    candidatos.putIfAbsent(p.getId(), p);
                    break;
                }
            }
        }

        if (candidatos.isEmpty()) {
            String nomeFonetico = simplificarParaFonetica(nomeNormalizado);
            for (Produto p : cacheProdutos) {
                if (simplificarParaFonetica(normalizarTexto(p.getNome())).equals(nomeFonetico)) {
                    candidatos.put(p.getId(), p);
                }
            }
        }

        Comparator<Produto> melhor = Comparator
                .<Produto>comparingInt(p -> calcularScoreProduto(p, nomeNormalizado))
                .thenComparingInt(p -> p.getEstoque() != null ? p.getEstoque() : 0);

        return candidatos.values().stream().max(melhor);
    }

    private String simplificarParaFonetica(String texto) {
        if (texto.length() < 3) return texto;
        String simplificada = texto.replaceAll("[aeiouAEIOU]", "")
                .replaceAll("(.)\\1+", "$1");
        if (simplificada.isEmpty()) {
            return "";
        }
        return simplificada.substring(0, Math.min(8, simplificada.length()));
    }

    private void adicionarPorNomeContendo(Map<Long, Produto> candidatos, String termo) {
        if (termo == null || termo.length() < 3) {
            return;
        }
        adicionarCandidatos(candidatos, produtoRepository.findByNomeContainingIgnoreCase(termo));
    }

    private void adicionarPorNomeComecando(Map<Long, Produto> candidatos, String termo) {
        if (termo == null || termo.length() < 2) {
            return;
        }
        adicionarCandidatos(candidatos, produtoRepository.findByNomeStartingWithIgnoreCase(termo));
    }

    private void adicionarPorNomeOuDescricao(Map<Long, Produto> candidatos, String termo) {
        if (termo == null || termo.length() < 3) {
            return;
        }
        adicionarCandidatos(candidatos, produtoRepository.buscarPorNomeOuDescricao(termo));
    }

    private void adicionarCandidatos(Map<Long, Produto> candidatos, List<Produto> produtos) {
        for (Produto produto : produtos) {
            candidatos.putIfAbsent(produto.getId(), produto);
        }
    }

    private int calcularScoreProduto(Produto produto, String nomeBuscadoNormalizado) {
        String nomeProduto = normalizarTexto(produto.getNome());
        String descricaoProduto = normalizarTexto(produto.getDescricao());
        int score = 0;

        if (nomeProduto.equals(nomeBuscadoNormalizado)) {
            score += 200;
        }
        if (nomeProduto.contains(nomeBuscadoNormalizado)) {
            score += 120;
        }
        if (nomeBuscadoNormalizado.contains(nomeProduto)) {
            score += 80;
        }

        for (String token : extrairTokensRelevantes(nomeBuscadoNormalizado)) {
            if (nomeProduto.contains(token)) {
                score += 35;
            }
            if (!descricaoProduto.isBlank() && descricaoProduto.contains(token)) {
                score += 15;
            }
        }

        int distancia = levenshtein(nomeBuscadoNormalizado, nomeProduto);
        score += Math.max(0, 60 - (distancia * 8));

        return score;
    }

    private Set<String> extrairTokensRelevantes(String texto) {
        if (texto == null || texto.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(texto.split("\\s+"))
                .map(String::trim)
                .filter(token -> token.length() >= 3)
                .filter(token -> !STOP_TOKENS_BUSCA.contains(token))
                .filter(token -> !Set.of("comprimido", "comprimidos", "capsula", "capsulas", "solucao").contains(token))
                .collect(Collectors.toSet());
    }

    private String removerDosagem(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replaceAll("(?i)\\b\\d+[\\.,]?\\d*\\s*(mg|g|mcg|ml|ui|%)\\b", " ")
                .replaceAll("(?i)\\b\\d+\\s*(cp|cps|caps|comprimidos?)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return semAcento
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int levenshtein(String a, String b) {
        if (a.equals(b)) {
            return 0;
        }
        if (a.isEmpty()) {
            return b.length();
        }
        if (b.isEmpty()) {
            return a.length();
        }

        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];

	        for (int j = 0; j <= b.length(); j++) {
	            previous[j] = j;
	        }
	
	        for (int i = 1; i <= a.length(); i++) {
	            current[0] = i;
	            for (int j = 1; j <= b.length(); j++) {
	                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
	                current[j] = Math.min(
	                        Math.min(current[j - 1] + 1, previous[j] + 1),
	                        previous[j - 1] + cost
	                );
	            }

            int[] swap = previous;
	            previous = current;
	            current = swap;
	        }

        return previous[b.length()];
    }
}
