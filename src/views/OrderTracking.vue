<template>
  <div class="order-tracking-page">
    <div class="container mt-4">
      <!-- Cabeçalho -->
      <div class="tracking-header text-center mb-5">
        <h1 class="text-primary mb-3">📦 Rastrear Pedido</h1>
        <p class="lead text-muted">Digite o número do pedido para acompanhar a entrega</p>
      </div>

      <!-- Busca de Pedido -->
      <div class="card mb-4">
        <div class="card-body">
          <h5 class="mb-3">Informe o número do pedido</h5>
          <div class="row g-3">
            <div class="col-md-8">
              <input 
                v-model="searchOrderId" 
                type="text" 
                class="form-control form-control-lg" 
                placeholder="Ex: ORD-ABC123XYZ"
                @keyup.enter="loadOrder"
              >
              <div class="form-text">
                O número do pedido começa com "ORD-" seguido de letras e números
              </div>
            </div>
            <div class="col-md-4">
              <button 
                class="btn btn-primary btn-lg w-100" 
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

      <!-- Meus Pedidos Recentes -->
      

      <!-- Pedido Encontrado -->
      <div v-if="currentOrder" class="row">
        <div class="col-lg-8">
          <!-- Informações do Pedido -->
          <div class="card mb-4">
            <div class="card-header bg-primary text-white">
              <h5 class="mb-0">
                <i class="fas fa-receipt me-2"></i>
                Pedido #{{ currentOrder.id }}
              </h5>
            </div>
            <div class="card-body">
              <div class="row">
                <div class="col-md-3">
                  <strong>Data:</strong><br>
                  {{ formatDate(currentOrder.date) }}
                </div>
                <div class="col-md-3">
                  <strong>Total:</strong><br>
                  R$ {{ currentOrder.total.toFixed(2) }}
                </div>
                <div class="col-md-3">
                  <strong>Pagamento:</strong><br>
                  {{ getPaymentMethodLabel(currentOrder.paymentMethod) }}
                </div>
                <div class="col-md-3">
                  <strong>Status:</strong><br>
                  <span :class="statusClass(currentOrder.status)" class="badge">
                    {{ getStatusText(currentOrder.status) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Timeline do Pedido -->
          <div class="card mb-4">
            <div class="card-header bg-light">
              <h5 class="mb-0">
                <i class="fas fa-history me-2"></i>Status do Pedido
              </h5>
            </div>
            <div class="card-body">
              <OrderTimeline :order="currentOrder" :auto-refresh="true" />
            </div>
          </div>

          <!-- Mapa de Rastreamento -->
          <div class="card mb-4">
            <div class="card-header bg-light">
              <h5 class="mb-0">
                <i class="fas fa-map-marker-alt me-2"></i>Localização Atual
              </h5>
            </div>
            <div class="card-body">
              <LiveTrackingMap 
                v-if="trackingInfo"
                :tracking-info="trackingInfo"
                :order-id="currentOrder.id"
              />
              <div v-else class="text-center text-muted py-4">
                <i class="fas fa-truck fa-3x mb-3"></i>
                <p>Buscando informações de localização...</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="col-lg-4">
          <div class="sticky-top" style="top: 20px;">
            <!-- Ações Rápidas -->
            <div class="card mb-4">
              <div class="card-header bg-light">
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
                  class="btn btn-outline-info w-100 mb-2" 
                  @click="currentOrder = null; searchOrderId = '';"
                >
                  <i class="fas fa-search me-2"></i>Buscar Outro Pedido
                </button>
                <router-link to="/orders" class="btn btn-outline-secondary w-100">
                  <i class="fas fa-list me-2"></i>Ver Todos os Pedidos
                </router-link>
              </div>
            </div>

            <!-- Informações de Contato -->
            <div class="card">
              <div class="card-body text-center">
                <i class="fas fa-headset fa-2x text-primary mb-3"></i>
                <h6>Precisa de Ajuda?</h6>
                <p class="small text-muted mb-2">Nossa equipe está aqui para ajudar</p>
                <p class="mb-0">
                  <strong>📞 (81) 99818-9999</strong><br>
                  <strong>✉️ gustavson.adm@gmail.com</strong>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pedido Não Encontrado -->
      <div v-if="searchPerformed && !currentOrder && !loading" class="text-center py-5">
        <i class="fas fa-search fa-4x text-muted mb-3"></i>
        <h4 class="text-muted">Pedido não encontrado</h4>
        <p class="text-muted mb-4">
          Verifique o número do pedido e tente novamente.<br>
          Se você acabou de fazer o pedido, aguarde alguns minutos.
        </p>
        <button class="btn btn-primary" @click="resetSearch">
          Tentar Novamente
        </button>
      </div>

      <!-- Sem Pedidos -->
      <div v-if="userOrders.length === 0 && !searchPerformed && !currentOrder" class="text-center py-5">
        <i class="fas fa-shopping-bag fa-4x text-muted mb-3"></i>
        <h4 class="text-muted">Nenhum pedido encontrado</h4>
        <p class="text-muted mb-4">
          Digite o número do pedido acima ou faça login para ver seus pedidos recentes.
        </p>
        <div class="d-flex justify-content-center gap-2">
          <router-link to="/login" class="btn btn-primary">
            <i class="fas fa-sign-in-alt me-2"></i>Fazer Login
          </router-link>
          <router-link to="/products" class="btn btn-outline-primary">
            <i class="fas fa-shopping-cart me-2"></i>Fazer Compras
          </router-link>
        </div>
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
      searchPerformed: false,
      trackingInfo: null
    }
  },
  computed: {
    ...mapGetters(['getOrderTracking', 'isAuthenticated']),
    
    userOrders() {
      try {
        // Busca pedidos do localStorage
        const savedOrders = JSON.parse(localStorage.getItem('userOrders') || '[]')
        
        // Se o usuário está autenticado, mostra apenas seus pedidos
        if (this.isAuthenticated) {
          const user = JSON.parse(localStorage.getItem('user') || '{}')
          return savedOrders
            .filter(order => order.userId === user.id)
            .slice(0, 10) // Últimos 10 pedidos
            .sort((a, b) => new Date(b.date) - new Date(a.date))
        }
        
        // Se não está autenticado, mostra todos os pedidos do localStorage
        return savedOrders
          .slice(0, 5) // Últimos 5 pedidos
          .sort((a, b) => new Date(b.date) - new Date(a.date))
      } catch (error) {
        console.error('Erro ao carregar pedidos:', error)
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
        // Buscar pedido do localStorage
        const savedOrders = JSON.parse(localStorage.getItem('userOrders') || '[]')
        const order = savedOrders.find(o => 
          o.id.toLowerCase() === this.searchOrderId.trim().toLowerCase()
        )
        
        if (order) {
          this.currentOrder = order
          await this.loadTrackingInfo()
        } else {
          this.currentOrder = null
          alert('Pedido não encontrado. Verifique o número e tente novamente.')
        }
      } catch (error) {
        console.error('Erro ao carregar pedido:', error)
        alert('Erro ao buscar pedido. Tente novamente.')
      } finally {
        this.loading = false
      }
    },
    
    selectOrder(order) {
      this.searchOrderId = order.id
      this.loadOrder()
    },
    
    async loadTrackingInfo() {
      if (!this.currentOrder) return
      
      try {
        this.trackingInfo = await this.fetchRealTimeTracking(this.currentOrder.id)
      } catch (error) {
        console.error('Erro ao carregar rastreamento:', error)
        // CORREÇÃO: Usar error.message em vez de error.response.data
        alert('Erro ao carregar informações de rastreamento: ' + (error.message || 'Erro desconhecido'))
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

    // Adicionar pedidos de exemplo para teste
    addSampleOrders() {
      const sampleOrders = [
        {
          id: 'ORD-ABC123XYZ',
          transactionId: 'TXN123456',
          items: [
            { id: 1, name: 'Paracetamol 500mg', price: 12.90, quantity: 2 },
            { id: 3, name: 'Shampoo Anti-Caspa', price: 24.90, quantity: 1 }
          ],
          total: 50.70,
          subtotal: 50.70,
          status: 'out_for_delivery',
          date: new Date().toISOString(),
          paymentMethod: 'credit_card',
          deliveryType: 'delivery',
          deliveryInfo: {
            street: 'Rua das Flores',
            number: '123',
            neighborhood: 'Centro',
            city: 'Recife',
            state: 'PE',
            zipcode: '50000-000'
          },
          userId: 1
        },
        {
          id: 'ORD-DEF456ABC',
          transactionId: 'TXN789012',
          items: [
            { id: 4, name: 'Vitamina C 1000mg', price: 45.00, quantity: 1 }
          ],
          total: 45.00,
          subtotal: 45.00,
          status: 'delivered',
          date: new Date(Date.now() - 86400000).toISOString(), // 1 dia atrás
          paymentMethod: 'pix',
          deliveryType: 'pickup',
          deliveryInfo: {
            name: 'ClickFarma Centro',
            address: 'Av. Conde da Boa Vista, 500',
            phone: '(81) 3333-3333'
          },
          userId: 1
        }
      ];

      // Salvar pedidos de exemplo no localStorage
      const savedOrders = JSON.parse(localStorage.getItem('userOrders') || '[]');
      sampleOrders.forEach(order => {
        if (!savedOrders.find(o => o.id === order.id)) {
          savedOrders.push(order);
        }
      });
      localStorage.setItem('userOrders', JSON.stringify(savedOrders));
    }
  },
  
  async mounted() {
    // Adicionar pedidos de exemplo se não houver nenhum (apenas para desenvolvimento)
    const savedOrders = JSON.parse(localStorage.getItem('userOrders') || '[]');
    if (savedOrders.length === 0) {
      this.addSampleOrders();
    }

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

.sticky-top {
  position: sticky;
  z-index: 100;
}

.list-group-item {
  border: none;
  border-bottom: 1px solid #eee;
  transition: all 0.3s ease;
}

.list-group-item:hover {
  background-color: #f8f9fa;
  transform: translateX(5px);
}

.list-group-item:last-child {
  border-bottom: none;
}

.badge {
  font-size: 0.75em;
  padding: 0.5em 0.75em;
}

.form-control-lg {
  padding: 0.75rem 1rem;
  font-size: 1.1rem;
}

.btn-lg {
  padding: 0.75rem 1.5rem;
  font-size: 1.1rem;
}
</style>