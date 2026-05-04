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
                  <img :src="getItemImage(item)" :alt="item.name || item.nome" class="rounded me-3" width="60" height="60">
                  <div>
                    <h6 class="mb-1">{{ item.name || item.nome }}</h6>
                    <small class="text-muted">Quantidade: {{ item.quantity }}</small>
                  </div>
                </div>
                <span class="fw-bold">R$ {{ ((item.price || item.preco || 0) * item.quantity).toFixed(2) }}</span>
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
              <OrderTimeline :status="order.status" />
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
                  {{ order.deliveryInfo.address }}<br>
                  {{ order.deliveryInfo.city }} - {{ order.deliveryInfo.state }}<br>
                  CEP: {{ order.deliveryInfo.zip }}
                  <span v-if="order.deliveryInfo.number">, Nº {{ order.deliveryInfo.number }}</span>
                </p>
                <p class="mb-0">
                  <strong>Previsão de Entrega:</strong> 
                  {{ calculateDeliveryDate() }}
                </p>
              </div>
              <div v-else-if="order.deliveryType === 'pickup'">
                <h6>Retirada na Loja</h6>
                <p class="mb-1">
                  <strong>Loja:</strong> ClickFarma Centro<br>
                  <strong>Endereço:</strong> Rua das Farmácias, 123 - Centro, São Paulo - SP<br>
                  <strong>Telefone:</strong> (11) 3333-3333
                </p>
                <p class="mb-0">
                  <strong>Horário de Funcionamento:</strong> Segunda a Sexta: 8h às 18h | Sábado: 8h às 12h
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
import OrderTimeline from '@/components/OrderTimeline.vue'

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
    
    // Limpar dados temporários após confirmação
    this.clearTemporaryData()
    
    // Rolar para o topo
    window.scrollTo(0, 0)
  },
  methods: {
    getRealOrderData() {
      // 1. Primeiro tenta pegar do Vuex (se veio do processamento de pagamento)
      if (this.lastOrder) {
        console.log('✅ Usando dados do Vuex (lastOrder)')
        return this.lastOrder
      }

      // 2. Se não tem no Vuex, monta com dados reais do localStorage
      console.log('🔄 Montando pedido com dados reais do localStorage')
      
      const cart = JSON.parse(localStorage.getItem('cart') || '[]')
      const checkoutData = JSON.parse(localStorage.getItem('checkoutData') || '{}')
      const paymentData = JSON.parse(localStorage.getItem('paymentData') || '{}')

      console.log('📋 Itens do carrinho:', cart)
      console.log('🚚 Dados de entrega:', checkoutData)
      console.log('💳 Dados de pagamento:', paymentData)

      if (cart.length === 0) {
        console.warn('⚠️ Carrinho vazio! Mostrando dados de exemplo')
        return this.getSampleOrderData()
      }

      const subtotal = cart.reduce((total, item) => total + (item.price * item.quantity), 0)
      const deliveryCost = this.calculateDeliveryCost(subtotal, checkoutData.deliveryType)
      
      let total = subtotal + deliveryCost
      
      // Aplicar desconto PIX se aplicável
      if (paymentData.method === 'pix') {
        total *= 0.95 // 5% de desconto
      }

      const orderData = {
        id: 'ORD-' + Math.random().toString(36).substr(2, 9).toUpperCase(),
        subtotal: subtotal,
        total: total,
        paymentMethod: paymentData.method || checkoutData.paymentMethod || 'credit_card',
        cardData: paymentData.cardData || null,
        status: 'confirmed',
        deliveryType: checkoutData.deliveryType || 'delivery',
        deliveryInfo: checkoutData.deliveryInfo || {},
        items: [...cart] // Cópia dos itens do carrinho real
      }

      console.log('🎯 Pedido final montado:', orderData)
      return orderData
    },

    getSampleOrderData() {
      // Apenas como fallback se não houver dados reais
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
      // Remove apenas dados temporários, mantém o lastOrder se existir
      localStorage.removeItem('cart')
      localStorage.removeItem('checkoutData')
      localStorage.removeItem('paymentData')
      
      // Limpa o carrinho no Vuex também
      if (this.$store && this.$store.commit) {
        this.$store.commit('CLEAR_CART')
      }
      
      console.log('🧹 Dados temporários limpos')
    }
  }
}
</script>

<style scoped>
/* Seus estilos existentes permanecem os mesmos */
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