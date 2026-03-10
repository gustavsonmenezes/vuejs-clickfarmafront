<template>
  <div class="user-dashboard">
    <div class="dashboard-header">
      <h1>Meu Painel</h1>
      <p>Bem-vindo de volta, {{ userName }}!</p>
    </div>

    <div class="dashboard-grid">
      <!-- Resumo Rápido -->
      <div class="dashboard-card">
        <h3>📊 Resumo</h3>
        <div class="stats">
          <div class="stat-item">
            <span class="stat-number">{{ ordersCount }}</span>
            <span class="stat-label">Pedidos</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ pendingOrders }}</span>
            <span class="stat-label">Pendentes</span>
          </div>
        </div>
      </div>

      <!-- Pedidos Recentes -->
      <div class="dashboard-card">
        <h3>📦 Pedidos Recentes</h3>
        <div v-if="recentOrders.length > 0" class="recent-orders">
          <div v-for="order in recentOrders" :key="order.id" class="order-item">
            <div class="order-info">
              <strong>#{{ order.id }}</strong>
              <span class="order-status" :class="order.status">
                {{ getStatusText(order.status) }}
              </span>
            </div>
            <div class="order-actions">
              <button 
                @click="trackOrder(order.id)" 
                class="track-btn"
                v-if="order.status !== 'delivered'"
              >
                📍 Rastrear
              </button>
              <router-link 
                :to="`/tracking/${order.id}`" 
                class="details-btn"
              >
                🔍 Detalhes
              </router-link>
            </div>
          </div>
        </div>
        <div v-else class="no-orders">
          <p>Nenhum pedido recente</p>
          <router-link to="/products" class="shop-btn">
            🛒 Fazer Compras
          </router-link>
        </div>
      </div>

      <!-- Rastreamento Ativo -->
      <div class="dashboard-card">
        <h3>🚚 Rastreamento Ativo</h3>
        <div v-if="activeTracking" class="active-tracking">
          <div class="tracking-info">
            <p><strong>Pedido:</strong> #{{ activeTracking.orderId }}</p>
            <p><strong>Status:</strong> {{ getStatusText(activeTracking.status) }}</p>
            <p><strong>Localização:</strong> {{ activeTracking.currentLocation }}</p>
            <p><strong>Previsão:</strong> {{ formatDate(activeTracking.estimatedDelivery) }}</p>
          </div>
          <button 
            @click="viewFullTracking(activeTracking.orderId)" 
            class="full-tracking-btn"
          >
            📋 Ver Rastreamento Completo
          </button>
        </div>
        <div v-else class="no-tracking">
          <p>Nenhum pedido em transporte no momento</p>
        </div>
      </div>

      <!-- Ações Rápidas -->
      <div class="dashboard-card">
        <h3>⚡ Ações Rápidas</h3>
        <div class="quick-actions">
          <router-link to="/products" class="quick-action">
            🛍️ Continuar Comprando
          </router-link>
          <router-link to="/orders" class="quick-action">
            📋 Ver Todos os Pedidos
          </router-link>
          <router-link to="/profile" class="quick-action">
            👤 Meu Perfil
          </router-link>
          <router-link to="/addresses" class="quick-action">
            📍 Meus Endereços
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserDashboard',
  data() {
    return {
      recentOrders: [],
      activeTracking: null,
      loading: false
    }
  },
  computed: {
    userName() {
      const user = this.$store.state.user;
      return user ? user.name : 'Usuário';
    },
    ordersCount() {
      return this.recentOrders.length;
    },
    pendingOrders() {
      return this.recentOrders.filter(order => 
        order.status !== 'delivered' && order.status !== 'cancelled'
      ).length;
    }
  },
  async mounted() {
    await this.loadDashboardData();
  },
  methods: {
    async loadDashboardData() {
      this.loading = true;
      try {
        // Carrega pedidos recentes (mock data por enquanto)
        await this.loadRecentOrders();
        
        // Carrega rastreamento ativo
        await this.loadActiveTracking();
      } catch (error) {
        console.error('Erro ao carregar dashboard:', error);
      } finally {
        this.loading = false;
      }
    },

    async loadRecentOrders() {
      // Mock data - substitua por chamada real à API
      this.recentOrders = [
        {
          id: 'ORD-ABC123',
          date: new Date().toISOString(),
          total: 156.90,
          status: 'out_for_delivery',
          paymentMethod: 'credit_card'
        },
        {
          id: 'ORD-DEF456',
          date: new Date(Date.now() - 86400000).toISOString(),
          total: 89.50,
          status: 'delivered',
          paymentMethod: 'pix'
        },
        {
          id: 'ORD-GHI789',
          date: new Date(Date.now() - 172800000).toISOString(),
          total: 234.75,
          status: 'processing',
          paymentMethod: 'boleto'
        }
      ];
    },

    async loadActiveTracking() {
      try {
        // Encontra o primeiro pedido que não foi entregue
        const activeOrder = this.recentOrders.find(order => 
          order.status !== 'delivered' && order.status !== 'cancelled'
        );

        if (activeOrder) {
          this.activeTracking = await this.$store.dispatch(
            'fetchRealTimeTracking', 
            activeOrder.id
          );
          this.activeTracking.orderId = activeOrder.id;
        }
      } catch (error) {
        console.error('Erro ao carregar rastreamento ativo:', error);
      }
    },

    async trackOrder(orderId) {
      try {
        const tracking = await this.$store.dispatch('fetchRealTimeTracking', orderId);
        alert(`📍 Pedido ${orderId}\nLocalização: ${tracking.currentLocation}\nStatus: ${this.getStatusText(tracking.status)}`);
      } catch (error) {
        console.error('Erro no rastreamento rápido:', error);
        alert('Erro ao carregar rastreamento');
      }
    },

    viewFullTracking(orderId) {
      this.$router.push(`/tracking/${orderId}`);
    },

    getStatusText(status) {
      const statusMap = {
        'pending': 'Pendente',
        'confirmed': 'Confirmado',
        'processing': 'Processando',
        'shipped': 'Enviado',
        'out_for_delivery': 'Saiu para Entrega',
        'delivered': 'Entregue',
        'cancelled': 'Cancelado'
      };
      return statusMap[status] || status;
    },

    formatDate(dateString) {
      if (!dateString) return 'N/A';
      return new Date(dateString).toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }
}
</script>

