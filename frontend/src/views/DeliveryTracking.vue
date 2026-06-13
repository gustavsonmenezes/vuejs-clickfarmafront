<template>
  <div class="tracking-page">
    <div id="map" class="tracking-map"></div>

    <transition name="panel">
      <div v-if="rastreio" class="tracking-panel">
        <div class="panel-drag" />

        <div class="panel-top">
          <div class="status-tag" :class="statusClass">
            <span class="status-bullet" />
            {{ statusLabel }}
          </div>
          <span class="order-ref">#{{ rastreio.codigoPedido || codigo }}</span>
        </div>

        <div class="timeline">
          <div class="tl-track" :style="{ height: timelinePct + '%' }" />
          <div class="tl-step" :class="{ done: progressPercent >= 0 }">
            <div class="tl-dot"><svg v-if="progressPercent >= 0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg></div>
            <div class="tl-info"><span class="tl-label">Coleta</span><span class="tl-desc">Pedido coletado na farmácia</span></div>
          </div>
          <div class="tl-step" :class="{ done: progressPercent >= 35 }">
            <div class="tl-dot"><svg v-if="progressPercent >= 35" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg></div>
            <div class="tl-info"><span class="tl-label">Em trânsito</span><span class="tl-desc">Saiu para entrega</span></div>
          </div>
          <div class="tl-step" :class="{ done: progressPercent >= 70 }">
            <div class="tl-dot"><svg v-if="progressPercent >= 70" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg></div>
            <div class="tl-info"><span class="tl-label">Próximo da entrega</span><span class="tl-desc">Entregador está chegando</span></div>
          </div>
          <div class="tl-step" :class="{ done: progressPercent >= 100 }">
            <div class="tl-dot"><svg v-if="progressPercent >= 100" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg></div>
            <div class="tl-info"><span class="tl-label">Entregue</span><span class="tl-desc">Pedido finalizado</span></div>
          </div>
        </div>

        <div class="panel-details">
          <div class="detail-row">
            <svg class="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            <div><span class="detail-label">Última localização</span><span class="detail-value">{{ rastreio.ultimaLocalizacao || 'Saindo do centro de distribuição' }}</span></div>
          </div>
          <div class="detail-row">
            <svg class="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M5 17h14M5 17a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2M5 17l-2 4M19 17l2 4"/><circle cx="12" cy="8" r="1.5" fill="currentColor"/></svg>
            <div><span class="detail-label">Transportadora</span><span class="detail-value">{{ rastreio.transportadora || 'ClickFarma Express' }}</span></div>
          </div>
          <div class="detail-row" v-if="rastreio.dataPrevisaoEntrega">
            <svg class="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <div><span class="detail-label">Previsão de entrega</span><span class="detail-value">{{ formatDate(rastreio.dataPrevisaoEntrega) }}</span></div>
          </div>
          <div class="detail-row" v-if="rastreio.enderecoEntrega">
            <svg class="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            <div><span class="detail-label">Endereço</span><span class="detail-value">{{ rastreio.enderecoEntrega }}</span></div>
          </div>
        </div>

        <div class="panel-driver" v-if="progressPercent >= 35 && progressPercent < 100">
          <div class="driver-mini">
            <div class="driver-mini-avatar">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="8" r="4"/>
                <path d="M4 21v-2a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v2"/>
              </svg>
            </div>
            <div class="driver-mini-info">
              <span class="driver-mini-name">{{ rastreio.entregadorNome || 'Entregador' }}</span>
              <span class="driver-mini-label">Seu motorista</span>
            </div>
            <button class="driver-mini-call" @click="ligarEntregador" v-if="rastreio.entregadorTelefone">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.362 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.338 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="live-row" :class="{ on: liveConnected }">
          <span class="live-dot" />
          <span>{{ liveConnected ? 'AO VIVO' : 'RECONECTANDO...' }}</span>
        </div>
      </div>
    </transition>

    <transition name="fade">
      <div v-if="!rastreio && !loading" class="search-layer">
        <div class="search-card">
          <div class="search-brand">
            <svg class="search-logo" viewBox="0 0 40 40" fill="none">
              <rect width="40" height="40" rx="12" fill="#2A5C45"/>
              <path d="M20 8C16 8 14 11 14 14v4h-2v14h16V18h-2v-4c0-3-2-6-6-6zm4 10h-8v-4c0-2 1.5-4 4-4s4 2 4 4v4z" fill="white"/>
            </svg>
            <div><h1 class="search-title">Rastrear Pedido</h1><p class="search-sub">Acompanhe sua entrega em tempo real</p></div>
          </div>
          <div class="search-field">
            <svg class="search-mag" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="11" cy="11" r="7"/><path d="m16.5 16.5 4 4"/></svg>
            <input v-model="searchCode" class="search-input" placeholder="Código do pedido" @keyup.enter="loadTracking" />
            <button class="search-go" @click="loadTracking" :disabled="!searchCode.trim()">Rastrear</button>
          </div>
          <div v-if="error" class="search-error"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>{{ error }}</div>
        </div>
      </div>
    </transition>

    <transition name="fade">
      <div v-if="loading" class="loading-layer">
        <div class="loading-spinner"><div class="spinner-ring" /><span class="loading-text">Buscando pedido...</span></div>
      </div>
    </transition>

    <button v-if="rastreio" class="btn-back" @click="voltar">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
    </button>
  </div>
