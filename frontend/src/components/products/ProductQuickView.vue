<template>
  <Transition name="modal-fade">
    <div v-if="isOpen" class="cf-modal-overlay" @click.self="close">
      <div class="cf-product-modal" role="dialog" aria-modal="true">
        
        <!-- Botão Fechar -->
        <button class="cf-modal-close" @click="close" aria-label="Fechar">
          <i class="fa-solid fa-xmark"></i>
        </button>

        <div class="cf-modal-content row g-0">
          
          <!-- Lado Esquerdo: Galeria -->
          <div class="col-lg-6 cf-modal-gallery">
            <div class="cf-gallery-main">
              <div class="cf-visual-placeholder">
                  <span class="cf-large-icon">{{ getCategoryIcon(product?.category) }}</span>
              </div>
            </div>
            <div v-if="product?.images && product.images.length > 1" class="cf-gallery-thumbs">
              <div 
                v-for="(img, idx) in product.images" 
                :key="idx"
                class="cf-thumb-item"
                :class="{ active: currentImgIdx === idx }"
                @click="currentImgIdx = idx"
              >
                <img :src="img" :alt="product.name">
              </div>
            </div>
          </div>

          <!-- Lado Direito: Info -->
          <div class="col-lg-6 cf-modal-info">
            <div class="cf-info-header">
              <span class="section-eyebrow">{{ product.category }}</span>
              <h2 class="cf-modal-title">{{ product.name }}</h2>
              
              <div class="cf-modal-price-box mb-4">
                <div class="d-flex align-items-baseline gap-1">
                   <span class="price-symbol">R$</span>
                   <span class="cf-modal-price">{{ formattedPrice }}</span>
                </div>
                <span class="cf-modal-installment">ou 12x de R$ {{ installmentPrice }}</span>
              </div>
            </div>

            <div class="cf-info-body">
              <p class="cf-modal-description">{{ product.longDescription || product.description }}</p>
              
              <div class="cf-modal-status mb-4" :class="{ 'is-out': !product.inStock }">
                <i class="fa-solid" :class="product.inStock ? 'fa-circle-check' : 'fa-circle-xmark'"></i>
                {{ product.inStock ? 'Disponível em Estoque' : 'Produto Indisponível' }}
              </div>
            </div>

            <div class="cf-info-actions mt-auto">
              <div class="cf-qty-group" v-if="product.inStock">
                <button @click="qty > 1 && qty--" :disabled="qty <= 1">
                    <i class="fa-solid fa-minus"></i>
                </button>
                <input type="number" v-model.number="qty" min="1" readonly>
                <button @click="qty < 10 && qty++" :disabled="qty >= 10">
                    <i class="fa-solid fa-plus"></i>
                </button>
              </div>

              <button 
                class="btn btn-primary cf-modal-add-btn" 
                :disabled="!product.inStock || adding"
                @click="addToCart"
              >
                <i v-if="adding" class="fa-solid fa-spinner fa-spin me-2"></i>
                <i v-else-if="added" class="fa-solid fa-check me-2"></i>
                <i v-else class="fa-solid fa-cart-plus me-2"></i>
                {{ modalButtonText }}
              </button>
            </div>

            <div class="cf-info-footer mt-4">
              <router-link :to="`/products/${product.id}`" class="cf-full-details-link" @click="close">
                Ver detalhes completos
                <i class="fa-solid fa-arrow-right ms-1"></i>
              </router-link>
            </div>
          </div>

        </div>
      </div>
    </div>
  </Transition>
</template>

<script>
import historicoService from '@/services/historicoService';

export default {
  name: 'ProductQuickView',
  props: {
    isOpen: Boolean,
    product: Object
  },
  data() {
    return {
      currentImgIdx: 0,
      qty: 1,
      adding: false,
      added: false
    }
  },
  computed: {
    formattedPrice() {
      return this.product?.price?.toFixed(2).replace('.', ',') || '0,00'
    },
    installmentPrice() {
      return (this.product?.price / 12).toFixed(2).replace('.', ',') || '0,00'
    },
    modalButtonText() {
      if (this.added) return 'Adicionado!'
      return this.product?.inStock ? 'Adicionar' : 'Indisponível'
    }
  },
  watch: {
    isOpen(newVal) {
      if (newVal) {
        this.currentImgIdx = 0
        this.qty = 1
        this.added = false
        document.body.style.overflow = 'hidden'
        if (this.product?.id) {
          historicoService.registrarVisualizacao(this.product.id);
        }
      } else {
        document.body.style.overflow = ''
      }
    }
  },
  methods: {
    getCategoryIcon(cat) {
      return { 'Medicamentos':'💊','Cosméticos':'🧴','Higiene':'🚿','Vitaminas':'🌿','Maternidade':'👶' }[cat] || '📦'
    },
    close() {
      this.$emit('close')
    },
    async addToCart() {
      if (!this.product.inStock || this.adding) return
      this.adding = true
      try {
        for (let i = 0; i < this.qty; i++) {
          await this.$store.dispatch('addToCart', this.product)
        }
        this.added = true
        setTimeout(() => { this.added = false }, 2000)
      } catch (err) {
        console.error(err)
      } finally {
        this.adding = false
      }
    }
  }
}
</script>

