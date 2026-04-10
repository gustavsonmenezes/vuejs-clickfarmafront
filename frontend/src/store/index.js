import { createStore } from 'vuex'
import authService from '../services/auth' 
import cartService from '../services/cart'
import productsService from '../services/products' 

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
          method: paymentData.paymentMethod,
          cardType: paymentData.paymentMethod,
          status: 'approved',
          timestamp: new Date().toISOString(),
          authorizationCode: this.generateAuthCode()
        });
      }, 2000);
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
          expiresAt: new Date(Date.now() + 30 * 60 * 1000).toISOString()
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
          dueDate: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toISOString(),
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
    return 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==';
  }

  static generateBoletoCode() {
    const bank = '341';
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

// Serviço de rastreamento de entrega
class DeliveryService {
  static async trackOrder(orderId) {
    try {
      const response = await new Promise(resolve => {
        setTimeout(() => {
          const statuses = [
            'confirmed', 'processing', 'shipped', 'out_for_delivery', 'delivered'
          ];
          const currentStatus = statuses[Math.floor(Math.random() * statuses.length)];

          resolve({
            orderId,
            status: currentStatus,
            trackingCode: `TRK${orderId}`,
            estimatedDelivery: this.calculateDeliveryDate(),
            currentLocation: this.getRandomLocation(),
            updates: this.generateTrackingUpdates(orderId, currentStatus),
            carrier: 'ClickFarma Express',
            contact: '(81) 99818-9999'
          });
        }, 1000);
      });

      return response;
    } catch (error) {
      console.error('Erro no rastreamento:', error);
      throw error;
    }
  }

  static calculateDeliveryDate() {
    const date = new Date();
    date.setDate(date.getDate() + 3);
    return date.toISOString();
  }

  static getRandomLocation() {
    const locations = [
      'Centro de Distribuição - Recife',
      'Unidade Paulista',
      'A caminho da entrega',
      'Em separação'
    ];
    return locations[Math.floor(Math.random() * locations.length)];
  }

  static generateTrackingUpdates(orderId, currentStatus) {
    const updates = [];
    const now = new Date();

    updates.push({
      status: 'confirmed',
      description: 'Pedido confirmado e em processamento',
      timestamp: new Date(now - 3600000).toISOString(),
      location: 'ClickFarma - Loja Virtual'
    });

    if (['processing', 'shipped', 'out_for_delivery', 'delivered'].includes(currentStatus)) {
      updates.push({
        status: 'processing',
        description: 'Produtos sendo separados',
        timestamp: new Date(now - 1800000).toISOString(),
        location: 'Centro de Distribuição'
      });
    }

    if (['shipped', 'out_for_delivery', 'delivered'].includes(currentStatus)) {
      updates.push({
        status: 'shipped',
        description: 'Pedido enviado para transporte',
        timestamp: new Date(now - 900000).toISOString(),
        location: 'Unidade de Logística'
      });
    }

    if (['out_for_delivery', 'delivered'].includes(currentStatus)) {
      updates.push({
        status: 'out_for_delivery',
        description: 'Pedido saiu para entrega',
        timestamp: new Date(now - 300000).toISOString(),
        location: 'A caminho do destino'
      });
    }

    if (currentStatus === 'delivered') {
      updates.push({
        status: 'delivered',
        description: 'Pedido entregue com sucesso',
        timestamp: now.toISOString(),
        location: 'Local de entrega'
      });
    }

    return updates;
  }
}

export default createStore({
  state: {
    user: null,
    products: [],
    cart: [],
    categories: ['Medicamentos', 'Cosméticos', 'Higiene', 'Vitaminas', 'Maternidade'],
    authToken: localStorage.getItem('authToken') || null,
    authChecked: false,
    lastOrder: null,
    paymentMethod: null,
    adminProducts: [],
    adminOrders: [],
    adminPrescriptions: [],
    adminUsers: [],
    orderTracking: {}
  },

  getters: {
    user: (state) => state.user,
    isAuthenticated: (state) => !!state.authToken,
    authChecked: (state) => state.authChecked,
    // Garante que itera de forma segura
    cartItemsCount: (state) => {
      if (!Array.isArray(state.cart)) return 0;
      return state.cart.reduce((total, item) => total + (item.quantidade || item.quantity || 1), 0);
    },
    cartTotal: (state) => {
      if (!Array.isArray(state.cart)) return 0;
      return state.cart.reduce((total, item) => {
         // Verificando profundamente de onde vem o preco para não quebrar a soma do Total
         const price = Number(item.produto?.preco || item.preco || item.price || 0);
         const qty = Number(item.quantidade || item.quantity || 1);
         return total + (price * qty);
      }, 0);
    },
    cart: (state) => state.cart || [],
    products: (state) => state.products,
    categories: (state) => state.categories,
    adminProducts: (state) => state.adminProducts,
    adminOrders: (state) => state.adminOrders,
    adminPrescriptions: (state) => state.adminPrescriptions,
    adminUsers: (state) => state.adminUsers,
    getOrderTracking: (state) => (orderId) => state.orderTracking[orderId]
  },

  mutations: {
    SET_USER(state, user) {
      state.user = user;
    },
    SET_AUTH_TOKEN(state, token) {
      state.authToken = token;
      localStorage.setItem('authToken', token);
    },
    SET_AUTH_CHECKED(state, status) {
      state.authChecked = status;
    },
    CLEAR_AUTH(state) {
      state.authToken = null;
      state.user = null;
      state.authChecked = false;
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
    },
    SET_PRODUCTS(state, products) {
      state.products = products;
    },
    SET_CART(state, cartData) {
      // Garante que seja sempre um array para nao dar erro no length
      state.cart = Array.isArray(cartData) ? cartData : [];
    },
    SET_ORDER(state, order) {
      state.lastOrder = order;
    },
    SET_PAYMENT_METHOD(state, method) {
      state.paymentMethod = method;
    },
    SET_ADMIN_PRODUCTS(state, products) {
      state.adminProducts = products;
    },
    SET_ADMIN_ORDERS(state, orders) {
      state.adminOrders = orders;
    },
    SET_ADMIN_PRESCRIPTIONS(state, prescriptions) {
      state.adminPrescriptions = prescriptions;
    },
    SET_ADMIN_USERS(state, users) {
      state.adminUsers = users;
    },
    SET_TRACKING_INFO(state, { orderId, trackingInfo }) {
      if (!state.orderTracking) state.orderTracking = {};
      state.orderTracking[orderId] = trackingInfo;
    },
    SAVE_ORDER_TO_LOCAL_STORAGE(state, order) {
      try {
        const savedOrders = JSON.parse(localStorage.getItem('userOrders') || '[]');
        savedOrders.unshift(order); 
        localStorage.setItem('userOrders', JSON.stringify(savedOrders));
      } catch (error) {
        console.error('Erro ao salvar pedido no localStorage:', error);
      }
    }
  },

  actions: {
    async checkAuthStatus({ commit, dispatch }) {
      try {
        console.log('🔐 Verificando status de autenticação...');
        const token = localStorage.getItem('authToken');
        if (token) {
          const user = JSON.parse(localStorage.getItem('user') || 'null');
          commit('SET_USER', user);
          console.log('✅ Usuário autenticado:', user);
          
          dispatch('fetchCart');
        } else {
          console.log('🔒 Usuário não autenticado');
        }
        commit('SET_AUTH_CHECKED', true);
      } catch (error) {
        console.error('❌ Erro ao verificar autenticação:', error);
        commit('SET_AUTH_CHECKED', true);
      }
    },

    async login({ commit, dispatch }, credentials) {
      try {
        const response = await authService.login(credentials);
        
        const user = response.data;
        commit('SET_USER', user);
        commit('SET_AUTH_TOKEN', user.token);
        localStorage.setItem('user', JSON.stringify(user));
        
        console.log('✅ Login realizado com sucesso no backend');
        
        dispatch('fetchCart');
        
        return user;
      } catch (error) {
        console.error('❌ Erro no login:', error);
        throw error.response ? error.response.data : { message: 'Erro de conexão' };
      }
    },

    async register({ commit }, userData) {
      try {
        console.log('📤 Enviando registro para o backend:', userData);
        const response = await authService.register(userData);
        console.log('✅ Registro realizado com sucesso no backend');
        return response.data;
      } catch (error) {
        console.error('❌ Erro no registro:', error.response?.data || error.message);
        throw error.response?.data || { message: 'Erro de conexão com o servidor' };
      }
    },
    
    logout({ commit }) {
      commit('CLEAR_AUTH');
      commit('SET_CART', []);
      console.log('✅ Logout realizado com sucesso');
    },
    
    // 👇 CARREGANDO PRODUTOS DA API 👇
    async fetchProducts({ commit }) {
      try {
        console.log('Buscando produtos do backend...');
        const response = await productsService.getProducts();
        
        console.log('Produtos recebidos e extraídos:', response.data);
        
        // A API retorna response.data (que no Axios já é o JSON parseado).
        // Nosso backend retorna direto List<ProdutoResponseDTO> (um Array).
        const responseData = Array.isArray(response.data) ? response.data : (response.data.dados || []);
        
        const apiProducts = responseData.map(p => ({
          id: p.id,
          name: p.nome,
          description: p.descricao || 'Sem descrição',
          price: Number(p.preco) || 0,
          inStock: p.estoque > 0,
          category: p.categoriaNome || 'Medicamentos'
        }));

        commit('SET_PRODUCTS', apiProducts);
      } catch (error) {
        console.error('Erro ao buscar produtos da API:', error);
      }
    },
    
    async fetchCart({ commit, state }) {
      if (!state.authToken) return;
      try {
        const response = await cartService.getCart();
        // Garantir que a API não retorne String, Null ou Objeto único, mas sim um Array de itens
        const cartData = Array.isArray(response.data) ? response.data : [];
        commit('SET_CART', cartData);
      } catch (error) {
        console.error('Erro ao buscar carrinho do servidor:', error);
      }
    },

    async addToCart({ commit, state, dispatch }, product) {
      if (!state.authToken) {
         console.warn("Usuário não logado. O carrinho não será salvo no servidor.");
         const cart = Array.isArray(state.cart) ? [...state.cart] : [];
         // Tentamos encontrar o id independentemente do nível onde o produto esteja
         const existing = cart.find(i => (i.produto?.id || i.id) === product.id);
         
         if(existing) {
             existing.quantidade = (existing.quantidade || existing.quantity || 1) + 1;
             existing.quantity = existing.quantidade; // Manter os dois para compatibilidade
         } else {
             // O ProdutoCard emite o objeto product. Mapeamos ele para que fique igual ao formato do Backend (com a chave 'produto')
             cart.push({ 
                 id: product.id,
                 produto: product, 
                 quantidade: 1, 
                 quantity: 1, 
                 price: product.price || product.preco 
             });
         }
         commit('SET_CART', cart);
         return;
      }
      
      try {
        await cartService.addItem(product.id, product.quantidade || 1);
        dispatch('fetchCart'); // Recarrega o carrinho inteiro do backend para garantir os dados e a renderizacao corretos
      } catch (error) {
        console.error('Erro ao adicionar produto no backend:', error);
      }
    },
    
    async removeFromCart({ commit, state }, productId) {
      if (!state.authToken) {
          const cart = Array.isArray(state.cart) ? state.cart.filter(item => (item.produto?.id || item.id) !== productId) : [];
          commit('SET_CART', cart);
          return;
      }

      try {
        await cartService.removeItem(productId);
        const cart = Array.isArray(state.cart) ? state.cart : [];
        const newCart = cart.filter(i => (i.produto?.id || i.id) !== productId);
        commit('SET_CART', newCart);
      } catch (error) {
        console.error('Erro ao remover produto no backend:', error);
      }
    },
    
    async updateCartQuantity({ commit, state, dispatch }, { productId, quantity }) {
      if (!state.authToken) {
          const cart = Array.isArray(state.cart) ? [...state.cart] : [];
          const item = cart.find(i => (i.produto?.id || i.id) === productId);
          if (item) {
              item.quantidade = quantity;
              item.quantity = quantity;
          }
          commit('SET_CART', cart);
          return;
      }

      try {
        await cartService.updateItem(productId, quantity);
        dispatch('fetchCart');
      } catch (error) {
        console.error('Erro ao atualizar quantidade no backend:', error);
      }
    },
    
    async clearCart({ commit, state }) {
      if(state.authToken) {
         try {
           await cartService.clearCart();
         } catch(error) {
           console.error("Erro limpando carrinho do backend", error);
         }
      }
      commit('SET_CART', []);
    },

    async processPayment({ commit, state }, paymentData) {
      try {
        let paymentResult;
        const deliveryCost = paymentData.deliveryType === 'delivery' ? (state.cartTotal >= 100 ? 0 : 10) : 0;
        const amount = state.cartTotal + deliveryCost;
        
        console.log('🔄 Processando pagamento com método:', paymentData.paymentMethod);
        
        switch (paymentData.paymentMethod) {
          case 'credit_card':
          case 'debit_card':
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
          const order = {
            id: 'ORD-' + Math.random().toString(36).substr(2, 9).toUpperCase(),
            transactionId: paymentResult.transactionId,
            items: [...state.cart],
            total: amount,
            subtotal: state.cartTotal,
            status: paymentResult.status,
            date: new Date().toISOString(),
            paymentMethod: paymentData.paymentMethod,
            deliveryType: paymentData.deliveryType,
            deliveryInfo: paymentData.deliveryInfo,
            paymentDetails: paymentResult,
            userId: state.user ? state.user.id : null // Adiciona userId se usuário estiver logado
          };
          
          commit('SET_ORDER', order);
          commit('SET_CART', []);
          
          // SALVAR PEDIDO NO LOCALSTORAGE
          commit('SAVE_ORDER_TO_LOCAL_STORAGE', order);
          
          console.log('✅ Pedido criado com sucesso:', order);
        }
        
        return paymentResult;
      } catch (error) {
        console.error('❌ Erro no processamento do pagamento:', error);
        throw error.response ? error.response.data : { message: error.message || 'Erro ao processar pagamento' };
      }
    },

    async fetchOrderTracking({ commit }, orderId) {
      try {
        const trackingInfo = await DeliveryService.trackOrder(orderId);
        commit('SET_TRACKING_INFO', { orderId, trackingInfo });
        return trackingInfo;
      } catch (error) {
        console.error('Erro ao buscar rastreamento:', error);
        throw error;
      }
    },

    async fetchRealTimeTracking({ commit }, orderId) {
      try {
        console.log('📍 Buscando localização em tempo real para:', orderId);
        
        const generateMockCoordinates = () => {
          const baseLat = -8.0476;
          const baseLng = -34.8770;
          const variation = (Math.random() - 0.5) * 0.1;
          return {
            lat: baseLat + variation,
            lng: baseLng + variation,
            accuracy: Math.random() * 100 + 50
          };
        };

        const generateRealTimeUpdates = (orderId, currentLocation) => {
          const updates = [];
          const now = new Date();
          
          updates.push({
            status: 'location_update',
            description: `Localização atual: ${currentLocation}`,
            timestamp: now.toISOString(),
            location: currentLocation,
            type: 'current'
          });
          
          if (currentLocation.includes('Centro')) {
            updates.unshift({
              status: 'processing',
              description: 'Pedido confirmado e em separação',
              timestamp: new Date(now - 3600000).toISOString(),
              location: 'Centro de Distribuição',
              type: 'history'
            });
          }
          
          if (currentLocation.includes('Paulista') || currentLocation.includes('Zona')) {
            updates.unshift({
              status: 'shipped',
              description: 'Pedido enviado para transporte',
              timestamp: new Date(now - 1800000).toISOString(),
              location: 'Unidade de Logística',
              type: 'history'
            });
          }
          
          if (currentLocation.includes('caminho') || currentLocation.includes('região')) {
            updates.unshift({
              status: 'out_for_delivery',
              description: 'Pedido saiu para entrega',
              timestamp: new Date(now - 900000).toISOString(),
              location: 'Base de Entregas',
              type: 'history'
            });
          }
          
          return updates;
        };

        const trackingInfo = await new Promise(resolve => {
          setTimeout(() => {
            const locations = [
              'Centro de Distribuição - Recife',
              'Unidade Paulista',
              'A caminho da entrega - Zona Norte',
              'Próximo ao destino - 5km',
              'Na sua região - 1km'
            ];
            
            const statuses = [
              'confirmed', 'processing', 'shipped', 'out_for_delivery', 'delivered'
            ];
            
            const randomIndex = Math.floor(Math.random() * locations.length);
            const currentLocation = locations[randomIndex];
            
            resolve({
              orderId,
              status: statuses[randomIndex],
              currentLocation: currentLocation,
              trackingCode: `TRK${orderId}`,
              estimatedDelivery: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
              carrier: 'ClickFarma Express',
              driver: {
                name: 'Carlos Entregador',
                phone: '(81) 99818-9999',
                vehicle: 'Moto - ABC1234'
              },
              coordinates: generateMockCoordinates(),
              lastUpdate: new Date().toISOString(),
              updates: generateRealTimeUpdates(orderId, currentLocation)
            });
          }, 1500);
        });
        
        commit('SET_TRACKING_INFO', { orderId, trackingInfo });
        return trackingInfo;
      } catch (error) {
        console.error('❌ Erro no rastreamento:', error);
        throw error;
      }
    },

    async requestPasswordReset({ commit }, email) {
      try {
        await new Promise(resolve => setTimeout(resolve, 1000));
        return { success: true };
      } catch (error) {
        throw new Error('Erro ao solicitar redefinição de senha');
      }
    },
    
    async updateUserProfile({ commit }, userData) {
      try {
        await new Promise(resolve => setTimeout(resolve, 1000));
        commit('SET_USER', userData);
        localStorage.setItem('user', JSON.stringify(userData));
        return { success: true };
      } catch (error) {
        throw new Error('Erro ao atualizar perfil');
      }
    },
    
    async fetchAdminProducts({ commit }) {
      try {
        const mockProducts = [
          { id: 1, name: 'Paracetamol 500mg', price: 12.90, category: 'Medicamentos', stock: 150, status: 'active' },
          { id: 2, name: 'Dipirona 500mg', price: 8.50, category: 'Medicamentos', stock: 89, status: 'active' },
          { id: 3, name: 'Shampoo Anti-Caspa', price: 24.90, category: 'Higiene', stock: 45, status: 'active' },
          { id: 4, name: 'Vitamina C 1000mg', price: 45.00, category: 'Vitaminas', stock: 23, status: 'inactive' }
        ];
        commit('SET_ADMIN_PRODUCTS', mockProducts);
      } catch (error) {
        console.error('Erro ao buscar produtos admin:', error);
      }
    },
    
    async updateProductStock({ commit, state }, { productId, newStock }) {
      try {
        await new Promise(resolve => setTimeout(resolve, 500));
        const products = JSON.parse(JSON.stringify(state.adminProducts));
        const product = products.find(p => p.id === productId);
        if (product) {
          product.stock = newStock;
          commit('SET_ADMIN_PRODUCTS', products);
        }
      } catch (error) {
        console.error('Erro ao atualizar estoque:', error);
        throw error;
      }
    }
  }
});
