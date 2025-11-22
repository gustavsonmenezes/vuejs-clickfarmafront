<!-- src/components/orders/OrderTimeline.vue -->
<template>
  <div class="order-timeline">
    <div class="timeline-header mb-4">
      <h6 class="mb-1">Acompanhe seu pedido</h6>
      <p class="text-muted small mb-0" v-if="deliveryEstimate">
        Previsão de entrega: {{ deliveryEstimate }}
      </p>
      <button 
        v-if="order && order.status !== 'delivered'" 
        class="btn btn-sm btn-outline-primary mt-2"
        @click="refreshTracking"
        :disabled="loading"
      >
        <i class="fas fa-sync-alt" :class="{ 'fa-spin': loading }"></i>
        Atualizar
      </button>
    </div>
    
    <div class="timeline">
      <div 
        v-for="(step, index) in enhancedTimeline" 
        :key="step.status" 
        class="timeline-step"
        :class="{
          'completed': step.completed,
          'current': step.current,
          'pending': step.pending
        }"
      >
        <div class="timeline-icon">
          <i :class="step.icon"></i>
        </div>
        <div class="timeline-content">
          <h6 class="mb-1">{{ step.title }}</h6>
          <p class="mb-0 text-muted small">{{ step.description }}</p>
          <small v-if="step.timestamp" class="text-primary d-block mt-1">
            {{ formatTimestamp(step.timestamp) }}
          </small>
          <small v-if="step.location" class="text-muted d-block">
            📍 {{ step.location }}
          </small>
        </div>
      </div>
    </div>

    <!-- Informações de rastreamento -->
    <div v-if="trackingInfo" class="tracking-info mt-4 p-3 bg-light rounded">
      <div class="row">
        <div class="col-md-6">
          <small class="text-muted">Código de rastreamento</small>
          <p class="mb-0 fw-bold">{{ trackingInfo.trackingCode }}</p>
        </div>
        <div class="col-md-6">
          <small class="text-muted">Transportadora</small>
          <p class="mb-0">{{ trackingInfo.carrier }}</p>
        </div>
        <div class="col-12 mt-2">
          <small class="text-muted">Localização atual</small>
          <p class="mb-0">{{ trackingInfo.currentLocation }}</p>
        </div>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="text-center py-3">
      <div class="spinner-border spinner-border-sm text-primary"></div>
      <span class="ms-2">Buscando atualizações...</span>
    </div>

    <!-- Fallback para quando não há order -->
    <div v-if="!order" class="text-center text-muted">
      <p>Informações do pedido não disponíveis</p>
    </div>
  </div>
</template>

<script>
import { mapActions, mapGetters } from 'vuex'

