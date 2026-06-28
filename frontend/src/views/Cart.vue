<template>
  <div class="cf-cart-page">

    <!-- Header -->
    <div class="cf-cart-header">
      <div class="cf-container">
        <h1 class="cf-cart-title">Carrinho</h1>
        <p v-if="cart.length > 0" class="cf-cart-sub">{{ cart.length }} {{ cart.length === 1 ? 'item' : 'itens' }}</p>
      </div>
    </div>

    <div class="cf-container">
      <!-- Carrinho Vazio -->
      <div v-if="cart.length === 0" class="cf-cart-empty">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--cf-gray-300)" stroke-width="1.2">
          <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
        </svg>
        <h3>Seu carrinho está vazio</h3>
        <p>Adicione medicamentos e produtos para começar sua compra.</p>
        <router-link to="/products" class="cf-btn cf-btn-primary">Ver produtos</router-link>
      </div>

      <!-- Com Itens -->
      <div v-else class="cf-cart-layout">
        <!-- Coluna da esquerda: itens -->
        <div class="cf-cart-main">
          <!-- Lista de itens -->
          <div class="cf-cart-items">
            <div v-for="item in cart" :key="item.id" class="cf-cart-item">
              <div class="cf-ci-visual">
                <div class="cf-ci-icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--cf-gray-400)" stroke-width="1.5">
                    <rect x="4" y="2" width="16" height="20" rx="2"/><path d="M9 22v-4h6v4"/>
                  </svg>
                </div>
              </div>
              <div class="cf-ci-info">
                <h3 class="cf-ci-name">{{ item.nome }}</h3>
                <span v-if="item.principioAtivo" class="cf-ci-active">{{ item.principioAtivo }}</span>
                <div class="cf-ci-meta">
                  <span class="cf-ci-unit">R$ {{ formatPrice(item.preco) }}</span>
                </div>
              </div>
              <div class="cf-ci-qty">
                <button class="cf-qty-btn" @click="decrement(item)" :disabled="getQuantity(item) <= 1">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M5 12h14"/>
                  </svg>
                </button>
                <span class="cf-qty-value">{{ getQuantity(item) }}</span>
                <button class="cf-qty-btn" @click="increment(item)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 5v14m7-7H5"/>
                  </svg>
                </button>
              </div>
              <div class="cf-ci-total">
                <span class="cf-ci-total-price">R$ {{ formatPrice(getQuantity(item) * item.preco) }}</span>
              </div>
              <button class="cf-ci-remove" @click="removeFromCart(item)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6 6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Área de Receita Médica -->
          <div class="cf-rx-section">
            <div class="cf-rx-header">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--cf-teal)" stroke-width="1.8">
                <rect x="2" y="3" width="20" height="18" rx="2"/>
                <path d="M12 8v8M8 12h8"/>
              </svg>
              <div>
                <strong>Receita Médica</strong>
                <p v-if="hasRxItems" class="cf-rx-desc">Este pedido contém medicamentos que exigem prescrição. Envie sua receita para análise.</p>
                <p v-else class="cf-rx-desc">Nenhum item do carrinho exige receita médica.</p>
              </div>
            </div>
            <div v-if="hasRxItems" class="cf-rx-upload">
              <div class="cf-rx-dropzone" @click="triggerRxUpload" @dragover.prevent @drop.prevent="handleRxDrop">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--cf-gray-400)" stroke-width="1.5">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="17 8 12 3 7 8"/>
                  <line x1="12" y1="3" x2="12" y2="15"/>
                </svg>
                <span v-if="!rxFile">Clique para enviar a receita (PDF ou imagem)</span>
                <span v-else class="cf-rx-file">{{ rxFile.name }}</span>
              </div>
              <p class="cf-rx-note">Sua receita será analisada por um farmacêutico responsável.</p>
            </div>
          </div>
        </div>

        <!-- Sidebar: Resumo -->
        <div class="cf-cart-sidebar">
          <div class="cf-summary-card">
            <h3 class="cf-summary-title">Resumo do Pedido</h3>

            <div class="cf-summary-rows">
              <div class="cf-summary-row">
                <span>Subtotal ({{ totalItems }} itens)</span>
                <span>R$ {{ formatPrice(subtotal) }}</span>
              </div>
              <div class="cf-summary-row">
                <span>Frete</span>
                <span v-if="frete === 0" class="cf-frete-free">Grátis</span>
                <span v-else>R$ {{ formatPrice(frete) }}</span>
              </div>
              <div v-if="desconto > 0" class="cf-summary-row cf-summary-discount">
                <span>Desconto</span>
                <span>-R$ {{ formatPrice(desconto) }}</span>
              </div>
            </div>

            <!-- Cálculo de frete -->
            <div class="cf-frete-calc">
              <div class="cf-frete-input-group">
                <input
                  v-model="cepInput"
                  type="text"
                  class="cf-input"
                  placeholder="Calcular frete (CEP)"
                  maxlength="9"
                  @input="maskCep"
                />
                <button class="cf-btn cf-btn-sm cf-btn-outline" @click="calcFrete" :disabled="!cepInput.trim()">
                  OK
                </button>
              </div>
              <div v-if="freteCalculado" class="cf-frete-result">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="var(--cf-teal)" stroke-width="2">
                  <rect x="2" y="6" width="20" height="12" rx="2"/>
                  <circle cx="8" cy="16" r="2"/><circle cx="18" cy="16" r="2"/>
                </svg>
                <span>Entrega em <strong>{{ tempoEstimado }}</strong></span>
              </div>
            </div>

            <hr class="cf-divider">

            <div class="cf-summary-total">
              <span>Total</span>
              <span class="cf-total-value">R$ {{ formatPrice(total) }}</span>
            </div>

            <!-- Método de Pagamento -->
            <div class="cf-payment-section">
              <h4 class="cf-payment-title">Forma de pagamento</h4>
              <div class="cf-payment-options">
                <label class="cf-payment-option" :class="{ active: paymentMethod === 'pix' }">
                  <input type="radio" v-model="paymentMethod" value="pix" />
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="2" y="4" width="20" height="16" rx="3"/><path d="M2 8h20"/>
                  </svg>
                  <span>PIX</span>
                  <span class="cf-payment-badge">À vista</span>
                </label>
                <label class="cf-payment-option" :class="{ active: paymentMethod === 'card' }">
                  <input type="radio" v-model="paymentMethod" value="card" />
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/>
                  </svg>
                  <span>Cartão</span>
                  <span class="cf-payment-badge">Até 12x</span>
                </label>
              </div>
            </div>

            <button class="cf-btn cf-btn-primary cf-btn-block cf-btn-lg" @click="finalizarPedido">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              Finalizar Pedido
            </button>

            <router-link to="/products" class="cf-continue-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 12H5m7-7-7 7 7 7"/>
              </svg>
              Continuar comprando
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapGetters, mapActions } from 'vuex'

