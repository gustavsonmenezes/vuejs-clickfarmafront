<!-- src/components/orders/TrackingModal.vue -->
<template>
  <div class="modal fade show d-block" style="background-color: rgba(0,0,0,0.5)">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="fas fa-shipping-fast me-2"></i>
            Rastreamento - Pedido #{{ order.id }}
          </h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <OrderTimeline :order="order" :auto-refresh="true" />
          
          <!-- Informações Adicionais de Rastreamento -->
          <div v-if="trackingInfo" class="tracking-details mt-4 p-3 bg-light rounded">
            <h6 class="mb-3">
              <i class="fas fa-info-circle me-2"></i>Informações de Entrega
            </h6>
            <div class="row">
              <div class="col-md-6">
                <small class="text-muted">Código de Rastreamento</small>
                <p class="mb-2 fw-bold">{{ trackingInfo.trackingCode }}</p>
              </div>
              <div class="col-md-6">
                <small class="text-muted">Transportadora</small>
                <p class="mb-2">{{ trackingInfo.carrier }}</p>
              </div>
              <div class="col-md-6">
                <small class="text-muted">Previsão de Entrega</small>
                <p class="mb-2">{{ formatDate(trackingInfo.estimatedDelivery) }}</p>
              </div>
              <div class="col-md-6">
                <small class="text-muted">Localização Atual</small>
                <p class="mb-2">{{ trackingInfo.currentLocation }}</p>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline-primary" @click="refreshTracking">
            <i class="fas fa-sync-alt me-2" :class="{ 'fa-spin': loading }"></i>
            Atualizar
          </button>
          <button class="btn btn-secondary" @click="$emit('close')">Fechar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions, mapGetters } from 'vuex'
import OrderTimeline from './OrderTimeline.vue'

export default {
  name: 'TrackingModal',
  components: {
    OrderTimeline
  },
  props: {
    order: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      loading: false
    }
  },
  computed: {
    ...mapGetters(['getOrderTracking']),
    
    trackingInfo() {
      return this.getOrderTracking(this.order.id)
    }
  },
  methods: {
    ...mapActions(['fetchOrderTracking']),
    
    async refreshTracking() {
      this.loading = true
      try {
        await this.fetchOrderTracking(this.order.id)
      } catch (error) {
        console.error('Erro ao atualizar rastreamento:', error)
      } finally {
        this.loading = false
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return 'Não disponível'
      try {
        return new Date(dateString).toLocaleDateString('pt-BR')
      } catch (error) {
        return 'Data inválida'
      }
    }
  },
  emits: ['close'],
  
  async mounted() {
    // Buscar informações de rastreamento ao abrir o modal
    await this.refreshTracking()
  }
}
</script>

<style scoped>
.modal {
  backdrop-filter: blur(5px);
}

.tracking-details {
  border-left: 4px solid #007bff;
}
</style>