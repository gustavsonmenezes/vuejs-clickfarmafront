[file name]: LiveTrackingMap.vue
[file content begin]
<template>
  <div class="live-tracking-map">
    <!-- Status Principal -->
    <div class="status-card mb-4">
      <div class="status-header">
        <h5 class="mb-2">Status da Entrega</h5>
        <span :class="statusClass" class="status-badge">{{ deliveryStatus }}</span>
      </div>
      <div class="progress-container">
        <div class="progress-bar" :style="progressStyle"></div>
      </div>
      <div class="progress-text">{{ progress }}% concluído</div>
    </div>

    <!-- Mapa Real -->
    <div class="card mb-4">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h6 class="mb-0">🗺️ Mapa de Rastreamento</h6>
        <button class="btn btn-sm btn-outline-primary" @click="refreshLocation" :disabled="refreshing">
          <i class="fas fa-sync-alt" :class="{ 'fa-spin': refreshing }"></i>
        </button>
      </div>
      <div class="card-body p-0">
        <div id="tracking-map" class="tracking-map"></div>
        <div class="map-overlay">
          <div class="location-info">
            <div class="location-address">
              <i class="fas fa-map-marker-alt text-danger me-2"></i>
              <strong>{{ currentLocation }}</strong>
            </div>
            <div class="location-update">
              <small class="text-muted">
                <i class="fas fa-clock me-1"></i>
                Atualizado {{ lastUpdateTime }}
              </small>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Informações do Entregador -->
    <div class="card mb-4">
      <div class="card-header">
        <h6 class="mb-0">👤 Entregador</h6>
      </div>
      <div class="card-body">
        <div class="driver-info">
          <div class="driver-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="driver-details">
            <div class="driver-name">{{ driverInfo.name }}</div>
            <div class="driver-rating">
              <i class="fas fa-star text-warning"></i>
              <i class="fas fa-star text-warning"></i>
              <i class="fas fa-star text-warning"></i>
              <i class="fas fa-star text-warning"></i>
              <i class="fas fa-star-half-alt text-warning"></i>
              <span class="rating-text">4.7</span>
            </div>
          </div>
        </div>
        <div class="driver-contact mt-3">
          <div class="contact-item">
            <i class="fas fa-motorcycle me-2"></i>
            {{ driverInfo.vehicle }}
          </div>
          <div class="contact-item">
            <i class="fas fa-phone me-2"></i>
            {{ driverInfo.phone }}
          </div>
        </div>
        <button class="btn btn-primary w-100 mt-3" @click="contactDriver">
          <i class="fas fa-phone me-2"></i>
          Falar com Entregador
        </button>
      </div>
    </div>

    <!-- Detalhes da Entrega -->
    <div class="card">
      <div class="card-header">
        <h6 class="mb-0">📊 Detalhes da Entrega</h6>
      </div>
      <div class="card-body">
        <div class="delivery-details">
          <div class="detail-item">
            <div class="detail-label">
              <i class="fas fa-clock me-2"></i>
              Previsão
            </div>
            <div class="detail-value">{{ estimatedTime }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">
              <i class="fas fa-road me-2"></i>
              Distância
            </div>
            <div class="detail-value">{{ estimatedDistance }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">
              <i class="fas fa-hourglass-half me-2"></i>
              Tempo
            </div>
            <div class="detail-value">{{ travelTime }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">
              <i class="fas fa-tachometer-alt me-2"></i>
              Velocidade
            </div>
            <div class="detail-value">{{ averageSpeed }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions, mapGetters } from 'vuex'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Corrigir ícones do Leaflet no Webpack
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
})

export default {
  name: 'LiveTrackingMap',
  props: {
    orderId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      refreshing: false,
      map: null,
      driverMarker: null,
      destinationMarker: null,
      routeLine: null,
      mapInterval: null
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
      
      const statusMap = {
        'confirmed': 'CONFIRMADO',
        'processing': 'EM PREPARAÇÃO',
        'shipped': 'EM TRÂNSITO',
        'out_for_delivery': 'SAIU PARA ENTREGA',
        'delivered': 'ENTREGUE'
      }
      return statusMap[this.trackingInfo.status] || 'PROCESSANDO'
    },
    
    statusClass() {
      if (!this.trackingInfo) return 'status-processing'
      
      const classes = {
        'confirmed': 'status-confirmed',
        'processing': 'status-processing',
        'shipped': 'status-shipped',
        'out_for_delivery': 'status-delivery',
        'delivered': 'status-delivered'
      }
      return classes[this.trackingInfo.status] || 'status-processing'
    },
    
    progress() {
      if (!this.trackingInfo) return 25
      
      const progressMap = {
        'confirmed': 25,
        'processing': 45,
        'shipped': 65,
        'out_for_delivery': 85,
        'delivered': 100
      }
      return progressMap[this.trackingInfo.status] || 25
    },
    
    progressStyle() {
      return {
        width: `${this.progress}%`
      }
    },
    
    estimatedTime() {
      if (!this.trackingInfo?.estimatedDelivery) return 'Calculando...'
      
      try {
        const deliveryTime = new Date(this.trackingInfo.estimatedDelivery)
        return deliveryTime.toLocaleTimeString('pt-BR', {
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (error) {
        return 'Em breve'
      }
    },
    
    estimatedDistance() {
      if (!this.trackingInfo) return 'Calculando...'
      
      const statusDistances = {
        'confirmed': '12.5 km',
        'processing': '11.2 km', 
        'shipped': '8.7 km',
        'out_for_delivery': '3.2 km',
        'delivered': '0.0 km'
      }
      
      return statusDistances[this.trackingInfo.status] || '10.0 km'
    },
    
    travelTime() {
      if (!this.trackingInfo) return '--:--'
      
      const statusTimes = {
        'confirmed': '45-60 min',
        'processing': '35-50 min', 
        'shipped': '25-40 min',
        'out_for_delivery': '10-20 min',
        'delivered': '0 min'
      }
      
      return statusTimes[this.trackingInfo.status] || '30-45 min'
    },
    
    averageSpeed() {
      return '38 km/h'
    },
    
    lastUpdateTime() {
      if (!this.trackingInfo?.lastUpdate) return 'agora mesmo'
      return this.formatRelativeTime(this.trackingInfo.lastUpdate)
    },
    
    // Coordenadas baseadas no status (simulação realista)
    currentCoordinates() {
      if (this.trackingInfo?.coordinates) {
        return this.trackingInfo.coordinates
      }
      
      // Coordenadas de Recife como fallback
      const baseCoords = { lat: -8.047562, lng: -34.877003 }
      
      // Simula movimento baseado no status
      const statusOffsets = {
        'confirmed': { lat: 0, lng: 0 },
        'processing': { lat: 0.01, lng: 0.01 },
        'shipped': { lat: 0.02, lng: 0.03 },
        'out_for_delivery': { lat: 0.03, lng: 0.05 },
        'delivered': { lat: 0.035, lng: 0.055 }
      }
      
      const offset = statusOffsets[this.trackingInfo?.status] || { lat: 0, lng: 0 }
      
      return {
        lat: baseCoords.lat + offset.lat,
        lng: baseCoords.lng + offset.lng
      }
    },
    
    destinationCoordinates() {
      // Destino fixo em Recife (pode ser personalizado)
      return { lat: -8.061373, lng: -34.871141 }
    }
  },
  methods: {
    ...mapActions(['fetchRealTimeTracking']),
    
    async refreshLocation() {
      this.refreshing = true
      try {
        await this.fetchRealTimeTracking(this.orderId)
        this.updateMap()
      } catch (error) {
        console.error('Erro ao atualizar localização:', error)
      } finally {
        this.refreshing = false
      }
    },
    
    contactDriver() {
      const phone = this.driverInfo.phone
      if (phone) {
        window.open(`tel:${phone}`, '_self')
      }
    },
    
    formatRelativeTime(timestamp) {
      if (!timestamp) return 'agora mesmo'
      
      try {
        const date = new Date(timestamp)
        const now = new Date()
        const diffMs = now - date
        const diffMins = Math.round(diffMs / 60000)
        
        if (diffMins < 1) return 'agora mesmo'
        if (diffMins === 1) return 'há 1 minuto'
        if (diffMins < 60) return `há ${diffMins} minutos`
        if (diffMins < 120) return 'há 1 hora'
        return `há ${Math.round(diffMins / 60)} horas`
      } catch (error) {
        return 'recentemente'
      }
    },
    
    initMap() {
      // Inicializa o mapa
      this.map = L.map('tracking-map').setView([-8.047562, -34.877003], 13)
      
      // Adiciona tile layer do OpenStreetMap
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 18
      }).addTo(this.map)
      
      // Ícone personalizado para o entregador
      const driverIcon = L.divIcon({
        html: '<div class="driver-marker"><i class="fas fa-motorcycle"></i></div>',
        className: 'driver-icon',
        iconSize: [30, 30],
        iconAnchor: [15, 15]
      })
      
      // Ícone para o destino
      const destinationIcon = L.divIcon({
        html: '<div class="destination-marker"><i class="fas fa-home"></i></div>',
        className: 'destination-icon',
        iconSize: [25, 25],
        iconAnchor: [12, 12]
      })
      
      // Adiciona marcador do destino
      this.destinationMarker = L.marker(this.destinationCoordinates, { icon: destinationIcon })
        .addTo(this.map)
        .bindPopup('📍 Destino da Entrega')
        .openPopup()
      
      // Adiciona marcador do entregador
      this.updateDriverMarker()
      
      // Adiciona linha da rota
      this.updateRoute()
    },
    
    updateDriverMarker() {
      const driverIcon = L.divIcon({
        html: '<div class="driver-marker"><i class="fas fa-motorcycle"></i></div>',
        className: 'driver-icon',
        iconSize: [30, 30],
        iconAnchor: [15, 15]
      })
      
      if (this.driverMarker) {
        this.map.removeLayer(this.driverMarker)
      }
      
      this.driverMarker = L.marker(this.currentCoordinates, { icon: driverIcon })
        .addTo(this.map)
        .bindPopup(`🚚 ${this.driverInfo.name}<br>${this.currentLocation}`)
    },
    
    updateRoute() {
      if (this.routeLine) {
        this.map.removeLayer(this.routeLine)
      }
      
      // Cria uma rota simulada entre as coordenadas
      const routeCoordinates = [
        this.currentCoordinates,
        this.destinationCoordinates
      ]
      
      this.routeLine = L.polyline(routeCoordinates, {
        color: '#007bff',
        weight: 4,
        opacity: 0.7,
        dashArray: '10, 10'
      }).addTo(this.map)
    },
    
    updateMap() {
      if (!this.map) return
      
      this.updateDriverMarker()
      this.updateRoute()
      
      // Ajusta a visualização para mostrar ambos os marcadores
      const bounds = L.latLngBounds([
        this.currentCoordinates,
        this.destinationCoordinates
      ])
      this.map.fitBounds(bounds, { padding: [20, 20] })
    },
    
    startMapAutoRefresh() {
      // Atualiza o mapa a cada 30 segundos
      this.mapInterval = setInterval(() => {
        if (this.trackingInfo && this.trackingInfo.status !== 'delivered') {
          this.refreshLocation()
        }
      }, 30000)
    },
    
    stopMapAutoRefresh() {
      if (this.mapInterval) {
        clearInterval(this.mapInterval)
        this.mapInterval = null
      }
    }
  },
  
  watch: {
    trackingInfo: {
      handler() {
        this.updateMap()
      },
      deep: true
    }
  },
  
  async mounted() {
    // Aguarda o DOM estar pronto
    this.$nextTick(() => {
      this.initMap()
    })
    
    // Busca dados iniciais
    if (!this.trackingInfo) {
      await this.refreshLocation()
    } else {
      this.updateMap()
    }
    
    // Inicia atualização automática
    this.startMapAutoRefresh()
  },
  
  beforeUnmount() {
    this.stopMapAutoRefresh()
    if (this.map) {
      this.map.remove()
    }
  }
}
</script>

<style scoped>
.live-tracking-map {
  margin-bottom: 2rem;
}

.status-card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.status-badge {
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: bold;
}

.status-confirmed { background: #17a2b8; color: white; }
.status-processing { background: #ffc107; color: #000; }
.status-shipped { background: #fd7e14; color: white; }
.status-delivery { background: #007bff; color: white; }
.status-delivered { background: #28a745; color: white; }

.progress-container {
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #28a745, #007bff);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.progress-text {
  font-size: 0.9rem;
  color: #6c757d;
  text-align: center;
}

.card {
  border: none;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.tracking-map {
  height: 300px;
  width: 100%;
  border-radius: 0 0 8px 8px;
}

.map-overlay {
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.95);
  padding: 10px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  z-index: 1000;
}

.location-address {
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.location-update {
  font-size: 0.75rem;
}

.driver-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.driver-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.driver-details {
  flex: 1;
}

.driver-name {
  font-weight: bold;
  margin-bottom: 0.25rem;
}

.driver-rating {
  font-size: 0.8rem;
}

.rating-text {
  margin-left: 0.5rem;
  color: #6c757d;
}

.driver-contact {
  font-size: 0.9rem;
}

.contact-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
}

.delivery-details {
  display: grid;
  gap: 0.75rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f8f9fa;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  color: #6c757d;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
}

.detail-value {
  font-weight: 600;
  color: #2c3e50;
}

.btn {
  padding: 0.75rem;
}

/* Estilos para os marcadores do mapa */
.driver-marker {
  background: #007bff;
  border: 3px solid white;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.8rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.3);
  animation: pulse 2s infinite;
}

.destination-marker {
  background: #28a745;
  border: 3px solid white;
  border-radius: 50%;
  width: 25px;
  height: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.7rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.3);
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

/* Responsividade */
@media (max-width: 768px) {
  .tracking-map {
    height: 250px;
  }
  
  .status-header {
    flex-direction: column;
    gap: 0.5rem;
    text-align: center;
  }
}
</style>
[file content end]