export default {
  name: 'CartPage',
  data() {
    return {
      cepInput: '',
      frete: 0,
      freteCalculado: false,
      tempoEstimado: '20-40 min',
      desconto: 0,
      paymentMethod: 'pix',
      rxFile: null
    }
  },
  computed: {
    ...mapState(['cart']),
    ...mapGetters(['cartItemsCount', 'cartTotal']),
    totalItems() {
      return this.cart.reduce((s, i) => s + (i.quantity || 1), 0)
    },
    subtotal() {
      return this.cart.reduce((s, i) => s + (i.preco || 0) * (i.quantity || 1), 0)
    },
    total() {
      return Math.max(0, this.subtotal + this.frete - this.desconto)
    },
    hasRxItems() {
      return this.cart.some(i => i.exigeReceita)
    }
  },
  methods: {
    ...mapActions(['removeFromCart', 'updateCartQuantity']),
    formatPrice(v) { return (v || 0).toFixed(2).replace('.', ',') },
    getQuantity(item) { return item.quantity || 1 },
    increment(item) {
      this.updateCartQuantity({ productId: item.id, quantity: this.getQuantity(item) + 1 })
    },
    decrement(item) {
      const q = this.getQuantity(item)
      if (q > 1) this.updateCartQuantity({ productId: item.id, quantity: q - 1 })
    },
    maskCep() {
      let v = this.cepInput.replace(/\D/g, '')
      if (v.length > 5) v = v.slice(0, 5) + '-' + v.slice(5, 8)
      this.cepInput = v
    },
    calcFrete() {
      this.frete = 12.90
      this.freteCalculado = true
      this.tempoEstimado = this.frete === 0 ? '20-30 min' : '30-50 min'
    },
    triggerRxUpload() {
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = 'image/*,application/pdf'
      input.onchange = (e) => {
        if (e.target.files[0]) this.rxFile = e.target.files[0]
      }
      input.click()
    },
    handleRxDrop(e) {
      if (e.dataTransfer.files[0]) this.rxFile = e.dataTransfer.files[0]
    },
    finalizarPedido() {
      if (this.hasRxItems && !this.rxFile) {
        alert('Este pedido contém medicamentos que exigem receita médica. Por favor, envie a receita para continuar.')
        return
      }
      this.$router.push('/checkout')
    }
  }
}
</script>

