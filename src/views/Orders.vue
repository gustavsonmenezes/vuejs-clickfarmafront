[file name]: Orders.vue
[file content begin]
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
          <button 
            v-if="hasOrders" 
            class="btn btn-outline-danger" 
            @click="clearAllOrders" 
            title="Limpar todos os pedidos"
          >
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

            <!-- Pedidos Reais do Usuário -->
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
                    <p class="text-muted small mb-0">
                      <i class="fas fa-truck me-1"></i>
                      {{ getDeliveryTypeText(order.deliveryType) }}
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
                        <small class="text-muted">Qtd: {{ item.quantity }} × R$ {{ item.price.toFixed(2) }}</small>
                      </div>
                    </div>
                    <span class="fw-bold">R$ {{ (item.price * item.quantity).toFixed(2) }}</span>
                  </div>
                </div>

                <!-- Informações de Entrega -->
                <div class="delivery-info mb-3 p-3 bg-light rounded">
                  <h6 class="mb-2">
                    <i class="fas fa-map-marker-alt me-1"></i>
                    {{ order.deliveryType === 'delivery' ? 'Entrega em:' : 'Retirada em:' }}
                  </h6>
                  <p class="small mb-1" v-if="order.deliveryType === 'delivery' && order.deliveryInfo">
                    {{ order.deliveryInfo.street }}, {{ order.deliveryInfo.number }} - 
                    {{ order.deliveryInfo.neighborhood }}, {{ order.deliveryInfo.city }}
                  </p>
                  <p class="small mb-1" v-else-if="order.deliveryType === 'pickup' && order.deliveryInfo">
                    {{ order.deliveryInfo.name || 'ClickFarma' }} - 
                    {{ order.deliveryInfo.address || order.deliveryInfo.street }}
                  </p>
                </div>

                <!-- Ações -->
                <div class="d-flex gap-2">
                  <button class="btn btn-outline-primary btn-sm" @click="viewOrderDetails(order)">
                    <i class="fas fa-eye me-1"></i>Detalhes
                  </button>
                  
                  <button 
                    class="btn btn-outline-success btn-sm" 
                    @click="trackOrder(order)"
                  >
                    <i class="fas fa-shipping-fast me-1"></i>Rastrear
                  </button>
                  
                  <button 
                    v-if="order.status === 'confirmed' || order.status === 'processing'" 
                    class="btn btn-outline-danger btn-sm" 
                    @click="cancelOrder(order)"
                  >
                    <i class="fas fa-times me-1"></i>Cancelar
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Sidebar de Resumo -->
          <div class="col-lg-4">
            <div class="card">
              <div class="card-header">
                <h6 class="mb-0">📊 Resumo dos Pedidos</h6>
              </div>
              <div class="card-body">
                <div class="small">
                  <p><strong>Total de pedidos:</strong> {{ validOrders.length }}</p>
                  <p><strong>Último pedido:</strong> {{ lastOrderDate }}</p>
                  <p><strong>Valor médio:</strong> R$ {{ averageOrderValue }}</p>
                  <p><strong>Total gasto:</strong> R$ {{ totalSpent }}</p>
                  
                  <!-- Status dos Pedidos -->
                  <div class="mt-3">
                    <h6 class="mb-2">Status dos Pedidos:</h6>
                    <div v-for="status in orderStatusCount" :key="status.type" class="d-flex justify-content-between small mb-1">
                      <span>{{ status.label }}:</span>
                      <span class="badge" :class="statusClass(status.type)">{{ status.count }}</span>
                    </div>
                  </div>
                </div>
                
                <hr>
                
                <!-- Ações Rápidas -->
                <div class="text-center">
                  <router-link to="/products" class="btn btn-primary btn-sm w-100 mb-2">
                    <i class="fas fa-plus me-1"></i>Fazer Novo Pedido
                  </router-link>
                  <button 
                    v-if="hasOrders" 
                    class="btn btn-warning btn-sm w-100" 
                    @click="clearAllOrders"
                  >
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
export default {
  name: 'Orders',
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
      // Filtra apenas pedidos válidos e ordena por data (mais recente primeiro)
      return this.orders
        .filter(order => this.isValidOrder(order))
        .sort((a, b) => new Date(b.date) - new Date(a.date))
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
    },
    
    orderStatusCount() {
      const statusCount = {}
      
      this.validOrders.forEach(order => {
        if (!statusCount[order.status]) {
          statusCount[order.status] = 0
        }
        statusCount[order.status]++
      })
      
      return Object.keys(statusCount).map(status => ({
        type: status,
        label: this.statusText(status),
        count: statusCount[status]
      }))
    }
  },
  methods: {
    isValidOrder(order) {
      // VALIDAÇÃO RIGOROSA - apenas pedidos reais
      return order && 
             order.id && 
             order.id.startsWith('ORD-') && // Deve começar com ORD-
             order.items && 
             Array.isArray(order.items) && 
             order.items.length > 0 &&
             order.date &&
             order.total !== undefined &&
             order.total !== null &&
             order.total > 0 && // Total deve ser maior que zero
             order.paymentMethod && // Deve ter método de pagamento
             order.deliveryType // Deve ter tipo de entrega
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
        'out_for_delivery': 'bg-warning text-dark',
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
        'out_for_delivery': 'Saiu para Entrega',
        'delivered': 'Entregue',
        'cancelled': 'Cancelado'
      }
      return statusTexts[status] || status
    },
    
    getDeliveryTypeText(deliveryType) {
      return deliveryType === 'delivery' ? 'Entrega' : 'Retirada'
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
      // Navega para a página de detalhes do pedido
      this.$router.push(`/tracking/${order.id}`)
    },
    
    trackOrder(order) {
      // Navega para a página de rastreamento
      this.$router.push(`/tracking/${order.id}`)
    },
    
    cancelOrder(order) {
      if (confirm(`Tem certeza que deseja cancelar o pedido #${order.id}?`)) {
        order.status = 'cancelled'
        this.saveOrdersToStorage()
        alert('Pedido cancelado com sucesso!')
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
      console.log('🔄 Buscando pedidos reais do usuário...')
      
      const savedOrders = localStorage.getItem('userOrders')
      
      if (!savedOrders) {
        console.log('✅ Nenhum pedido encontrado - estado inicial limpo')
        this.orders = []
        return
      }
      
      try {
        const parsedOrders = JSON.parse(savedOrders)
        console.log('📦 Pedidos brutos encontrados:', parsedOrders.length)
        
        // FILTRAGEM RIGOROSA - apenas pedidos válidos
        this.orders = parsedOrders.filter(order => this.isValidOrder(order))
        console.log('✅ Pedidos válidos após filtro:', this.orders.length)
        
        if (this.orders.length !== parsedOrders.length) {
          console.log('🧹 Pedidos inválidos removidos:', parsedOrders.length - this.orders.length)
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
        console.log('💾 Pedidos válidos salvos:', this.orders.length)
      } else {
        localStorage.removeItem('userOrders')
        console.log('🗑️ localStorage limpo - nenhum pedido válido')
      }
    }
  },
  
  mounted() {
    this.loadOrders()
    console.log('🎯 Estado atual dos pedidos:')
    console.log('- Pedidos carregados:', this.orders.length)
    console.log('- Pedidos válidos:', this.validOrders.length)
    
    // Log detalhado dos pedidos
    this.validOrders.forEach((order, index) => {
      console.log(`📦 Pedido ${index + 1}:`, {
        id: order.id,
        status: order.status,
        total: order.total,
        items: order.items.length,
        date: order.date
      })
    })
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

.delivery-info {
  border-left: 4px solid #007bff;
}

.badge {
  font-size: 0.7em;
  padding: 0.4em 0.6em;
}
</style>
[file content end]