</template>

<script>
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

export default {
  name: 'DeliveryTracking',
  props: {
    codigoPedido: { type: String, default: null }
  },
  data() {
    return {
      searchCode: '',
      codigo: '',
      loading: false,
      error: null,
      rastreio: null,
      pedidoId: null,
      liveConnected: false,
      eventSource: null,
      map: null,
      marker: null,
      destinationMarker: null,
      routeLine: null,
      ghostLine: null,
      prevLat: null,
      prevLng: null,
      animFrame: null
    }
  },
  computed: {
    statusLabel() {
      const m = { 'AGUARDANDO_ENVIO': 'Aguardando envio', 'EM_TRANSITO': 'Em trânsito', 'PROXIMO_DA_ENTREGA': 'Próximo da entrega', 'ENTREGUE': 'Entregue', 'CANCELADO': 'Cancelado' }
      return m[this.rastreio?.status] || this.rastreio?.status || 'Processando'
    },
    statusClass() {
      const m = { 'AGUARDANDO_ENVIO': 'tag-waiting', 'EM_TRANSITO': 'tag-transit', 'PROXIMO_DA_ENTREGA': 'tag-near', 'ENTREGUE': 'tag-delivered' }
      return m[this.rastreio?.status] || 'tag-waiting'
    },
    progressPercent() {
      if (!this.rastreio) return 0
      const m = { 'AGUARDANDO_ENVIO': 10, 'EM_TRANSITO': 40, 'PROXIMO_DA_ENTREGA': 75, 'ENTREGUE': 100 }
      return m[this.rastreio.status] || 10
    },
    timelinePct() {
      if (this.progressPercent >= 100) return 100
      const step = Math.floor(this.progressPercent / 33) * 33
      return Math.min(step + 10, 97)
    }
  },
  methods: {
    ligarEntregador() {
      if (this.rastreio?.entregadorTelefone) {
        window.open(`tel:${this.rastreio.entregadorTelefone}`, '_self')
      }
    },
    voltar() {
      this.rastreio = null; this.desconectarSSE()
      if (this.animFrame) cancelAnimationFrame(this.animFrame)
      if (this.map) { this.map.remove(); this.map = null }
    },
    formatDate(d) {
      if (!d) return ''
      try { return new Date(d).toLocaleString('pt-BR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) }
      catch { return d }
    },
    async loadTracking() {
      const code = (this.searchCode || this.codigo).trim()
      if (!code) return
      this.codigo = code; this.loading = true; this.error = null
      this.desconectarSSE()
      try {
        const r = await fetch(`/api/pedidos/${encodeURIComponent(code)}/rastreio`)
        if (!r.ok) throw new Error('Pedido não encontrado')
        this.rastreio = await r.json()
        const pr = await fetch(`/api/pedidos/codigo/${encodeURIComponent(code)}`)
        if (pr.ok) { const p = await pr.json(); this.pedidoId = p.id; this.conectarSSE(p.id) }
        await this.$nextTick(); this.initMap()
      } catch (e) { this.error = e.message; this.rastreio = null }
      finally { this.loading = false }
    },
    conectarSSE(id) {
      this.desconectarSSE()
      if (!id) return
      const es = new EventSource(`/api/rastreios/pedido/${id}/stream`)
      this.eventSource = es
      es.onopen = () => { this.liveConnected = true }
      es.addEventListener('rastreio', (e) => {
        try { const d = JSON.parse(e.data); this.rastreio = d; this.animarMarcador(d.latitude, d.longitude) }
        catch { }
      })
      es.onerror = () => { this.liveConnected = false }
    },
    desconectarSSE() {
      if (this.eventSource) this.eventSource.close()
      this.eventSource = null; this.liveConnected = false
    },
    initMap() {
      if (this.map) return
      const dest = this.extrairDestino()
      const start = { lat: this.rastreio?.latitude || -8.047562, lng: this.rastreio?.longitude || -34.877003 }

      this.map = L.map('map', { zoomControl: false, attributionControl: false, zoomSnap: 0.5 }).setView([start.lat, start.lng], 13)
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(this.map)

      this.destinationMarker = L.marker([dest.lat, dest.lng], {
        icon: L.divIcon({
          html: `<div class="pin-dest"><svg viewBox="0 0 24 24" fill="#2A5C45" stroke="white" stroke-width="1.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3" fill="white"/></svg></div>`,
          className: 'pin-wrap', iconSize: [32, 32], iconAnchor: [16, 32]
        })
      }).addTo(this.map)

      this.marker = L.marker([start.lat, start.lng], {
        icon: L.divIcon({
          html: `<div class="pulse-ring"><div class="pulse-core"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="3"/><path d="M19 12a7 7 0 1 1-14 0 7 7 0 0 1 14 0z"/></svg></div></div>`,
          className: 'pin-wrap', iconSize: [44, 44], iconAnchor: [22, 22]
        })
      }).addTo(this.map)

      this.atualizarRota(); this.ajustarZoom()
    },
    animarMarcador(lat, lng) {
      if (!this.marker || !lat || !lng) return
      if (this.animFrame) cancelAnimationFrame(this.animFrame)
      const slat = this.marker.getLatLng().lat
      const slng = this.marker.getLatLng().lng
      const dur = 800; const t0 = performance.now()
      const fn = (t) => {
        const p = Math.min((t - t0) / dur, 1)
        const e = 1 - Math.pow(1 - p, 3)
        this.marker.setLatLng([slat + (lat - slat) * e, slng + (lng - slng) * e])
        if (p < 1) this.animFrame = requestAnimationFrame(fn)
        else { this.atualizarRota(); this.ajustarZoom() }
      }
      this.animFrame = requestAnimationFrame(fn)
      this.prevLat = lat; this.prevLng = lng
    },
    atualizarRota() {
      if (!this.map || !this.marker || !this.destinationMarker) return
      if (this.routeLine) this.map.removeLayer(this.routeLine)
      if (this.ghostLine) this.map.removeLayer(this.ghostLine)
      const dp = this.destinationMarker.getLatLng()
      const dv = this.marker.getLatLng()
      const mid = (a, b, t) => a + (b - a) * t
      const ghost = [dv, ...Array.from({ length: 20 }, (_, i) => {
        const t = (i + 1) / 21
        return L.latLng(mid(dv.lat, dp.lat, t) + (Math.random() - 0.5) * 0.0015, mid(dv.lng, dp.lng, t) + (Math.random() - 0.5) * 0.0015)
      }), dp]
      this.ghostLine = L.polyline(ghost, { color: '#B0AFA9', weight: 2, opacity: 0.25, dashArray: '6 8' }).addTo(this.map)
      this.routeLine = L.polyline([dv, ghost[1]], { color: '#2A5C45', weight: 4, opacity: 0.85 }).addTo(this.map)
    },
    ajustarZoom() {
      if (!this.map || !this.marker || !this.destinationMarker) return
      this.map.fitBounds(L.latLngBounds([this.marker.getLatLng(), this.destinationMarker.getLatLng()]), { padding: [60, 60], maxZoom: 16, animate: true })
    },
    extrairDestino() {
      if (this.rastreio?.latitude && this.rastreio?.longitude) {
        const e = (this.rastreio.enderecoEntrega || '').toUpperCase()
        if (e.includes('PALMARES')) return { lat: -8.6845, lng: -35.5898 }
        return { lat: -8.061373, lng: -34.871141 }
      }
      return { lat: -8.061373, lng: -34.871141 }
    }
  },
  mounted() {
    if (this.codigoPedido) { this.searchCode = this.codigoPedido; this.codigo = this.codigoPedido; this.loadTracking() }
  },
  beforeUnmount() {
    this.desconectarSSE()
    if (this.animFrame) cancelAnimationFrame(this.animFrame)
    if (this.map) this.map.remove()
  }
}
</script>

<style>
/* ── RESET & BASE ── */
.tracking-page { position: relative; width: 100%; height: 100vh; overflow: hidden; font-family: var(--cf-sans, 'DM Sans', system-ui, sans-serif); background: var(--cf-green-dark, #1C3D2E); }
.tracking-map { width: 100%; height: 100%; }
.pin-wrap { background: none !important; border: none !important; }

/* ── MAP MARKERS ── */
.pin-dest { filter: drop-shadow(0 2px 8px rgba(42,92,69,0.4)); }
.pin-dest svg { display: block; }
.pulse-ring { width: 44px; height: 44px; position: relative; display: flex; align-items: center; justify-content: center; }
.pulse-core { width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, var(--cf-green, #2A5C45), var(--cf-green-mid, #3D7A5E)); display: flex; align-items: center; justify-content: center; color: white; box-shadow: 0 0 0 4px rgba(42,92,69,0.25); animation: pulse-marker 2s ease-in-out infinite; }
.pulse-core svg { width: 20px; height: 20px; }
@keyframes pulse-marker { 0%, 100% { box-shadow: 0 0 0 4px rgba(42,92,69,0.25); } 50% { box-shadow: 0 0 0 12px rgba(42,92,69,0.08); } }

/* ── BACK BUTTON ── */
.btn-back { position: absolute; top: 16px; left: 16px; z-index: 1001; width: 40px; height: 40px; border-radius: 12px; border: none; background: rgba(255,255,255,0.92); backdrop-filter: blur(8px); display: flex; align-items: center; justify-content: center; cursor: pointer; color: var(--cf-text-dark, #1C1C1A); box-shadow: var(--cf-shadow-sm, 0 2px 8px rgba(0,0,0,0.07)); transition: all 0.2s; }
.btn-back:hover { background: white; transform: scale(1.05); }
.btn-back svg { width: 20px; height: 20px; }

/* ── TRACKING PANEL ── */
.tracking-panel { position: absolute; bottom: 0; left: 0; right: 0; background: rgba(255,255,255,0.96); backdrop-filter: blur(24px); -webkit-backdrop-filter: blur(24px); border-radius: 28px 28px 0 0; padding: 8px 24px 24px; max-height: 62vh; overflow-y: auto; z-index: 1000; box-shadow: 0 -8px 40px rgba(0,0,0,0.12); }
.panel-drag { width: 36px; height: 4px; border-radius: 2px; background: var(--cf-text-faint, #B0AFA9); margin: 0 auto 16px; }

.panel-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.status-tag { display: inline-flex; align-items: center; gap: 6px; padding: 6px 14px; border-radius: 20px; font-size: 0.8rem; font-weight: 600; }
.status-bullet { width: 8px; height: 8px; border-radius: 50%; }
.tag-waiting { background: var(--cf-gold-light, #F8F0DC); color: #7A6530; }
.tag-waiting .status-bullet { background: var(--cf-gold, #B89550); animation: pulse-dot 1.5s infinite; }
.tag-transit { background: #E8EDF2; color: var(--color-info, #2A4C6E); }
.tag-transit .status-bullet { background: var(--color-info, #2A4C6E); animation: pulse-dot 1.5s infinite; }
.tag-near { background: #FDF0E0; color: #7A5520; }
.tag-near .status-bullet { background: var(--cf-gold, #B89550); animation: pulse-dot 0.8s infinite; }
.tag-delivered { background: var(--cf-green-light, #E8F2EC); color: var(--cf-green-dark, #1C3D2E); }
.tag-delivered .status-bullet { background: var(--cf-green, #2A5C45); }

.order-ref { font-size: 0.8rem; font-weight: 700; color: var(--cf-text-muted, #868680); letter-spacing: 0.03em; }

/* ── TIMELINE ── */
.timeline { position: relative; padding-left: 28px; margin-bottom: 20px; }
.tl-track { position: absolute; left: 11px; top: 4px; width: 2px; background: linear-gradient(180deg, var(--cf-green, #2A5C45), var(--cf-green-mid, #3D7A5E)); border-radius: 1px; transition: height 0.8s cubic-bezier(0.16,1,0.3,1); z-index: 1; }
.tl-step { position: relative; display: flex; align-items: flex-start; gap: 12px; padding-bottom: 20px; }
.tl-step:last-child { padding-bottom: 0; }
.tl-dot { position: relative; z-index: 2; width: 24px; height: 24px; border-radius: 50%; background: var(--cf-cream, #F4F1EB); flex-shrink: 0; display: flex; align-items: center; justify-content: center; transition: all 0.4s cubic-bezier(0.16,1,0.3,1); }
.tl-dot svg { width: 14px; height: 14px; color: white; }
.tl-step.done .tl-dot { background: var(--cf-green, #2A5C45); box-shadow: 0 0 0 4px rgba(42,92,69,0.15); }
.tl-info { padding-top: 2px; }
.tl-label { display: block; font-size: 0.85rem; font-weight: 600; color: var(--cf-text-dark, #1C1C1A); }
.tl-desc { display: block; font-size: 0.72rem; color: var(--cf-text-muted, #868680); margin-top: 1px; }
.tl-step:not(.done) .tl-label { color: var(--cf-text-muted, #868680); }

/* ── DETAIL ROWS ── */
.panel-details { display: flex; flex-direction: column; gap: 14px; margin-bottom: 16px; padding-top: 8px; border-top: 1px solid var(--cf-border, rgba(28,28,26,0.10)); }
.detail-row { display: flex; gap: 12px; align-items: flex-start; }
.detail-icon { width: 20px; height: 20px; flex-shrink: 0; color: var(--cf-green, #2A5C45); margin-top: 1px; }
.detail-label { display: block; font-size: 0.68rem; color: var(--cf-text-muted, #868680); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600; }
.detail-value { display: block; font-size: 0.82rem; color: var(--cf-text-dark, #1C1C1A); font-weight: 500; line-height: 1.35; }

/* ── DRIVER MINI ── */
.panel-driver { margin: 12px 0 8px; padding: 12px 14px; background: var(--cf-green-xlight, #F0F7F3); border-radius: var(--cf-r-md, 8px); border: 1px solid rgba(42,92,69,0.12); }
.driver-mini { display: flex; align-items: center; gap: 10px; }
.driver-mini-avatar { width: 36px; height: 36px; border-radius: 50%; background: var(--cf-green, #2A5C45); display: flex; align-items: center; justify-content: center; color: white; flex-shrink: 0; }
.driver-mini-avatar svg { width: 18px; height: 18px; }
.driver-mini-info { flex: 1; }
.driver-mini-name { display: block; font-size: 0.82rem; font-weight: 600; color: var(--cf-text-dark, #1C1C1A); }
.driver-mini-label { display: block; font-size: 0.68rem; color: var(--cf-text-muted, #868680); }
.driver-mini-call { width: 36px; height: 36px; border-radius: 50%; border: none; background: var(--cf-green, #2A5C45); color: white; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s; flex-shrink: 0; }
.driver-mini-call:hover { background: var(--cf-green-dark, #1C3D2E); transform: scale(1.05); }
.driver-mini-call svg { width: 16px; height: 16px; }

/* ── LIVE BADGE ── */
.live-row { display: flex; align-items: center; justify-content: center; gap: 6px; font-size: 0.68rem; font-weight: 700; letter-spacing: 0.08em; color: var(--cf-text-faint, #B0AFA9); padding: 8px; }
.live-row.on { color: var(--cf-green, #2A5C45); }
.live-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.live-row.on .live-dot { animation: pulse-dot 1.5s infinite; }

/* ── ANIMATIONS ── */
@keyframes pulse-dot { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.6; transform: scale(1.4); } }
.panel-enter-active { transition: all 0.55s cubic-bezier(0.16,1,0.3,1); }
.panel-leave-active { transition: all 0.3s ease; }
.panel-enter-from, .panel-leave-to { transform: translateY(100%); opacity: 0; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* ── SEARCH LAYER ── */
.search-layer { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: linear-gradient(145deg, var(--cf-green-dark, #1C3D2E) 0%, var(--cf-green, #2A5C45) 100%); z-index: 500; padding: 24px; }
.search-card { background: rgba(255,255,255,0.97); border-radius: 24px; padding: 32px; max-width: 440px; width: 100%; box-shadow: 0 25px 80px rgba(0,0,0,0.35); }
.search-brand { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.search-logo { width: 48px; height: 48px; flex-shrink: 0; }
.search-title { font-size: 1.35rem; font-weight: 700; color: var(--cf-text-dark, #1C1C1A); margin: 0; font-family: var(--cf-serif, 'Cormorant Garamond', Georgia, serif); }
.search-sub { font-size: 0.82rem; color: var(--cf-text-muted, #868680); margin: 2px 0 0; }
.search-field { display: flex; align-items: center; gap: 0; background: var(--cf-ivory, #FAF9F6); border: 1.5px solid var(--cf-border-mid, rgba(28,28,26,0.16)); border-radius: 14px; padding: 0 4px 0 14px; transition: all 0.2s; }
.search-field:focus-within { border-color: var(--cf-green, #2A5C45); box-shadow: 0 0 0 3px rgba(42,92,69,0.1); }
.search-mag { width: 18px; height: 18px; color: var(--cf-text-faint, #B0AFA9); flex-shrink: 0; }
.search-input { flex: 1; border: none; background: transparent; padding: 14px 10px; font-size: 0.9rem; outline: none; color: var(--cf-text-dark, #1C1C1A); font-family: var(--cf-sans, 'DM Sans', system-ui, sans-serif); }
.search-input::placeholder { color: var(--cf-text-faint, #B0AFA9); }
.search-go { padding: 10px 20px; border: none; border-radius: 10px; background: var(--cf-green, #2A5C45); color: white; font-weight: 600; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
.search-go:hover { background: var(--cf-green-dark, #1C3D2E); }
.search-go:disabled { opacity: 0.4; cursor: not-allowed; }
.search-error { display: flex; align-items: center; gap: 8px; margin-top: 14px; color: var(--cf-danger, #8B3A3A); font-size: 0.85rem; }
.search-error svg { width: 18px; height: 18px; flex-shrink: 0; }

/* ── LOADING ── */
.loading-layer { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: rgba(28,61,46,0.85); backdrop-filter: blur(8px); z-index: 999; }
.loading-spinner { display: flex; flex-direction: column; align-items: center; gap: 16px; }
.spinner-ring { width: 44px; height: 44px; border: 3px solid rgba(255,255,255,0.15); border-top: 3px solid var(--cf-green-mid, #3D7A5E); border-radius: 50%; animation: spin 0.8s linear infinite; }
.loading-text { color: rgba(255,255,255,0.7); font-size: 0.85rem; font-weight: 500; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ── DESKTOP ── */
@media (min-width: 768px) {
  .tracking-panel { left: auto; right: 24px; top: 24px; bottom: 24px; max-height: none; width: 380px; border-radius: 24px; box-shadow: 0 8px 40px rgba(0,0,0,0.15); }
  .panel-drag { display: none; }
  .search-card { padding: 40px; }
}
</style>
