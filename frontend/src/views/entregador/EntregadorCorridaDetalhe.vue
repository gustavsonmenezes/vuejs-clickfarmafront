<template>
  <div class="detalhe-page">
    <button class="btn-voltar" @click="$router.push('/entregador/corridas')">
      <IconEntregador name="arrow-left" size="16" />
      Voltar
    </button>

    <!-- Skeleton -->
    <div v-if="carregando" class="skeleton-detalhe">
      <div class="skeleton-block h-60"></div>
      <div class="skeleton-block h-40"></div>
      <div class="skeleton-row">
        <div class="skeleton-block h-40"></div>
        <div class="skeleton-block h-40"></div>
        <div class="skeleton-block h-40"></div>
      </div>
      <div class="skeleton-block h-80"></div>
    </div>

    <div v-if="corrida" class="detalhe-container">
      <div class="status-bar" :class="corrida.status.toLowerCase()">
        <IconEntregador :name="statusIcon" size="24" />
        <span class="status-text">{{ statusLabel }}</span>
      </div>

      <div class="info-section">
        <h3>Pedido #{{ corrida.entrega.codigoPedido }}</h3>
        <div class="info-row" v-if="corrida.entrega.clienteNome">
          <span class="label">
            <IconEntregador name="user" size="14" />
            Cliente
          </span>
          <span class="value">{{ corrida.entrega.clienteNome }}</span>
        </div>
        <div class="info-row" v-if="corrida.entrega.clienteTelefone">
          <span class="label">
            <IconEntregador name="phone" size="14" />
            Telefone
          </span>
          <span class="value">{{ corrida.entrega.clienteTelefone }}</span>
        </div>
        <div class="info-row" v-if="corrida.multiplicadorSurge > 1">
          <span class="label">
            <IconEntregador name="trending-up" size="14" />
            Surge
          </span>
          <span class="value surge">+{{ ((corrida.multiplicadorSurge - 1) * 100).toFixed(0) }}%</span>
        </div>
      </div>

      <div class="map-section">
        <div class="endereco-box">
          <p class="end-label">
            <IconEntregador name="map-pin" size="14" />
            Origem
          </p>
          <p class="end-value">{{ corrida.entrega.enderecoOrigem || 'Farmácia' }}</p>
        </div>
        <div class="endereco-box">
          <p class="end-label">
            <IconEntregador name="map-pin" size="14" />
            Destino
          </p>
          <p class="end-value">{{ corrida.entrega.enderecoDestino }}</p>
        </div>
        <div class="map-container" :class="{ loading: carregandoMapa }">
          <div id="rota-entregador-map" class="map-el"></div>
          <div class="map-loading" v-if="carregandoMapa">
            <span class="map-loading-spinner"></span>
            <span>Carregando rota...</span>
          </div>
        </div>
      </div>

      <div class="valores-section">
        <div class="valor-item">
          <span class="valor-label">
            <IconEntregador name="dollar" size="14" />
            Taxa final
          </span>
          <span class="valor-value">R$ {{ formatMoney(corrida.taxaFinal) }}</span>
        </div>
        <div class="valor-item" v-if="corrida.distanciaKm">
          <span class="valor-label">
            <IconEntregador name="map" size="14" />
            Distância
          </span>
          <span class="valor-value">{{ Number(corrida.distanciaKm).toFixed(1) }} km</span>
        </div>
        <div class="valor-item" v-if="corrida.multiplicadorSurge > 1">
          <span class="valor-label">
            <IconEntregador name="trending-up" size="14" />
            Multiplicador
          </span>
          <span class="valor-value surge">{{ corrida.multiplicadorSurge }}x</span>
        </div>
      </div>

      <div class="timeline">
        <div class="tl-item" :class="{ done: corrida.criadoEm }">
          <div class="tl-dot"></div>
          <div class="tl-content">
            <p class="tl-title">Corrida criada</p>
            <p class="tl-time">{{ formatTime(corrida.criadoEm) }}</p>
          </div>
        </div>
        <div class="tl-item" :class="{ done: corrida.aceitoEm }">
          <div class="tl-dot"></div>
          <div class="tl-content">
            <p class="tl-title">Aceita</p>
            <p class="tl-time">{{ formatTime(corrida.aceitoEm) }}</p>
          </div>
        </div>
        <div class="tl-item" :class="{ done: corrida.status === 'EM_ANDAMENTO' || corrida.status === 'FINALIZADA' }">
          <div class="tl-dot"></div>
          <div class="tl-content">
            <p class="tl-title">Em andamento</p>
            <p class="tl-time">{{ formatTime(corrida.iniciadoEm || corrida.aceitoEm) }}</p>
          </div>
        </div>
        <div class="tl-item" :class="{ done: corrida.finalizadoEm }">
          <div class="tl-dot"></div>
          <div class="tl-content">
            <p class="tl-title">Finalizada</p>
            <p class="tl-time">{{ formatTime(corrida.finalizadoEm) }}</p>
          </div>
        </div>
      </div>

      <div class="acoes">
        <button v-if="corrida.status === 'DISPONIVEL'" class="btn-aceitar" @click="aceitar">
          <IconEntregador name="check" size="16" />
          Aceitar corrida
        </button>
        <button v-if="corrida.status === 'DISPONIVEL'" class="btn-recusar" @click="recusar">
          <IconEntregador name="x" size="16" />
          Recusar
        </button>
        <button v-if="corrida.status === 'ACEITA'" class="btn-acao" @click="iniciar">
          <IconEntregador name="package" size="16" />
          Retirar pedido
        </button>
        <button v-if="corrida.status === 'EM_ANDAMENTO'" class="btn-finalizar" @click="finalizar">
          <IconEntregador name="check" size="16" />
          Finalizar entrega
        </button>
        <a v-if="corrida.status === 'FINALIZADA' && corrida.entrega.pedidoId" 
           :href="'/api/nota-fiscal/' + corrida.entrega.pedidoId" 
           target="_blank"
           class="btn-acao nota-fiscal">
          <IconEntregador name="clipboard" size="16" />
          Nota Fiscal
        </a>
      </div>
    </div>

    <AvaliacaoModal
      v-if="mostrarAvaliacao"
      :corrida="corrida"
      @fechar="mostrarAvaliacao = false"
      @avaliado="onAvaliado"
    />
  </div>
