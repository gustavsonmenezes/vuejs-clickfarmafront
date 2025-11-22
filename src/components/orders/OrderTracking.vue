<!-- src/components/orders/OrderTracking.vue -->
<template>
  <div class="order-tracking">
    <div class="tracking-header">
      <h4>Rastrear Pedido #{{ order.id }}</h4>
      <p class="text-muted">Código de rastreamento: {{ order.trackingCode }}</p>
    </div>
    
    <div class="tracking-steps">
      <div v-for="(step, index) in trackingSteps" :key="index" 
           class="tracking-step" :class="step.status">
        <div class="step-icon">
          <i :class="step.icon"></i>
        </div>
        <div class="step-content">
          <h6>{{ step.title }}</h6>
          <p class="mb-1">{{ step.description }}</p>
          <small class="text-muted">{{ step.time }}</small>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderTracking',
  props: {
    order: {
      type: Object,
      required: true
    }
  },
  computed: {
    trackingSteps() {
      return [
        {
          title: 'Pedido Confirmado',
          description: 'Seu pedido foi recebido',
          icon: 'fas fa-check-circle',
          time: this.formatDate(this.order.date),
          status: 'completed'
        },
        {
          title: 'Preparando Pedido',
          description: 'Estamos separando seus produtos',
          icon: 'fas fa-box',
          time: this.calculateTime(1),
          status: this.order.status === 'processing' ? 'current' : 'completed'
        },
        {
          title: 'Pedido Enviado',
          description: 'Seu pedido saiu para entrega',
          icon: 'fas fa-shipping-fast',
          time: this.calculateTime(2),
          status: this.order.status === 'shipped' ? 'current' : 'pending'
        },
        {
          title: 'Entregue',
          description: 'Pedido entregue com sucesso',
          icon: 'fas fa-home',
          time: this.calculateTime(3),
          status: this.order.status === 'delivered' ? 'completed' : 'pending'
        }
      ]
    }
  },
  methods: {
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    
    calculateTime(daysToAdd) {
      const date = new Date(this.order.date)
      date.setDate(date.getDate() + daysToAdd)
      return date.toLocaleDateString('pt-BR')
    }
  }
}
</script>

<style scoped>
.tracking-steps {
  position: relative;
  padding-left: 2rem;
}

.tracking-steps::before {
  content: '';
  position: absolute;
  left: 1rem;
  top: 0;
  bottom: 0;
  width: 2px;
  background-color: #dee2e6;
}

.tracking-step {
  position: relative;
  padding: 1rem 0;
  display: flex;
  align-items: flex-start;
}

.step-icon {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #dee2e6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  position: relative;
  z-index: 2;
}

.tracking-step.completed .step-icon {
  border-color: #28a745;
  background: #28a745;
  color: white;
}

.tracking-step.current .step-icon {
  border-color: #007bff;
  background: #007bff;
  color: white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}
</style>