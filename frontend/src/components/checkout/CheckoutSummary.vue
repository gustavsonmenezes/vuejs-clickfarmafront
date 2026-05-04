<template>
  <div class="cf-summary">

    <!-- Header -->
    <div class="cf-sum-head">
      <span class="cf-sum-title">Resumo do pedido</span>
      <span class="cf-sum-count">{{ cartItemsCount }} {{ cartItemsCount === 1 ? 'item' : 'itens' }}</span>
    </div>

    <!-- Lista de itens -->
    <div class="cf-sum-items">
      <div v-for="item in cart" :key="item.id" class="cf-sum-item">
        <div class="cf-item-thumb">{{ getCategoryIcon(item.category || item.categoriaNome) }}</div>
        <div class="cf-item-info">
          <span class="cf-item-name">{{ item.name || item.nome }}</span>
          <span class="cf-item-qty">Qtd: {{ item.quantity }}</span>
        </div>
        <span class="cf-item-price">R$&nbsp;{{ ((item.price || item.preco || 0) * item.quantity).toFixed(2).replace('.', ',') }}</span>
      </div>
    </div>

    <div class="cf-sum-line"></div>

    <!-- Entrega -->
    <div class="cf-sum-section">
      <span class="cf-sum-section-label">Entrega</span>
      <div v-if="deliveryOption === 'delivery' && selectedAddress" class="cf-sum-detail">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="1" y="3" width="15" height="13"/><path d="m16 8 6 2v8H16"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>
        {{ selectedAddress.street }}, {{ selectedAddress.number }} · {{ selectedAddress.city }}
      </div>
      <div v-else-if="deliveryOption === 'pickup' && selectedStore" class="cf-sum-detail">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
        {{ selectedStore.name }}
      </div>
      <div v-else class="cf-sum-pending">A definir</div>
    </div>

    <!-- Pagamento -->
    <div class="cf-sum-section" v-if="currentStep >= 2">
      <span class="cf-sum-section-label">Pagamento</span>
      <div v-if="selectedPaymentMethod" class="cf-sum-detail">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M2 9h20"/></svg>
        {{ paymentMethods[selectedPaymentMethod] }}
      </div>
      <div v-else class="cf-sum-pending">A definir</div>
    </div>

    <div class="cf-sum-line"></div>

    <!-- Totais -->
    <div class="cf-totals">
      <div class="cf-total-row">
        <span>Subtotal</span>
        <span>R$&nbsp;{{ cartTotal.toFixed(2).replace('.', ',') }}</span>
      </div>
      <div class="cf-total-row">
        <span>Frete</span>
        <span :class="{ 'cf-free': deliveryPrice === 0 }">
          {{ deliveryPrice > 0 ? 'R$ ' + deliveryPrice.toFixed(2).replace('.', ',') : 'Grátis' }}
        </span>
      </div>
      <div v-if="selectedPaymentMethod === 'pix'" class="cf-total-row cf-discount">
        <span>Desconto PIX (5%)</span>
        <span>− R$&nbsp;{{ (cartTotal * 0.05).toFixed(2).replace('.', ',') }}</span>
      </div>
      <div class="cf-total-final">
        <span>Total</span>
        <span>R$&nbsp;{{ orderTotal.toFixed(2).replace('.', ',') }}</span>
      </div>
    </div>

    <!-- Dica PIX -->
    <div v-if="selectedPaymentMethod !== 'pix'" class="cf-pix-hint">
      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/></svg>
      Pague com PIX e ganhe 5% de desconto
    </div>

  </div>
</template>