<style scoped>
.cf-cart-page {
  padding-bottom: 3rem;
  min-height: 70vh;
}

/* Header */
.cf-cart-header {
  padding: 1.5rem 0 0.5rem;
}
.cf-cart-title {
  font-family: var(--cf-heading);
  font-size: 1.6rem;
  font-weight: 700;
}
.cf-cart-sub {
  font-size: 0.9rem;
  color: var(--cf-gray-500);
  margin-top: 4px;
}

/* Empty state */
.cf-cart-empty {
  text-align: center;
  padding: 4rem 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.cf-cart-empty h3 {
  font-family: var(--cf-heading);
  font-size: 1.2rem;
  color: var(--cf-gray-700);
}
.cf-cart-empty p { color: var(--cf-gray-500); font-size: 0.9rem; }

/* Layout */
.cf-cart-layout {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 24px;
  margin-top: 1rem;
  align-items: start;
}

/* ============================
   ITENS
   ============================ */
.cf-cart-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.cf-cart-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 1rem;
  border-radius: var(--cf-r-lg);
  border: 1px solid var(--cf-gray-200);
  background: var(--cf-white);
  transition: all var(--cf-fast);
}
.cf-cart-item:hover {
  border-color: var(--cf-teal-light);
  box-shadow: var(--cf-shadow-xs);
}
.cf-ci-visual {
  width: 56px;
  height: 56px;
  border-radius: var(--cf-r-md);
  background: var(--cf-teal-xlight);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.cf-ci-info { flex: 1; min-width: 0; }
.cf-ci-name {
  font-family: var(--cf-heading);
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--cf-gray-900);
}
.cf-ci-active {
  font-size: 0.75rem;
  font-style: italic;
  color: var(--cf-gray-500);
}
.cf-ci-meta { margin-top: 2px; }
.cf-ci-unit {
  font-size: 0.78rem;
  color: var(--cf-gray-500);
}

/* Quantity */
.cf-ci-qty {
  display: flex;
  align-items: center;
  gap: 6px;
}
.cf-qty-btn {
  width: 30px;
  height: 30px;
  border-radius: var(--cf-r-sm);
  border: 1px solid var(--cf-gray-200);
  background: var(--cf-white);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cf-gray-600);
  cursor: pointer;
  transition: all var(--cf-fast);
}
.cf-qty-btn:hover:not(:disabled) {
  background: var(--cf-teal-xlight);
  border-color: var(--cf-teal);
  color: var(--cf-teal);
}
.cf-qty-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.cf-qty-value {
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--cf-gray-800);
  min-width: 20px;
  text-align: center;
}

/* Total + Remove */
.cf-ci-total { text-align: right; }
.cf-ci-total-price {
  font-family: var(--cf-heading);
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--cf-gray-900);
}
.cf-ci-remove {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--cf-gray-400);
  cursor: pointer;
  border-radius: var(--cf-r-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--cf-fast);
}
.cf-ci-remove:hover {
  background: #FEF2F2;
  color: var(--cf-danger);
}

