<template>
  <div class="card product-card h-100">
    <div class="card-body">
      <!-- Product Image/Badge -->
      <div class="product-image-container mb-3">
        <div class="product-badge" :class="product.inStock ? 'in-stock' : 'out-of-stock'">
          {{ product.inStock ? '📦 Em estoque' : '⏳ Fora de estoque' }}
        </div>
        <div class="product-icon">
          {{ getCategoryIcon(product.category) }}
        </div>
      </div>

      <!-- Product Info -->
      <router-link 
        :to="`/products/${product.id}`" 
        class="text-decoration-none text-dark"
      >
        <h5 class="product-title">{{ product.name }}</h5>
        <p class="product-description">{{ product.description }}</p>
        
        <div class="product-meta">
          <span class="category-badge">
            {{ getCategoryIcon(product.category) }} {{ product.category }}
          </span>
        </div>
      </router-link>

      <!-- Price & Actions -->
      <div class="product-footer mt-3">
        <div class="price-section mb-2">
          <h4 class="product-price mb-1">R$ {{ formattedPrice }}</h4>
          <small class="price-installment">ou 12x de R$ {{ (product.price / 12).toFixed(2) }}</small>
        </div>

        <button 
          @click="handleAddToCart" 
          class="btn btn-accent w-100 add-to-cart-btn"
          :disabled="!product.inStock"
          :class="{ 'btn-success': addedToCart }"
        >
          <span v-if="addingToCart" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="fas" :class="addedToCart ? 'fa-check' : 'fa-cart-plus'"></i>
          {{ buttonText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductCard',
  props: {
    product: {
      type: Object,
      required: true,
      validator: (product) => {
        return (
          product.id &&
          product.name &&
          product.description &&
          typeof product.price === 'number' &&
          typeof product.inStock === 'boolean'
        )
      }
    }
  },
  data() {
    return {
      addingToCart: false,
      addedToCart: false
    }
  },
  computed: {
    formattedPrice() {
      return this.product.price.toFixed(2).replace('.', ',')
    },
    buttonText() {
      if (this.addingToCart) return 'Adicionando...'
      if (this.addedToCart) return 'Adicionado!'
      if (!this.product.inStock) return 'Indisponível'
      return 'Adicionar ao Carrinho'
    }
  },
  methods: {
    getCategoryIcon(category) {
      const icons = {
        'Medicamentos': '💊',
        'Cosméticos': '🧴',
        'Higiene': '🚿',
        'Vitaminas': '🌿',
        'Maternidade': '👶'
      };
      return icons[category] || '📦';
    },

    async handleAddToCart(event) {
      event.stopPropagation()
      
      if (!this.product.inStock || this.addingToCart) return
      
      this.addingToCart = true
      
      try {
        await this.$store.dispatch('addToCart', this.product)
        this.addedToCart = true
        this.$emit('add-to-cart', this.product)
        
        // Reset after 2 seconds
        setTimeout(() => {
          this.addedToCart = false
        }, 2000)
        
      } catch (error) {
        console.error('Erro ao adicionar ao carrinho:', error)
      } finally {
        this.addingToCart = false
      }
    }
  }
}
</script>

<style scoped>
.product-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 250ms ease-in-out;
  overflow: hidden;
  background-color: #ffffff;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
  border-color: #d4af37;
}

.product-image-container {
  position: relative;
  text-align: center;
  min-height: 120px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.05), rgba(212, 175, 55, 0.02));
  border-radius: 8px;
  margin: -1rem -1rem 1rem -1rem;
  padding: 1rem;
}

.product-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 0.4rem 0.9rem;
  border-radius: 20px;
  font-size: 0.7rem;
  font-weight: 600;
  z-index: 2;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.product-badge.in-stock {
  background: rgba(45, 80, 22, 0.15);
  color: #2d5016;
  border: 1px solid rgba(45, 80, 22, 0.3);
}

.product-badge.out-of-stock {
  background: rgba(196, 30, 58, 0.15);
  color: #c41e3a;
  border: 1px solid rgba(196, 30, 58, 0.3);
}

.product-icon {
  font-size: 4rem;
  opacity: 0.9;
  transition: transform 250ms ease-in-out;
}

.product-card:hover .product-icon {
  transform: scale(1.1);
}

.product-title {
  font-family: 'Cormorant', serif;
  font-weight: 600;
  font-size: 1.25rem;
  line-height: 1.3;
  margin-bottom: 0.5rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: #1a1a1a;
  letter-spacing: -0.01em;
}

.product-description {
  font-size: 0.9rem;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8rem;
  color: #666666;
}

.category-badge {
  display: inline-block;
  background: rgba(212, 175, 55, 0.15);
  color: #d4af37;
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.product-price {
  font-family: 'Cormorant', serif;
  font-weight: 700;
  font-size: 1.75rem;
  color: #d4af37;
  letter-spacing: -0.01em;
}

.price-installment {
  color: #999999;
  font-size: 0.85rem;
  font-weight: 500;
}

.add-to-cart-btn {
  border-radius: 8px;
  padding: 0.75rem 1rem;
  font-weight: 600;
  transition: all 250ms ease-in-out;
  border: none;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-size: 0.875rem;
  background-color: #d4af37;
  color: #1a1a1a;
}

.add-to-cart-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(212, 175, 55, 0.3);
  background-color: #e8c547;
}

.add-to-cart-btn.btn-success {
  background: #2d5016;
  color: white;
  border-color: #2d5016;
}

.add-to-cart-btn:disabled {
  background: #999999;
  border-color: #999999;
  cursor: not-allowed;
  color: white;
}

/* Responsividade */
@media (max-width: 576px) {
  .product-icon {
    font-size: 3rem;
  }
  
  .product-title {
    font-size: 1.1rem;
  }
  
  .product-price {
    font-size: 1.5rem;
  }
}
</style>
