<template>
  <div class="ltm-wrap">
    <!-- Status Card -->
    <div class="ltm-card ltm-card-status">
      <div class="ltm-status-head">
        <div class="ltm-status-left">
          <svg class="ltm-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
          </svg>
          <span class="ltm-status-label">Status da Entrega</span>
        </div>
        <span class="ltm-badge" :class="statusBadgeClass">{{ deliveryStatus }}</span>
      </div>
      <div class="ltm-progress">
        <div class="ltm-progress-track">
          <div class="ltm-progress-bar" :style="progressStyle" />
        </div>
        <span class="ltm-progress-text">{{ progress }}%</span>
      </div>
    </div>

    <!-- Map Card -->
    <div class="ltm-card ltm-card-map">
      <div class="ltm-map-head">
        <svg class="ltm-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
          <circle cx="12" cy="10" r="3"/>
        </svg>
        <span class="ltm-map-title">Mapa de Rastreamento</span>
        <button class="ltm-refresh" @click="refreshLocation" :disabled="refreshing" :title="'Atualizar'">
          <svg class="ltm-icon-sm" :class="{ spin: refreshing }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.2-8.6"/>
            <path d="M21 3v6h-6"/>
          </svg>
        </button>
      </div>
      <div class="ltm-map-body">
        <div id="tracking-map" class="ltm-map-el" />
        <div class="ltm-map-overlay">
          <div class="ltm-overlay-row">
            <svg class="ltm-icon-xs" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
            <strong class="ltm-overlay-text">{{ currentLocation }}</strong>
          </div>
          <div class="ltm-overlay-meta">
            <svg class="ltm-icon-xxs" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            Atualizado {{ lastUpdateTime }}
          </div>
        </div>
      </div>
    </div>

    <!-- Driver Card -->
    <div class="ltm-card ltm-card-driver">
      <div class="ltm-driver-head">
        <svg class="ltm-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="8" r="4"/>
          <path d="M4 21v-2a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v2"/>
        </svg>
        <span class="ltm-driver-title">Entregador</span>
      </div>
      <div class="ltm-driver-body">
        <div class="ltm-driver-main">
          <div class="ltm-driver-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="8" r="4"/>
              <path d="M4 21v-2a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v2"/>
            </svg>
          </div>
          <div class="ltm-driver-info">
            <span class="ltm-driver-name">{{ driverInfo.name }}</span>
            <div class="ltm-driver-rating">
              <svg v-for="s in 5" :key="s" class="ltm-star" :class="{ full: s <= 4, half: s === 5 }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              <span class="ltm-rating-text">4.7</span>
            </div>
          </div>
        </div>
        <div class="ltm-driver-contact">
          <div class="ltm-contact-row">
            <svg class="ltm-icon-xs" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M5 17h14M5 17a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2M5 17l-2 4M19 17l2 4"/>
              <path d="M12 11v4"/>
              <circle cx="12" cy="8" r="1.5" fill="currentColor"/>
            </svg>
            <span>{{ driverInfo.vehicle }}</span>
          </div>
          <div class="ltm-contact-row">
            <svg class="ltm-icon-xs" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.362 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.338 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
            </svg>
            <span>{{ driverInfo.phone }}</span>
          </div>
        </div>
        <button class="ltm-call-btn" @click="contactDriver">
          <svg class="ltm-icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.362 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.338 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
          </svg>
          Falar com Entregador
        </button>
      </div>
    </div>

    <!-- Details Card -->
    <div class="ltm-card ltm-card-details">
      <div class="ltm-details-head">
        <svg class="ltm-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/>
          <line x1="16" y1="17" x2="8" y2="17"/>
          <polyline points="10 9 9 9 8 9"/>
        </svg>
        <span class="ltm-details-title">Detalhes da Entrega</span>
      </div>
      <div class="ltm-details-body">
        <div class="ltm-detail-item">
          <div class="ltm-detail-label">
            <svg class="ltm-icon-xs" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            Previsão
          </div>
          <span class="ltm-detail-value">{{ estimatedTime }}</span>
        </div>
        <div class="ltm-detail-item">
          <div class="ltm-detail-label">
            <svg class="ltm-icon-xs" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 2a10 10 0 0 0 0 20c4.4 0 8-3 8-7a5.5 5.5 0 0 0-5-5.5c-2 0-3.5 1.5-3.5 3.5S9 16 12 16s3-2 3-3.5"/>
            </svg>
            Distância
          </div>
          <span class="ltm-detail-value">{{ estimatedDistance }}</span>
        </div>
        <div class="ltm-detail-item">
          <div class="ltm-detail-label">
            <svg class="ltm-icon-xs" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            Tempo
          </div>
          <span class="ltm-detail-value">{{ travelTime }}</span>
        </div>
        <div class="ltm-detail-item">
          <div class="ltm-detail-label">
            <svg class="ltm-icon-xs" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 2v4M12 18v4M2 12h4M18 12h4"/>
              <circle cx="12" cy="12" r="6"/>
            </svg>
            Velocidade
          </div>
          <span class="ltm-detail-value">{{ averageSpeed }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions, mapGetters } from 'vuex'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { routingService } from '@/services/routingService'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
})

