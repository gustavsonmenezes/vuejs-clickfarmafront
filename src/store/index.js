import { createStore } from 'vuex'
import axios from 'axios'

// Serviço de pagamento mockado para ClickFarma
class PaymentService {
  
  // Simula processamento de pagamento com cartão de crédito
  static async processCardPayment(paymentData) {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        // Simula validação do cartão
        if (!this.validateCardNumber(paymentData.cardNumber)) {
          reject({ 
            error: 'INVALID_CARD', 
            message: 'Número do cartão inválido' 
          });
          return;
        }

        if (!this.validateExpiryDate(paymentData.expiryDate)) {
          reject({ 
            error: 'EXPIRED_CARD', 
            message: 'Cartão expirado' 
          });
          return;
        }

        if (!this.validateCVV(paymentData.cvv)) {
          reject({ 
            error: 'INVALID_CVV', 
            message: 'CVV inválido' 
          });
          return;
        }

        // Simula 5% de chance de falha no pagamento
        if (Math.random() < 0.05) {
          reject({ 
            error: 'PAYMENT_FAILED', 
            message: 'Pagamento recusado pela operadora' 
          });
          return;
        }

        // Sucesso no pagamento
        resolve({
          success: true,
          transactionId: this.generateTransactionId(),
          amount: paymentData.amount,
          method: paymentData.paymentMethod, // Inclui se é credit_card ou debit_card
          cardType: paymentData.paymentMethod,
          status: 'approved',
          timestamp: new Date().toISOString(),
          authorizationCode: this.generateAuthCode()
        });
      }, 2000); // Simula delay de processamento
    });
  }

  // Simula processamento de pagamento via PIX
  static async processPixPayment(paymentData) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          success: true,
          transactionId: this.generateTransactionId(),
          amount: paymentData.amount,
          method: 'pix',
          status: 'pending',
          timestamp: new Date().toISOString(),
          pixCode: this.generatePixCode(),
          qrCode: this.generateQRCode(),
          expiresAt: new Date(Date.now() + 30 * 60 * 1000).toISOString() // 30 minutos
        });
      }, 1000);
    });
  }

  // Simula processamento de pagamento via boleto
  static async processBoletoPayment(paymentData) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          success: true,
          transactionId: this.generateTransactionId(),
          amount: paymentData.amount,
          method: 'boleto',
          status: 'pending',
          timestamp: new Date().toISOString(),
          boletoCode: this.generateBoletoCode(),
          dueDate: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toISOString(), // 3 dias
          boletoUrl: `https://boleto.clickfarma.com.br/${this.generateTransactionId()}.pdf`
        });
      }, 1500);
    });
  }

  // Validações
  static validateCardNumber(cardNumber) {
    const cleaned = cardNumber.replace(/\s/g, '');
    return /^\d{13,19}$/.test(cleaned) && this.luhnCheck(cleaned);
  }

  static validateExpiryDate(expiryDate) {
    const [month, year] = expiryDate.split('/');
    const expiry = new Date(2000 + parseInt(year), parseInt(month) - 1);
    return expiry > new Date();
  }

  static validateCVV(cvv) {
    return /^\d{3,4}$/.test(cvv);
  }

  // Algoritmo de Luhn para validação de cartão
  static luhnCheck(cardNumber) {
    let sum = 0;
    let isEven = false;
    
    for (let i = cardNumber.length - 1; i >= 0; i--) {
      let digit = parseInt(cardNumber[i]);
      
      if (isEven) {
        digit *= 2;
        if (digit > 9) {
          digit -= 9;
        }
      }
      
      sum += digit;
      isEven = !isEven;
    }
    
    return sum % 10 === 0;
  }

  // Geradores de códigos
  static generateTransactionId() {
    return 'TXN' + Date.now() + Math.random().toString(36).substr(2, 5).toUpperCase();
  }

  static generateAuthCode() {
    return Math.random().toString(36).substr(2, 8).toUpperCase();
  }

  static generatePixCode() {
    return '00020126580014BR.GOV.BCB.PIX0136' + 
           Math.random().toString(36).substr(2, 32) + 
           '5204000053039865802BR5925CLICKFARMA LTDA6009SAO PAULO62070503***6304';
  }

  static generateQRCode() {
    // Em uma implementação real, isso geraria um QR code real
    return 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==';
  }

  static generateBoletoCode() {
    const bank = '341'; // Itaú
    const dv = Math.floor(Math.random() * 10);
    const sequence = Math.random().toString().substr(2, 10);
    return `${bank}${dv}${sequence}`;
  }

  // Utilitários para formatação
  static formatCardNumber(value) {
    const v = value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
    const matches = v.match(/\d{4,16}/g);
    const match = matches && matches[0] || '';
    const parts = [];
    
    for (let i = 0, len = match.length; i < len; i += 4) {
      parts.push(match.substring(i, i + 4));
    }
    
    if (parts.length) {
      return parts.join(' ');
    } else {
      return v;
    }
  }

  static formatExpiryDate(value) {
    const v = value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
    if (v.length >= 2) {
      return v.substring(0, 2) + '/' + v.substring(2, 4);
    }
    return v;
  }

  static getCardBrand(cardNumber) {
    const number = cardNumber.replace(/\s/g, '');
    
    if (/^4/.test(number)) return 'visa';
    if (/^5[1-5]/.test(number)) return 'mastercard';
    if (/^3[47]/.test(number)) return 'amex';
    if (/^6(?:011|5)/.test(number)) return 'discover';
    if (/^(?:2131|1800|35\d{3})\d{11}$/.test(number)) return 'jcb';
    
    return 'unknown';
  }

  // Simula consulta de status de pagamento
  static async getPaymentStatus(transactionId) {
    return new Promise((resolve) => {
      setTimeout(() => {
        // Simula diferentes status baseado no ID
        const statuses = ['pending', 'approved', 'failed', 'cancelled'];
        const randomStatus = statuses[Math.floor(Math.random() * statuses.length)];
        
        resolve({
          transactionId,
          status: randomStatus,
          updatedAt: new Date().toISOString()
        });
      }, 500);
    });
  }
}

