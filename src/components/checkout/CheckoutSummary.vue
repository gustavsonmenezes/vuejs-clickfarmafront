<template>
  <div class="checkout-summary">
    <div class="card">
      <div class="card-header bg-light">
        <h5 class="mb-0">
          <i class="fas fa-receipt me-2"></i>Resumo do Pedido
        </h5>
      </div>
      <div class="card-body">
        <!-- Itens do Carrinho -->
        <div class="cart-items mb-3">
          <h6 class="mb-3">Itens ({{ cartItemsCount }})</h6>
          <div v-for="item in cart" :key="item.id" class="cart-item mb-2">
            <div class="d-flex justify-content-between align-items-start">
              <div class="item-info">
                <span class="item-name">{{ item.name }}</span>
                <small class="text-muted d-block">Qtd: {{ item.quantity }}</small>
              </div>
              <span class="item-price">R$ {{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <hr>

        <!-- Entrega -->
        <div class="delivery-info mb-3">
          <h6 class="mb-2">Entrega</h6>
          <div v-if="deliveryOption === 'delivery' && selectedAddress">
            <small class="text-muted">
              <i class="fas fa-truck me-1"></i>Entrega em Casa<br>
              {{ selectedAddress.street }}, {{ selectedAddress.number }}<br>
              {{ selectedAddress.neighborhood }} - {{ selectedAddress.city }}
            </small>
          </div>
          <div v-else-if="deliveryOption === 'pickup' && selectedStore">
            <small class="text-muted">
              <i class="fas fa-store me-1"></i>Retirada na Loja<br>
              {{ selectedStore.name }}<br>
              {{ selectedStore.address }}
            </small>
          </div>
          <div v-else>
            <small class="text-muted">Selecione uma opção de entrega</small>
          </div>
        </div>

        <hr>

        <!-- Pagamento -->
        <div class="payment-info mb-3" v-if="currentStep >= 2">
          <h6 class="mb-2">Pagamento</h6>
          <div v-if="selectedPaymentMethod">
            <small class="text-muted">
              <i class="fas fa-credit-card me-1"></i>
              {{ paymentMethods[selectedPaymentMethod] }}
            </small>
          </div>
          <div v-else>
            <small class="text-muted">Selecione um método de pagamento</small>
          </div>
        </div>

        <hr>

        <!-- Total -->
        <div class="total-info">
          <div class="d-flex justify-content-between mb-2">
            <span>Subtotal:</span>
            <span>R$ {{ cartTotal.toFixed(2) }}</span>
          </div>
          <div class="d-flex justify-content-between mb-2">
            <span>Entrega:</span>
            <span>{{ deliveryPrice > 0 ? 'R$ ' + deliveryPrice.toFixed(2) : 'Grátis' }}</span>
          </div>
          <div v-if="selectedPaymentMethod === 'pix'" class="d-flex justify-content-between mb-2 text-success">
            <span>Desconto PIX (5%):</span>
            <span>- R$ {{ (cartTotal * 0.05).toFixed(2) }}</span>
          </div>
          <hr>
          <div class="d-flex justify-content-between fw-bold fs-5">
            <span>Total:</span>
            <span>R$ {{ orderTotal.toFixed(2) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CheckoutSummary',
  props: {
    cart: {
      type: Array,
      default: () => []
    },
    deliveryOption: {
      type: String,
      default: ''
    },
    selectedAddress: {
      type: Object,
      default: null
    },
    selectedStore: {
      type: Object,
      default: null
    },
    selectedPaymentMethod: {
      type: String,
      default: ''
    },
    deliveryPrice: {
      type: Number,
      default: 0
    },
    currentStep: {
      type: Number,
      default: 1
    }
  },
  computed: {
    cartItemsCount() {
      return this.cart.reduce((total, item) => total + item.quantity, 0)
    },
    cartTotal() {
      return this.cart.reduce((total, item) => total + (item.price * item.quantity), 0)
    },
    deliveryPrice() {
      if (this.deliveryOption === 'delivery') {
        if (this.cartTotal >= 300) return 0;
        if (this.cartTotal < 100) return 10.00;
        return 0;
      }
      return 0;
    },
    orderTotal() {
      let total = this.cartTotal + this.deliveryPrice
      if (this.selectedPaymentMethod === 'pix') {
        total -= this.cartTotal * 0.05 // 5% discount for PIX
      }
      return total
    },
    paymentMethods() {
      return {
        'credit_card': 'Cartão de Crédito',
        'debit_card': 'Cartão de Débito',
        'pix': 'PIX',
        'boleto': 'Boleto Bancário'
      }
    }
  }
}
</script>


<style scoped>
.checkout-summary .card {
  border: none;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.cart-item {
  padding: 0.5rem;
  border-radius: 5px;
  background-color: #f8f9fa;
}

.item-name {
  font-weight: 500;
  font-size: 0.9rem;
}

.item-price {
  font-weight: 600;
  color: #198754;
}

.total-info {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 5px;
}
</style>