<script>
export default {
  name: 'CheckoutSummary',
  props: {
    cart:                  { type: Array,  default: () => [] },
    deliveryOption:        { type: String, default: '' },
    selectedAddress:       { type: Object, default: null },
    selectedStore:         { type: Object, default: null },
    selectedPaymentMethod: { type: String, default: '' },
    deliveryPrice:         { type: Number, default: 0 },
    currentStep:           { type: Number, default: 1 }
  },
  computed: {
    cartItemsCount() {
      return this.cart.reduce((t, i) => t + i.quantity, 0)
    },
    cartTotal() {
      return this.cart.reduce((t, i) => t + (i.price || i.preco || 0) * i.quantity, 0)
    },
    orderTotal() {
      const delivery = this.deliveryPrice ?? 0
      let total = this.cartTotal + delivery
      if (this.selectedPaymentMethod === 'pix') total -= this.cartTotal * 0.05
      return total
    },
    paymentMethods() {
      return {
        credit_card: 'Cartão de Crédito',
        debit_card:  'Cartão de Débito',
        pix:         'PIX',
        boleto:      'Boleto Bancário'
      }
    }
  },
  methods: {
    getCategoryIcon(cat) {
      return { 'Medicamentos':'💊','Cosméticos':'🧴','Higiene':'🚿','Vitaminas':'🌿','Maternidade':'👶' }[cat] || '📦'
    }
  }
}
</script>

<style scoped>
.cf-summary {
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg);
  overflow: hidden;
}

/* Cabeçalho */
.cf-sum-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 1.1rem 1.3rem 0.9rem;
  border-bottom: 1px solid var(--cf-border);
  background: var(--cf-ivory);
}
.cf-sum-title {
  font-family: var(--cf-serif);
  font-size: 1.15rem;
  font-weight: 400;
  color: var(--cf-text-dark);
}
.cf-sum-count {
  font-size: 0.68rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--cf-text-muted);
}

/* Itens */
.cf-sum-items {
  padding: 0.6rem 1.3rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 190px;
  overflow-y: auto;
}
.cf-sum-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0.45rem 0;
}
.cf-item-thumb {
  width: 34px; height: 34px;
  background: var(--cf-green-xlight);
  border-radius: var(--cf-r-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}
.cf-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.cf-item-name {
  font-size: 0.84rem;
  font-weight: 400;
  color: var(--cf-text-dark);
  line-height: 1.3;
}
.cf-item-qty {
  font-size: 0.7rem;
  color: var(--cf-text-muted);
}
.cf-item-price {
  font-family: var(--cf-serif);
  font-size: 0.98rem;
  font-weight: 500;
  color: var(--cf-text-dark);
  flex-shrink: 0;
}

/* Divisor */
.cf-sum-line {
  height: 1px;
  background: var(--cf-border);
  margin: 0 1.3rem;
}

/* Seções info */
.cf-sum-section {
  padding: 0.65rem 1.3rem;
}
.cf-sum-section-label {
  display: block;
  font-size: 0.62rem;
  letter-spacing: 0.13em;
  text-transform: uppercase;
  color: var(--cf-text-faint);
  font-weight: 500;
  margin-bottom: 0.3rem;
}
.cf-sum-detail {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.83rem;
  color: var(--cf-text-mid);
}
.cf-sum-detail svg { color: var(--cf-green); flex-shrink: 0; }
.cf-sum-pending {
  font-size: 0.8rem;
  color: var(--cf-text-faint);
  font-style: italic;
}

/* Totais */
.cf-totals {
  padding: 0.8rem 1.3rem;
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}
.cf-total-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  color: var(--cf-text-muted);
}
.cf-free { color: var(--cf-green); font-weight: 500; }
.cf-discount { color: var(--cf-green); }
.cf-total-final {
  display: flex;
  justify-content: space-between;
  font-family: var(--cf-serif);
  font-size: 1.25rem;
  font-weight: 500;
  color: var(--cf-text-dark);
  padding-top: 0.65rem;
  margin-top: 0.15rem;
  border-top: 1px solid var(--cf-border);
}

/* Dica PIX */
.cf-pix-hint {
  margin: 0 1.3rem 1.1rem;
  padding: 0.55rem 0.8rem;
  background: var(--cf-green-xlight);
  border: 1px solid var(--cf-green-light);
  border-radius: var(--cf-r-md);
  font-size: 0.75rem;
  color: var(--cf-green);
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>