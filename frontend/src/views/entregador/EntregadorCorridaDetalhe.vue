<template>
  <div class="detalhe-page">
    <button class="btn-voltar" @click="$router.push('/entregador/corridas')">← Voltar</button>

    <div v-if="carregando" class="loading">Carregando...</div>

    <div v-if="entrega" class="detalhe-container">
      <div class="status-bar" :class="entrega.status.toLowerCase()">
        <span class="status-icon">{{ statusIcon }}</span>
        <span class="status-text">{{ statusLabel }}</span>
      </div>

      <div class="info-section">
        <h3>Pedido #{{ entrega.codigoPedido }}</h3>
        <div class="info-row" v-if="entrega.clienteNome">
          <span class="label">Cliente</span>
          <span class="value">{{ entrega.clienteNome }}</span>
        </div>
        <div class="info-row" v-if="entrega.clienteTelefone">
          <span class="label">Telefone</span>
          <span class="value">{{ entrega.clienteTelefone }}</span>
        </div>
      </div>

      <div class="map-section">
        <div class="endereco-box">
          <p class="end-label">📍 Origem</p>
          <p class="end-value">{{ entrega.enderecoOrigem || 'Farmácia' }}</p>
        </div>
        <div class="endereco-box">
          <p class="end-label">🏠 Destino</p>
          <p class="end-value">{{ entrega.enderecoDestino }}</p>
        </div>
      </div>

      <div class="valores-section">
        <div class="valor-item">
          <span class="valor-label">Taxa de entrega</span>
          <span class="valor-value">R$ {{ formatMoney(entrega.taxaEntrega) }}</span>
        </div>
        <div class="valor-item" v-if="entrega.distanciaKm">
          <span class="valor-label">Distância</span>
          <span class="valor-value">{{ entrega.distanciaKm }} km</span>
        </div>
      </div>

      <div class="timeline">
        <div class="tl-item" :class="{ done: entrega.criadoEm }">
          <div class="tl-dot"></div>
          <div class="tl-content">
            <p class="tl-title">Pedido criado</p>
            <p class="tl-time">{{ formatTime(entrega.criadoEm) }}</p>
          </div>
        </div>
        <div class="tl-item" :class="{ done: entrega.aceitoEm }">
          <div class="tl-dot"></div>
          <div class="tl-content">
            <p class="tl-title">Aceito</p>
            <p class="tl-time">{{ formatTime(entrega.aceitoEm) }}</p>
          </div>
        </div>
        <div class="tl-item" :class="{ done: entrega.retiradoEm }">
          <div class="tl-dot"></div>
          <div class="tl-content">
            <p class="tl-title">Retirado</p>
            <p class="tl-time">{{ formatTime(entrega.retiradoEm) }}</p>
          </div>
        </div>
        <div class="tl-item" :class="{ done: entrega.entregueEm }">
          <div class="tl-dot"></div>
          <div class="tl-content">
            <p class="tl-title">Entregue</p>
            <p class="tl-time">{{ formatTime(entrega.entregueEm) }}</p>
          </div>
        </div>
      </div>

      <div class="acoes">
        <button v-if="entrega.status === 'PENDENTE'" class="btn-aceitar" @click="aceitar">
          ✅ Aceitar corrida
        </button>
        <button v-if="entrega.status === 'PENDENTE'" class="btn-recusar" @click="recusar">
          ❌ Recusar
        </button>
        <button v-if="entrega.status === 'ACEITA'" class="btn-acao" @click="retirar">
          📦 Retirar pedido
        </button>
        <button v-if="entrega.status === 'RETIRADA'" class="btn-acao" @click="iniciarRota">
          🚀 Iniciar rota
        </button>
        <button v-if="entrega.status === 'EM_ROTA'" class="btn-finalizar" @click="finalizar">
          ✅ Finalizar entrega
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'

