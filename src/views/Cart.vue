<template>
  <div class="container mt-4">
    <h2 class="page-title">🛒 Meu Carrinho</h2>
    
    <!-- Carrinho vazio -->
    <div v-if="cart.length === 0" class="text-center py-5">
      <div class="empty-cart">
        <i class="bi bi-cart-x" style="font-size: 4rem; color: #6c757d;"></i>
        <h4 class="mt-3">Seu carrinho está vazio</h4>
        <p class="text-muted">Adicione produtos para continuar</p>
        <router-link to="/products" class="btn btn-primary mt-3">
          Continuar Comprando
        </router-link>
      </div>
    </div>
    
    <!-- Carrinho com itens -->
    <div v-else>
      <div class="row">
        <!-- Lista de itens -->
        <div class="col-md-8">
          <div class="cart-items">
            <div v-for="item in cart" :key="item.id" class="cart-item card mb-3">
              <div class="card-body">
                <div class="row align-items-center">
                  <div class="col-md-2">
                    <img :src="getItemImage(item)" :alt="item.name" class="img-fluid rounded">
                  </div>
                  <div class="col-md-4">
                    <h6 class="item-name">{{ item.name }}</h6>
                    <p class="item-category text-muted mb-0">{{ item.category }}</p>
                  </div>
                  <div class="col-md-2">
                    <div class="item-price">R$ {{ item.price.toFixed(2) }}</div>
                  </div>
                  <div class="col-md-2">
                    <div class="quantity-controls">
                      <button 
                        class="btn btn-outline-secondary btn-sm" 
                        @click="updateQuantity(item.id, item.quantity - 1)"
                        :disabled="item.quantity <= 1"
                      >
                        -
                      </button>
                      <span class="mx-2">{{ item.quantity }}</span>
                      <button 
                        class="btn btn-outline-secondary btn-sm" 
                        @click="updateQuantity(item.id, item.quantity + 1)"
                      >
                        +
                      </button>
                    </div>
                  </div>
                  <div class="col-md-2 text-end">
                    <div class="item-total">R$ {{ (item.price * item.quantity).toFixed(2) }}</div>
                    <button 
                      class="btn btn-danger btn-sm mt-2" 
                      @click="removeFromCart(item.id)"
                    >
                      Remover
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Resumo do pedido -->
        <div class="col-md-4">
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0">Resumo do Pedido</h5>
            </div>
            <div class="card-body">
              <div class="summary-item d-flex justify-content-between mb-2">
                <span>Itens ({{ cartItemsCount }}):</span>
                <span>R$ {{ cartTotal.toFixed(2) }}</span>
              </div>
              <div class="summary-item d-flex justify-content-between mb-2">
                <span>Frete:</span>
                <span class="text-success">Grátis</span>
              </div>
              <hr>
              <div class="summary-total d-flex justify-content-between">
                <strong>Total:</strong>
                <strong>R$ {{ cartTotal.toFixed(2) }}</strong>
              </div>
              <button 
                class="btn btn-primary w-100 mt-3" 
                @click="proceedToCheckout"
              >
                Finalizar Compra
              </button>
            </div>
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
  computed: {
    ...mapState(['cart']),
    ...mapGetters(['cartItemsCount', 'cartTotal'])
  },
  methods: {
    ...mapActions(['removeFromCart', 'updateCartQuantity']),
    
    updateQuantity(productId, quantity) {
      const validQuantity = Math.max(1, quantity)
      this.updateCartQuantity({ productId, quantity: validQuantity })
    },
    
    getItemImage(item) {
      return item.image || '/placeholder-product.jpg'
    },
    
    proceedToCheckout() {
      // Salvar carrinho atual no localStorage antes de ir para o checkout
      localStorage.setItem('cart', JSON.stringify(this.cart))
      this.$router.push('/checkout')
    }
  }
}
</script>

<style scoped>
.page-title {
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

.cart-item {
  transition: transform 0.2s;
}

.cart-item:hover {
  transform: translateY(-2px);
}

.quantity-controls {
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity-controls button {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-name {
  font-weight: 600;
  color: #2c3e50;
}

.item-price, .item-total {
  font-weight: 600;
  color: #4a90e2;
}

.empty-cart {
  padding: 3rem 1rem;
}
</style>
