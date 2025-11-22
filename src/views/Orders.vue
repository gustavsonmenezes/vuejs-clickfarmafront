<template>
  <div class="orders-page">
    <div class="container mt-4">
      <!-- Cabeçalho -->
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="mb-0">📦 Meus Pedidos</h2>
        <div class="d-flex gap-2">
          <button class="btn btn-outline-primary" @click="refreshOrders">
            <i class="fas fa-sync-alt" :class="{ 'fa-spin': loading }"></i>
          </button>
          <button class="btn btn-outline-danger" @click="clearAllOrders" title="Limpar todos os pedidos">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>

      <!-- Estado Vazio -->
      <div v-if="!hasOrders && !loading" class="text-center py-5">
        <div class="empty-state">
          <i class="fas fa-box-open fa-5x text-muted mb-4"></i>
          <h3 class="mb-3">Nenhum pedido realizado</h3>
          <p class="text-muted mb-4">
            Você ainda não fez nenhuma compra. Que tal explorar nossos produtos?
          </p>
          <router-link to="/products" class="btn btn-primary btn-lg">
            <i class="fas fa-shopping-bag me-2"></i>Explorar Produtos
          </router-link>
        </div>
      </div>

      <!-- Lista de Pedidos -->
      <div v-else>
        <div class="alert alert-info d-flex justify-content-between align-items-center">
          <div>
            <strong>{{ validOrders.length }}</strong> pedido(s) encontrado(s)
          </div>
          <button v-if="hasOrders" class="btn btn-sm btn-outline-info" @click="clearAllOrders">
            <i class="fas fa-trash me-1"></i>Limpar Tudo
          </button>
        </div>

        <div class="row">
          <div class="col-lg-8">
            <!-- Loading -->
            <div v-if="loading" class="text-center py-4">
              <div class="spinner-border text-primary"></div>
              <p class="mt-2 text-muted">Carregando pedidos...</p>
            </div>

            <!-- Pedidos -->
            <div v-for="order in validOrders" :key="order.id" class="card mb-4">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                  <div>
                    <h5 class="card-title mb-1">Pedido #{{ order.id }}</h5>
                    <p class="text-muted small mb-0">
                      <i class="fas fa-calendar me-1"></i>
                      {{ formatDate(order.date) }}
                    </p>
                    <p class="text-muted small mb-0">
                      <i class="fas fa-shopping-bag me-1"></i>
                      {{ order.items.length }} item(ns)
                    </p>
                  </div>
                  <div class="text-end">
                    <span :class="statusClass(order.status)" class="badge mb-2">
                      {{ statusText(order.status) }}
                    </span>
                    <p class="h4 text-primary mb-0">R$ {{ getOrderTotal(order) }}</p>
                  </div>
                </div>

                <!-- Itens do Pedido -->
                <div class="mb-3">
                  <div v-for="item in order.items" :key="item.id" class="d-flex justify-content-between align-items-center py-2 border-bottom">
                    <div class="d-flex align-items-center">
                      <img :src="getItemImage(item)" :alt="item.name" class="rounded me-3" width="40" height="40">
                      <div>
                        <h6 class="mb-0 small">{{ item.name }}</h6>
                        <small class="text-muted">Qtd: {{ item.quantity }}</small>
                      </div>
                    </div>
                    <span class="fw-bold">R$ {{ (item.price * item.quantity).toFixed(2) }}</span>
                  </div>
                </div>

                <!-- Ações -->
                <div class="d-flex gap-2">
                  <button class="btn btn-outline-primary btn-sm" @click="viewOrderDetails(order)">
                    <i class="fas fa-eye me-1"></i>Detalhes
                  </button>
                  
                  <button 
                    v-if="order.status === 'confirmed'" 
                    class="btn btn-outline-danger btn-sm" 
                    @click="cancelOrder(order)"
                  >
                    <i class="fas fa-times me-1"></i>Cancelar
                  </button>
                  <button class="btn btn-outline-secondary btn-sm" @click="removeOrder(order.id)">
                    <i class="fas fa-trash me-1"></i>Remover
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Sidebar -->
          <div class="col-lg-4">
            <div class="card">
              <div class="card-header">
                <h6 class="mb-0">📊 Resumo</h6>
              </div>
              <div class="card-body">
                <div class="small">
                  <p><strong>Total de pedidos:</strong> {{ validOrders.length }}</p>
                  <p><strong>Último pedido:</strong> {{ lastOrderDate }}</p>
                  <p><strong>Valor médio:</strong> R$ {{ averageOrderValue }}</p>
                  <p><strong>Total gasto:</strong> R$ {{ totalSpent }}</p>
                </div>
                <hr>
                <div class="text-center">
                  <button class="btn btn-warning btn-sm w-100" @click="clearAllOrders">
                    <i class="fas fa-broom me-1"></i>Limpar Todos os Pedidos
                  </button>
                  <small class="text-muted d-block mt-1">Remove todos os pedidos da lista</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import OrderTimeline from '@/components/orders/OrderTimeline.vue'
import LiveTrackingMap from '@/components/orders/LiveTrackingMap.vue'

