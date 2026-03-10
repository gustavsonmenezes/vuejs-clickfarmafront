<template>
  <div class="payment-page">
    <!-- Header -->
    <div class="payment-header">
      <div class="container">
        <div class="header-content">
          <router-link to="/" class="logo">
            <i class="bi bi-capsule-pill"></i>
            ClickFarma
          </router-link>
          <div class="checkout-steps">
            <div class="step completed">
              <span class="step-number">1</span>
              <span class="step-label">Carrinho</span>
            </div>
            <div class="step completed">
              <span class="step-number">2</span>
              <span class="step-label">Checkout</span>
            </div>
            <div class="step active">
              <span class="step-number">3</span>
              <span class="step-label">Pagamento</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Conteúdo Principal -->
    <div class="container payment-content">
      <div class="row justify-content-center">
        <div class="col-lg-10">
          <div class="payment-card">
            <!-- Cabeçalho -->
            <div class="payment-card-header">
              <h1 class="payment-title">
                <i class="bi bi-credit-card-2-front"></i>
                Método de Pagamento
              </h1>
              <p class="payment-subtitle">Finalize o pagamento do seu pedido</p>
            </div>

            <div class="payment-card-body">
              <div class="row">
                <!-- Coluna de Métodos de Pagamento -->
                <div class="col-lg-7">
                  <!-- Resumo dos Itens -->
                  <div class="items-section mb-4">
                    <h3 class="section-title">Itens do Pedido</h3>
                    <div class="items-list">
                      <div v-for="item in cartItems" :key="item.id" class="cart-item">
                        <div class="item-image">
                          <img :src="getItemImage(item)" :alt="item.name" />
                        </div>
                        <div class="item-details">
                          <h6 class="item-name">{{ item.name }}</h6>
                          <p class="item-category">{{ item.category }}</p>
                          <div class="item-price">R$ {{ item.price.toFixed(2) }}</div>
                        </div>
                        <div class="item-quantity">
                          <span class="quantity-badge">Qtd: {{ item.quantity }}</span>
                          <div class="item-total">R$ {{ (item.price * item.quantity).toFixed(2) }}</div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="methods-section">
                    <h3 class="section-title">Selecione o método de pagamento</h3>
                    
                    <!-- Cartão de Crédito -->
                    <div class="method-option" :class="{ active: selectedMethod === 'credit_card' }" 
                         @click="selectMethod('credit_card')">
                      <div class="method-selector">
                        <div class="radio-wrapper">
                          <input 
                            v-model="selectedMethod" 
                            value="credit_card" 
                            type="radio" 
                            id="creditCard"
                          >
                          <span class="radio-checkmark"></span>
                        </div>
                      </div>
                      <div class="method-content">
                        <div class="method-icon">
                          <i class="bi bi-credit-card-2-front"></i>
                        </div>
                        <div class="method-info">
                          <span class="method-name">Cartão de Crédito</span>
                          <span class="method-desc">Parcele em até 12x sem juros</span>
                        </div>
                        <div class="method-badges">
                          <span class="badge visa">Visa</span>
                          <span class="badge mastercard">Mastercard</span>
                        </div>
                      </div>
                    </div>

                    <!-- Cartão de Débito -->
                    <div class="method-option" :class="{ active: selectedMethod === 'debit_card' }" 
                         @click="selectMethod('debit_card')">
                      <div class="method-selector">
                        <div class="radio-wrapper">
                          <input 
                            v-model="selectedMethod" 
                            value="debit_card" 
                            type="radio" 
                            id="debitCard"
                          >
                          <span class="radio-checkmark"></span>
                        </div>
                      </div>
                      <div class="method-content">
                        <div class="method-icon">
                          <i class="bi bi-card-checklist"></i>
                        </div>
                        <div class="method-info">
                          <span class="method-name">Cartão de Débito</span>
                          <span class="method-desc">Pagamento à vista</span>
                        </div>
                        <div class="method-badges">
                          <span class="badge visa">Visa</span>
                          <span class="badge mastercard">Mastercard</span>
                        </div>
                      </div>
                    </div>

                    <!-- PIX -->
                    <div class="method-option" :class="{ active: selectedMethod === 'pix' }" 
                         @click="selectMethod('pix')">
                      <div class="method-selector">
                        <div class="radio-wrapper">
                          <input 
                            v-model="selectedMethod" 
                            value="pix" 
                            type="radio" 
                            id="pix"
                          >
                          <span class="radio-checkmark"></span>
                        </div>
                      </div>
                      <div class="method-content">
                        <div class="method-icon pix">
                          <i class="bi bi-qr-code-scan"></i>
                        </div>
                        <div class="method-info">
                          <span class="method-name">PIX</span>
                          <span class="method-desc">Pagamento instantâneo</span>
                        </div>
                        <div class="method-badges">
                          <span class="badge discount">5% OFF</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Dados do Cartão -->
                  <div v-if="selectedMethod === 'credit_card' || selectedMethod === 'debit_card'" 
                       class="card-form-section">
                    <h3 class="section-title">Dados do Cartão</h3>
                    <div class="card-form">
                      <div class="row">
                        <div class="col-12 mb-3">
                          <label class="form-label">Número do Cartão</label>
                          <div class="input-with-icon">
                            <input 
                              v-model="cardData.number" 
                              type="text" 
                              class="form-control" 
                              placeholder="0000 0000 0000 0000"
                              maxlength="19"
                              @input="formatCardNumber"
                            >
                            <i class="bi bi-credit-card input-icon"></i>
                          </div>
                          <div class="card-type-indicator">
                            <span class="card-type" :class="cardType.toLowerCase()">{{ cardType }}</span>
                          </div>
                        </div>
                      </div>

                      <div class="row">
                        <div class="col-12 mb-3">
                          <label class="form-label">Nome no Cartão</label>
                          <input 
                            v-model="cardData.name" 
                            type="text" 
                            class="form-control" 
                            placeholder="JOÃO DA SILVA"
                            @input="formatCardName"
                          >
                        </div>
                      </div>

                      <div class="row">
                        <div class="col-md-6 mb-3">
                          <label class="form-label">Validade</label>
                          <input 
                            v-model="cardData.expiry" 
                            type="text" 
                            class="form-control" 
                            placeholder="MM/AA"
                            maxlength="5"
                            @input="formatExpiry"
                          >
                        </div>
                        <div class="col-md-6 mb-3">
                          <label class="form-label">CVV</label>
                          <div class="input-with-icon">
                            <input 
                              v-model="cardData.cvv" 
                              type="password" 
                              class="form-control" 
                              placeholder="123"
                              maxlength="4"
                            >
                            <i class="bi bi-shield-lock input-icon"></i>
                          </div>
                          <small class="text-muted">3 ou 4 dígitos no verso</small>
                        </div>
                      </div>

                      <!-- Parcelas apenas para crédito -->
                      <div v-if="selectedMethod === 'credit_card'" class="row">
                        <div class="col-12 mb-3">
                          <label class="form-label">Parcelas</label>
                          <select v-model="cardData.installments" class="form-select">
                            <option v-for="i in 12" :key="i" :value="i">
                              {{ i }}x de R$ {{ (getTotal() / i).toFixed(2) }} 
                            </option>
                          </select>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Informações do PIX -->
                  <div v-if="selectedMethod === 'pix'" class="pix-info-section">
                    <div class="pix-highlight">
                      <div class="pix-header">
                        <i class="bi bi-qr-code-scan"></i>
                        <h4>Pagamento via PIX</h4>
                        <span class="discount-tag">5% DE DESCONTO</span>
                      </div>
                      <p>Você será redirecionado para gerar um código PIX após confirmar o pedido.</p>
                      <div class="pix-benefits">
                        <div class="benefit">
                          <i class="bi bi-lightning-charge"></i>
                          <span>Pagamento instantâneo</span>
                        </div>
                        <div class="benefit">
                          <i class="bi bi-shield-check"></i>
                          <span>100% seguro</span>
                        </div>
                        <div class="benefit">
                          <i class="bi bi-coin"></i>
                          <span>Sem taxas</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Coluna do Resumo -->
                <div class="col-lg-5">
                  <div class="order-summary-section">
                    <h3 class="section-title">Resumo do Pedido</h3>
                    <div class="summary-card">
                      <div class="summary-items">
                        <div class="summary-item">
                          <span>Itens ({{ totalItems }}):</span>
                          <span>R$ {{ subtotal.toFixed(2) }}</span>
                        </div>
                        <div class="summary-item">
                          <span>Frete:</span>
                          <span class="text-success">{{ deliveryCost }}</span>
                        </div>
                        <div v-if="selectedMethod === 'pix'" class="summary-item discount">
                          <span>Desconto PIX:</span>
                          <span class="text-success">- R$ {{ (subtotal * 0.05).toFixed(2) }}</span>
                        </div>
                      </div>
                      <div class="summary-total">
                        <div class="total-line">
                          <strong>Total:</strong>
                          <strong class="total-amount">
                            R$ {{ getTotal().toFixed(2) }}
                          </strong>
                        </div>
                        <div v-if="selectedMethod === 'credit_card'" class="installment-info">
                          <small>
                            ou {{ cardData.installments }}x de R$ {{ (getTotal() / cardData.installments).toFixed(2) }}
                          </small>
                        </div>
                      </div>
                    </div>

                    <!-- Informações de Entrega -->
                    <div class="delivery-info">
                      <h4 class="info-title">
                        <i class="bi bi-truck"></i>
                        Entrega
                      </h4>
                      <div class="delivery-details">
                        <p><strong>Tipo:</strong> {{ deliveryType === 'delivery' ? 'Entrega em domicílio' : 'Retirada na loja' }}</p>
                        <p v-if="deliveryType === 'delivery'"><strong>Endereço:</strong> {{ deliveryAddress }}</p>
                        <p v-else><strong>Local:</strong> Rua das Farmácias, 123 - Centro, São Paulo - SP</p>
                        <p><strong>Previsão:</strong> {{ deliveryType === 'delivery' ? '2-3 dias úteis' : 'Pronta para retirada' }}</p>
                      </div>
                    </div>

                    <!-- Botões de Ação -->
                    <div class="action-buttons">
                      <button class="btn btn-back" @click="$router.back()">
                        <i class="bi bi-arrow-left"></i>
                        Voltar ao Checkout
                      </button>
                      <button class="btn btn-primary" @click="handlePayment" :disabled="!isFormValid">
                        <i class="bi bi-lock-fill"></i>
                        {{ selectedMethod === 'pix' ? 'Pagar com PIX' : 'Finalizar Pagamento' }}
                        <span class="total-badge">R$ {{ getTotal().toFixed(2) }}</span>
                      </button>
                    </div>

                    <div class="security-info">
                      <div class="security-badges">
                        <div class="security-badge">
                          <i class="bi bi-shield-check"></i>
                          <span>Pagamento Seguro</span>
                        </div>
                        <div class="security-badge">
                          <i class="bi bi-lock-fill"></i>
                          <span>Dados Criptografados</span>
                        </div>
                      </div>
                    </div>
                  </div>
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
  name: 'PaymentMethod',
  data() {
    return {
      selectedMethod: 'credit_card',
      cardData: {
        number: '',
        name: '',
        expiry: '',
        cvv: '',
        installments: 1
      },
      cartItems: [],
      checkoutData: {},
      deliveryType: 'delivery',
      deliveryAddress: ''
    }
  },
  computed: {
    cardType() {
      const number = this.cardData.number.replace(/\s/g, '');
      if (/^4/.test(number)) return 'Visa';
      if (/^5[1-5]/.test(number)) return 'Mastercard';
      if (/^3[47]/.test(number)) return 'Amex';
      if (/^6(?:011|5)/.test(number)) return 'Discover';
      return 'Cartão';
    },
    isFormValid() {
      if (this.selectedMethod === 'pix') return true;
      
      const { number, name, expiry, cvv } = this.cardData;
      return number.length >= 16 && 
             name.length > 0 && 
             expiry.length === 5 && 
             cvv.length >= 3;
    },
    subtotal() {
      return this.cartItems.reduce((total, item) => total + (item.price * item.quantity), 0);
    },
    totalItems() {
      return this.cartItems.reduce((total, item) => total + item.quantity, 0);
    },
    deliveryCost() {
      if (this.deliveryType === 'delivery') {
        if (this.subtotal >= 300) return 'Grátis';
        if (this.subtotal < 100) return 'R$ 10.00';
        return 'Grátis';
      }
      return 'Grátis';
    }
  },
  mounted() {
    try {
      this.clearOldData();
      this.loadCartData();
      this.loadCheckoutData();
      
      console.log('🛒 Itens do carrinho carregados:', this.cartItems);
      console.log('🏪 Dados da store Vuex:', this.$store.state.cart);
      
      // Verificar se há dados mínimos necessários
      if (this.cartItems.length === 0 && this.$store.state.cart.length === 0) {
        alert('Seu carrinho está vazio! Redirecionando para produtos...');
        this.$router.push('/products');
        return;
      }
    } catch (error) {
      console.error('❌ Erro ao inicializar página de pagamento:', error);
      alert('Erro ao carregar página de pagamento. Tente novamente.');
      this.$router.push('/cart');
    }
  },
  methods: {
    clearOldData() {
      const checkoutData = localStorage.getItem('checkoutData');
      if (checkoutData) {
        try {
          const data = JSON.parse(checkoutData);
          if (data.timestamp && (Date.now() - new Date(data.timestamp).getTime()) > 3600000) {
            localStorage.removeItem('checkoutData');
          }
        } catch (error) {
          localStorage.removeItem('checkoutData');
        }
      }
    },
    loadCartData() {
      const savedCart = localStorage.getItem('cart');
      
      if (savedCart) {
        try {
          this.cartItems = JSON.parse(savedCart);
        } catch (error) {
          console.error('Erro ao carregar carrinho:', error);
          this.cartItems = [];
        }
      } else {
        this.cartItems = this.$store.state.cart || [];
      }
      
      if (this.cartItems.length === 0) {
        alert('Seu carrinho está vazio!');
        this.$router.push('/cart');
      }
    },
    loadCheckoutData() {
      const checkoutData = JSON.parse(localStorage.getItem('checkoutData') || '{}');
      console.log('📦 Dados de checkout carregados:', checkoutData); // ← DEBUG
      
      this.checkoutData = checkoutData;
      this.deliveryType = checkoutData.deliveryOption || checkoutData.deliveryType || 'delivery';
      
      if (this.deliveryType === 'delivery' && checkoutData.selectedAddress) {
        const address = checkoutData.selectedAddress;
        this.deliveryAddress = `${address.street}, ${address.number} - ${address.neighborhood}, ${address.city} - ${address.state}`;
        this.checkoutData.deliveryInfo = address; // ← GARANTIR estrutura
      } else if (this.deliveryType === 'pickup' && checkoutData.selectedStore) {
        const store = checkoutData.selectedStore;
        this.deliveryAddress = `${store.name} - ${store.address}`;
        this.checkoutData.deliveryInfo = store; // ← GARANTIR estrutura
      }
      
      console.log('📍 Configuração de entrega:', {
        deliveryType: this.deliveryType,
        deliveryAddress: this.deliveryAddress,
        deliveryInfo: this.checkoutData.deliveryInfo
      });
    },
    getDeliveryCost() {
      if (this.subtotal >= 300) return 0;
      if (this.subtotal < 100) return 10.00;
      return 0;
    },
    selectMethod(method) {
      this.selectedMethod = method;
    },
    formatCardNumber(event) {
      let value = event.target.value.replace(/\D/g, '');
      value = value.replace(/(\d{4})(?=\d)/g, '$1 ');
      this.cardData.number = value.substring(0, 19);
    },
    formatCardName(event) {
      this.cardData.name = event.target.value.toUpperCase();
    },
    formatExpiry(event) {
      let value = event.target.value.replace(/\D/g, '');
      if (value.length >= 2) {
        value = value.substring(0, 2) + '/' + value.substring(2, 4);
      }
      this.cardData.expiry = value.substring(0, 5);
    },
    getTotal() {
      let total = this.subtotal;
      
      if (this.deliveryType === 'delivery') {
        total += this.getDeliveryCost();
      }
      
      if (this.selectedMethod === 'pix') {
        total *= 0.95;
      }
      
      return total;
    },
    getItemImage(item) {
      return item.image || '/placeholder-product.jpg';
    },


    // NO PaymentMethod.vue - método handlePayment
handlePayment() {
  if (!this.isFormValid) return;
  
  const paymentData = {
    method: this.selectedMethod,
    paymentMethod: this.selectedMethod,
    cardData: this.selectedMethod !== 'pix' ? this.cardData : null,
    total: this.getTotal(),
    subtotal: this.subtotal, // ← ADICIONAR
    items: [...this.cartItems], // ← FAZER CÓPIA
    deliveryType: this.deliveryType,
    deliveryInfo: this.checkoutData.deliveryInfo || this.checkoutData,
    deliveryCost: this.getDeliveryCost(), // ← ADICIONAR
  };
  
  console.log('💳 Dados de pagamento enviados:', paymentData);
  
  // ✅ SALVAR DADOS COMPLETOS NO LOCALSTORAGE
  localStorage.setItem('paymentData', JSON.stringify(paymentData));
  
  // ✅ SALVAR CARRINHO ATUAL COMO BACKUP
  localStorage.setItem('cart_backup', JSON.stringify(this.cartItems));
  
  // Processar no Vuex
  this.$store.dispatch('processPayment', paymentData)
    .then((result) => {
      console.log('✅ Pagamento processado:', result);
      this.$router.push('/order-confirmation');
    })
    .catch(error => {
      console.error('❌ Erro no pagamento:', error);
      alert('Erro no pagamento: ' + (error.message || 'Tente novamente'));
    });
}
  }
}
</script>

