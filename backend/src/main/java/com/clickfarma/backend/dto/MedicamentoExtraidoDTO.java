package com.clickfarma.backend.dto;

import java.util.List;

/**
 * DTO para retorno dos medicamentos extraídos da receita
 */
public class MedicamentoExtraidoDTO {
    private List<MedicamentoItem> medicamentos;
    private String textoOriginal;  // O texto extraído da receita (para debug)

    // Construtores
    public MedicamentoExtraidoDTO() {
    }

    public MedicamentoExtraidoDTO(List<MedicamentoItem> medicamentos, String textoOriginal) {
        this.medicamentos = medicamentos;
        this.textoOriginal = textoOriginal;
    }

    // Getters e Setters
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

    /**
     * Classe interna para representar um item de medicamento
     */
    public static class MedicamentoItem {
        private String nome;
        private Integer quantidade;
        private String dosagem;  // Ex: "500mg"
        private Long produtoId;   // ID do produto no banco (opcional)
        private Double preco;     // Preço do produto (opcional)
        private String descricaoProduto;  // Descrição do produto
        private String nomeCompleto;      // Nome completo do produto no banco
        private String descricaoIA;       // Descrição fornecida pela IA
        private Integer estoque;          // Quantidade em estoque

        // Construtores
        public MedicamentoItem() {
            this.quantidade = 1; // Quantidade padrão
            this.preco = 0.0;
        }

        public MedicamentoItem(String nome, Integer quantidade, String dosagem) {
            this.nome = nome;
            this.quantidade = quantidade != null ? quantidade : 1;
            this.dosagem = dosagem;
            this.preco = 0.0;
            this.estoque = 0;
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
            this.quantidade = quantidade != null ? quantidade : 1;
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
            this.estoque = estoque != null ? estoque : 0;
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