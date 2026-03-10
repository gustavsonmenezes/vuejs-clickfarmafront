[file name]: OrderTracking.vue
[file content begin]
<template>
  <div class="order-tracking-page">
    <div class="container mt-4">
      <!-- Cabeçalho Simples -->
      <div class="tracking-header text-center mb-4">
        <h1 class="text-primary mb-2">📦 Rastrear Pedido</h1>
        <p class="text-muted">Acompanhe a entrega do seu pedido</p>
      </div>

      <!-- Busca de Pedido -->
      <div class="card mb-4">
        <div class="card-body">
          <div class="row g-3 align-items-end">
            <div class="col-md-8">
              <label class="form-label">Número do pedido</label>
              <input 
                v-model="searchOrderId" 
                type="text" 
                class="form-control" 
                placeholder="Ex: ORD-ABC123XYZ"
                @keyup.enter="loadOrder"
              >
            </div>
            <div class="col-md-4">
              <button 
                class="btn btn-primary w-100" 
                @click="loadOrder"
                :disabled="loading || !searchOrderId.trim()"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                {{ loading ? 'Buscando...' : 'Buscar Pedido' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Pedidos Recentes -->
      <div v-if="userOrders.length > 0 && !currentOrder" class="card mb-4">
        <div class="card-header">
          <h6 class="mb-0">Seus Pedidos Recentes</h6>
        </div>
        <div class="card-body">
          <div class="list-group">
            <button 
              v-for="order in userOrders" 
              :key="order.id"
              class="list-group-item list-group-item-action"
              @click="selectOrder(order)"
            >
              <div class="d-flex w-100 justify-content-between">
                <h6 class="mb-1">Pedido #{{ order.id }}</h6>
                <span :class="statusClass(order.status)" class="badge">
                  {{ getStatusText(order.status) }}
                </span>
              </div>
              <p class="mb-1">Data: {{ formatDate(order.date) }}</p>
              <small>Total: R$ {{ getOrderTotal(order) }}</small>
            </button>
          </div>
        </div>
      </div>

      <!-- Pedido Encontrado -->
      <div v-if="currentOrder">
        <div class="row">
          <div class="col-lg-8">
            <!-- Informações Básicas do Pedido -->
            <div class="card mb-4">
              <div class="card-header bg-primary text-white">
                <h5 class="mb-0">Pedido #{{ currentOrder.id }}</h5>
              </div>
              <div class="card-body">
                <div class="row">
                  <div class="col-md-6">
                    <strong>Data:</strong> {{ formatDate(currentOrder.date) }}<br>
                    <strong>Total:</strong> R$ {{ getOrderTotal(currentOrder) }}<br>
                    <strong>Status:</strong> 
                    <span :class="statusClass(currentOrder.status)" class="badge ms-1">
                      {{ getStatusText(currentOrder.status) }}
                    </span>
                  </div>
                  <div class="col-md-6">
                    <strong>Pagamento:</strong> {{ getPaymentMethodLabel(currentOrder.paymentMethod) }}<br>
                    <strong>Entrega:</strong> {{ currentOrder.deliveryType === 'delivery' ? 'Entrega' : 'Retirada' }}<br>
                    <strong>Itens:</strong> {{ currentOrder.items?.length || 0 }}
                  </div>
                </div>
              </div>
            </div>

            <!-- Timeline do Pedido -->
            <div class="card mb-4">
              <div class="card-header">
                <h5 class="mb-0">Status do Pedido</h5>
              </div>
              <div class="card-body">
                <OrderTimeline :order="currentOrder" :auto-refresh="true" />
              </div>
            </div>

            <!-- Mapa de Rastreamento -->
            <div class="card mb-4">
              <div class="card-header">
                <h5 class="mb-0">Localização</h5>
              </div>
              <div class="card-body">
                <LiveTrackingMap 
                  v-if="currentOrder && currentOrder.id"
                  :order-id="currentOrder.id"
                />
              </div>
            </div>
          </div>

          <!-- Sidebar de Ações -->
          <div class="col-lg-4">
            <div class="card mb-4">
              <div class="card-header">
                <h6 class="mb-0">Ações</h6>
              </div>
              <div class="card-body">
                <button 
                  class="btn btn-outline-primary w-100 mb-2" 
                  @click="refreshTracking"
                  :disabled="loading"
                >
                  <i class="fas fa-sync-alt me-2" :class="{ 'fa-spin': loading }"></i>
                  Atualizar
                </button>
                <button 
                  class="btn btn-outline-secondary w-100 mb-2" 
                  @click="currentOrder = null; searchOrderId = '';"
                >
                  Buscar Outro Pedido
                </button>
                <router-link to="/orders" class="btn btn-outline-secondary w-100">
                  Meus Pedidos
                </router-link>
              </div>
            </div>

            <!-- Informações de Contato -->
            <div class="card">
              <div class="card-body">
                <h6>Precisa de Ajuda?</h6>
                <p class="small text-muted mb-2">Entre em contato com nosso suporte</p>
                <p class="mb-1">
                  <strong>📞 (81) 99818-9999</strong>
                </p>
                <p class="mb-0">
                  <strong>✉️ gustavson.adm@gmail.com</strong>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Estados Vazios -->
      <div v-if="searchPerformed && !currentOrder && !loading" class="text-center py-4">
        <i class="fas fa-search fa-3x text-muted mb-3"></i>
        <h4 class="text-muted">Pedido não encontrado</h4>
        <p class="text-muted">Verifique o número do pedido e tente novamente.</p>
        <button class="btn btn-primary" @click="resetSearch">
          Tentar Novamente
        </button>
      </div>

      <div v-if="userOrders.length === 0 && !searchPerformed && !currentOrder" class="text-center py-4">
        <i class="fas fa-shopping-bag fa-3x text-muted mb-3"></i>
        <h4 class="text-muted">Nenhum pedido encontrado</h4>
        <p class="text-muted mb-3">Digite o número do pedido acima para rastrear.</p>
        <router-link to="/products" class="btn btn-primary">
          Fazer Compras
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions, mapGetters } from 'vuex'
import OrderTimeline from '@/components/orders/OrderTimeline.vue'
import LiveTrackingMap from '@/components/orders/LiveTrackingMap.vue'

export default {
  name: 'OrderTracking',
  components: {
    OrderTimeline,
    LiveTrackingMap
  },
  props: {
    orderId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      searchOrderId: '',
      currentOrder: null,
      loading: false,
      searchPerformed: false
    }
  },
  computed: {
    ...mapGetters(['isAuthenticated']),
    
    userOrders() {
      try {
        const savedOrders = JSON.parse(localStorage.getItem('userOrders') || '[]')
        
        if (this.isAuthenticated) {
          const user = JSON.parse(localStorage.getItem('user') || '{}')
          return savedOrders
            .filter(order => order.userId === user.id)
            .slice(0, 5)
            .sort((a, b) => new Date(b.date) - new Date(a.date))
        }
        
        return savedOrders
          .slice(0, 3)
          .sort((a, b) => new Date(b.date) - new Date(a.date))
      } catch (error) {
        return []
      }
    }
  },
  methods: {
    ...mapActions(['fetchRealTimeTracking']),
    
    async loadOrder() {
      if (!this.searchOrderId.trim()) {
        alert('Por favor, informe o número do pedido')
        return
      }

      this.loading = true
      this.searchPerformed = true

      try {
        const savedOrders = JSON.parse(localStorage.getItem('userOrders') || '[]')
        const order = savedOrders.find(o => 
          o.id.toLowerCase() === this.searchOrderId.trim().toLowerCase()
        )
        
        if (order) {
          this.currentOrder = order
          await this.loadTrackingInfo()
        } else {
          this.currentOrder = null
        }
      } catch (error) {
        console.error('Erro ao carregar pedido:', error)
        this.currentOrder = null
      } finally {
        this.loading = false
      }
    },
    
    selectOrder(order) {
      this.searchOrderId = order.id
      this.loadOrder()
    },
    
    async loadTrackingInfo() {
      if (!this.currentOrder?.id) return
      
      try {
        await this.fetchRealTimeTracking(this.currentOrder.id)
      } catch (error) {
        console.error('Erro ao carregar rastreamento:', error)
      }
    },
    
    async refreshTracking() {
      await this.loadTrackingInfo()
    },
    
    resetSearch() {
      this.searchOrderId = ''
      this.currentOrder = null
      this.searchPerformed = false
    },
    
    getStatusText(status) {
      const statusMap = {
        'confirmed': 'Confirmado',
        'processing': 'Processando',
        'shipped': 'Enviado',
        'out_for_delivery': 'Saiu para Entrega',
        'delivered': 'Entregue',
        'cancelled': 'Cancelado'
      }
      return statusMap[status] || status
    },
    
    statusClass(status) {
      const classes = {
        'confirmed': 'bg-primary',
        'processing': 'bg-info',
        'shipped': 'bg-warning',
        'out_for_delivery': 'bg-warning text-dark',
        'delivered': 'bg-success',
        'cancelled': 'bg-danger'
      }
      return classes[status] || 'bg-secondary'
    },
    
    getPaymentMethodLabel(method) {
      const methods = {
        'credit_card': 'Cartão de Crédito',
        'debit_card': 'Cartão de Débito',
        'pix': 'PIX',
        'boleto': 'Boleto'
      }
      return methods[method] || method
    },
    
    formatDate(dateString) {
      try {
        if (!dateString) return 'Data não disponível'
        return new Date(dateString).toLocaleDateString('pt-BR', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (error) {
        return 'Data inválida'
      }
    },

    getOrderTotal(order) {
      if (!order) return '0.00'
      
      if (order.total !== null && order.total !== undefined && !isNaN(order.total)) {
        return parseFloat(order.total).toFixed(2)
      }
      
      if (order.items && order.items.length > 0) {
        const total = order.items.reduce((sum, item) => {
          const price = parseFloat(item.price) || 0
          const quantity = parseInt(item.quantity) || 0
          return sum + (price * quantity)
        }, 0)
        return total.toFixed(2)
      }
      
      return '0.00'
    }
  },
  
  async mounted() {
    // Se veio com orderId via prop (URL), carregar automaticamente
    if (this.orderId) {
      this.searchOrderId = this.orderId
      await this.loadOrder()
    }
    
    // Tentar carregar o último pedido se existir
    else if (this.userOrders.length > 0) {
      this.currentOrder = this.userOrders[0]
      await this.loadTrackingInfo()
    }
  }
}
</script>

<style scoped>
.order-tracking-page {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.card {
  border: none;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.badge {
  font-size: 0.75em;
  padding: 0.4em 0.6em;
}

.list-group-item {
  border: none;
  border-bottom: 1px solid #eee;
  transition: all 0.3s ease;
}

.list-group-item:hover {
  background-color: #f8f9fa;
}
</style>
[file content end]