<style scoped>
/* Seus estilos permanecem os mesmos */
.payment-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.payment-header {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 1rem 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #4a90e2;
  text-decoration: none;
}

.logo i {
  margin-right: 0.5rem;
}

.checkout-steps {
  display: flex;
  gap: 2rem;
}

.step {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6c757d;
}

.step-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #6c757d;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
}

.step.completed .step-number {
  background: #28a745;
}

.step.active .step-number {
  background: #4a90e2;
  transform: scale(1.1);
}

.step.active .step-label {
  color: #4a90e2;
  font-weight: 600;
}

.payment-content {
  padding: 2rem 0;
}

.payment-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.payment-card-header {
  background: linear-gradient(135deg, #4a90e2, #357abd);
  color: white;
  padding: 2rem;
  text-align: center;
}

.payment-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.payment-subtitle {
  opacity: 0.9;
  margin: 0;
}

.payment-card-body {
  padding: 2rem;
}

.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #f8f9fa;
}

/* Métodos de Pagamento */
.method-option {
  display: flex;
  align-items: center;
  padding: 1.5rem;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  margin-bottom: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.method-option:hover {
  border-color: #4a90e2;
  transform: translateY(-2px);
}

.method-option.active {
  border-color: #4a90e2;
  background: linear-gradient(135deg, #f8f9ff, #e3f2fd);
  box-shadow: 0 5px 15px rgba(74, 144, 226, 0.2);
}

.radio-wrapper {
  position: relative;
  margin-right: 1rem;
}

.radio-wrapper input {
  display: none;
}

.radio-checkmark {
  width: 20px;
  height: 20px;
  border: 2px solid #dee2e6;
  border-radius: 50%;
  display: block;
  position: relative;
  transition: all 0.3s ease;
}

.method-option.active .radio-checkmark {
  border-color: #4a90e2;
  background: #4a90e2;
}

.method-option.active .radio-checkmark::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
}

.method-content {
  display: flex;
  align-items: center;
  flex: 1;
  gap: 1rem;
}

.method-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #4a90e2, #357abd);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.3rem;
}

