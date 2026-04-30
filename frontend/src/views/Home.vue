<template>
  <div class="home fade-in-up">
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border cf-spinner" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
    </div>

    <div v-else>
      <!-- Hero Section -->
      <section class="hero-section">
        <div class="container h-100">
          <div class="row align-items-center h-100">
            <div class="col-lg-6">
              <div class="hero-content py-5">
                <span class="hero-badge animate__animated animate__fadeInDown">
                  <i class="fas fa-sparkles me-2"></i>Sua saúde, nossa prioridade
                </span>
                <h1 class="hero-title mb-4 animate__animated animate__fadeInLeft">Sua saúde, cuidada com <span class="text-gradient">inteligência</span></h1>
                <p class="hero-subtitle mb-5 animate__animated animate__fadeInLeft" style="animation-delay: 0.1s">
                  Encontre medicamentos, dermocosméticos e itens de higiene com a agilidade que você precisa e a segurança que você merece.
                </p>
                
                <div class="hero-actions d-flex gap-3 animate__animated animate__fadeInLeft" style="animation-delay: 0.2s">
                  <router-link to="/products" class="btn btn-primary btn-lg px-5 rounded-pill shadow-lg">
                    Explorar Agora
                  </router-link>
                  <button @click="scrollToProducts" class="btn btn-outline-dark btn-lg px-4 rounded-pill">
                    Categorias
                  </button>
                </div>
              </div>
            </div>
            <div class="col-lg-6 d-none d-lg-block animate__animated animate__fadeInRight">
              <div class="hero-visual">
                <img src="/images/medica-header.webp" alt="Dra. ClickFarma" class="hero-img">
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- AI Recommendations (Conditional) -->
      <section v-if="userRecommendations.length > 0" class="recommendations-section py-5">
        <div class="container">
          <div class="ai-banner mb-5 p-4 rounded-4 shadow-sm border-0 d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-3">
              <div class="ai-icon-pulse"><i class="fas fa-robot"></i></div>
              <div>
                <h4 class="mb-0 fw-bold">Recomendados para Você</h4>
                <p class="mb-0 text-muted small">Nossa IA analisou seu histórico para sugerir estes produtos.</p>
              </div>
            </div>
            <div class="d-none d-md-block">
              <span class="badge bg-white text-primary border border-primary-subtle px-3 py-2 rounded-pill">
                Powered by Gemini 1.5 Pro
              </span>
            </div>
          </div>

          <div class="cf-horizontal-scroll">
            <div class="cf-scroll-content">
              <div v-for="product in recommendedProductsList" :key="'rec-'+product.id" class="cf-product-card-horizontal">
                <ProductCard :product="product" @add-to-cart="handleAddToCart" />
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Seções por Categoria -->
      <section id="category-sections" class="products-sections py-5 bg-light">
        <div class="container">
          <div v-for="cat in categories" :key="cat" class="category-row mb-5">
            <div class="d-flex justify-content-between align-items-end mb-4 px-2">
              <div class="d-flex align-items-center gap-3">
                <div>
                  <h3 class="category-title mb-0">{{ cat }}</h3>
                  <div class="cf-h-line"></div>
                </div>
              </div>
              <router-link :to="`/products?category=${cat}`" class="text-decoration-none fw-bold text-primary small">
                Ver Tudo <i class="fas fa-arrow-right ms-1"></i>
              </router-link>
            </div>

            <div class="cf-horizontal-scroll">
              <div class="cf-scroll-content">
                <div v-for="product in getProdutosPorCategoria(cat)" :key="product.id" class="cf-product-card-horizontal">
                  <ProductCard :product="product" @add-to-cart="handleAddToCart" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Features Section -->
      <section class="features-section py-5">
        <div class="container py-lg-4">
          <div class="row g-4">
            <div class="col-md-4" v-for="f in features" :key="f.title">
              <div class="cf-feature-box text-center">
                <div class="feature-icon-blob">{{ f.icon }}</div>
                <h3 class="feature-h">{{ f.title }}</h3>
                <p class="feature-p">{{ f.text }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions, mapState } from 'vuex'
import api from '@/services/api'
import ProductCard from '@/components/products/ProductCard.vue'

