<template>
  <div class="card mb-3 cart-item">
    <div class="card-body">
      <div class="row align-items-center">
        <!-- Imagem do produto -->
        <div class="col-md-2 text-center">
          <img 
            :src="itemImage" 
            :alt="itemName"
            class="img-fluid rounded item-image"
            @error="handleImageError"
            loading="lazy"
          >
        </div>
        
        <!-- Informações do produto -->
        <div class="col-md-4">
          <h5 class="item-name">{{ itemName }}</h5>
          <p class="text-muted item-description">{{ truncatedDescription }}</p>
          <span class="badge bg-secondary">{{ itemCat }}</span>
        </div>
        
        <!-- Quantidade -->
        <div class="col-md-2">
          <div class="quantity-control">
            <label :for="'quantity-' + item.id" class="form-label small">Quantidade</label>
            <input 
              type="number" 
              :id="'quantity-' + item.id"
              :value="item.quantity" 
              min="1" 
              class="form-control"
              @input="handleQuantityChange($event)"
              aria-label="Alterar quantidade"
            >
          </div>
        </div>
        
        <!-- Preço -->
        <div class="col-md-2">
          <p class="fw-bold item-total">R$ {{ itemTotal }}</p>
          <p class="text-muted item-unit-price">R$ {{ itemPrice }} cada</p>
        </div>
        
        <!-- Remover -->
        <div class="col-md-2 text-center">
          <button 
            @click="$emit('remove-item', item.id)" 
            class="btn btn-outline-danger btn-sm"
            aria-label="Remover item do carrinho"
          >
            ❌ Remover
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { placeholderConfig } from '@/utils/placeholder'

export default {
  name: 'CartItem',
  props: {
    item: {
      type: Object,
      required: true,
      validator: (item) => {
        return (
          item.id &&
          (item.name || item.nome) &&
          (item.description || item.descricao) &&
          typeof (item.price || item.preco) === 'number' &&
          typeof item.quantity === 'number'
        )
      }
    }
  },
  computed: {
    itemName() {
      return this.item.name || this.item.nome || 'Produto'
    },
    itemDesc() {
      return this.item.description || this.item.descricao || ''
    },
    itemCat() {
      return this.item.category || this.item.categoriaNome || ''
    },
    itemPriceVal() {
      return this.item.price || this.item.preco || 0
    },
    truncatedDescription() {
      const maxLength = 60
      return this.itemDesc.length > maxLength 
        ? this.itemDesc.substring(0, maxLength) + '...' 
        : this.itemDesc
    },
    itemTotal() {
      return (this.itemPriceVal * this.item.quantity).toFixed(2)
    },
    itemPrice() {
      return this.itemPriceVal.toFixed(2)
    },
    itemImage() {
      return placeholderConfig.getProductImage(this.item.image)
    }
  },
  methods: {
    handleQuantityChange(event) {
      const quantity = parseInt(event.target.value) || 1
      this.$emit('update-quantity', this.item.id, quantity)
    },
    handleImageError(event) {
      placeholderConfig.handleImageError(event)
    }
  }
}
</script>

<style scoped>
.cart-item {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.cart-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.item-image {
  max-height: 80px;
  object-fit: cover;
  background-color: #f8f9fa;
  padding: 4px;
  border: 1px solid #dee2e6;
}

.item-name {
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.item-description {
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.quantity-control input {
  text-align: center;
  max-width: 80px;
}

.item-total {
  font-size: 1.1rem;
  margin-bottom: 0.2rem;
  color: #28a745;
}

.item-unit-price {
  font-size: 0.8rem;
  margin-bottom: 0;
}

.badge {
  font-size: 0.7rem;
}
</style>