.method-icon.pix {
  background: linear-gradient(135deg, #28a745, #20c997);
}

.method-info {
  flex: 1;
}

.method-name {
  display: block;
  font-weight: 600;
  color: #2c3e50;
  font-size: 1.1rem;
}

.method-desc {
  display: block;
  color: #6c757d;
  font-size: 0.9rem;
}

.method-badges {
  display: flex;
  gap: 0.5rem;
}

.badge {
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  font-size: 0.7rem;
  font-weight: 600;
}

.badge.visa {
  background: #1a1f71;
  color: white;
}

.badge.mastercard {
  background: #eb001b;
  color: white;
}

.badge.discount {
  background: #28a745;
  color: white;
}

/* Formulário do Cartão */
.card-form-section {
  margin-top: 2rem;
}

.input-with-icon {
  position: relative;
}

.input-icon {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
}

.card-type-indicator {
  margin-top: 0.5rem;
}

.card-type {
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 600;
  background: #f8f9fa;
  color: #6c757d;
}

.card-type.visa {
  background: #1a1f71;
  color: white;
}

.card-type.mastercard {
  background: #eb001b;
  color: white;
}

.card-type.amex {
  background: #007bc7;
  color: white;
}

/* Informações PIX */
.pix-highlight {
  background: linear-gradient(135deg, #d4edda, #c3e6cb);
  border-radius: 15px;
  padding: 2rem;
  text-align: center;
  border: 2px solid #28a745;
}

.pix-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.pix-header i {
  font-size: 2rem;
  color: #28a745;
}

.pix-header h4 {
  margin: 0;
  color: #155724;
}

.discount-tag {
  background: #28a745;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.pix-benefits {
  display: flex;
  justify-content: space-around;
  margin-top: 1.5rem;
}

.benefit {
  text-align: center;
}

.benefit i {
  display: block;
  font-size: 1.5rem;
  color: #28a745;
  margin-bottom: 0.5rem;
}

.benefit span {
  font-size: 0.8rem;
  color: #155724;
}

/* Resumo do Pedido */
.order-summary-section {
  position: sticky;
  top: 2rem;
}

.summary-card {
  background: #f8f9fa;
  border-radius: 15px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e9ecef;
}

.summary-item.discount {
  color: #28a745;
}

.summary-total {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 2px solid #4a90e2;
}

.total-line {
  display: flex;
  justify-content: space-between;
  font-size: 1.3rem;
}

.total-amount {
  color: #4a90e2;
}

.installment-info {
  text-align: right;
  margin-top: 0.5rem;
}

/* Botões de Ação */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.btn {
  padding: 1rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  border: none;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-back {
  background: white;
  color: #6c757d;
  border: 2px solid #e9ecef;
}

.btn-back:hover {
  background: #f8f9fa;
  transform: translateY(-2px);
}

.btn-primary {
  background: linear-gradient(135deg, #4a90e2, #357abd);
  color: white;
  position: relative;
  overflow: hidden;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(74, 144, 226, 0.4);
}

.btn-primary:disabled {
  background: #6c757d;
  cursor: not-allowed;
  transform: none;
}

.total-badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.9rem;
  margin-left: auto;
}

/* Informações de Segurança */
.security-info {
  margin-top: 1.5rem;
  text-align: center;
}

.security-badges {
  display: flex;
  justify-content: center;
  gap: 2rem;
}

.security-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #28a745;
  font-size: 0.9rem;
}

/* Responsividade */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
  }
  
  .checkout-steps {
    gap: 1rem;
  }
  
  .payment-card-body {
    padding: 1rem;
  }
  
  .method-content {
    flex-direction: column;
    text-align: center;
    gap: 0.5rem;
  }
  
  .method-badges {
    justify-content: center;
  }
  
  .pix-benefits {
    flex-direction: column;
    gap: 1rem;
  }
  
  .security-badges {
    flex-direction: column;
    gap: 1rem;
  }
}

