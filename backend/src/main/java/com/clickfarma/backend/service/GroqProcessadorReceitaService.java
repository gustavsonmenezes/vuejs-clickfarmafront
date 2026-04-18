package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.MedicamentoExtraidoDTO;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.*;

@Service
@Slf4j
public class GroqProcessadorReceitaService {

    @Autowired
    private GroqService groqService;

    @Autowired
    private ProdutoRepository produtoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<MedicamentoExtraidoDTO> processarReceita(String textoReceita) {
        log.info("Processando receita com Groq. Texto: {} caracteres", textoReceita.length());

        // Se o texto for muito pequeno ou for mensagem de erro, retorna vazio
        if (textoReceita == null || textoReceita.length() < 10 || textoReceita.startsWith("Erro")) {
            log.warn("Texto muito curto ou erro: {}", textoReceita);
            MedicamentoExtraidoDTO dto = new MedicamentoExtraidoDTO();
            dto.setMedicamentos(new ArrayList<>());
            dto.setTextoOriginal(textoReceita);
            return Mono.just(dto);
        }

        // PROMPT MELHORADO: Mais resiliente a ruídos de OCR e focado em identificar medicamentos mesmo com erros de digitação
        String prompt = String.format("""
                Você é um assistente farmacêutico especializado em extrair informações de receitas médicas para um e-commerce de farmácia.
                
                TEXTO DA RECEITA (Extraído via OCR, pode conter ruídos e erros de leitura):
                ---
                %s
                ---
                
                INSTRUÇÕES CRÍTICAS:
                1. Analise o texto acima e identifique os medicamentos prescritos. Ignore caracteres aleatórios e ruídos comuns de OCR.
                
                2. Retorne APENAS um JSON válido no formato abaixo:
                {
                  "medicamentos": [
                    {
                      "nome": "nome genérico ou princípio ativo",
                      "quantidade": 1,
                      "dosagem": "ex: 500mg",
                      "descricao": "Descrição breve e amigável do uso do medicamento"
                    }
                  ]
                }
                
                3. REGRAS DE EXTRAÇÃO:
                   - nome: Extraia o princípio ativo ou nome genérico (ex: "dipirona", "amoxicilina", "floratil").
                   - quantidade: Se não estiver claro, use 1.
                   - dosagem: Extraia a concentração (ex: "500mg", "200mg").
                   - descricao: Crie uma frase curta explicando para que serve (ex: "Analgésico para dor e febre").
                
                4. RESILIÊNCIA: Mesmo que o nome esteja levemente errado no OCR (ex: "Dipir0na", "Amoxcilina"), identifique o medicamento correto.
                
                5. Se não encontrar NENHUM medicamento, retorne: {"medicamentos": []}
                
                NÃO inclua explicações, markdown ou qualquer texto fora do JSON.
                """, textoReceita);

        return groqService.chat(prompt)
                .map(respostaGroq -> {
                    log.debug("Resposta Groq: {}", respostaGroq);

                    try {
                        // Limpeza robusta da resposta JSON
                        String respostaLimpa = respostaGroq.trim();
                        if (respostaLimpa.contains("{")) {
                            respostaLimpa = respostaLimpa.substring(respostaLimpa.indexOf("{"));
                        }
                        if (respostaLimpa.contains("}")) {
                            respostaLimpa = respostaLimpa.substring(0, respostaLimpa.lastIndexOf("}") + 1);
                        }

                        respostaLimpa = respostaLimpa
                                .replace("```json", "")
                                .replace("```", "")
                                .trim();

                        @SuppressWarnings("unchecked")
                        Map<String, Object> resultado = objectMapper.readValue(respostaLimpa, Map.class);

                        MedicamentoExtraidoDTO dto = new MedicamentoExtraidoDTO();
                        dto.setTextoOriginal(textoReceita);

                        List<MedicamentoExtraidoDTO.MedicamentoItem> medicamentos = new ArrayList<>();

                        if (resultado.containsKey("medicamentos")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> medicamentosMap =
                                    (List<Map<String, Object>>) resultado.get("medicamentos");

                            if (medicamentosMap != null) {
                                for (Map<String, Object> med : medicamentosMap) {
                                    String nomeMedicamento = (String) med.getOrDefault("nome", "");
                                    if (nomeMedicamento == null || nomeMedicamento.trim().isEmpty()) {
                                        continue;
                                    }

                                    MedicamentoExtraidoDTO.MedicamentoItem item =
                                            new MedicamentoExtraidoDTO.MedicamentoItem();

                                    item.setNome(nomeMedicamento.toLowerCase());

                                    Object qtd = med.get("quantidade");
                                    if (qtd instanceof Number) {
                                        item.setQuantidade(((Number) qtd).intValue());
                                    } else {
                                        item.setQuantidade(1);
                                    }

                                    item.setDosagem((String) med.getOrDefault("dosagem", ""));

                                    // Adicionar descrição fornecida pela IA
                                    String descricaoIA = (String) med.getOrDefault("descricao", "");
                                    item.setDescricaoIA(descricaoIA);

                                    // Buscar produto no banco de dados
                                    buscarProdutoNoBanco(item);

                                    medicamentos.add(item);
                                }
                            }
                        }

                        dto.setMedicamentos(medicamentos);
                        log.info("Processados {} medicamentos da receita", medicamentos.size());
                        return dto;

                    } catch (Exception e) {
                        log.error("Erro ao parsear resposta do Groq", e);
                        log.error("Resposta original: {}", respostaGroq);

                        MedicamentoExtraidoDTO dto = new MedicamentoExtraidoDTO();
                        dto.setMedicamentos(new ArrayList<>());
                        dto.setTextoOriginal("Erro ao processar: " + e.getMessage() + "\n\nResposta da IA:\n" + respostaGroq);
                        return dto;
                    }
                });
    }

