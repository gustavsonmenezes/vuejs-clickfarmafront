package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.MedicamentoExtraidoDTO;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.*;

@Service
public class GroqProcessadorReceitaService {
    private static final Logger log = LoggerFactory.getLogger(GroqProcessadorReceitaService.class);

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

        String prompt = String.format("""
                Você é um assistente farmacêutico especializado em extrair informações de receitas médicas para um e-commerce de farmácia.
                
                TEXTO DA RECEITA:
                %s
                
                INSTRUÇÕES CRÍTICAS:
                1. Retorne APENAS um JSON válido (sem markdown ```json ```, sem texto adicional, sem explicações)
                
                2. Para cada medicamento, extraia APENAS o PRINCÍPIO ATIVO ou NOME GENÉRICO:
                   - Correto: "paracetamol", "ibuprofeno", "dipirona", "amoxicilina", "loratadina"
                   - Incorreto: "Paracetamol 500mg", "Tylenol", "Novalgina", "Amoxil"
                   - Remova marcas comerciais e mantenha apenas o nome do fármaco
                
                3. Identifique com precisão:
                   - nome: SOMENTE o princípio ativo em minúsculo (ex: "paracetamol")
                   - quantidade: número de unidades/caixas prescritas (padrão: 1 se não informado)
                   - dosagem: concentração por unidade (ex: "500mg", "10mg/ml", "250mg/5ml")
                   - descricao: breve descrição amigável do medicamento (ex: "Analgésico e antitérmico para dor e febre")
                
                4. REGRAS ESPECIAIS PARA BUSCA NO BANCO DE DADOS:
                   - Para medicamentos combinados, liste os princípios ativos separadamente
                   - Exemplo: "AAS + Cafeína" -> dois medicamentos separados
                   - Ignore termos como "comprimido", "drágea", "cápsula", "solução"
                   - Padronize nomes: "ácido acetilsalicílico" -> "aspirina"
                
                5. Se um medicamento não puder ser identificado claramente, NÃO o inclua no resultado
                
                6. Se não encontrar NENHUM medicamento, retorne EXATAMENTE: {"medicamentos": []}
                
                EXEMPLOS DE RESPOSTA CORRETA:
                
                Entrada: "Prescrevo ao paciente Amoxicilina 500mg, tomar 1 comprimido de 8/8h por 7 dias"
                Saída: {"medicamentos": [{"nome": "amoxicilina", "quantidade": 1, "dosagem": "500mg", "descricao": "Antibiótico para infecções bacterianas"}]}
                
                Entrada: "Receita: Ibuprofeno 400mg - 20 comprimidos, Dipirona 500mg/ml - 1 frasco"
                Saída: {"medicamentos": [{"nome": "ibuprofeno", "quantidade": 20, "dosagem": "400mg", "descricao": "Anti-inflamatório e analgésico para dor e inflamação"}, {"nome": "dipirona", "quantidade": 1, "dosagem": "500mg/ml", "descricao": "Analgésico e antitérmico de ação rápida"}]}
                
                Entrada: "Uso contínuo: Losartana 50mg, 30 comprimidos"
                Saída: {"medicamentos": [{"nome": "losartana", "quantidade": 30, "dosagem": "50mg", "descricao": "Medicamento para controle de pressão arterial"}]}
                
                FORMATO EXATO DE RESPOSTA (sem formatação adicional):
                {
                  "medicamentos": [
                    {"nome": "medicamento1", "quantidade": 1, "dosagem": "500mg", "descricao": "Descrição breve do medicamento"},
                    {"nome": "medicamento2", "quantidade": 2, "dosagem": "400mg", "descricao": "Outra descrição breve"}
                  ]
                }
                """, textoReceita);

        return groqService.chat(prompt)
                .map(respostaGroq -> {
                    log.debug("Resposta Groq: {}", respostaGroq);

                    try {
                        String respostaLimpa = respostaGroq
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