/* ============================
   RECEITA MÉDICA
   ============================ */
.cf-rx-section {
  margin-top: 1rem;
  padding: 1rem 1.25rem;
  border: 1px solid var(--cf-gray-200);
  border-radius: var(--cf-r-lg);
  background: var(--cf-white);
}
.cf-rx-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.cf-rx-header strong {
  font-family: var(--cf-heading);
  font-size: 0.9rem;
  color: var(--cf-gray-900);
}
.cf-rx-desc {
  font-size: 0.8rem;
  color: var(--cf-gray-500);
  margin: 2px 0 0;
}
.cf-rx-upload { margin-top: 12px; }
.cf-rx-dropzone {
  border: 2px dashed var(--cf-gray-200);
  border-radius: var(--cf-r-md);
  padding: 1.25rem;
  text-align: center;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--cf-gray-500);
  font-size: 0.8rem;
  transition: all var(--cf-fast);
}
.cf-rx-dropzone:hover {
  border-color: var(--cf-teal);
  background: var(--cf-teal-xlight);
}
.cf-rx-file {
  color: var(--cf-teal);
  font-weight: 500;
}
.cf-rx-note {
  font-size: 0.7rem;
  color: var(--cf-gray-400);
  margin-top: 6px;
  text-align: center;
}

/* ============================
   SIDEBAR
   ============================ */
.cf-cart-sidebar {
  position: sticky;
  top: 130px;
}
.cf-summary-card {
  border: 1px solid var(--cf-gray-200);
  border-radius: var(--cf-r-lg);
  padding: 1.25rem;
  background: var(--cf-white);
  box-shadow: var(--cf-shadow-sm);
}
.cf-summary-title {
  font-family: var(--cf-heading);
  font-size: 1rem;
  font-weight: 700;
  color: var(--cf-gray-900);
  margin-bottom: 1rem;
}
.cf-summary-rows { display: flex; flex-direction: column; gap: 8px; }
.cf-summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.88rem;
  color: var(--cf-gray-600);
}
.cf-summary-discount { color: var(--cf-teal); }
.cf-frete-free {
  color: var(--cf-teal);
  font-weight: 600;
}

/* Frete calc */
.cf-frete-calc {
  margin-top: 12px;
}
.cf-frete-input-group {
  display: flex;
  gap: 6px;
}
.cf-frete-result {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 0.78rem;
  color: var(--cf-teal-dark);
}

/* Total */
.cf-summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1rem;
}
.cf-total-value {
  font-family: var(--cf-heading);
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--cf-gray-900);
}

/* Payment */
.cf-payment-section { margin: 1rem 0; }
.cf-payment-title {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--cf-gray-700);
  margin-bottom: 8px;
}
.cf-payment-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.cf-payment-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0.65rem 0.75rem;
  border: 1.5px solid var(--cf-gray-200);
  border-radius: var(--cf-r-md);
  cursor: pointer;
  transition: all var(--cf-fast);
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--cf-gray-600);
}
.cf-payment-option input { display: none; }
.cf-payment-option.active {
  border-color: var(--cf-teal);
  background: var(--cf-teal-xlight);
  color: var(--cf-teal-dark);
}
.cf-payment-option:hover { border-color: var(--cf-gray-300); }
.cf-payment-badge {
  font-size: 0.6rem;
  color: var(--cf-gray-400);
  margin-left: auto;
}

.cf-continue-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 12px;
  font-size: 0.82rem;
  color: var(--cf-gray-500);
  text-decoration: none;
}
.cf-continue-link:hover { color: var(--cf-teal); }

/* ============================
   RESPONSIVO
   ============================ */
@media (max-width: 900px) {
  .cf-cart-layout { grid-template-columns: 1fr; }
  .cf-cart-sidebar { position: static; }
}
@media (max-width: 600px) {
  .cf-cart-item { flex-wrap: wrap; }
  .cf-ci-visual { display: none; }
  .cf-payment-options { grid-template-columns: 1fr; }
}
</style>