export default {
  name: 'Home',
  components: {
    ProductCard
  },
  data() {
    return {
      loading: true,
      allProducts: [],
      userRecommendations: [],
      features: [
        { icon: '🚚', title: 'Entrega Expressa', text: 'Receba seus produtos em até 2 horas em sua residência.' },
        { icon: '🤖', title: 'IA ClickFarma', text: 'Nosso agente inteligente ajuda você a encontrar o melhor produto.' },
        { icon: '💊', title: 'Procedência', text: 'Trabalhamos apenas com farmácias certificadas e produtos originais.' }
      ]
    }
  },
  computed: {
    ...mapState(['products']),
    ...mapGetters(['cartItemsCount']),
    categories() {
      // Retorna apenas as categorias que possuem ao menos um produto
      const allCats = this.$store.getters.categories;
      return allCats.filter(cat => 
        this.allProducts.some(p => p.categoriaNome === cat || p.categoria === cat)
      );
    },
    recommendedProductsList() {
      if (!this.userRecommendations || this.userRecommendations.length === 0) return [];
      // Filtra os produtos reais que estão na lista de IDs recomendados pela IA
      return this.allProducts
        .filter(p => this.userRecommendations.includes(p.id))
        .sort((a, b) => this.userRecommendations.indexOf(a.id) - this.userRecommendations.indexOf(b.id))
        .slice(0, 10);
    }
  },
  async mounted() {
    await this.initHome();
  },
  methods: {
    ...mapActions(['addToCart', 'fetchCategories']),
    async initHome() {
      this.loading = true;
      try {
        // Carregar produtos e categorias reais em paralelo
        await Promise.all([
          this.fetchProductsList(),
          this.fetchCategories()
        ]);

        // Tentar recomendações se logado
        const rawUser = localStorage.getItem('user');
        if (rawUser) {
          const user = JSON.parse(rawUser);
          const resRec = await api.get(`/gemini/recomendacoes?usuarioId=${user.id}`);
          if (resRec.data && resRec.data.recomendacoes) {
             this.userRecommendations = resRec.data.recomendacoes;
          }
        }
      } catch (err) {
        console.error('Erro ao inicializar Home:', err);
      } finally {
        this.loading = false;
      }
    },
    async fetchProductsList() {
      try {
        const resP = await api.get('/produtos');
        this.allProducts = resP.data;
      } catch (err) {
        console.error('Erro ao buscar produtos:', err);
        this.allProducts = this.products; // Fallback
      }
    },
    scrollToProducts() {
      const el = document.getElementById('category-sections');
      if (el) el.scrollIntoView({ behavior: 'smooth' });
    },
    getProdutosPorCategoria(cat) {
      let lista = this.allProducts.filter(p => p.categoriaNome === cat || p.categoria === cat);
      
      // Se tiver recomendações, ordena
      if (this.userRecommendations.length > 0) {
        lista.sort((a, b) => {
          const idxA = this.userRecommendations.indexOf(a.id);
          const idxB = this.userRecommendations.indexOf(b.id);
          if (idxA === -1 && idxB === -1) return 0;
          if (idxA === -1) return 1;
          if (idxB === -1) return -1;
          return idxA - idxB;
        });
      }
      return lista.slice(0, 12);
    },
    handleAddToCart(product) {
      this.addToCart(product);
      alert('Produto adicionado ao carrinho!');
    }
  }
}
</script>

<style scoped>
.home {
  overflow-x: hidden;
  width: 100%;
  position: relative;
  background: #fff;
}

/* HERO PREMIUM */
.hero-section {
  position: relative;
  min-height: 85vh;
  background: #ffffff;
  display: flex;
  align-items: center;
  width: 100%;
  overflow: hidden;
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  background: rgba(42,92,69,0.06);
  color: var(--cf-green);
  border-radius: 100px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 1.5rem;
}
.hero-title {
  font-size: clamp(2.5rem, 6vw, 4.2rem);
  color: #0f172a;
  font-weight: 900;
  line-height: 1.05;
  letter-spacing: -0.02em;
}
.text-gradient {
  background: linear-gradient(135deg, var(--cf-green) 0%, #3d7a5e 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.hero-subtitle {
  font-size: 1.25rem;
  color: #64748b;
  max-width: 540px;
  line-height: 1.6;
}
.hero-visual { position: relative; z-index: 2; }
.hero-img {
  width: 100%;
  border-radius: 30px;
  position: relative;
  z-index: 2;
}


/* RECOMMENDATIONS */
.ai-banner {
  background: linear-gradient(90deg, #f8fafc 0%, #f1f5f9 100%);
  border-left: 5px solid var(--cf-green) !important;
}
.ai-icon-pulse {
  width: 48px; height: 48px;
  background: var(--cf-green);
  color: #fff;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.2rem;
  box-shadow: 0 0 0 0 rgba(42,92,69,0.4);
  animation: ai-pulse 2s infinite;
}
@keyframes ai-pulse {
  0% { box-shadow: 0 0 0 0 rgba(42,92,69,0.4); }
  70% { box-shadow: 0 0 0 15px rgba(42,92,69,0); }
  100% { box-shadow: 0 0 0 0 rgba(42,92,69,0); }
}

/* CARDS */
.premium-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0,0,0,0.03) !important;
}
.premium-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px -15px rgba(0,0,0,0.1) !important;
}
.card-img-wrap {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 1.5rem;
}
.card-img-wrap img { max-height: 100%; width: auto; object-fit: contain; }

.category-title {
  font-family: 'Fraunces', serif;
  font-size: 2.2rem;
  font-weight: 600;
  color: var(--cf-text-dark);
  margin-bottom: 0;
}

.cf-h-line { width: 40px; height: 3px; background: var(--cf-green); border-radius: 2px; margin-top: 4px; }

/* Scroll Horizontal */
.cf-horizontal-scroll {
  overflow-x: auto;
  padding: 10px 0 25px;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
}
.cf-horizontal-scroll::-webkit-scrollbar { display: none; }
.cf-scroll-content { display: flex; gap: 20px; width: max-content; padding: 0 5px; }
.cf-product-card-horizontal { width: 280px; }

@media (max-width: 991px) {
  .hero-section { min-height: 60vh; padding: 60px 0; }
  .hero-title { font-size: 2.8rem; }
  .cf-product-card-horizontal { width: 240px; }
}
</style>