export default {
  name: 'LiveTrackingMap',
  props: {
    orderId: { type: String, required: true },
    destinoEndereco: { type: String, default: '' },
    destinoCoords: { type: Object, default: null }
  },
  data() {
    return {
      refreshing: false,
      map: null,
      driverMarker: null,
      destinationMarker: null,
      routeLine: null,
      mapInterval: null,
      rotaInfo: null,
      destCoords: null
    }
  },
  computed: {
    ...mapGetters(['getOrderTracking']),
    trackingInfo() {
      return this.getOrderTracking(this.orderId)
    },
    currentLocation() {
      if (!this.trackingInfo) return 'Centro de Distribuição ClickFarma - Recife'
      return this.trackingInfo.currentLocation || 'Centro de Distribuição ClickFarma - Recife'
    },
    driverInfo() {
      const defaultDriver = {
        name: 'Carlos Silva',
        vehicle: 'Honda CG 160 Titan',
        phone: '(81) 99818-9999'
      }
      if (!this.trackingInfo) return defaultDriver
      return {
        name: this.trackingInfo.driver?.name || defaultDriver.name,
        vehicle: this.trackingInfo.driver?.vehicle || defaultDriver.vehicle,
        phone: this.trackingInfo.driver?.phone || this.trackingInfo.contact || defaultDriver.phone
      }
    },
    deliveryStatus() {
      if (!this.trackingInfo) return 'PROCESSANDO'
      const m = {
        'confirmed': 'CONFIRMADO',
        'processing': 'EM PREPARAÇÃO',
        'shipped': 'EM TRÂNSITO',
        'out_for_delivery': 'SAIU PARA ENTREGA',
        'delivered': 'ENTREGUE'
      }
      return m[this.trackingInfo.status] || 'PROCESSANDO'
    },
    statusBadgeClass() {
      if (!this.trackingInfo) return 'badge-processing'
      const c = {
        'confirmed': 'badge-confirmed',
        'processing': 'badge-processing',
        'shipped': 'badge-transit',
        'out_for_delivery': 'badge-near',
        'delivered': 'badge-done'
      }
      return c[this.trackingInfo.status] || 'badge-processing'
    },
    progress() {
      if (!this.trackingInfo) return 25
      const m = { 'confirmed': 25, 'processing': 45, 'shipped': 65, 'out_for_delivery': 85, 'delivered': 100 }
      return m[this.trackingInfo.status] || 25
    },
    progressStyle() {
      return { width: `${this.progress}%` }
    },
    estimatedTime() {
      if (!this.trackingInfo?.estimatedDelivery) return 'Calculando...'
      try {
        return new Date(this.trackingInfo.estimatedDelivery).toLocaleTimeString('pt-BR', {
          hour: '2-digit', minute: '2-digit'
        })
      } catch { return 'Em breve' }
    },
    estimatedDistance() {
      if (!this.trackingInfo) return 'Calculando...'
      return this.rotaInfo?.distanceKm != null ? this.rotaInfo.distanceKm + ' km' : 'Calculando...'
    },
    travelTime() {
      if (!this.trackingInfo) return '--:--'
      return this.rotaInfo?.durationMin != null ? this.rotaInfo.durationMin + ' min' : '--'
    },
    averageSpeed() {
      if (this.rotaInfo?.distanceKm && this.rotaInfo?.durationMin) {
        const kmh = Math.round(this.rotaInfo.distanceKm / (this.rotaInfo.durationMin / 60))
        return kmh + ' km/h'
      }
      return '-- km/h'
    },
    lastUpdateTime() {
      if (!this.trackingInfo?.lastUpdate) return 'agora mesmo'
      return this.formatRelativeTime(this.trackingInfo.lastUpdate)
    },
    currentCoordinates() {
      if (this.trackingInfo?.coordinates) return this.trackingInfo.coordinates
      const base = { lat: -8.6845, lng: -35.5898 }
      const off = {
        'confirmed': { lat: 0, lng: 0 }, 'processing': { lat: 0.01, lng: 0.01 },
        'shipped': { lat: 0.02, lng: 0.03 }, 'out_for_delivery': { lat: 0.03, lng: 0.05 },
        'delivered': { lat: 0.035, lng: 0.055 }
      }
      const o = off[this.trackingInfo?.status] || { lat: 0, lng: 0 }
      return { lat: base.lat + o.lat, lng: base.lng + o.lng }
    },
    destinationCoordinates() {
      return this.destCoords || this.destinoCoords || { lat: -8.6845, lng: -35.5898 }
    },

    initMap() {
      const dest = this.destinationCoordinates
      this.map = L.map('tracking-map').setView([-8.6845, -35.5898], 13)
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors', maxZoom: 18
      }).addTo(this.map)

      this.destinationMarker = L.marker(dest, {
        icon: L.divIcon({
          html: '<div class="ltm-marker-dest"><svg viewBox="0 0 24 24" fill="var(--cf-green)" stroke="white" stroke-width="1.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3" fill="white"/></svg></div>',
          className: 'ltm-marker-wrap', iconSize: [32, 32], iconAnchor: [16, 32]
        })
      }).addTo(this.map).bindPopup('📍 Destino da Entrega')

      this.updateDriverMarker()
      this.updateRoute()
      this.fitMap()
    },
    updateDriverMarker() {
      if (this.driverMarker) this.map.removeLayer(this.driverMarker)
      this.driverMarker = L.marker(this.currentCoordinates, {
        icon: L.divIcon({
          html: `<div class="ltm-pulse"><div class="ltm-pulse-core"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="3"/><path d="M19 12a7 7 0 1 1-14 0 7 7 0 0 1 14 0z"/></svg></div></div>`,
          className: 'ltm-marker-wrap', iconSize: [44, 44], iconAnchor: [22, 22]
        })
      }).addTo(this.map).bindPopup(`🚚 ${this.driverInfo.name}<br>${this.currentLocation}`)
    },
    async carregarRotaOSRM() {
      const dest = this.destinationCoordinates
      const src = this.currentCoordinates
      const route = await routingService.getRoute(src, dest)
      if (!route) return
      this.rotaInfo = route
      if (this.routeLine) this.map.removeLayer(this.routeLine)
      const latlngs = route.coordinates.map(c => [c.lat, c.lng])
      this.routeLine = L.polyline(latlngs, { color: '#2A5C45', weight: 5, opacity: 0.85 }).addTo(this.map)
    },
    updateRoute() {
      if (this.routeLine) this.map.removeLayer(this.routeLine)
      this.carregarRotaOSRM()
    },
    fitMap() {
      if (!this.map || !this.driverMarker) return
      const dest = this.destinationMarker?.getLatLng() || this.destinationCoordinates
      this.map.fitBounds(L.latLngBounds([
        this.driverMarker.getLatLng(), dest
      ]), { padding: [50, 50], maxZoom: 15, animate: true })
    },
    updateMap() {
      if (!this.map) return
      this.updateDriverMarker()
      this.updateRoute()
      this.fitMap()
    },
    startAutoRefresh() {
      this.mapInterval = setInterval(() => {
        if (this.trackingInfo && this.trackingInfo.status !== 'delivered') this.refreshLocation()
      }, 30000)
    },
    stopAutoRefresh() {
      if (this.mapInterval) { clearInterval(this.mapInterval); this.mapInterval = null }
    }
  },
  watch: {
    trackingInfo: { handler() { this.updateMap() }, deep: true }
  },
  async mounted() {
    this.$nextTick(() => this.initMap())
    if (this.destinoEndereco && !this.destinoCoords && !this.destCoords) {
      const c = await routingService.geocodeAddress(this.destinoEndereco)
      if (c) this.destCoords = c
    }
    if (!this.trackingInfo) await this.refreshLocation()
    else this.updateMap()
    this.startAutoRefresh()
  },
  beforeUnmount() {
    this.stopAutoRefresh()
    if (this.map) { this.map.remove(); this.map = null }
  }
}
</script>