export default {
  name: 'Orders',
  components: {
    OrderTimeline,
    LiveTrackingMap
  },
  data() {
    return {
      orders: [],
      loading: false
    }
  },
  computed: {
    hasOrders() {
      return this.validOrders.length > 0
    },
    
    validOrders() {
      return this.orders.filter(order => this.isValidOrder(order))
    },
    
    lastOrderDate() {
      if (this.validOrders.length === 0) return 'N/A'
      return this.formatDate(this.validOrders[0].date)
    },
    
    averageOrderValue() {
      if (this.validOrders.length === 0) return '0.00'
      const total = this.validOrders.reduce((sum, order) => {
        return sum + this.calculateOrderTotal(order)
      }, 0)
      return (total / this.validOrders.length).toFixed(2)
    },
    
    totalSpent() {
      const total = this.validOrders.reduce((sum, order) => {
        return sum + this.calculateOrderTotal(order)
      }, 0)
      return total.toFixed(2)
    }
  },
  methods: {
    isValidOrder(order) {
      return order && 
             order.id && 
             order.items && 
             Array.isArray(order.items) && 
             order.items.length > 0 &&
             order.date &&
             order.total !== undefined &&
             order.total !== null
    },
    
    formatDate(dateString) {
      if (!dateString) return 'Data não disponível'
      try {
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
    
    calculateOrderTotal(order) {
      if (order.total !== null && order.total !== undefined && !isNaN(order.total)) {
        return parseFloat(order.total)
      }
      
      if (order.items && order.items.length > 0) {
        return order.items.reduce((sum, item) => {
          const price = parseFloat(item.price) || 0
          const quantity = parseInt(item.quantity) || 0
          return sum + (price * quantity)
        }, 0)
      }
      
      return 0
    },
    
    getOrderTotal(order) {
      return this.calculateOrderTotal(order).toFixed(2)
    },
    
    getItemImage(item) {
      return item.image || 'https://via.placeholder.com/40x40?text=Produto'
    },
    
    async refreshOrders() {
      this.loading = true
      try {
        await this.loadOrders()
        setTimeout(() => {
          this.loading = false
        }, 500)
      } catch (error) {
        console.error('Erro ao atualizar pedidos:', error)
        this.loading = false
      }
    },
    
    viewOrderDetails(order) {
      alert(`Detalhes do pedido #${order.id}\nStatus: ${this.statusText(order.status)}\nItens: ${order.items.length}\nTotal: R$ ${this.getOrderTotal(order)}`)
    },
    
    trackOrder(order) {
      alert(`Rastreamento do pedido #${order.id}\nEm breve você poderá acompanhar a entrega aqui!`)
    },
    
    cancelOrder(order) {
      if (confirm(`Cancelar pedido #${order.id}?`)) {
        order.status = 'cancelled'
        this.saveOrdersToStorage()
        alert('Pedido cancelado com sucesso!')
      }
    },
    
    removeOrder(orderId) {
      if (confirm('Remover este pedido da lista?')) {
        this.orders = this.orders.filter(order => order.id !== orderId)
        this.saveOrdersToStorage()
        alert('Pedido removido!')
      }
    },
    
    clearAllOrders() {
      if (confirm('TEM CERTEZA que deseja remover TODOS os pedidos?\nEsta ação não pode ser desfeita.')) {
        this.orders = []
        this.saveOrdersToStorage()
        alert('Todos os pedidos foram removidos!')
      }
    },
    
    loadOrders() {
      console.log('🔄 Buscando pedidos...')
      
      const savedOrders = localStorage.getItem('userOrders')
      
      if (!savedOrders) {
        console.log('✅ Nenhum pedido encontrado - estado inicial limpo')
        this.orders = []
        return
      }
      
      try {
        const parsedOrders = JSON.parse(savedOrders)
        console.log('📦 Pedidos encontrados:', parsedOrders.length)
        
        this.orders = parsedOrders.filter(order => this.isValidOrder(order))
        console.log('✅ Pedidos válidos:', this.orders.length)
        
        if (this.orders.length !== parsedOrders.length) {
          console.log('🧹 Pedidos inválidos removidos')
          this.saveOrdersToStorage()
        }
        
      } catch (error) {
        console.error('❌ Erro ao carregar pedidos:', error)
        this.orders = []
        localStorage.removeItem('userOrders')
      }
    },
    
    saveOrdersToStorage() {
      if (this.orders.length > 0) {
        localStorage.setItem('userOrders', JSON.stringify(this.orders))
        console.log('💾 Pedidos salvos:', this.orders.length)
      } else {
        localStorage.removeItem('userOrders')
        console.log('🗑️ localStorage limpo')
      }
    }
  },
  
  mounted() {
    this.loadOrders()
    console.log('🎯 Estado atual:')
    console.log('- Pedidos carregados:', this.orders.length)
    console.log('- Pedidos válidos:', this.validOrders.length)
  }
}
</script>

<style scoped>
.orders-page {
  min-height: 80vh;
  background-color: #f8f9fa;
}

.empty-state {
  padding: 3rem 1rem;
}

.card {
  border: none;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-2px);
}
</style>