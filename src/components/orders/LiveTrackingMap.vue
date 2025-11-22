<template>
  <div class="live-tracking-map">
    <!-- Mapa -->
    <div class="map-container card">
      <div class="card-header bg-primary text-white">
        <h5 class="mb-0">
          <i class="fas fa-map-marker-alt me-2"></i>
          Localização em Tempo Real
        </h5>
      </div>
      <div class="card-body p-0">
        <div class="map-placeholder" v-if="!showRealMap">
          <div class="map-content">
            <i class="fas fa-truck fa-3x text-primary mb-3"></i>
            <h6>Rastreamento Ativo</h6>
            <p class="small text-muted mb-2">{{ currentLocation }}</p>
            <div class="driver-info">
              <strong>Entregador:</strong> {{ trackingInfo.driver?.name }}<br>
              <strong>Veículo:</strong> {{ trackingInfo.driver?.vehicle }}<br>
              <strong>Contato:</strong> {{ trackingInfo.driver?.phone }}
            </div>
          </div>
        </div>
        
        <!-- Aqui você pode integrar com Google Maps ou Mapbox -->
        <div class="real-map" v-if="showRealMap">
          <!-- Integração com API de mapas viria aqui -->
          <div class="map-integration-placeholder">
            <i class="fas fa-map fa-4x text-muted mb-3"></i>
            <p>Integração com Google Maps</p>
            <small class="text-muted">Coordenadas: {{ trackingInfo.coordinates?.lat }}, {{ trackingInfo.coordinates?.lng }}</small>
          </div>
        </div>
      </div>
    </div>

    <!-- Controles do Mapa -->
    <div class="map-controls mt-3">
      <div class="row g-2">
        <div class="col-md-6">
          <button class="btn btn-outline-primary w-100" @click="refreshLocation">
            <i class="fas fa-sync-alt me-2" :class="{ 'fa-spin': refreshing }"></i>
            Atualizar Localização
          </button>
        </div>
        <div class="col-md-6">
          <button class="btn btn-outline-success w-100" @click="contactDriver">
            <i class="fas fa-phone me-2"></i>
            Falar com Entregador
          </button>
        </div>
      </div>
    </div>

    <!-- Informações de Tempo Real -->
    <div class="real-time-info mt-3">
      <div class="card">
        <div class="card-body">
          <div class="row text-center">
            <div class="col-4">
              <div class="info-item">
                <i class="fas fa-clock text-warning fa-2x mb-2"></i>
                <h6 class="mb-1">Previsão</h6>
                <small class="text-muted">{{ estimatedTime }}</small>
              </div>
            </div>
            <div class="col-4">
              <div class="info-item">
                <i class="fas fa-road text-info fa-2x mb-2"></i>
                <h6 class="mb-1">Distância</h6>
                <small class="text-muted">{{ estimatedDistance }}</small>
              </div>
            </div>
            <div class="col-4">
              <div class="info-item">
                <i class="fas fa-battery-three-quarters text-success fa-2x mb-2"></i>
                <h6 class="mb-1">Status</h6>
                <small class="text-muted">{{ deliveryStatus }}</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LiveTrackingMap',
  props: {
    trackingInfo: {
      type: Object,
      required: true
    },
    orderId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      refreshing: false,
      showRealMap: false, // Alterne para true quando integrar com API real
      autoRefresh: null
    }
  },
  computed: {
    currentLocation() {
      return this.trackingInfo.currentLocation || 'Buscando localização...'
    },
    estimatedTime() {
      if (!this.trackingInfo.estimatedDelivery) return 'Calculando...'
      const deliveryTime = new Date(this.trackingInfo.estimatedDelivery)
      const now = new Date()
      const diffMs = deliveryTime - now
      const diffMins = Math.round(diffMs / 60000)
      
      if (diffMins <= 0) return 'Chegando agora'
      if (diffMins < 60) return `${diffMins} min`
      return `${Math.round(diffMins / 60)} h ${diffMins % 60} min`
    },
    estimatedDistance() {
      const distances = ['2 km', '5 km', '8 km', '10 km', '15 km']
      return distances[Math.floor(Math.random() * distances.length)]
    },
    deliveryStatus() {
      const statusMap = {
        'confirmed': 'Confirmado',
        'processing': 'Processando',
        'shipped': 'Enviado',
        'out_for_delivery': 'Saiu para entrega',
        'delivered': 'Entregue'
      }
      return statusMap[this.trackingInfo.status] || 'Em trânsito'
    }
  },
  methods: {
    async refreshLocation() {
      this.refreshing = true
      try {
        await this.$store.dispatch('fetchRealTimeTracking', this.orderId)
        // Em produção, isso atualizaria o trackingInfo via prop
        console.log('📍 Localização atualizada')
      } catch (error) {
        console.error('Erro ao atualizar localização:', error)
      } finally {
        this.refreshing = false
      }
    },
    
    contactDriver() {
      const phone = this.trackingInfo.driver?.phone
      if (phone) {
        // Formata o número para link do WhatsApp
        const cleanPhone = phone.replace(/\D/g, '')
        const message = `Olá! Estou acompanhando o pedido ${this.orderId}. Poderia me informar a previsão de entrega?`
        const whatsappUrl = `https://wa.me/55${cleanPhone}?text=${encodeURIComponent(message)}`
        window.open(whatappUrl, '_blank')
      } else {
        alert('Número do entregador não disponível no momento.')
      }
    },
    
    startAutoRefresh() {
      // Atualiza a cada 30 segundos
      this.autoRefresh = setInterval(() => {
        this.refreshLocation()
      }, 30000)
    },
    
    stopAutoRefresh() {
      if (this.autoRefresh) {
        clearInterval(this.autoRefresh)
      }
    }
  },
  
  mounted() {
    this.startAutoRefresh()
  },
  
  beforeUnmount() {
    this.stopAutoRefresh()
  }
}
</script>

<style scoped>
.live-tracking-map {
  margin-bottom: 2rem;
}

.map-container {
  border: none;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.map-placeholder {
  height: 300px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.map-content {
  text-align: center;
  padding: 2rem;
}

.driver-info {
  background: rgba(255, 255, 255, 0.9);
  padding: 1rem;
  border-radius: 10px;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.real-map {
  height: 300px;
  background: #f8f9fa;
}

.map-integration-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6c757d;
}

.map-controls .btn {
  padding: 0.75rem;
}

.info-item {
  padding: 0.5rem;
}

.info-item h6 {
  font-size: 0.9rem;
  font-weight: 600;
}

/* Animação para atualização */
@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

.fa-spin {
  animation: pulse 1s infinite;
}
</style>