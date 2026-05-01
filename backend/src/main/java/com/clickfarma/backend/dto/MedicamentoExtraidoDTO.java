package com.clickfarma.backend.dto;

import java.util.List;
import java.util.ArrayList;

/**
 * DTO para retorno dos medicamentos extraídos da receita
 */
public class MedicamentoExtraidoDTO {

    private List<MedicamentoItem> medicamentos = new ArrayList<>();
    private String textoOriginal;

    /**
     * Texto explicando a leitura da receita e o que o ClickFarma oferece (incluindo alternativas).
     */
    private String mensagemOrientacao;

    // Construtor padrão
    public MedicamentoExtraidoDTO() {
    }

    // Getter e Setter
    public List<MedicamentoItem> getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(List<MedicamentoItem> medicamentos) {
        this.medicamentos = medicamentos;
    }

    public String getTextoOriginal() {
        return textoOriginal;
    }

    public void setTextoOriginal(String textoOriginal) {
        this.textoOriginal = textoOriginal;
    }

    public String getMensagemOrientacao() {
        return mensagemOrientacao;
    }

    public void setMensagemOrientacao(String mensagemOrientacao) {
        this.mensagemOrientacao = mensagemOrientacao;
    }

    /**
     * Classe interna para representar um item de medicamento
     */
    public static class MedicamentoItem {
        private String nome;
        private Integer quantidade;
        private String dosagem;           // Ex: "500mg"
        private Long produtoId;           // ID do produto no banco (opcional)
        private Double preco;             // Preço do produto (opcional)
        private String descricaoProduto;  // Descrição do produto
        private String nomeCompleto;      // Nome completo do produto no banco
        private String descricaoIA;       // Descrição fornecida pela IA
        private Integer estoque;          // Quantidade em estoque

        // NOVOS CAMPOS PARA RECOMPRA
        private String posologia;         // Ex: "1 comprimido a cada 12 horas"
        private Integer diasDuracao;      // Ex: 30 (calculado pela IA)

        /**
         * DISPONIVEL | SEM_ESTOQUE | NAO_ENCONTRADO
         */
        private String situacaoCatalogo;

        private List<AlternativaSugeridaDTO> alternativasSugeridas = new ArrayList<>();

        // Construtor padrão (estoque null = ainda não vinculado ao catálogo; 0 = vinculado mas esgotado)
        public MedicamentoItem() {
            this.quantidade = 1;
            this.preco = 0.0;
        }

        // Getters e Setters
        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public Integer getQuantidade() {
            return quantidade;
        }

        public void setQuantidade(Integer quantidade) {
            this.quantidade = quantidade;
        }

        public String getDosagem() {
            return dosagem;
        }

        public void setDosagem(String dosagem) {
            this.dosagem = dosagem;
        }

        public Long getProdutoId() {
            return produtoId;
        }

        public void setProdutoId(Long produtoId) {
            this.produtoId = produtoId;
        }

        public Double getPreco() {
            return preco;
        }

        public void setPreco(Double preco) {
            this.preco = preco;
        }

        public String getDescricaoProduto() {
            return descricaoProduto;
        }

        public void setDescricaoProduto(String descricaoProduto) {
            this.descricaoProduto = descricaoProduto;
        }

        public String getNomeCompleto() {
            return nomeCompleto;
        }

        public void setNomeCompleto(String nomeCompleto) {
            this.nomeCompleto = nomeCompleto;
        }

        public String getDescricaoIA() {
            return descricaoIA;
        }

        public void setDescricaoIA(String descricaoIA) {
            this.descricaoIA = descricaoIA;
        }

        public Integer getEstoque() {
            return estoque;
        }

        public void setEstoque(Integer estoque) {
            this.estoque = estoque;
        }

        // NOVOS GETTERS E SETTERS
        public String getPosologia() {
            return posologia;
        }

        public void setPosologia(String posologia) {
            this.posologia = posologia;
        }

        public Integer getDiasDuracao() {
            return diasDuracao;
        }

        public void setDiasDuracao(Integer diasDuracao) {
            this.diasDuracao = diasDuracao;
        }

        public String getSituacaoCatalogo() {
            return situacaoCatalogo;
        }

        public void setSituacaoCatalogo(String situacaoCatalogo) {
            this.situacaoCatalogo = situacaoCatalogo;
        }

        public List<AlternativaSugeridaDTO> getAlternativasSugeridas() {
            return alternativasSugeridas;
        }

        public void setAlternativasSugeridas(List<AlternativaSugeridaDTO> alternativasSugeridas) {
            this.alternativasSugeridas = alternativasSugeridas != null ? alternativasSugeridas : new ArrayList<>();
        }

        @Override
        public String toString() {
            return "MedicamentoItem{" +
                    "nome='" + nome + '\'' +
                    ", quantidade=" + quantidade +
                    ", dosagem='" + dosagem + '\'' +
                    ", preco=" + preco +
                    ", descricaoProduto='" + descricaoProduto + '\'' +
                    ", estoque=" + estoque +
                    ", posologia='" + posologia + '\'' +
                    ", diasDuracao=" + diasDuracao +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MedicamentoExtraidoDTO{" +
                "medicamentos=" + medicamentos +
                ", textoOriginalLength=" + (textoOriginal != null ? textoOriginal.length() : 0) +
                '}';
    }
}