export default {
  name: 'EntregadorCorridaDetalhe',
  props: ['id'],
  data() {
    return { entrega: null, carregando: true }
  },
  async created() {
    await this.carregar()
  },
  computed: {
    statusIcon() {
      const icons = { PENDENTE: '🆕', ACEITA: '✅', RETIRADA: '📦', EM_ROTA: '🚀', ENTREGUE: '🎉', RECUSADA: '❌', CANCELADA: '🚫' }
      return icons[this.entrega?.status] || '📋'
    },
    statusLabel() {
      const labels = {
        PENDENTE: 'Pedido disponível', ACEITA: 'Aceito por você', RETIRADA: 'Pedido retirado',
        EM_ROTA: 'Saiu para entrega', ENTREGUE: 'Entregue com sucesso',
        RECUSADA: 'Recusado', CANCELADA: 'Cancelado'
      }
      return labels[this.entrega?.status] || this.entrega?.status
    }
  },
  methods: {
    async carregar() {
      try {
        this.entrega = await entregadorService.getEntrega(this.id)
      } catch (err) {
        console.error('Erro:', err)
      } finally {
        this.carregando = false
      }
    },
    async aceitar() {
      const entregador = entregadorService.getEntregador()
      if (!entregador) return
      try {
        this.entrega = await entregadorService.aceitarEntrega(this.id, entregador.id)
      } catch (err) { alert(err.response?.data || 'Erro') }
    },
    async recusar() {
      const entregador = entregadorService.getEntregador()
      if (!entregador) return
      try {
        await entregadorService.recusarEntrega(this.id, entregador.id)
        this.$router.push('/entregador/corridas')
      } catch (err) { alert(err.response?.data || 'Erro') }
    },
    async retirar() {
      try {
        this.entrega = await entregadorService.marcarRetirada(this.id)
      } catch (err) { alert(err.response?.data || 'Erro') }
    },
    async iniciarRota() {
      try {
        this.entrega = await entregadorService.iniciarRota(this.id)
      } catch (err) { alert(err.response?.data || 'Erro') }
    },
    async finalizar() {
      try {
        this.entrega = await entregadorService.finalizarEntrega(this.id)
      } catch (err) { alert(err.response?.data || 'Erro') }
    },
    formatMoney(v) { return v ? Number(v).toFixed(2).replace('.', ',') : '0,00' },
    formatTime(d) {
      if (!d) return '—'
      return new Date(d).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
    }
  }
}
</script>

<style scoped>
.detalhe-page { padding-top: 4px; }
.btn-voltar {
  background: none; border: none; color: #667eea; font-size: 14px;
  font-weight: 600; cursor: pointer; padding: 0; margin-bottom: 12px;
}
.loading { text-align: center; padding: 40px; color: #999; }
.status-bar {
  display: flex; align-items: center; gap: 10px; padding: 16px; border-radius: 14px;
  margin-bottom: 16px; color: white; font-weight: 600;
}
.status-bar.pendente { background: #667eea; }
.status-bar.aceita { background: #2ecc71; }
.status-bar.retirada { background: #f39c12; }
.status-bar.em_rota { background: #3498db; }
.status-bar.entregue { background: #27ae60; }
.status-bar.recusada, .status-bar.cancelada { background: #e74c3c; }
.status-icon { font-size: 24px; }
.status-text { font-size: 15px; }
.info-section { background: white; border-radius: 14px; padding: 16px; margin-bottom: 12px; }
.info-section h3 { margin: 0 0 12px; font-size: 16px; color: #333; }
.info-row { display: flex; justify-content: space-between; padding: 6px 0; }
.label { color: #888; font-size: 13px; }
.value { color: #333; font-size: 13px; font-weight: 500; }
.map-section { margin-bottom: 12px; }
.endereco-box {
  background: white; border-radius: 10px; padding: 12px 16px; margin-bottom: 8px;
}
.end-label { font-size: 12px; color: #888; margin: 0 0 4px; }
.end-value { font-size: 14px; color: #333; margin: 0; }
.valores-section {
  display: flex; gap: 12px; margin-bottom: 16px;
}
.valor-item {
  flex: 1; background: white; border-radius: 14px; padding: 14px; text-align: center;
}
.valor-label { font-size: 12px; color: #888; display: block; margin-bottom: 4px; }
.valor-value { font-size: 18px; font-weight: 700; color: #2ecc71; }
.timeline { background: white; border-radius: 14px; padding: 16px; margin-bottom: 16px; }
.tl-item { display: flex; gap: 12px; padding: 8px 0; position: relative; }
.tl-item::before {
  content: ''; position: absolute; left: 7px; top: 28px;
  width: 2px; height: calc(100% - 28px); background: #e0e0e0;
}
.tl-item:last-child::before { display: none; }
.tl-dot {
  width: 16px; height: 16px; border-radius: 50%; background: #e0e0e0;
  flex-shrink: 0; margin-top: 2px;
}
.tl-item.done .tl-dot { background: #2ecc71; }
.tl-title { font-size: 13px; color: #555; margin: 0; }
.tl-time { font-size: 11px; color: #bbb; margin: 2px 0 0; }
.acoes { display: flex; flex-direction: column; gap: 8px; }
.btn-aceitar, .btn-finalizar {
  padding: 14px; border: none; border-radius: 12px; font-size: 15px;
  font-weight: 600; cursor: pointer; background: #2ecc71; color: white;
}
.btn-recusar {
  padding: 12px; border: 2px solid #e74c3c; border-radius: 12px; font-size: 14px;
  font-weight: 600; cursor: pointer; background: white; color: #e74c3c;
}
.btn-acao {
  padding: 14px; border: none; border-radius: 12px; font-size: 15px;
  font-weight: 600; cursor: pointer; background: #667eea; color: white;
}
</style>
