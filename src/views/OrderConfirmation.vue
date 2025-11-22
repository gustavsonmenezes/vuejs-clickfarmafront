<template>
  <div class="order-confirmation-page">
    <div class="container mt-4">
      <!-- Cabeçalho de Confirmação -->
      <div class="confirmation-header text-center mb-5">
        <div class="confirmation-icon mb-3">
          <i class="fas fa-check-circle fa-5x text-success"></i>
        </div>
        <h1 class="text-primary mb-3">Pedido Confirmado!</h1>
        <p class="lead text-muted">Obrigado por comprar na ClickFarma. Seu pedido foi recebido com sucesso.</p>
        <p class="order-number">
          <strong>Número do Pedido:</strong> 
          <span class="text-primary">#{{ order.id }}</span>
        </p>
      </div>

      <div class="row">
        <!-- Detalhes do Pedido -->
        <div class="col-lg-8">
          <!-- Detalhes do Pedido -->
          <div class="card mb-4">
            <div class="card-header bg-light">
              <h5 class="mb-0">
                <i class="fas fa-list me-2"></i>Detalhes do Pedido
              </h5>
            </div>
            <div class="card-body">
              <div v-for="item in order.items" :key="item.id" class="d-flex justify-content-between align-items-center mb-3 pb-3 border-bottom">
                <div class="d-flex align-items-center">
                  <img :src="getItemImage(item)" :alt="item.name" class="rounded me-3" width="60" height="60">
                  <div>
                    <h6 class="mb-1">{{ item.name }}</h6>
                    <small class="text-muted">Quantidade: {{ item.quantity }}</small>
                  </div>
                </div>
                <span class="fw-bold">R$ {{ (item.price * item.quantity).toFixed(2) }}</span>
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
              <OrderTimeline :order="order" />
            </div>
          </div>

          <!-- Informações de Entrega -->
          <div class="card mb-4">
            <div class="card-header bg-light">
              <h5 class="mb-0">
                <i class="fas fa-truck me-2"></i>Informações de Entrega
              </h5>
            </div>
            <div class="card-body">
              <div v-if="order.deliveryType === 'delivery' && order.deliveryInfo">
                <h6>Entrega em Domicílio</h6>
                <p class="mb-1">
                  <strong>Endereço:</strong><br>
                  {{ order.deliveryInfo.street }}, {{ order.deliveryInfo.number }}<br>
                  {{ order.deliveryInfo.neighborhood }}<br>
                  {{ order.deliveryInfo.city }} - {{ order.deliveryInfo.state }}<br>
                  CEP: {{ order.deliveryInfo.zipcode || order.deliveryInfo.zip }}
                  <span v-if="order.deliveryInfo.complement">
                    <br>Complemento: {{ order.deliveryInfo.complement }}
                  </span>
                </p>
                <p class="mb-0">
                  <strong>Previsão de Entrega:</strong> 
                  {{ calculateDeliveryDate() }}
                </p>
              </div>
              <div v-else-if="order.deliveryType === 'pickup' && order.deliveryInfo">
                <h6>Retirada na Loja</h6>
                <p class="mb-1">
                  <strong>Loja:</strong> {{ order.deliveryInfo.name || 'ClickFarma Centro' }}<br>
                  <strong>Endereço:</strong> {{ order.deliveryInfo.address || order.deliveryInfo.street }}<br>
                  <strong>Telefone:</strong> {{ order.deliveryInfo.phone || '(11) 3333-3333' }}
                </p>
                <p class="mb-0">
                  <strong>Horário de Funcionamento:</strong> {{ order.deliveryInfo.hours || 'Segunda a Sexta: 8h às 18h | Sábado: 8h às 12h' }}
                </p>
              </div>
              <div v-else>
                <p class="text-muted">Informações de entrega não disponíveis.</p>
              </div>
            </div>
          </div>

          <!-- Informações de Pagamento -->
          <div class="card mb-4">
            <div class="card-header bg-light">
              <h5 class="mb-0">
                <i class="fas fa-credit-card me-2"></i>Informações de Pagamento
              </h5>
            </div>
            <div class="card-body">
              <p><strong>Método de Pagamento:</strong> {{ getPaymentMethodLabel() }}</p>
              <p v-if="order.paymentMethod === 'credit_card' && order.cardData">
                <strong>Cartão:</strong> {{ formatCardNumber(order.cardData.number) }}<br>
                <strong>Parcelas:</strong> {{ order.cardData.installments }}x
              </p>
              <p v-if="order.paymentMethod === 'pix'">
                <strong>Status PIX:</strong> <span class="text-success">Pagamento aprovado</span>
              </p>
            </div>
          </div>
        </div>

        <!-- Resumo e Ações -->
        <div class="col-lg-4">
          <div class="sticky-top" style="top: 20px;">
            <!-- Resumo do Pedido -->
            <div class="card mb-4">
              <div class="card-header bg-light">
                <h5 class="mb-0">
                  <i class="fas fa-receipt me-2"></i>Resumo do Pedido
                </h5>
              </div>
              <div class="card-body">
                <div class="d-flex justify-content-between mb-2">
                  <span>Subtotal:</span>
                  <span>R$ {{ order.subtotal.toFixed(2) }}</span>
                </div>
                <div class="d-flex justify-content-between mb-2">
                  <span>Entrega:</span>
                  <span>{{ getDeliveryCostLabel() }}</span>
                </div>
                <div v-if="order.paymentMethod === 'pix'" class="d-flex justify-content-between mb-2 text-success">
                  <span>Desconto PIX (5%):</span>
                  <span>- R$ {{ (order.subtotal * 0.05).toFixed(2) }}</span>
                </div>
                <hr>
                <div class="d-flex justify-content-between fw-bold fs-5">
                  <span>Total:</span>
                  <span>R$ {{ order.total.toFixed(2) }}</span>
                </div>
              </div>
            </div>

            <!-- Ações -->
            <div class="card">
              <div class="card-body">
                <h6 class="mb-3">Próximos Passos</h6>
                <button class="btn btn-primary w-100 mb-2" @click="continueShopping">
                  <i class="fas fa-shopping-bag me-2"></i>Continuar Comprando
                </button>
                <router-link to="/orders" class="btn btn-outline-primary w-100 mb-2">
                  <i class="fas fa-list me-2"></i>Ver Meus Pedidos
                </router-link>
                <button class="btn btn-outline-secondary w-100" @click="printOrder">
                  <i class="fas fa-print me-2"></i>Imprimir Pedido
                </button>
              </div>
            </div>

            <!-- Suporte -->
            <div class="card mt-4">
              <div class="card-body text-center">
                <i class="fas fa-headset fa-2x text-primary mb-3"></i>
                <h6>Precisa de Ajuda?</h6>
                <p class="small text-muted mb-2">Estamos aqui para ajudar com seu pedido</p>
                <p class="mb-0">
                  <strong>📞 (81) 99818-9999</strong><br>
                  <strong>✉️ gustavson.adm@gmail.com</strong>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import OrderTimeline from '@/components/orders/OrderTimeline.vue' 