.form-control, .form-select {
  border-radius: 10px;
  border: 2px solid #e9ecef;
  padding: 0.75rem 1rem;
  transition: all 0.3s ease;
}

.form-control:focus, .form-select:focus {
  border-color: #4a90e2;
  box-shadow: 0 0 0 0.2rem rgba(74, 144, 226, 0.1);
}

/* Novos estilos para itens do carrinho */
.items-section {
  background: white;
  border-radius: 15px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 1.5rem;
}

.items-list {
  max-height: 300px;
  overflow-y: auto;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #f8f9fa;
  gap: 1rem;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-details {
  flex: 1;
}

.item-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.item-category {
  color: #6c757d;
  font-size: 0.8rem;
  margin-bottom: 0.5rem;
}

.item-price {
  font-weight: 600;
  color: #4a90e2;
}

.item-quantity {
  text-align: right;
}

.quantity-badge {
  background: #f8f9fa;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  font-size: 0.8rem;
  color: #6c757d;
}

.item-total {
  font-weight: 600;
  color: #2c3e50;
  margin-top: 0.5rem;
}

.delivery-info {
  background: #f8f9fa;
  border-radius: 15px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.info-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.delivery-details p {
  margin-bottom: 0.5rem;
  color: #6c757d;
  font-size: 0.9rem;
}
</style>