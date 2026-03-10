<template>
  <div class="product-info">
    <h1 class="product-title">{{ product.name }}</h1>
    
    <div class="product-meta mb-3">
      <span class="badge bg-secondary">{{ product.category }}</span>
      <span class="ms-2" :class="product.inStock ? 'text-success' : 'text-danger'">
        <i class="fas fa-circle me-1" style="font-size: 0.5rem;"></i>
        {{ product.inStock ? 'Em estoque' : 'Fora de estoque' }}
      </span>
    </div>

    <div class="product-price mb-3">
      <h2 class="text-primary">R$ {{ product.price.toFixed(2) }}</h2>
      <small class="text-muted">ou 12x de R$ {{ (product.price / 12).toFixed(2) }} sem juros</small>
    </div>

    <div class="product-actions mb-4">
      <div class="quantity-selector mb-3">
        <label class="form-label">Quantidade:</label>
        <div class="input-group" style="max-width: 150px;">
          <button class="btn btn-outline-secondary" type="button" @click="decreaseQuantity">-</button>
          <input type="number" class="form-control text-center" v-model.number="quantity" min="1" :max="maxQuantity">
          <button class="btn btn-outline-secondary" type="button" @click="increaseQuantity">+</button>
        </div>
      </div>

      <button 
        class="btn btn-primary btn-lg w-100 mb-2"
        :disabled="!product.inStock || addingToCart"
        @click="handleAddToCart"
      >
        <span v-if="addingToCart" class="spinner-border spinner-border-sm me-1"></span>
        {{ addingToCart ? 'Adicionando...' : 'Adicionar ao Carrinho' }}
      </button>

      <button class="btn btn-outline-secondary w-100">
        <i class="far fa-heart me-2"></i>Favoritar
      </button>
    </div>

    <div class="product-features">
      <div class="feature-item d-flex align-items-center mb-2">
        <i class="fas fa-truck text-success me-2"></i>
        <span>Entrega em 2-3 dias úteis</span>
      </div>
      <div class="feature-item d-flex align-items-center mb-2">
        <i class="fas fa-store text-primary me-2"></i>
        <span>Retirada disponível em loja</span>
      </div>
      <div class="feature-item d-flex align-items-center mb-2">
        <i class="fas fa-shield-alt text-warning me-2"></i>
        <span>Produto original com garantia</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductInfo',
  props: {
    product: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      quantity: 1,
      addingToCart: false,
      maxQuantity: 10
    }
  },
  methods: {
    increaseQuantity() {
      if (this.quantity < this.maxQuantity) {
        this.quantity++
      }
    },
    decreaseQuantity() {
      if (this.quantity > 1) {
        this.quantity--
      }
    },
    async handleAddToCart() {
      this.addingToCart = true
      try {
        // Adicionar múltiplas unidades do produto
        for (let i = 0; i < this.quantity; i++) {
          await this.$store.dispatch('addToCart', this.product)
        }
        
        this.$emit('add-to-cart')
        this.$toast.success(`${this.quantity} ${this.quantity > 1 ? 'itens' : 'item'} adicionado(s) ao carrinho!`)
        this.quantity = 1
      } catch (error) {
        this.$toast.error('Erro ao adicionar ao carrinho')
      } finally {
        this.addingToCart = false
      }
    }
  }
}
</script>

<style scoped>
.product-title {
  font-size: 2rem;
  font-weight: 600;
  color: #333;
}

.product-price h2 {
  font-weight: 700;
}

.quantity-selector .input-group {
  max-width: 150px;
}

.quantity-selector input {
  font-weight: 600;
}

.feature-item {
  font-size: 0.9rem;
  color: #666;
}
</style>