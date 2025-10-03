<template>
  <div class="container mt-4">
    <h2>📦 Meus Pedidos</h2>
    
    <div v-if="orders.length === 0" class="text-center py-5">
      <div class="empty-state">
        <i class="fas fa-box-open fa-3x text-muted mb-3"></i>
        <h5>Nenhum pedido encontrado</h5>
        <p class="text-muted">Você ainda não realizou nenhum pedido.</p>
        <router-link to="/products" class="btn btn-primary">
          🛍️ Fazer Compras
        </router-link>
      </div>
    </div>

    <div v-else>
      <div class="row">
        <div class="col-md-8">
          <div v-for="order in orders" :key="order.id" class="card mb-3">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                  <h6 class="card-title mb-1">Pedido #{{ order.id }}</h6>
                  <p class="text-muted small mb-0">
                    Realizado em {{ formatDate(order.date) }}
                  </p>
                </div>
                <span :class="statusClass(order.status)" class="badge">
                  {{ statusText(order.status) }}
                </span>
              </div>
              
              <div class="row">
                <div class="col-md-6">
                  <small class="text-muted">Itens:</small>
                  <p class="mb-1">{{ order.items.length }} produto(s)</p>
                </div>
                <div class="col-md-6">
                  <small class="text-muted">Total:</small>
                  <p class="mb-1 fw-bold">R$ {{ order.total.toFixed(2) }}</p>
                </div>
              </div>
              
              <div class="d-flex gap-2 mt-3">
                <router-link 
                  :to="`/orders/${order.id}`" 
                  class="btn btn-outline-primary btn-sm"
                >
                  📋 Ver Detalhes
                </router-link>
                <button 
                  v-if="order.status === 'confirmed'" 
                  class="btn btn-outline-danger btn-sm"
                  @click="cancelOrder(order.id)"
                >
                  ❌ Cancelar
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <div class="col-md-4">
          <div class="card">
            <div class="card-header">
              <h6 class="mb-0">📊 Resumo</h6>
            </div>
            <div class="card-body">
              <div class="small">
                <p><strong>Total de pedidos:</strong> {{ orders.length }}</p>
                <p><strong>Último pedido:</strong> {{ lastOrderDate }}</p>
                <p><strong>Valor médio:</strong> R$ {{ averageOrderValue }}</p>
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
  name: 'Orders',
  data() {
    return {
      orders: [
        {
          id: '12345',
          date: '2024-01-15T10:30:00',
          status: 'delivered',
          total: 89.90,
          items: [
            { id: 1, name: 'Paracetamol 500mg', quantity: 2, price: 8.90 },
            { id: 2, name: 'Shampoo Anticaspa', quantity: 1, price: 24.90 }
          ]
        },
        {
          id: '12346',
          date: '2024-01-10T14:20:00',
          status: 'shipped',
          total: 45.50,
          items: [
            { id: 3, name: 'Vitamina C 1000mg', quantity: 1, price: 45.50 }
          ]
        }
      ]
    }
  },
  computed: {
    lastOrderDate() {
      if (this.orders.length === 0) return 'N/A'
      return this.formatDate(this.orders[0].date)
    },
    averageOrderValue() {
      if (this.orders.length === 0) return '0.00'
      const total = this.orders.reduce((sum, order) => sum + order.total, 0)
      return (total / this.orders.length).toFixed(2)
    }
  },
  methods: {
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('pt-BR')
    },
    statusClass(status) {
      const statusClasses = {
        'confirmed': 'bg-primary',
        'processing': 'bg-info',
        'shipped': 'bg-warning',
        'delivered': 'bg-success',
        'cancelled': 'bg-danger'
      }
      return statusClasses[status] || 'bg-secondary'
    },
    statusText(status) {
      const statusTexts = {
        'confirmed': 'Confirmado',
        'processing': 'Processando',
        'shipped': 'Enviado',
        'delivered': 'Entregue',
        'cancelled': 'Cancelado'
      }
      return statusTexts[status] || status
    },
    cancelOrder(orderId) {
      if (confirm('Tem certeza que deseja cancelar este pedido?')) {
        const order = this.orders.find(o => o.id === orderId)
        if (order) {
          order.status = 'cancelled'
          alert('Pedido cancelado com sucesso!')
        }
      }
    }
  }
}
</script>

<style scoped>
.empty-state {
  padding: 3rem 1rem;
}

.card {
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-2px);
}
</style>