</template>

<script>
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import entregadorService from '@/services/entregadorService'
import { routingService } from '@/services/routingService'
import AvaliacaoModal from '@/components/entregador/AvaliacaoModal.vue'
import IconEntregador from '@/components/entregador/IconEntregador.vue'
import EntregadorMixin from '@/components/entregador/EntregadorMixin.js'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
})

export default {
  name: 'EntregadorCorridaDetalhe',
  components: { AvaliacaoModal, IconEntregador },
  mixins: [EntregadorMixin],
  props: ['id'],
  data() {
    return {
      corrida: null,
      carregando: true,
      carregandoMapa: false,
      mostrarAvaliacao: false,
      map: null,
      originMarker: null,
      destinationMarker: null,
      routeLine: null,
      originCoords: null,
      destCoords: null
    }
  },
  async created() {
    await this.carregar()
  },
  computed: {
    statusIcon() {
      const icons = { DISPONIVEL: 'inbox', ACEITA: 'check', EM_ANDAMENTO: 'truck', FINALIZADA: 'package', CANCELADA: 'x' }
      return icons[this.corrida?.status] || 'info'
    },
    statusLabel() {
      const labels = {
        DISPONIVEL: 'Corrida disponível', ACEITA: 'Aceita por você',
        EM_ANDAMENTO: 'Saiu para entrega', FINALIZADA: 'Finalizada com sucesso',
        CANCELADA: 'Cancelada'
      }
      return labels[this.corrida?.status] || this.corrida?.status
    }
  },
  methods: {
    async carregar() {
      try {
        this.corrida = await entregadorService.getCorrida(this.id)
      } catch (err) {
        this.$toast.error('Erro ao carregar detalhes da corrida')
      } finally {
        this.carregando = false
      }
    },
    async initMap() {
      await this.$nextTick()
      const el = document.getElementById('rota-entregador-map')
      if (!el) return

      const entrega = this.corrida?.entrega
      if (!entrega) return

      this.carregandoMapa = true

      const origin = entrega.latitudeOrigem && entrega.longitudeOrigem
        ? { lat: entrega.latitudeOrigem, lng: entrega.longitudeOrigem }
        : await routingService.geocodeAddress(entrega.enderecoOrigem || 'Farmácia')

      const dest = entrega.latitudeDestino && entrega.longitudeDestino
        ? { lat: entrega.latitudeDestino, lng: entrega.longitudeDestino }
        : await routingService.geocodeAddress(entrega.enderecoDestino)

      if (!origin || !dest) {
        this.carregandoMapa = false
        return
      }

      this.originCoords = origin
      this.destCoords = dest

      const center = { lat: (origin.lat + dest.lat) / 2, lng: (origin.lng + dest.lng) / 2 }
      this.map = L.map('rota-entregador-map', { zoomControl: true }).setView([center.lat, center.lng], 13)

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors',
        maxZoom: 18
      }).addTo(this.map)

      this.originMarker = L.marker([origin.lat, origin.lng], {
        icon: L.divIcon({
          html: '<div class="rota-marker rota-marker-orig"><svg viewBox="0 0 24 24" fill="#2563EB" stroke="white" stroke-width="1.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3" fill="white"/></svg></div>',
          className: 'rota-marker-wrap', iconSize: [32, 32], iconAnchor: [16, 32]
        })
      }).addTo(this.map).bindPopup('<b>Origem</b><br>' + (entrega.enderecoOrigem || 'Farmácia'))

      this.destinationMarker = L.marker([dest.lat, dest.lng], {
        icon: L.divIcon({
          html: '<div class="rota-marker rota-marker-dest"><svg viewBox="0 0 24 24" fill="#EA580C" stroke="white" stroke-width="1.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3" fill="white"/></svg></div>',
          className: 'rota-marker-wrap', iconSize: [32, 32], iconAnchor: [16, 32]
        })
      }).addTo(this.map).bindPopup('<b>Destino</b><br>' + entrega.enderecoDestino)

      await this.carregarRotaOSRM(origin, dest)
      this.fitMap()
      this.carregandoMapa = false
    },
    async carregarRotaOSRM(origin, dest) {
      const route = await routingService.getRoute(origin, dest)
      if (!route) return
      const latlngs = route.coordinates.map(c => [c.lat, c.lng])
      this.routeLine = L.polyline(latlngs, { color: '#EA580C', weight: 5, opacity: 0.85 }).addTo(this.map)
    },
    fitMap() {
      if (!this.map || !this.originMarker || !this.destinationMarker) return
      const bounds = L.latLngBounds([
        this.originMarker.getLatLng(),
        this.destinationMarker.getLatLng()
      ])
      this.map.fitBounds(bounds, { padding: [60, 60], maxZoom: 15, animate: true })
    },
    async aceitar() {
      const entregador = entregadorService.getEntregador()
      if (!entregador) return
      try {
        this.corrida = await entregadorService.aceitarCorrida(this.id, entregador.id)
        this.$toast.success('Corrida aceita!')
      } catch (err) { this.$toast.error(err.response?.data || 'Erro ao aceitar') }
    },
    async recusar() {
      try {
        await entregadorService.cancelarCorrida(this.id)
        this.$toast.success('Corrida recusada')
        this.$router.push('/entregador/corridas')
      } catch (err) { this.$toast.error(err.response?.data || 'Erro ao recusar') }
    },
    async iniciar() {
      try {
        this.corrida = await entregadorService.iniciarCorrida(this.id)
        this.$toast.success('Pedido retirado! Vá para a entrega.')
      } catch (err) { this.$toast.error(err.response?.data || 'Erro ao iniciar') }
    },
    async finalizar() {
      try {
        this.corrida = await entregadorService.finalizarCorrida(this.id)
        this.mostrarAvaliacao = true
        this.$toast.success('Entrega finalizada com sucesso!')
      } catch (err) { this.$toast.error(err.response?.data || 'Erro ao finalizar') }
    },
    onAvaliado() {
      this.mostrarAvaliacao = false
      this.$toast.success('Avaliação enviada!')
    },
  },
  mounted() {
    if (!this.carregando && this.corrida) this.initMap()
  },
  watch: {
    corrida(val) {
      if (val && !this.map) this.initMap()
    }
  },
  beforeUnmount() {
    if (this.map) { this.map.remove(); this.map = null }
  }
}
</script>