<style scoped>
/* ── CONTAINER ── */
.ltm-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-family: var(--cf-sans, 'DM Sans', system-ui, sans-serif);
}

/* ── CARDS ── */
.ltm-card {
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--cf-border, rgba(28,28,26,0.10));
  border-radius: var(--cf-r-lg, 12px);
  box-shadow: var(--cf-shadow-sm, 0 2px 8px rgba(0,0,0,0.07));
  overflow: hidden;
  transition: box-shadow 0.25s var(--cf-ease, ease);
}
.ltm-card:hover {
  box-shadow: var(--cf-shadow-md, 0 6px 24px rgba(0,0,0,0.09));
}

/* ── ICONS ── */
.ltm-icon { width: 20px; height: 20px; color: var(--cf-green, #2A5C45); flex-shrink: 0; }
.ltm-icon-sm { width: 16px; height: 16px; flex-shrink: 0; }
.ltm-icon-xs { width: 15px; height: 15px; color: var(--cf-green, #2A5C45); flex-shrink: 0; }
.ltm-icon-xxs { width: 12px; height: 12px; margin-right: 4px; }

/* ── STATUS CARD ── */
.ltm-card-status {
  background: linear-gradient(135deg, var(--cf-green-dark, #1C3D2E) 0%, var(--cf-green, #2A5C45) 100%);
  border: none;
  box-shadow: 0 4px 20px rgba(42,92,69,0.25);
}
.ltm-status-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 12px;
}
.ltm-status-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.ltm-status-left .ltm-icon { color: rgba(255,255,255,0.8); }
.ltm-status-label {
  font-size: 0.82rem;
  font-weight: 600;
  color: rgba(255,255,255,0.9);
  letter-spacing: 0.02em;
}
.ltm-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}
.badge-confirmed { background: rgba(255,255,255,0.2); color: white; }
.badge-processing { background: rgba(255,255,255,0.2); color: white; }
.badge-transit { background: var(--cf-gold-light, #F8F0DC); color: var(--cf-gold, #B89550); }
.badge-near { background: var(--cf-gold-light, #F8F0DC); color: var(--cf-gold, #B89550); }
.badge-done { background: rgba(255,255,255,0.25); color: white; }

.ltm-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px 16px;
}
.ltm-progress-track {
  flex: 1;
  height: 6px;
  background: rgba(255,255,255,0.15);
  border-radius: 3px;
  overflow: hidden;
}
.ltm-progress-bar {
  height: 100%;
  background: var(--cf-gold, #B89550);
  border-radius: 3px;
  transition: width 0.6s cubic-bezier(0.16,1,0.3,1);
}
.ltm-progress-text {
  font-size: 0.75rem;
  font-weight: 700;
  color: rgba(255,255,255,0.8);
  min-width: 32px;
  text-align: right;
}

/* ── MAP CARD ── */
.ltm-map-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--cf-border, rgba(28,28,26,0.10));
}
.ltm-map-title {
  flex: 1;
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--cf-text-dark, #1C1C1A);
}
.ltm-refresh {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: var(--cf-cream, #F4F1EB);
  color: var(--cf-green, #2A5C45);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.ltm-refresh:hover { background: var(--cf-green-light, #E8F2EC); }
.ltm-refresh:disabled { opacity: 0.5; cursor: not-allowed; }

.ltm-map-body {
  position: relative;
}
.ltm-map-el {
  height: 280px;
  width: 100%;
}

.ltm-map-overlay {
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  background: rgba(255,255,255,0.94);
  backdrop-filter: blur(8px);
  padding: 10px 14px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.12);
}
.ltm-overlay-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 2px;
}
.ltm-overlay-row .ltm-icon-xs { color: var(--cf-green, #2A5C45); }
.ltm-overlay-text {
  font-size: 0.78rem;
  color: var(--cf-text-dark, #1C1C1A);
  line-height: 1.3;
}
.ltm-overlay-meta {
  font-size: 0.7rem;
  color: var(--cf-text-muted, #868680);
  display: flex;
  align-items: center;
  margin-top: 2px;
}

/* ── DRIVER CARD ── */
.ltm-driver-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--cf-border, rgba(28,28,26,0.10));
  background: var(--cf-ivory, #FAF9F6);
}
.ltm-driver-title {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--cf-text-dark, #1C1C1A);
}
.ltm-driver-body {
  padding: 16px;
}
.ltm-driver-main {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}
.ltm-driver-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--cf-green, #2A5C45), var(--cf-green-mid, #3D7A5E));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}
.ltm-driver-avatar svg { width: 26px; height: 26px; }
.ltm-driver-info { flex: 1; }
.ltm-driver-name {
  display: block;
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--cf-text-dark, #1C1C1A);
  margin-bottom: 2px;
}
.ltm-driver-rating {
  display: flex;
  align-items: center;
  gap: 2px;
}
.ltm-star {
  width: 14px;
  height: 14px;
  color: var(--cf-text-faint, #B0AFA9);
}
.ltm-star.full {
  fill: var(--cf-gold, #B89550);
  color: var(--cf-gold, #B89550);
}
.ltm-star.half {
  fill: var(--cf-gold, #B89550);
  color: var(--cf-gold, #B89550);
  opacity: 0.5;
}
.ltm-rating-text {
  margin-left: 4px;
  font-size: 0.75rem;
  color: var(--cf-text-muted, #868680);
  font-weight: 600;
}
.ltm-driver-contact {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
  padding: 12px;
  background: var(--cf-ivory, #FAF9F6);
  border-radius: var(--cf-r-md, 8px);
}
.ltm-contact-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: var(--cf-text-mid, #4A4A47);
}
.ltm-call-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: var(--cf-r-md, 8px);
  background: var(--cf-green, #2A5C45);
  color: white;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: var(--cf-sans, 'DM Sans', system-ui, sans-serif);
}
.ltm-call-btn:hover {
  background: var(--cf-green-dark, #1C3D2E);
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(42,92,69,0.25);
}
.ltm-call-btn .ltm-icon-sm { color: white; }

/* ── DETAILS CARD ── */
.ltm-details-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--cf-border, rgba(28,28,26,0.10));
  background: var(--cf-ivory, #FAF9F6);
}
.ltm-details-title {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--cf-text-dark, #1C1C1A);
}
.ltm-details-body {
  padding: 8px 16px;
  display: grid;
  gap: 0;
}
.ltm-detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--cf-border, rgba(28,28,26,0.10));
}
.ltm-detail-item:last-child { border-bottom: none; }
.ltm-detail-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.82rem;
  color: var(--cf-text-muted, #868680);
}
.ltm-detail-value {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--cf-text-dark, #1C1C1A);
}

/* ── MAP MARKERS ── */
.ltm-marker-wrap { background: none !important; border: none !important; }
.ltm-marker-dest { filter: drop-shadow(0 2px 8px rgba(42,92,69,0.4)); }
.ltm-marker-dest svg { display: block; }
.ltm-pulse {
  width: 44px; height: 44px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ltm-pulse-core {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--cf-green, #2A5C45), var(--cf-green-mid, #3D7A5E));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 0 0 4px rgba(42,92,69,0.25);
  animation: ltm-pulse 2s ease-in-out infinite;
}
.ltm-pulse-core svg { width: 18px; height: 18px; }
@keyframes ltm-pulse {
  0%, 100% { box-shadow: 0 0 0 4px rgba(42,92,69,0.25); }
  50% { box-shadow: 0 0 0 14px rgba(42,92,69,0.08); }
}

/* ── ANIMATIONS ── */
.spin { animation: ltm-spin 0.8s linear infinite; }
@keyframes ltm-spin { to { transform: rotate(360deg); } }

/* ── RESPONSIVE ── */
@media (max-width: 768px) {
  .ltm-map-el { height: 220px; }
  .ltm-card-status { border-radius: var(--cf-r-md, 8px); }
  .ltm-marker-dest { filter: drop-shadow(0 1px 4px rgba(42,92,69,0.3)); }
  .ltm-pulse-core { width: 30px; height: 30px; }
  .ltm-pulse-core svg { width: 14px; height: 14px; }
}
</style>