    private void buscarProdutoNoBanco(MedicamentoExtraidoDTO.MedicamentoItem item) {
        try {
            String nomeBusca = item.getNome().toLowerCase().trim();
            log.info("Buscando produto no banco: {}", nomeBusca);

            // Estratégia de busca em ordem de prioridade:
            Produto produtoEncontrado = null;

            // 1. Tentativa: Busca exata (melhor cenário)
            Optional<Produto> exato = produtoRepository.findByNomeIgnoreCase(nomeBusca);
            if (exato.isPresent()) {
                produtoEncontrado = exato.get();
                log.info("✅ Match exato encontrado: {}", produtoEncontrado.getNome());
            }

            // 2. Tentativa: Busca por início do nome (ex: "para" -> "Paracetamol")
            if (produtoEncontrado == null) {
                List<Produto> comecamCom = produtoRepository.findByNomeStartingWithIgnoreCase(nomeBusca);
                if (!comecamCom.isEmpty()) {
                    produtoEncontrado = comecamCom.get(0);
                    log.info("✅ Match por início do nome: {}", produtoEncontrado.getNome());
                }
            }

            // 3. Tentativa: Busca contendo o nome (método original)
            if (produtoEncontrado == null) {
                List<Produto> contem = produtoRepository.findByNomeContainingIgnoreCase(nomeBusca);
                if (!contem.isEmpty()) {
                    produtoEncontrado = contem.get(0);
                    log.info("✅ Match por nome contendo: {}", produtoEncontrado.getNome());
                }
            }

            // 4. Tentativa: Busca na descrição (último recurso)
            if (produtoEncontrado == null) {
                List<Produto> descricao = produtoRepository.buscarPorNomeOuDescricao(nomeBusca);
                if (!descricao.isEmpty()) {
                    produtoEncontrado = descricao.get(0);
                    log.info("✅ Match encontrado na descrição: {}", produtoEncontrado.getNome());
                }
            }

            // Se encontrou, preenche os dados
            if (produtoEncontrado != null) {
                item.setProdutoId(produtoEncontrado.getId());
                item.setPreco(produtoEncontrado.getPreco().doubleValue());
                item.setDescricaoProduto(produtoEncontrado.getDescricao());
                item.setNomeCompleto(produtoEncontrado.getNome());
                item.setNome(produtoEncontrado.getNome());
                item.setEstoque(produtoEncontrado.getEstoque());
                log.info("✅ Produto selecionado: {} - R$ {} - Estoque: {}",
                        produtoEncontrado.getNome(), produtoEncontrado.getPreco(), produtoEncontrado.getEstoque());
            } else {
                log.warn("❌ Produto NÃO encontrado no banco: {}", nomeBusca);
                // Usar a descrição fornecida pela IA se o produto não foi encontrado
                String descricaoFinal = item.getDescricaoIA() != null && !item.getDescricaoIA().isEmpty()
                        ? item.getDescricaoIA()
                        : "⚠️ Medicamento identificado na receita, mas não encontrado no nosso catálogo. Consulte nosso atendimento.";
                item.setDescricaoProduto(descricaoFinal);
                item.setPreco(0.0);
                item.setNomeCompleto(item.getNome());
                item.setEstoque(0);
            }

        } catch (Exception e) {
            log.error("Erro ao buscar produto: {}", item.getNome(), e);
            item.setDescricaoProduto("Erro ao consultar disponibilidade do medicamento.");
            item.setPreco(0.0);
            item.setEstoque(0);
        }
    }
}