export default createStore({
  state: {
    user: null,
    products: [],
    cart: [],
    categories: ['Medicamentos', 'Cosméticos', 'Higiene', 'Vitaminas', 'Maternidade'],
    authToken: localStorage.getItem('authToken') || null,
    lastOrder: null,
    paymentMethod: null,
    adminProducts: [],
    adminOrders: [],
    adminPrescriptions: [],
    adminUsers: []
  },
  
  getters: {
    isAuthenticated: state => !!state.authToken,
    cartItemsCount: state => state.cart.reduce((total, item) => total + item.quantity, 0),
    cartTotal: state => state.cart.reduce((total, item) => total + (item.price * item.quantity), 0),
    products: state => state.products,
    categories: state => state.categories,
    adminProducts: state => state.adminProducts,
    adminOrders: state => state.adminOrders,
    adminPrescriptions: state => state.adminPrescriptions,
    adminUsers: state => state.adminUsers
  },
  
  mutations: {
    SET_USER(state, user) {
      state.user = user
    },
    SET_AUTH_TOKEN(state, token) {
      state.authToken = token
      localStorage.setItem('authToken', token)
    },
    CLEAR_AUTH(state) {
      state.authToken = null
      state.user = null
      localStorage.removeItem('authToken')
    },
    SET_PRODUCTS(state, products) {
      state.products = products
    },
    ADD_TO_CART(state, product) {
      const existingItem = state.cart.find(item => item.id === product.id)
      if (existingItem) {
        existingItem.quantity++
      } else {
        state.cart.push({ ...product, quantity: 1 })
      }
    },
    REMOVE_FROM_CART(state, productId) {
      state.cart = state.cart.filter(item => item.id !== productId)
    },
    UPDATE_CART_QUANTITY(state, { productId, quantity }) {
      const item = state.cart.find(item => item.id === productId)
      if (item) {
        item.quantity = quantity
      }
    },
    CLEAR_CART(state) {
      state.cart = []
    },
    SET_ORDER(state, order) {
      state.lastOrder = order;
    },
    SET_PAYMENT_METHOD(state, method) {
      state.paymentMethod = method;
    },
    SET_ADMIN_PRODUCTS(state, products) {
      state.adminProducts = products
    },
    SET_ADMIN_ORDERS(state, orders) {
      state.adminOrders = orders
    },
    SET_ADMIN_PRESCRIPTIONS(state, prescriptions) {
      state.adminPrescriptions = prescriptions
    },
    SET_ADMIN_USERS(state, users) {
      state.adminUsers = users
    }
  },
  
  actions: {
    async login({ commit }, credentials) {
      try {
        // Simulação de API
        const response = await new Promise(resolve => setTimeout(() => {
          resolve({ 
            data: { 
              user: { id: 1, name: credentials.email, email: credentials.email, role: 'user' },
              token: 'mock-token-' + Math.random().toString(36).substr(2)
            }
          })
        }, 1000))
        
        commit('SET_USER', response.data.user)
        commit('SET_AUTH_TOKEN', response.data.token)
        return response.data
      } catch (error) {
        throw error.response ? error.response.data : { message: 'Erro de conexão' }
      }
    },
    
    async register({ commit }, userData) {
      try {
        // Simulação de API
        const response = await new Promise(resolve => setTimeout(() => {
          resolve({ 
            data: { 
              user: { id: 2, name: userData.name, email: userData.email, role: 'user' },
              token: 'mock-token-' + Math.random().toString(36).substr(2)
            }
          })
        }, 1000))
        
        commit('SET_USER', response.data.user)
        commit('SET_AUTH_TOKEN', response.data.token)
        return response.data
      } catch (error) {
        throw error.response ? error.response.data : { message: 'Erro de conexão' }
      }
    },
    
    logout({ commit }) {
      commit('CLEAR_AUTH')
    },
    
    async fetchProducts({ commit }) {
      try {
        // Dados mockados
        const mockProducts = [
          { id: 1, name: 'Paracetamol 500mg', price: 12.90, category: 'Medicamentos', description: 'Analgésico e antitérmico', inStock: true },
          { id: 2, name: 'Dipirona 500mg', price: 8.50, category: 'Medicamentos', description: 'Analgésico e antitérmico', inStock: true },
          { id: 3, name: 'Shampoo Anti-Caspa', price: 24.90, category: 'Higiene', description: 'Shampoo para controle de caspa', inStock: true },
          { id: 4, name: 'Vitamina C 1000mg', price: 45.00, category: 'Vitaminas', description: 'Suplemento de vitamina C', inStock: true },
          { id: 5, name: 'Protetor Solar FPS 50', price: 32.90, category: 'Cosméticos', description: 'Protetor solar facial', inStock: false },
          { id: 6, name: 'Fralda P - 30 unidades', price: 28.90, category: 'Maternidade', description: 'Fraldas para bebê', inStock: true }
        ]
        
        commit('SET_PRODUCTS', mockProducts)
      } catch (error) {
        console.error('Erro ao buscar produtos:', error)
      }
    },
    
    addToCart({ commit }, product) {
      commit('ADD_TO_CART', product)
    },
    
    removeFromCart({ commit }, productId) {
      commit('REMOVE_FROM_CART', productId)
    },
    
    updateCartQuantity({ commit }, payload) {
      commit('UPDATE_CART_QUANTITY', payload)
    },
    
    clearCart({ commit }) {
      commit('CLEAR_CART')
    },
    
    async processPayment({ commit, state }, paymentData) {
      try {
        let paymentResult;
        const deliveryCost = paymentData.deliveryType === 'delivery' ? (state.cartTotal >= 100 ? 0 : 10) : 0;
        const amount = state.cartTotal + deliveryCost;
        
        // Processa o pagamento de acordo com o método selecionado
        console.log('🔄 Processando pagamento com método:', paymentData.paymentMethod);
        
        switch (paymentData.paymentMethod) {
          case 'credit_card':
          case 'debit_card': // ← CORREÇÃO: Agora suporta cartão de débito
            paymentResult = await PaymentService.processCardPayment({
              ...paymentData,
              amount: amount
            });
            break;
            
          case 'pix':
            paymentResult = await PaymentService.processPixPayment({
              amount: amount
            });
            break;
            
          case 'boleto':
            paymentResult = await PaymentService.processBoletoPayment({
              amount: amount
            });
            break;
            
          default:
            throw new Error(`Método de pagamento não suportado: ${paymentData.paymentMethod}`);
        }
        
        if (paymentResult.success) {
          // Criar objeto de pedido
          const order = {
            id: 'ORD-' + Math.random().toString(36).substr(2, 9).toUpperCase(),
            transactionId: paymentResult.transactionId,
            items: [...state.cart],
            total: amount,
            status: paymentResult.status,
            date: new Date().toISOString(),
            paymentMethod: paymentData.paymentMethod,
            deliveryType: paymentData.deliveryType,
            deliveryInfo: paymentData.deliveryInfo,
            paymentDetails: paymentResult
          };
          
          commit('SET_ORDER', order);
          commit('CLEAR_CART');
          
          console.log('✅ Pedido criado com sucesso:', order);
        }
        
        return paymentResult;
      } catch (error) {
        console.error('❌ Erro no processamento do pagamento:', error);
        throw error.response ? error.response.data : { message: error.message || 'Erro ao processar pagamento' };
      }
    },

    async requestPasswordReset({ commit }, email) {
      try {
        // Simulação de API
        await new Promise(resolve => setTimeout(resolve, 1000))
        return { success: true }
      } catch (error) {
        throw new Error('Erro ao solicitar redefinição de senha')
      }
    },
    
    async updateUserProfile({ commit }, userData) {
      try {
        // Simulação de atualização
        await new Promise(resolve => setTimeout(resolve, 1000))
        commit('SET_USER', userData)
        return { success: true }
      } catch (error) {
        throw new Error('Erro ao atualizar perfil')
      }
    },
    
    // Admin actions
    async fetchAdminProducts({ commit }) {
      try {
        // Mock data - substituir por API real
        const mockProducts = [
          { id: 1, name: 'Paracetamol 500mg', price: 12.90, category: 'Medicamentos', stock: 150, status: 'active' },
          { id: 2, name: 'Dipirona 500mg', price: 8.50, category: 'Medicamentos', stock: 89, status: 'active' },
          { id: 3, name: 'Shampoo Anti-Caspa', price: 24.90, category: 'Higiene', stock: 45, status: 'active' },
          { id: 4, name: 'Vitamina C 1000mg', price: 45.00, category: 'Vitaminas', stock: 23, status: 'inactive' }
        ]
        commit('SET_ADMIN_PRODUCTS', mockProducts)
      } catch (error) {
        console.error('Erro ao buscar produtos admin:', error)
      }
    },
    
    async updateProductStock({ commit }, { productId, newStock }) {
      try {
        // Mock - substituir por API real
        await new Promise(resolve => setTimeout(resolve, 500))
        // Atualizar localmente
        const products = JSON.parse(JSON.stringify(this.state.adminProducts))
        const product = products.find(p => p.id === productId)
        if (product) {
          product.stock = newStock
          commit('SET_ADMIN_PRODUCTS', products)
        }
      } catch (error) {
        console.error('Erro ao atualizar estoque:', error)
        throw error
      }
    }
  }
})