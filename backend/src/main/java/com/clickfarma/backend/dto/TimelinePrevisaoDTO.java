package com.clickfarma.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TimelinePrevisaoDTO {

    private List<ItemTimeline> medicamentos;
    private ResumoTimeline resumo;

    public List<ItemTimeline> getMedicamentos() { return medicamentos; }
    public void setMedicamentos(List<ItemTimeline> medicamentos) { this.medicamentos = medicamentos; }
    public ResumoTimeline getResumo() { return resumo; }
    public void setResumo(ResumoTimeline resumo) { this.resumo = resumo; }

    public static class ItemTimeline {
        private Long produtoId;
        private String produtoNome;
        private String categoria;
        private int totalUnidadesCompradas;
        private String primeiraCompra;
        private String ultimaCompra;
        private double consumoDiarioEstimado;
        private double estoqueRestanteEstimado;
        private long diasAteEsgotar;
        private String dataPrevisaoEsgotamento;
        private String status;
        private boolean temAgendamentoAtivo;
        private Long agendamentoId;
        private String recomendacao;
        private List<EventoCompra> historicoCompras;

        public Long getProdutoId() { return produtoId; }
        public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
        public String getProdutoNome() { return produtoNome; }
        public void setProdutoNome(String produtoNome) { this.produtoNome = produtoNome; }
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public int getTotalUnidadesCompradas() { return totalUnidadesCompradas; }
        public void setTotalUnidadesCompradas(int totalUnidadesCompradas) { this.totalUnidadesCompradas = totalUnidadesCompradas; }
        public String getPrimeiraCompra() { return primeiraCompra; }
        public void setPrimeiraCompra(String primeiraCompra) { this.primeiraCompra = primeiraCompra; }
        public String getUltimaCompra() { return ultimaCompra; }
        public void setUltimaCompra(String ultimaCompra) { this.ultimaCompra = ultimaCompra; }
        public double getConsumoDiarioEstimado() { return consumoDiarioEstimado; }
        public void setConsumoDiarioEstimado(double consumoDiarioEstimado) { this.consumoDiarioEstimado = consumoDiarioEstimado; }
        public double getEstoqueRestanteEstimado() { return estoqueRestanteEstimado; }
        public void setEstoqueRestanteEstimado(double estoqueRestanteEstimado) { this.estoqueRestanteEstimado = estoqueRestanteEstimado; }
        public long getDiasAteEsgotar() { return diasAteEsgotar; }
        public void setDiasAteEsgotar(long diasAteEsgotar) { this.diasAteEsgotar = diasAteEsgotar; }
        public String getDataPrevisaoEsgotamento() { return dataPrevisaoEsgotamento; }
        public void setDataPrevisaoEsgotamento(String dataPrevisaoEsgotamento) { this.dataPrevisaoEsgotamento = dataPrevisaoEsgotamento; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isTemAgendamentoAtivo() { return temAgendamentoAtivo; }
        public void setTemAgendamentoAtivo(boolean temAgendamentoAtivo) { this.temAgendamentoAtivo = temAgendamentoAtivo; }
        public Long getAgendamentoId() { return agendamentoId; }
        public void setAgendamentoId(Long agendamentoId) { this.agendamentoId = agendamentoId; }
        public String getRecomendacao() { return recomendacao; }
        public void setRecomendacao(String recomendacao) { this.recomendacao = recomendacao; }
        public List<EventoCompra> getHistoricoCompras() { return historicoCompras; }
        public void setHistoricoCompras(List<EventoCompra> historicoCompras) { this.historicoCompras = historicoCompras; }
    }

    public static class EventoCompra {
        private String data;
        private int quantidade;
        private BigDecimal valor;

        public EventoCompra(String data, int quantidade, BigDecimal valor) {
            this.data = data;
            this.quantidade = quantidade;
            this.valor = valor;
        }

        public String getData() { return data; }
        public int getQuantidade() { return quantidade; }
        public BigDecimal getValor() { return valor; }
    }

    public static class ResumoTimeline {
        private int totalMedicamentos;
        private int criticos;
        private int atencao;
        private int ok;

        public ResumoTimeline(int totalMedicamentos, int criticos, int atencao, int ok) {
            this.totalMedicamentos = totalMedicamentos;
            this.criticos = criticos;
            this.atencao = atencao;
            this.ok = ok;
        }

        public int getTotalMedicamentos() { return totalMedicamentos; }
        public int getCriticos() { return criticos; }
        public int getAtencao() { return atencao; }
        public int getOk() { return ok; }
    }
}