export default {
  name: 'OrderConfirmation',
  components: {
    OrderTimeline
  },
  computed: {
    ...mapState(['lastOrder'])
  },
  data() {
    return {
      order: this.getRealOrderData()
    }
  },
  mounted() {
  console.log('📦 Dados do último pedido (Vuex):', this.lastOrder)
  console.log('🛒 Carrinho atual:', JSON.parse(localStorage.getItem('cart') || '[]'))
  console.log('💳 Dados do checkout:', JSON.parse(localStorage.getItem('checkoutData') || '{}'))
  console.log('💰 Dados do pagamento:', JSON.parse(localStorage.getItem('paymentData') || '{}'))
  
  this.clearTemporaryData()
  this.clearBackupData() // ← ADICIONE ESTA LINHA AQUI
  this.saveOrderToStorage(this.order)
  
  window.scrollTo(0, 0)
},
  methods: {
    saveOrderToStorage(order) {
      // VALIDAÇÃO FORTE - só salva se for pedido REAL
      if (!order || 
          !order.id || 
          !order.items || 
          !Array.isArray(order.items) || 
          order.items.length === 0 ||
          !order.date ||
          order.total === undefined ||
          order.total === null) {
        console.error('❌ PEDIDO INVÁLIDO - Não será salvo:', order)
        return
      }
      
      // Garante que todos os campos necessários existam
      const validOrder = {
        id: order.id,
        date: order.date,
        status: order.status || 'confirmed',
        total: order.total,
        subtotal: order.subtotal || order.total,
        paymentMethod: order.paymentMethod || 'credit_card',
        deliveryType: order.deliveryType || 'delivery',
        deliveryInfo: order.deliveryInfo || {},
        items: order.items.map(item => ({
          id: item.id,
          name: item.name || 'Produto',
          price: item.price || 0,
          quantity: item.quantity || 1,
          image: item.image
        }))
      }

      const savedOrders = JSON.parse(localStorage.getItem('userOrders') || '[]')
      const filteredOrders = savedOrders.filter(o => o.id !== validOrder.id)
      filteredOrders.unshift(validOrder)
      localStorage.setItem('userOrders', JSON.stringify(filteredOrders))
      console.log('💾 PEDIDO VÁLIDO SALVO:', validOrder.id)
    },

    // NO OrderConfirmation.vue - método getRealOrderData
getRealOrderData() {
  // 1. Primeiro tenta usar o lastOrder do Vuex
  if (this.lastOrder && this.lastOrder.items && this.lastOrder.items.length > 0) {
    console.log('✅ Usando dados do Vuex (lastOrder)');
    return this.lastOrder;
  }

  // 2. Se não tem no Vuex, tenta montar com dados salvos
  console.log('🔄 Montando pedido com dados salvos');
  
  const cartBackup = JSON.parse(localStorage.getItem('cart_backup') || '[]');
  const checkoutData = JSON.parse(localStorage.getItem('checkoutData') || '{}');
  const paymentData = JSON.parse(localStorage.getItem('paymentData') || '{}');

  console.log('📋 Backup do carrinho:', cartBackup);
  console.log('🚚 Dados de entrega:', checkoutData);
  console.log('💳 Dados de pagamento:', paymentData);

  // Se não tem itens, usa dados de exemplo
  if (cartBackup.length === 0) {
    console.warn('⚠️ Sem dados válidos! Mostrando dados de exemplo');
    return this.getSampleOrderData();
  }

  const subtotal = cartBackup.reduce((total, item) => total + (item.price * item.quantity), 0);
  const deliveryCost = paymentData.deliveryCost || this.calculateDeliveryCost(subtotal, checkoutData.deliveryType || checkoutData.deliveryOption);
  
  let total = subtotal + deliveryCost;
  
  // Aplicar desconto PIX se necessário
  if (paymentData.paymentMethod === 'pix' || paymentData.method === 'pix') {
    total *= 0.95; // 5% de desconto
  }

  const orderData = {
    id: paymentData.transactionId || 'ORD-' + Math.random().toString(36).substr(2, 9).toUpperCase(),
    subtotal: subtotal,
    total: total,
    paymentMethod: paymentData.paymentMethod || paymentData.method || 'credit_card',
    cardData: paymentData.cardData || null,
    status: 'confirmed',
    deliveryType: checkoutData.deliveryType || checkoutData.deliveryOption || 'delivery',
    deliveryInfo: checkoutData.deliveryInfo || checkoutData.selectedAddress || checkoutData.selectedStore || {},
    items: [...cartBackup], // Usa o backup do carrinho
    date: new Date().toISOString()
  };

  console.log('🎯 Pedido final montado:', orderData);
  return orderData;
},

    getSampleOrderData() {
      
      return {
        id: 'ORD-' + Math.random().toString(36).substr(2, 9).toUpperCase(),
        subtotal: 0,
        total: 0,
        paymentMethod: 'credit_card',
        status: 'confirmed',
        deliveryType: 'delivery',
        items: []
      }
    },

    calculateDeliveryCost(subtotal, deliveryType) {
      if (deliveryType === 'pickup') return 0
      if (subtotal >= 300) return 0
      if (subtotal < 100) return 10.00
      return 0
    },

    getDeliveryCostLabel() {
      if (this.order.deliveryType === 'pickup') return 'Grátis'
      const deliveryCost = this.calculateDeliveryCost(this.order.subtotal, this.order.deliveryType)
      return deliveryCost > 0 ? `R$ ${deliveryCost.toFixed(2)}` : 'Grátis'
    },

    getPaymentMethodLabel() {
      const methods = {
        'credit_card': 'Cartão de Crédito',
        'debit_card': 'Cartão de Débito',
        'pix': 'PIX'
      }
      return methods[this.order.paymentMethod] || this.order.paymentMethod
    },

    formatCardNumber(cardNumber) {
      if (!cardNumber) return ''
      const last4 = cardNumber.slice(-4)
      return `**** **** **** ${last4}`
    },

    calculateDeliveryDate() {
      const today = new Date()
      const deliveryDate = new Date(today)
      deliveryDate.setDate(today.getDate() + 3) // 3 dias úteis
      return deliveryDate.toLocaleDateString('pt-BR')
    },

    getItemImage(item) {
      return item.image || 'https://via.placeholder.com/60x60?text=Produto'
    },

    continueShopping() {
      this.$router.push('/products')
    },

    printOrder() {
      window.print()
    },

    clearTemporaryData() {
      
      localStorage.removeItem('cart')
      localStorage.removeItem('checkoutData')
      localStorage.removeItem('paymentData')
      
      
      if (this.$store && this.$store.commit) {
        this.$store.commit('CLEAR_CART')
      }
      
      console.log('🧹 Dados temporários limpos')
    },

    clearBackupData() {
    // Remove apenas os backups, mantém o lastOrder no Vuex
    localStorage.removeItem('cart_backup');
    localStorage.removeItem('checkoutData');
    localStorage.removeItem('paymentData');
    console.log('🧹 Dados de backup limpos');
  },

  }
}
</script>

<style scoped>
.order-confirmation-page {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.confirmation-icon {
  animation: bounce 1s ease-in-out;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-20px);
  }
  60% {
    transform: translateY(-10px);
  }
}

.order-number {
  font-size: 1.2rem;
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  display: inline-block;
}

.sticky-top {
  position: sticky;
  z-index: 100;
}

@media print {
  .order-confirmation-page {
    background-color: white;
  }
  
  .btn, .sticky-top {
    display: none !important;
  }
}
</style>