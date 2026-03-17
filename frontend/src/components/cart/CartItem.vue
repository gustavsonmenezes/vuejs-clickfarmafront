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
          <span class="badge bg-secondary">{{ itemCategory }}</span>
        </div>
        
        <!-- Quantidade -->
        <div class="col-md-2">
          <div class="quantity-control">
            <label :for="'quantity-' + itemId" class="form-label small">Quantidade</label>
            <input 
              type="number" 
              :id="'quantity-' + itemId"
              :value="itemQuantity"
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
            @click="$emit('remove-item', itemId)"
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
      required: true
    }
  },
  data() {
    return {
      imageError: false
    }
  },
  computed: {
    // Normalização defensiva das propriedades vindas da API ou do LocalStorage
    itemId() {
      // Se o objeto for diretamente o produto, usa id, senao procura em produto.id
      return this.item.produto?.id || this.item.id;
    },
    itemName() {
      // De acordo com o debug fornecido, a propriedade está vindo como "name" dentro de "produto".
      // Ou seja: item.produto.name
      return this.item.produto?.name || this.item.produto?.nome || this.item.nome || this.item.name || 'Produto sem nome';
    },
    itemDescription() {
      return this.item.produto?.description || this.item.produto?.descricao || this.item.descricao || this.item.description || '';
    },
    itemPriceRaw() {
      return Number(this.item.produto?.price || this.item.produto?.preco || this.item.preco || this.item.price || 0);
    },
    itemQuantity() {
      return Number(this.item.quantidade || this.item.quantity || 1);
    },
    itemCategory() {
      // Adicionando fallback para 'Medicamentos' caso venha nulo
      return this.item.produto?.category || this.item.produto?.categoria?.nome || this.item.produto?.categoriaNome || this.item.categoriaNome || this.item.category || 'Medicamentos';
    },
    itemImageSrc() {
      return this.item.produto?.imagem || this.item.imagem || this.item.image;
    },

    // Computeds Formatados
    truncatedDescription() {
      const desc = this.itemDescription;
      if (!desc) return '';
      const maxLength = 60;
      return desc.length > maxLength
        ? desc.substring(0, maxLength) + '...'
        : desc;
    },
    itemTotal() {
      return (this.itemPriceRaw * this.itemQuantity).toFixed(2).replace('.', ',');
    },
    itemPrice() {
      return this.itemPriceRaw.toFixed(2).replace('.', ',');
    },
    itemImage() {
      return placeholderConfig.getProductImage(this.itemImageSrc);
    }
  },
  methods: {
    handleQuantityChange(event) {
      const quantity = parseInt(event.target.value) || 1
      this.$emit('update-quantity', this.itemId, quantity)
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
