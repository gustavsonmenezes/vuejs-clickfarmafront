<template>
  <div class="order-list">
    <h2>Meus Pedidos</h2>
    
    <div v-for="order in orders" :key="order.id" class="order-card">
      <div class="order-header">
        <h3>Pedido #{{ order.id }}</h3>
        <span class="order-status">{{ getStatusText(order.status) }}</span>
      </div>
      
      <div class="order-details">
        <p>Data: {{ formatDate(order.date) }}</p>
        <p>Total: R$ {{ order.total.toFixed(2) }}</p>
        <p>Método de Pagamento: {{ order.paymentMethod }}</p>
      </div>
      
      <div class="order-actions">
        <button 
          @click="trackOrder(order.id)" 
          class="track-btn"
        >
          📍 Rastrear Pedido
        </button>
        
        <button 
          @click="viewOrderDetails(order.id)" 
          class="details-btn"
        >
          📋 Ver Detalhes
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderList',
  data() {
    return {
      orders: []
    }
  },
  async mounted() {
    await this.loadOrders();
  },
  methods: {
    async loadOrders() {
      // Carrega pedidos do usuário
      // this.orders = await this.$store.dispatch('fetchUserOrders');
      
      // Mock data para exemplo
      this.orders = [
        {
          id: 'ORD-ABC123',
          date: new Date().toISOString(),
          total: 156.90,
          status: 'shipped',
          paymentMethod: 'credit_card'
        },
        {
          id: 'ORD-DEF456',
          date: new Date(Date.now() - 86400000).toISOString(),
          total: 89.50,
          status: 'delivered',
          paymentMethod: 'pix'
        }
      ];
    },

    async trackOrder(orderId) {
      try {
        // Navega para a página de rastreamento
        this.$router.push(`/tracking/${orderId}`);
      } catch (error) {
        console.error('Erro ao rastrear pedido:', error);
        alert('Erro ao carregar rastreamento');
      }
    },

    viewOrderDetails(orderId) {
      this.$router.push(`/orders/${orderId}`);
    },

    getStatusText(status) {
      const statusMap = {
        'pending': 'Pendente',
        'confirmed': 'Confirmado',
        'processing': 'Processando',
        'shipped': 'Enviado',
        'out_for_delivery': 'Saiu para Entrega',
        'delivered': 'Entregue'
      };
      return statusMap[status] || status;
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('pt-BR');
    }
  }
}
</script>

<style scoped>
.order-list {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.order-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  background: white;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.order-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8em;
  font-weight: bold;
}

.order-status[data-status="shipped"] {
  background: #fff3cd;
  color: #856404;
}

.order-status[data-status="delivered"] {
  background: #d4edda;
  color: #155724;
}

.order-details p {
  margin: 5px 0;
  color: #666;
}

.order-actions {
  margin-top: 10px;
}

.track-btn, .details-btn {
  padding: 8px 12px;
  margin-right: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.track-btn {
  background: #007bff;
  color: white;
}

.details-btn {
  background: #6c757d;
  color: white;
}
</style>