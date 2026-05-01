<template>
  <div class="container mt-5 mb-5">
    <div class="card shadow-sm border-0">
      <div class="card-body">
        <h3 class="mb-3">Rastreio do Pedido</h3>

        <div class="mb-3">
          <label class="form-label">Codigo do pedido</label>
          <div class="input-group">
            <input v-model="codigo" class="form-control" placeholder="Ex: PED171..." @keyup.enter="carregar" />
            <button class="btn btn-primary" :disabled="loading || !codigo.trim()" @click="carregar">
              {{ loading ? 'Buscando...' : 'Buscar' }}
            </button>
          </div>
        </div>

        <div v-if="erro" class="alert alert-danger">{{ erro }}</div>

        <div v-if="pedido" class="mb-4">
          <div><strong>Pedido:</strong> {{ pedido.codigoPedido }} (id {{ pedido.id }})</div>
          <div><strong>Status:</strong> {{ pedido.status }}</div>
          <div><strong>Total:</strong> R$ {{ (pedido.totalFinal ?? pedido.subtotal ?? 0).toFixed(2) }}</div>
        </div>

        <div v-if="rastreio" class="alert alert-success">
          <div><strong>Codigo rastreio:</strong> {{ rastreio.codigoRastreio }}</div>
          <div><strong>Status entrega:</strong> {{ rastreio.status }}</div>
          <div><strong>Ultima localizacao:</strong> {{ rastreio.ultimaLocalizacao || 'N/A' }}</div>
          <div><strong>Transportadora:</strong> {{ rastreio.transportadora || 'N/A' }}</div>
          <div class="mt-2">
            <span class="badge" :class="liveConnected ? 'bg-success' : 'bg-secondary'">
              {{ liveConnected ? 'Ao vivo' : 'Sem conexao ao vivo' }}
            </span>
            <span v-if="liveError" class="small text-muted ms-2">{{ liveError }}</span>
          </div>
        </div>

        <div v-else-if="pedido && !loading" class="alert alert-warning">
          Ainda nao existe rastreio para este pedido.
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api'

export default {
  name: 'BackendOrderTracking',
  props: {
    codigoPedido: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      codigo: '',
      loading: false,
      pedido: null,
      rastreio: null,
      erro: null,
      liveConnected: false,
      liveError: null,
      eventSource: null
    }
  },
  created() {
    this.codigo = this.codigoPedido || (this.$route?.params?.codigoPedido ?? '')
    if (this.codigo) {
      this.carregar()
    }
  },
  beforeUnmount() {
    this.desconectarAoVivo()
  },
  methods: {
    desconectarAoVivo() {
      if (this.eventSource) {
        try { this.eventSource.close() } catch (e) {}
        this.eventSource = null
      }
      this.liveConnected = false
      this.liveError = null
    },
    conectarAoVivo(pedidoId) {
      this.desconectarAoVivo()
      if (!pedidoId) return

      const base = String(api?.defaults?.baseURL || '/api').replace(/\/$/, '')
      const url = `${base}/rastreios/pedido/${encodeURIComponent(pedidoId)}/stream`

      try {
        const es = new EventSource(url)
        this.eventSource = es

        es.onopen = () => {
          this.liveConnected = true
          this.liveError = null
        }

        es.addEventListener('rastreio', (evt) => {
          try {
            const data = JSON.parse(evt.data)
            // Atualiza em tempo real quando o backend publica nova posicao/status.
            this.rastreio = data
          } catch (e) {
            // ignora payload invalido
          }
        })

        es.onerror = () => {
          // O browser tenta reconectar automaticamente; sinalizamos degradacao.
          this.liveConnected = false
          this.liveError = 'Conexao ao vivo instavel (reconectando...)'
        }
      } catch (e) {
        this.liveConnected = false
        this.liveError = 'Navegador nao suporta conexao ao vivo'
      }
    },
    async carregar() {
      this.erro = null
      this.loading = true
      this.pedido = null
      this.rastreio = null
      this.desconectarAoVivo()

      try {
        const codigo = String(this.codigo || '').trim()
        const pedidoResp = await api.get(`/pedidos/codigo/${encodeURIComponent(codigo)}`)
        this.pedido = pedidoResp.data
        this.conectarAoVivo(this.pedido?.id)

        try {
          const rastreioResp = await api.get(`/pedidos/${encodeURIComponent(codigo)}/rastreio`)
          // pode vir MensagemResponseDTO em caso de erro; tentamos detectar.
          if (rastreioResp.data && rastreioResp.data.codigoRastreio) {
            this.rastreio = rastreioResp.data
          } else {
            this.rastreio = null
          }
        } catch (e) {
          this.rastreio = null
        }
      } catch (e) {
        this.erro = e.response?.data?.message || e.response?.data?.mensagem || e.message || 'Erro ao buscar pedido'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