export default {
  name: 'OrderTimeline',
  props: {
    order: {
      type: Object,
      required: true
    },
    autoRefresh: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      loading: false,
      refreshInterval: null
    }
  },
  computed: {
    ...mapGetters(['getOrderTracking']),
    
    trackingInfo() {
      if (!this.order || !this.order.id) return null
      return this.getOrderTracking(this.order.id)
    },
    
    enhancedTimeline() {
      if (!this.order) return []
      
      if (!this.trackingInfo) return this.getBasicTimeline()
      
      return this.trackingInfo.updates.map(update => ({
        status: update.status,
        title: this.getStatusTitle(update.status),
        description: update.description,
        icon: this.getStatusIcon(update.status),
        timestamp: update.timestamp,
        location: update.location,
        completed: this.isStatusCompleted(update.status),
        current: this.isStatusCurrent(update.status),
        pending: false
      }))
    },
    
    deliveryEstimate() {
      if (!this.trackingInfo?.estimatedDelivery) return null
      return new Date(this.trackingInfo.estimatedDelivery).toLocaleDateString('pt-BR')
    }
  },
  methods: {
    ...mapActions(['fetchOrderTracking']),
    
    async refreshTracking() {
      if (!this.order || !this.order.id) return
      
      this.loading = true
      try {
        await this.fetchOrderTracking(this.order.id)
      } catch (error) {
        console.error('Erro ao atualizar rastreamento:', error)
      } finally {
        this.loading = false
      }
    },

    getBasicTimeline() {
      if (!this.order) return []
      
      // Se o pedido foi confirmado recentemente, mostra apenas o status atual
      if (this.order.status === 'confirmed') {
        return [
          {
            status: 'confirmed',
            title: 'Pedido Confirmado',
            description: 'Seu pedido foi recebido e confirmado com sucesso',
            icon: 'fas fa-check-circle',
            completed: true,
            current: true,
            pending: false,
            timestamp: this.order.date
          }
        ]
      }
      
      // Para outros status, mostra a timeline completa
      const basicSteps = [
        {
          status: 'confirmed',
          title: 'Pedido Confirmado',
          description: 'Seu pedido foi recebido e confirmado',
          icon: 'fas fa-check-circle',
          completed: true,
          current: false,
          pending: false
        },
        {
          status: 'processing', 
          title: 'Processando',
          description: 'Estamos preparando seu pedido',
          icon: 'fas fa-cog',
          completed: false,
          current: true,
          pending: false
        },
        {
          status: 'shipped',
          title: 'Enviado',
          description: 'Seu pedido saiu para entrega',
          icon: 'fas fa-shipping-fast',
          completed: false,
          current: false,
          pending: true
        },
        {
          status: 'delivered',
          title: 'Entregue',
          description: 'Pedido entregue com sucesso',
          icon: 'fas fa-home',
          completed: false,
          current: false,
          pending: true
        }
      ]

      return basicSteps.map(step => ({
        ...step,
        completed: this.isStatusCompleted(step.status),
        current: this.isStatusCurrent(step.status),
        pending: this.isStatusPending(step.status)
      }))
    },

    getStatusTitle(status) {
      const titles = {
        'confirmed': 'Pedido Confirmado',
        'processing': 'Em Processamento',
        'shipped': 'Enviado',
        'out_for_delivery': 'Saiu para Entrega',
        'delivered': 'Entregue'
      }
      return titles[status] || status
    },

    getStatusIcon(status) {
      const icons = {
        'confirmed': 'fas fa-check-circle',
        'processing': 'fas fa-cog',
        'shipped': 'fas fa-shipping-fast',
        'out_for_delivery': 'fas fa-truck',
        'delivered': 'fas fa-home'
      }
      return icons[status] || 'fas fa-info-circle'
    },

    isStatusCompleted(status) {
      if (!this.order) return false
      const statusOrder = ['confirmed', 'processing', 'shipped', 'out_for_delivery', 'delivered']
      const currentIndex = statusOrder.indexOf(this.order.status)
      const stepIndex = statusOrder.indexOf(status)
      return stepIndex < currentIndex
    },

    isStatusCurrent(status) {
      if (!this.order) return false
      return status === this.order.status
    },

    isStatusPending(status) {
      if (!this.order) return false
      const statusOrder = ['confirmed', 'processing', 'shipped', 'out_for_delivery', 'delivered']
      const currentIndex = statusOrder.indexOf(this.order.status)
      const stepIndex = statusOrder.indexOf(status)
      return stepIndex > currentIndex
    },

    formatTimestamp(timestamp) {
      return new Date(timestamp).toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  },

  async mounted() {
    if (!this.order || !this.order.id) return
    
    // Buscar informações iniciais
    await this.refreshTracking()
    
    // Configurar atualização automática se necessário
    if (this.autoRefresh && this.order.status !== 'delivered') {
      this.refreshInterval = setInterval(() => {
        this.refreshTracking()
      }, 30000) // Atualiza a cada 30 segundos
    }
  },

  beforeUnmount() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
    }
  }
}
</script>

<style scoped>
.tracking-info {
  border-left: 4px solid #007bff;
}
</style>