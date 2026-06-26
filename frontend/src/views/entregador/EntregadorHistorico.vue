<template>
  <div class="historico-page">
    <h2 class="page-title">📜 Histórico de entregas</h2>

    <div v-if="carregando" class="loading">Carregando...</div>

    <div v-if="!entregas.length && !carregando" class="empty-state">
      <span class="empty-icon">📭</span>
      <p>Nenhuma entrega finalizada ainda</p>
    </div>

    <div v-for="e in entregas" :key="e.id" class="historico-card">
      <div class="card-header">
        <span class="pedido-tag">#{{ e.codigoPedido }}</span>
        <span class="data">{{ formatDate(e.entregueEm || e.criadoEm) }}</span>
        <span class="valor">R$ {{ formatMoney(e.taxaEntrega) }}</span>
      </div>
      <p class="cliente" v-if="e.clienteNome">👤 {{ e.clienteNome }}</p>
      <p class="endereco" v-if="e.enderecoDestino">📍 {{ e.enderecoDestino }}</p>
    </div>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'

export default {
  name: 'EntregadorHistorico',
  data() {
    return { entregas: [], carregando: true }
  },
  async created() {
    try {
      const entregador = entregadorService.getEntregador()
      if (entregador) this.entregas = await entregadorService.getHistorico(entregador.id)
    } catch (err) {
      console.error('Erro:', err)
    } finally {
      this.carregando = false
    }
  },
  methods: {
    formatMoney(v) { return v ? Number(v).toFixed(2).replace('.', ',') : '0,00' },
    formatDate(d) {
      if (!d) return ''
      return new Date(d).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: '2-digit' })
    }
  }
}
</script>

<style scoped>
.historico-page { padding-top: 4px; }
.page-title { font-size: 18px; color: #333; margin: 0 0 16px; }
.loading { text-align: center; padding: 40px; color: #999; }
.empty-state { text-align: center; padding: 60px 20px; color: #999; }
.empty-icon { font-size: 48px; display: block; margin-bottom: 12px; }
.historico-card {
  background: white; border-radius: 14px; padding: 14px 16px; margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.pedido-tag { font-weight: 700; color: #333; font-size: 14px; }
.data { font-size: 12px; color: #888; }
.valor { color: #2ecc71; font-weight: 700; }
.cliente, .endereco { font-size: 13px; color: #555; margin: 3px 0; }
</style>
