<template>
  <div class="product-detail-page">
    <div v-if="product" class="container py-lg-5 py-4">
      
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb" class="mb-5 fade-in-up">
        <ol class="breadcrumb cf-breadcrumb">
          <li class="breadcrumb-item"><router-link to="/">Home</router-link></li>
          <li class="breadcrumb-item"><router-link to="/products">Produtos</router-link></li>
          <li class="breadcrumb-item active">{{ product.name }}</li>
        </ol>
      </nav>

      <div class="row g-5">
        <!-- Esquerda: Imagem -->
        <div class="col-md-6 fade-in-up">
          <div class="detail-gallery">
            <div class="main-image-wrapper mb-3 overflow-hidden">
              <img v-if="product.imageUrl" :src="product.imageUrl" class="product-img-large" style="width:100%; height:100%; object-fit: contain;">
              <div v-else class="cf-product-visual-large">
                <span class="cf-visual-icon">{{ getCategoryIcon(product.category) }}</span>
              </div>
            </div>
            <div v-if="product.images && product.images.length > 1" class="thumbnail-row d-flex gap-3">
               <div v-for="(img, i) in product.images" :key="i" class="thumb-wrapper">
                 <img :src="img" class="thumb-detail">
               </div>
            </div>
          </div>
        </div>

        <!-- Direita: Info -->
        <div class="col-md-6 fade-in-up" style="animation-delay: 0.1s">
          <div class="detail-info">
            <span class="section-eyebrow mb-2 d-block">{{ product.category }}</span>
            <h1 class="section-title detail-h1 mb-4">{{ product.name }}</h1>
            
            <div class="detail-price-box p-4 mb-4">
              <div class="d-flex align-items-baseline gap-2">
                <span class="price-currency">R$</span>
                <h2 class="price-amount">{{ product.price.toFixed(2).replace('.', ',') }}</h2>
              </div>
              <p class="price-installments mb-0">ou 12x de <strong>R$ {{ (product.price / 12).toFixed(2).replace('.', ',') }}</strong> sem juros</p>
            </div>

            <div class="detail-status mb-4">
               <span class="cf-badge" :class="product.inStock ? 'badge-success' : 'badge-danger'">
                 <i class="fa-solid" :class="product.inStock ? 'fa-check' : 'fa-xmark'"></i>
                 {{ product.inStock ? 'Disponível em Estoque' : 'Produto Indisponível' }}
               </span>
            </div>

            <div class="detail-description mb-5">
              <h5 class="info-label mb-3">Descrição do Produto</h5>
              <p class="info-text">{{ product.longDescription || product.description }}</p>
            </div>

            <div class="detail-actions d-flex gap-3">
              <button 
                @click="handleAddToCart(product)" 
                class="btn btn-primary btn-lg flex-grow-1"
                :disabled="!product.inStock"
              >
                <i class="fa-solid fa-cart-plus me-2"></i>
                {{ product.inStock ? 'Adicionar ao Carrinho' : 'Indisponível' }}
              </button>
              <button @click="$router.back()" class="btn btn-outline-primary btn-lg">
                <i class="fa-solid fa-arrow-left"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="container text-center py-5">
      <div class="empty-state py-5">
        <i class="fa-solid fa-box-open mb-4 text-faint" style="font-size: 4rem;"></i>
        <h2 class="section-title">Produto não encontrado</h2>
        <p class="text-muted mb-4">O item solicitado pode ter sido removido ou está temporariamente fora do ar.</p>
        <router-link to="/products" class="btn btn-primary">Ver Catálogo Completo</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex'
import api from '@/services/api'

export default {
  name: 'ProductDetail',
  data() {
    return {
      product: null
    }
  },
  computed: {
    ...mapState(['products'])
  },
  async mounted() {
    const productId = parseInt(this.$route.params.id)
    // Se não tiver no Vuex, buscar da API (melhor para links diretos)
    if (!this.product) {
      try {
        const res = await api.get(`/produtos/${productId}`);
        this.product = res.data;
      } catch (err) {
        this.$router.push('/products');
        return;
      }
    }
    
    this.registrarVisualizacao(productId);
  },
  methods: {
    ...mapActions(['addToCart']),
    async registrarVisualizacao(productId) {
      try {
        const rawUser = localStorage.getItem('user');
        if (rawUser) {
          const user = JSON.parse(rawUser);
          await api.post('/historico/visualizacao', {
            usuarioId: user.id,
            produtoId: productId
          });
        }
      } catch (e) { /* ignore */ }
    },
    getCategoryIcon(cat) {
      return { 'Medicamentos':'💊','Cosméticos':'🧴','Higiene':'🚿','Vitaminas':'🌿','Maternidade':'👶' }[cat] || '📦'
    },
    handleAddToCart(product) {
      this.addToCart(product);
      // Feedback opicional
    }
  }
}
</script>

<style scoped>
.product-detail-page {
  background: var(--cf-white);
  min-height: 85vh;
}

/* BREADCRUMB */
.cf-breadcrumb { background: transparent; padding: 0; margin: 0; }
.cf-breadcrumb .breadcrumb-item a { color: var(--cf-text-muted); text-decoration: none; font-size: 0.85rem; }
.cf-breadcrumb .breadcrumb-item.active { color: var(--cf-green); font-weight: 500; font-size: 0.85rem; }

/* GALERIA */
.detail-gallery { position: sticky; top: 100px; }
.main-image-wrapper {
  background: var(--cf-green-xlight);
  aspect-ratio: 1;
  border-radius: var(--cf-r-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--cf-border);
}
.cf-visual-icon { font-size: 8rem; filter: drop-shadow(0 10px 20px rgba(0,0,0,0.06)); }

.thumb-wrapper {
  width: 80px; height: 80px;
  background: var(--cf-ivory);
  border-radius: var(--cf-r-md);
  border: 1px solid var(--cf-border);
  overflow: hidden;
  cursor: pointer;
}

/* INFO */
.detail-h1 { font-size: 3.2rem; color: var(--cf-green); }

.detail-price-box {
  background: var(--cf-ivory);
  border-radius: var(--cf-r-lg);
  border-left: 4px solid var(--cf-green);
}
.price-currency { font-size: 1.2rem; font-weight: 300; color: var(--cf-text-muted); }
.price-amount { font-family: var(--cf-sans); font-size: 2.8rem; color: var(--cf-text-dark); margin: 0; font-weight: 500; }
.price-installments { font-size: 0.95rem; color: var(--cf-text-mid); }

.cf-badge {
  font-size: 0.72rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  padding: 6px 14px;
  border-radius: 100px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.badge-success { background: var(--cf-green-light); color: var(--cf-green); }
.badge-danger { background: #F9EDED; color: var(--cf-danger); }

.info-label { 
    font-size: 0.7rem; 
    text-transform: uppercase; 
    letter-spacing: 0.12em; 
    color: var(--cf-text-faint); 
    font-weight: 600;
}
.info-text { color: var(--cf-text-mid); line-height: 1.8; font-size: 1.05rem; }

.btn-lg { padding: 1rem 2rem; font-size: 0.85rem; }

@media (max-width: 991.98px) {
  .detail-h1 { font-size: 2.4rem; }
  .price-amount { font-size: 2.2rem; }
}
</style>
