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
        <p class="product-description text-muted">{{ product.description }}</p>
        
        <div class="product-meta">
          <span class="category-badge">
            {{ getCategoryIcon(product.category) }} {{ product.category }}
          </span>
        </div>
      </router-link>

      <!-- Price & Actions -->
      <div class="product-footer mt-3">
        <div class="price-section mb-2">
          <h4 class="text-primary mb-1">R$ {{ formattedPrice }}</h4>
          <small class="text-muted">ou 12x de R$ {{ (product.price / 12).toFixed(2) }}</small>
        </div>

        <button 
          @click="handleAddToCart" 
          class="btn btn-primary w-100 add-to-cart-btn"
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
  border: none;
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.15);
}

.product-image-container {
  position: relative;
  text-align: center;
  min-height: 120px;
}

.product-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  z-index: 2;
}

.product-badge.in-stock {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.product-badge.out-of-stock {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f1b0b7;
}

.product-icon {
  font-size: 4rem;
  opacity: 0.8;
  transition: transform 0.3s ease;
}

.product-card:hover .product-icon {
  transform: scale(1.1);
}

.product-title {
  font-weight: 600;
  font-size: 1.1rem;
  line-height: 1.3;
  margin-bottom: 0.5rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-description {
  font-size: 0.9rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8rem;
}

.category-badge {
  display: inline-block;
  background: #e9ecef;
  color: #495057;
  padding: 0.3rem 0.6rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.price-section h4 {
  font-weight: 700;
}

.add-to-cart-btn {
  border-radius: 25px;
  padding: 0.75rem 1rem;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
}

.add-to-cart-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(13, 110, 253, 0.3);
}

.add-to-cart-btn.btn-success {
  background: #198754;
  border-color: #198754;
}

.add-to-cart-btn:disabled {
  background: #6c757d;
  border-color: #6c757d;
  cursor: not-allowed;
}

/* Responsividade */
@media (max-width: 576px) {
  .product-icon {
    font-size: 3rem;
  }
  
  .product-title {
    font-size: 1rem;
  }
}
</style>