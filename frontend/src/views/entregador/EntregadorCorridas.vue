<template>
  <div class="corridas-page">
    <div class="tab-bar">
      <button :class="['tab', { active: tab === 'pendentes' }]" @click="tab = 'pendentes'">
        Pendentes <span v-if="pendentes.length" class="tab-badge">{{ pendentes.length }}</span>
      </button>
      <button :class="['tab', { active: tab === 'ativas' }]" @click="tab = 'ativas'">
        Ativas <span v-if="ativas.length" class="tab-badge ativo">{{ ativas.length }}</span>
      </button>
    </div>

    <div v-if="carregando" class="loading">Carregando...</div>

    <div v-if="tab === 'pendentes'">
      <div v-if="!pendentes.length && !carregando" class="empty-state">
        <span class="empty-icon">🕊️</span>
        <p>Nenhuma corrida disponível no momento</p>
      </div>
      <div v-for="e in pendentes" :key="e.id" class="corrida-card" @click="verDetalhe(e.id)">
        <div class="card-header">
          <span class="pedido-tag">#{{ e.codigoPedido }}</span>
          <span class="taxa">R$ {{ e.taxaEntrega || '0,00' }}</span>
        </div>
        <div class="card-body">
          <p class="cliente" v-if="e.clienteNome">👤 {{ e.clienteNome }}</p>
          <p class="endereco" v-if="e.enderecoDestino">📍 {{ e.enderecoDestino }}</p>
          <p class="distancia" v-if="e.distanciaKm">📏 {{ e.distanciaKm }} km</p>
        </div>
        <div class="card-footer">
          <span class="time">{{ formatTime(e.criadoEm) }}</span>
        </div>
      </div>
    </div>

    <div v-if="tab === 'ativas'">
      <div v-if="!ativas.length && !carregando" class="empty-state">
        <span class="empty-icon">🏍️</span>
        <p>Nenhuma corrida ativa</p>
      </div>
      <div v-for="e in ativas" :key="e.id" class="corrida-card ativa" @click="verDetalhe(e.id)">
        <div class="card-header">
          <span class="pedido-tag">#{{ e.codigoPedido }}</span>
          <span class="status-badge" :class="e.status.toLowerCase()">{{ e.status }}</span>
        </div>
        <div class="card-body">
          <p class="cliente" v-if="e.clienteNome">👤 {{ e.clienteNome }}</p>
          <p class="endereco" v-if="e.enderecoDestino">📍 {{ e.enderecoDestino }}</p>
        </div>
        <div class="card-footer">
          <span class="time">{{ formatTime(e.criadoEm) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'

export default {
  name: 'EntregadorCorridas',
  data() {
    return {
      tab: 'pendentes',
      pendentes: [],
      ativas: [],
      carregando: true,
      pollTimer: null
    }
  },
  async created() {
    await this.carregar()
    this.pollTimer = setInterval(() => this.carregar(), 10000)
  },
  beforeUnmount() {
    if (this.pollTimer) clearInterval(this.pollTimer)
  },
  methods: {
    async carregar() {
      try {
        const entregador = entregadorService.getEntregador()
        if (!entregador) return
        const [pends, atv] = await Promise.all([
          entregadorService.getPendentes(),
          entregadorService.getEntregasAtivas(entregador.id)
        ])
        this.pendentes = pends
        this.ativas = atv
      } catch (err) {
        console.error('Erro ao carregar corridas:', err)
      } finally {
        this.carregando = false
      }
    },
    verDetalhe(id) {
      this.$router.push(`/entregador/corridas/${id}`)
    },
    formatTime(date) {
      if (!date) return ''
      const d = new Date(date)
      return d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    }
  }
}
</script>

<style scoped>
.corridas-page { padding-top: 4px; }
.tab-bar { display: flex; gap: 8px; margin-bottom: 16px; }
.tab {
  flex: 1; padding: 10px; border: none; border-radius: 10px; font-size: 14px;
  font-weight: 600; cursor: pointer; background: #e8e8e8; color: #666; position: relative;
}
.tab.active { background: #667eea; color: white; }
.tab-badge {
  background: #e74c3c; color: white; border-radius: 50%; width: 20px; height: 20px;
  display: inline-flex; align-items: center; justify-content: center; font-size: 11px;
  margin-left: 4px;
}
.tab-badge.ativo { background: #2ecc71; }
.loading { text-align: center; padding: 40px; color: #999; }
.empty-state { text-align: center; padding: 60px 20px; color: #999; }
.empty-icon { font-size: 48px; display: block; margin-bottom: 12px; }
.corrida-card {
  background: white; border-radius: 14px; padding: 16px; margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06); cursor: pointer; transition: transform 0.15s;
}
.corrida-card:hover { transform: translateY(-1px); }
.corrida-card.ativa { border-left: 4px solid #2ecc71; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.pedido-tag { font-weight: 700; color: #333; font-size: 15px; }
.taxa { color: #2ecc71; font-weight: 700; font-size: 16px; }
.status-badge {
  padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 600;
}
.status-badge.aceita { background: #fef3cd; color: #856404; }
.status-badge.retirada { background: #d1ecf1; color: #0c5460; }
.status-badge.em_rota { background: #cce5ff; color: #004085; }
.card-body p { margin: 4px 0; font-size: 13px; color: #555; }
.cliente { font-weight: 500; }
.endereco { color: #777; }
.distancia { color: #999; font-size: 12px; }
.card-footer { margin-top: 8px; }
.time { font-size: 11px; color: #bbb; }
</style>