<style scoped>
.cf-modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(28,28,26,0.3);
  backdrop-filter: blur(8px);
  z-index: 11000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.cf-product-modal {
  background: var(--cf-white);
  width: 100%;
  max-width: 920px;
  max-height: 90vh;
  border-radius: var(--cf-r-xl);
  box-shadow: var(--cf-shadow-lg);
  position: relative;
  overflow: hidden;
  animation: modalIn 450ms var(--cf-ease);
}

@keyframes modalIn {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}

.cf-modal-close {
  position: absolute;
  top: 20px; right: 20px;
  width: 38px; height: 38px;
  border-radius: 50%;
  border: none;
  background: var(--cf-green-xlight);
  color: var(--cf-green);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 200ms var(--cf-ease);
}
.cf-modal-close:hover { transform: rotate(90deg); background: var(--cf-green-light); }

.cf-modal-content { height: 100%; min-height: 520px; }

/* Galeria */
.cf-modal-gallery {
  background: var(--cf-ivory);
  display: flex;
  flex-direction: column;
  padding: 50px;
  align-items: center;
  justify-content: center;
  border-right: 1px solid var(--cf-border);
}
.cf-visual-placeholder { 
    width: 100%; aspect-ratio: 1; 
    display: flex; align-items: center; justify-content: center;
}
.cf-large-icon { font-size: 8rem; filter: drop-shadow(0 10px 20px rgba(0,0,0,0.06)); }

.cf-gallery-thumbs { display: flex; gap: 12px; margin-top: 30px; }
.cf-thumb-item {
  width: 64px; height: 64px;
  border-radius: var(--cf-r-md);
  border: 1px solid var(--cf-border);
  cursor: pointer;
  background: white;
  transition: all 200ms var(--cf-ease);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.5rem;
}
.cf-thumb-item.active { border-color: var(--cf-green); background: var(--cf-green-xlight); }

/* Info */
.cf-modal-info { padding: 60px 50px; display: flex; flex-direction: column; }

.cf-modal-title {
  font-family: var(--cf-sans);
  font-size: 2.2rem;
  font-weight: 600;
  color: var(--cf-text-dark);
  line-height: 1.2;
  margin: 8px 0 20px;
}

.cf-modal-price-box { padding: 1.5rem; background: var(--cf-green-xlight); border-radius: var(--cf-r-lg); }
.price-symbol { font-size: 1.2rem; color: var(--cf-text-muted); font-weight: 300; }
.cf-modal-price { font-family: var(--cf-sans); font-size: 2.4rem; color: var(--cf-green); font-weight: 500; }
.cf-modal-installment { font-size: 0.85rem; color: var(--cf-text-muted); display: block; }

.cf-modal-description { font-size: 1rem; color: var(--cf-text-mid); line-height: 1.7; margin-bottom: 30px; }

.cf-modal-status { display: flex; align-items: center; gap: 10px; font-size: 0.9rem; color: var(--cf-green); font-weight: 500; }
.cf-modal-status.is-out { color: var(--cf-danger); }

.cf-info-actions { display: flex; gap: 15px; margin-top: auto; }
.cf-qty-group {
  display: flex;
  align-items: center;
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-md);
  overflow: hidden;
}
.cf-qty-group button { width: 45px; height: 52px; border: none; background: white; color: var(--cf-text-dark); cursor: pointer; transition: background 200ms; }
.cf-qty-group button:hover { background: var(--cf-ivory); }
.cf-qty-group input { width: 45px; height: 52px; border: none; text-align: center; font-weight: 600; font-family: var(--cf-sans); border-left: 1px solid var(--cf-border); border-right: 1px solid var(--cf-border); }

.cf-modal-add-btn { flex: 1; font-size: 0.78rem; letter-spacing: 0.1em; }

.cf-full-details-link { font-size: 0.85rem; color: var(--cf-text-muted); text-decoration: none; border-bottom: 1px solid transparent; transition: all 200ms; }
.cf-full-details-link:hover { color: var(--cf-green); border-color: var(--cf-green); }

.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 300ms ease; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }

@media (max-width: 991px) {
  .cf-modal-content { overflow-y: auto; }
  .cf-modal-gallery { display: none; }
  .cf-modal-info { padding: 40px 30px; }
}
</style>