<style scoped>
.detalhe-page { padding-top: 4px; }
.btn-voltar {
  background: none;
  border: none;
  color: var(--cf-ora-500);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  padding: 4px 0;
  margin-bottom: 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: gap var(--cf-transition);
  font-family: var(--cf-font);
}
.btn-voltar:hover { gap: 8px; }
.btn-voltar:focus-visible {
  outline: 2px solid var(--cf-ora-500);
  outline-offset: 2px;
  border-radius: 4px;
}

/* ─── Skeleton ─── */
.skeleton-detalhe { display: flex; flex-direction: column; gap: 12px; }
.skeleton-block {
  background: linear-gradient(90deg, var(--cf-ntr-100) 25%, var(--cf-ntr-200) 50%, var(--cf-ntr-100) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--cf-radius-lg);
}
.skeleton-block.h-60 { height: 60px; }
.skeleton-block.h-40 { height: 40px; }
.skeleton-block.h-80 { height: 80px; }
.skeleton-row { display: flex; gap: 12px; }
.skeleton-row .skeleton-block { flex: 1; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.status-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  border-radius: 14px;
  margin-bottom: 16px;
  color: white;
  font-weight: 600;
}
.status-bar.disponivel { background: var(--cf-ora-600); }
.status-bar.aceita { background: var(--cf-grn-600); }
.status-bar.em_andamento { background: var(--cf-blu-600); }
.status-bar.finalizada { background: var(--cf-grn-600); }
.status-bar.cancelada { background: var(--cf-red-600); }
.status-icon { font-size: 24px; }
.status-text { font-size: 15px; }
.info-section {
  background: var(--cf-surface);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 12px;
}
.info-section h3 { margin: 0 0 12px; font-size: 16px; color: var(--cf-ntr-800); }
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  gap: 8px;
}
.label {
  color: var(--cf-ntr-400);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.value {
  color: var(--cf-ntr-700);
  font-size: 13px;
  font-weight: 500;
}
.value.surge { color: var(--cf-amb-600); font-weight: 700; }
.map-section { margin-bottom: 12px; }
.endereco-box {
  background: var(--cf-surface);
  border-radius: var(--cf-radius-lg);
  padding: 12px 16px;
  margin-bottom: 8px;
}
.end-label {
  font-size: 12px;
  color: var(--cf-ntr-400);
  margin: 0 0 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.end-value {
  font-size: 14px;
  color: var(--cf-ntr-700);
  margin: 0;
  font-weight: 500;
}
.map-container {
  position: relative;
  border-radius: var(--cf-radius-lg);
  overflow: hidden;
  margin-top: 8px;
  min-height: 220px;
  background: var(--cf-ntr-100);
  transition: opacity 0.3s;
}
.map-container.loading { opacity: 0.7; }
.map-el {
  height: 220px;
  width: 100%;
  z-index: 1;
}
.map-loading {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: rgba(255,255,255,0.85);
  z-index: 2;
  font-size: 13px;
  color: var(--cf-ntr-500);
}
.map-loading-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid var(--cf-ntr-200);
  border-top-color: var(--cf-ora-500);
  border-radius: 50%;
  animation: mapa-spin 0.7s linear infinite;
}
@keyframes mapa-spin { to { transform: rotate(360deg); } }

/* Leaflet marker overrides */
:deep(.rota-marker-wrap) { background: none !important; border: none !important; }
:deep(.rota-marker) { display: block; filter: drop-shadow(0 2px 6px rgba(0,0,0,0.3)); }
:deep(.rota-marker svg) { display: block; }
.valores-section {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.valor-item {
  flex: 1;
  background: var(--cf-surface);
  border-radius: 14px;
  padding: 14px;
  text-align: center;
}
.valor-label {
  font-size: 12px;
  color: var(--cf-ntr-400);
  display: block;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.valor-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--cf-grn-600);
}
.valor-value.surge { color: var(--cf-amb-600); }
.timeline {
  background: var(--cf-surface);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 16px;
}
.tl-item {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  position: relative;
}
.tl-item::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 28px;
  width: 2px;
  height: calc(100% - 28px);
  background: var(--cf-ntr-200);
}
.tl-item:last-child::before { display: none; }
.tl-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--cf-ntr-200);
  flex-shrink: 0;
  margin-top: 2px;
  transition: background var(--cf-transition);
}
.tl-item.done .tl-dot {
  background: var(--cf-grn-600);
  box-shadow: 0 0 0 3px var(--cf-grn-50);
}
.tl-title { font-size: 13px; color: var(--cf-ntr-500); margin: 0; }
.tl-time { font-size: 11px; color: var(--cf-ntr-400); margin: 2px 0 0; }
.acoes { display: flex; flex-direction: column; gap: 8px; }
.btn-aceitar, .btn-finalizar {
  padding: 14px;
  border: none;
  border-radius: var(--cf-radius-lg);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  background: var(--cf-grn-600);
  color: white;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background var(--cf-transition), transform var(--cf-transition);
  font-family: var(--cf-font);
}
.btn-aceitar:hover, .btn-finalizar:hover {
  background: var(--cf-grn-700);
  transform: translateY(-1px);
}
.btn-aceitar:focus-visible, .btn-finalizar:focus-visible {
  outline: 2px solid var(--cf-grn-500);
  outline-offset: 2px;
}
.btn-recusar {
  padding: 12px;
  border: 2px solid var(--cf-red-500);
  border-radius: var(--cf-radius-lg);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  background: white;
  color: var(--cf-red-600);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all var(--cf-transition);
  font-family: var(--cf-font);
}
.btn-recusar:hover {
  background: var(--cf-red-50);
  border-color: var(--cf-red-600);
}
.btn-recusar:focus-visible {
  outline: 2px solid var(--cf-red-500);
  outline-offset: 2px;
}
.btn-acao {
  padding: 14px;
  border: none;
  border-radius: var(--cf-radius-lg);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  background: var(--cf-ora-600);
  color: white;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background var(--cf-transition), transform var(--cf-transition);
  font-family: var(--cf-font);
}
.btn-acao:hover {
  background: var(--cf-ora-700);
  transform: translateY(-1px);
}
.btn-acao:focus-visible {
  outline: 2px solid var(--cf-ora-500);
  outline-offset: 2px;
}
a.nota-fiscal {
  text-decoration: none;
  text-align: center;
}
</style>
