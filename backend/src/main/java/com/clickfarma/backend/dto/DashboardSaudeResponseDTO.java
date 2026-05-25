package com.clickfarma.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSaudeResponseDTO {

    private ResumoGeral resumo;
    private List<GastoMensal> gastosMensais;
    private List<DistribuicaoCategoria> distribuicaoCategoria;
    private List<TopMedicamento> topMedicamentos;
    private List<PrevisaoReposicao> previsaoReposicao;

    // Inner classes
    public static class ResumoGeral {
        private BigDecimal totalGasto;
        private int totalPedidos;
        private String medicamentoFrequente;
        private BigDecimal economiaEstimada;

        public ResumoGeral(BigDecimal totalGasto, int totalPedidos, String medicamentoFrequente, BigDecimal economiaEstimada) {
            this.totalGasto = totalGasto;
            this.totalPedidos = totalPedidos;
            this.medicamentoFrequente = medicamentoFrequente;
            this.economiaEstimada = economiaEstimada;
        }

        public BigDecimal getTotalGasto() { return totalGasto; }
        public int getTotalPedidos() { return totalPedidos; }
        public String getMedicamentoFrequente() { return medicamentoFrequente; }
        public BigDecimal getEconomiaEstimada() { return economiaEstimada; }
    }

    public static class GastoMensal {
        private String mes;
        private BigDecimal valor;

        public GastoMensal(String mes, BigDecimal valor) {
            this.mes = mes;
            this.valor = valor;
        }

        public String getMes() { return mes; }
        public BigDecimal getValor() { return valor; }
    }

    public static class DistribuicaoCategoria {
        private String categoria;
        private BigDecimal valor;
        private double percentual;

        public DistribuicaoCategoria(String categoria, BigDecimal valor, double percentual) {
            this.categoria = categoria;
            this.valor = valor;
            this.percentual = percentual;
        }

        public String getCategoria() { return categoria; }
        public BigDecimal getValor() { return valor; }
        public double getPercentual() { return percentual; }
    }

    public static class TopMedicamento {
        private String nome;
        private int quantidadeComprada;
        private BigDecimal totalGasto;
        private String ultimaCompra;

        public TopMedicamento(String nome, int quantidadeComprada, BigDecimal totalGasto, String ultimaCompra) {
            this.nome = nome;
            this.quantidadeComprada = quantidadeComprada;
            this.totalGasto = totalGasto;
            this.ultimaCompra = ultimaCompra;
        }

        public String getNome() { return nome; }
        public int getQuantidadeComprada() { return quantidadeComprada; }
        public BigDecimal getTotalGasto() { return totalGasto; }
        public String getUltimaCompra() { return ultimaCompra; }
    }

    public static class PrevisaoReposicao {
        private String produto;
        private String categoria;
        private String ultimaCompra;
        private int diasRestantes;
        private String status; // "ok", "atencao", "critico"

        public PrevisaoReposicao(String produto, String categoria, String ultimaCompra, int diasRestantes, String status) {
            this.produto = produto;
            this.categoria = categoria;
            this.ultimaCompra = ultimaCompra;
            this.diasRestantes = diasRestantes;
            this.status = status;
        }

        public String getProduto() { return produto; }
        public String getCategoria() { return categoria; }
        public String getUltimaCompra() { return ultimaCompra; }
        public int getDiasRestantes() { return diasRestantes; }
        public String getStatus() { return status; }
    }

    // Getters e Setters
    public ResumoGeral getResumo() { return resumo; }
    public void setResumo(ResumoGeral resumo) { this.resumo = resumo; }

    public List<GastoMensal> getGastosMensais() { return gastosMensais; }
    public void setGastosMensais(List<GastoMensal> gastosMensais) { this.gastosMensais = gastosMensais; }

    public List<DistribuicaoCategoria> getDistribuicaoCategoria() { return distribuicaoCategoria; }
    public void setDistribuicaoCategoria(List<DistribuicaoCategoria> distribuicaoCategoria) { this.distribuicaoCategoria = distribuicaoCategoria; }

    public List<TopMedicamento> getTopMedicamentos() { return topMedicamentos; }
    public void setTopMedicamentos(List<TopMedicamento> topMedicamentos) { this.topMedicamentos = topMedicamentos; }

    public List<PrevisaoReposicao> getPrevisaoReposicao() { return previsaoReposicao; }
    public void setPrevisaoReposicao(List<PrevisaoReposicao> previsaoReposicao) { this.previsaoReposicao = previsaoReposicao; }
}