<style scoped>
.user-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 30px;
}

.dashboard-header h1 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.dashboard-header p {
  color: #7f8c8d;
  font-size: 1.1em;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.dashboard-card {
  background: white;
  border: 1px solid #e1e8ed;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.dashboard-card h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #2c3e50;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
}

.stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 2em;
  font-weight: bold;
  color: #3498db;
}

.stat-label {
  color: #7f8c8d;
  font-size: 0.9em;
}

.recent-orders {
  space-y: 10px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ecf0f1;
}

.order-item:last-child {
  border-bottom: none;
}

.order-info {
  display: flex;
  flex-direction: column;
}

.order-status {
  font-size: 0.8em;
  padding: 2px 8px;
  border-radius: 12px;
  margin-top: 4px;
}

.order-status.processing {
  background: #fff3cd;
  color: #856404;
}

.order-status.shipped {
  background: #cce7ff;
  color: #004085;
}

.order-status.out_for_delivery {
  background: #d4edda;
  color: #155724;
}

.order-status.delivered {
  background: #d1ecf1;
  color: #0c5460;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.track-btn, .details-btn, .full-tracking-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  text-decoration: none;
  font-size: 0.8em;
  text-align: center;
}

.track-btn {
  background: #27ae60;
  color: white;
}

.details-btn {
  background: #3498db;
  color: white;
}

.full-tracking-btn {
  background: #e74c3c;
  color: white;
  width: 100%;
  margin-top: 10px;
}

.no-orders, .no-tracking {
  text-align: center;
  padding: 20px;
  color: #7f8c8d;
}

.shop-btn {
  display: inline-block;
  margin-top: 10px;
  padding: 8px 16px;
  background: #9b59b6;
  color: white;
  text-decoration: none;
  border-radius: 6px;
}

.tracking-info p {
  margin: 5px 0;
  color: #2c3e50;
}

.quick-actions {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.quick-action {
  display: block;
  padding: 12px;
  background: #f8f9fa;
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  text-decoration: none;
  color: #2c3e50;
  text-align: center;
  transition: all 0.3s ease;
}

.quick-action:hover {
  background: #3498db;
  color: white;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
  
  .user-dashboard {
    padding: 10px;
  }
}
</style>