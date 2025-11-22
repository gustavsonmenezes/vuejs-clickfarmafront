<template>
  <div class="container mt-4">
    <h2 class="page-title">🛒 Meu Carrinho</h2>
    
    <!-- Carrinho vazio -->
    <EmptyCart v-if="cart.length === 0" />
    
    <!-- Carrinho com itens -->
    <div v-else>
      <div class="row">
        <!-- Lista de itens -->
        <div class="col-md-8">
          <div class="cart-items">
            <CartItem 
              v-for="item in cart" 
              :key="item.id" 
              :item="item"
              @update-quantity="updateQuantity"
              @remove-item="removeFromCart"
            />
          </div>
        </div>
        
        <!-- Resumo do pedido -->
        <div class="col-md-4">
          <OrderSummary 
            :items-count="cartItemsCount"
            :total="cartTotal"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapGetters, mapActions } from 'vuex'
import EmptyCart from '@/components/cart/EmptyCart.vue'
import CartItem from '@/components/cart/CartItem.vue'
import OrderSummary from '@/components/cart/OrderSummary.vue'

export default {
  name: 'CartPage',
  components: {
    EmptyCart,
    CartItem,
    OrderSummary
  },
  computed: {
    ...mapState(['cart']),
    ...mapGetters(['cartItemsCount', 'cartTotal'])
  },
  methods: {
    ...mapActions(['removeFromCart', 'updateCartQuantity']),
    
    updateQuantity(productId, quantity) {
      const validQuantity = Math.max(1, quantity)
      this.updateCartQuantity({ productId, quantity: validQuantity })
    }
  }
}
</script>

<style scoped>
.page-title {
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-weight: 600;
}

.cart-items {
  margin-bottom: 2rem;
}
</style>