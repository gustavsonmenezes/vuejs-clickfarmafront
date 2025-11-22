<template>
  <div class="home">
    <!-- Loading State -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
    </div>

    <!-- Content -->
    <div v-else>
      <!-- Hero Section -->
      <section class="hero-section bg-gradient-primary text-white py-5">
        <div class="container">
          <div class="row align-items-center min-vh-50">
            <div class="col-lg-6">
              <h1 class="display-4 fw-bold mb-4">Sua saúde em primeiro lugar</h1>
              <p class="lead mb-4">Encontre os melhores medicamentos e produtos com entrega rápida e preços justos.</p>
              <div class="d-flex gap-3 flex-wrap">
                <router-link to="/products" class="btn btn-light btn-lg px-4 py-3 fw-semibold">
                  🛍️ Ver Produtos
                </router-link>
                <router-link to="/promotions" class="btn btn-outline-light btn-lg px-4 py-3">
                  💰 Promoções
                </router-link>
              </div>
              <div class="mt-4 d-flex flex-wrap gap-4 text-white-50">
                <div class="d-flex align-items-center">
                  <span class="me-2">✅</span>
                  <small>Entregas em até 2h</small>
                </div>
                <div class="d-flex align-items-center">
                  <span class="me-2">✅</span>
                  <small>Preços competitivos</small>
                </div>
                <div class="d-flex align-items-center">
                  <span class="me-2">✅</span>
                  <small>Farmácia 24h</small>
                </div>
              </div>
            </div>
            <div class="col-lg-6 text-center mt-4 mt-lg-0">
              <div class="hero-visual">
                <div class="floating-pills">
                  <span class="pill pill-1">💊</span>
                  <span class="pill pill-2">🧴</span>
                  <span class="pill pill-3">🌡️</span>
                  <span class="pill pill-4">💊</span>
                </div>
                <div class="delivery-bike">
                  <span class="bike-icon">🚚</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Features Section -->
      <section class="features-section py-5">
        <div class="container">
          <div class="row text-center mb-5">
            <div class="col-12">
              <h2 class="fw-bold text-primary mb-3">Por que escolher a ClickFarma?</h2>
              <p class="text-muted">Tudo que você precisa para sua saúde e bem-estar</p>
            </div>
          </div>
          <div class="row g-4">
            <div class="col-md-4">
              <div class="feature-card card border-0 shadow-sm h-100 text-center p-4">
                <div class="feature-icon-wrapper mb-3">
                  <span class="feature-icon delivery">🚚</span>
                </div>
                <h4 class="fw-bold text-primary">Entrega Rápida</h4>
                <p class="text-muted">Receba seus produtos em até 2 horas na região metropolitana com nossa frota própria.</p>
                <div class="mt-3">
                  <small class="text-success fw-semibold">⏱️ Entrega Expressa Disponível</small>
                </div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="feature-card card border-0 shadow-sm h-100 text-center p-4">
                <div class="feature-icon-wrapper mb-3">
                  <span class="feature-icon payment">💳</span>
                </div>
                <h4 class="fw-bold text-primary">Pagamento Seguro</h4>
                <p class="text-muted">Diversas formas de pagamento com total segurança e certificação digital.</p>
                <div class="mt-3">
                  <small class="text-success fw-semibold">🔒 Ambiente 100% Seguro</small>
                </div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="feature-card card border-0 shadow-sm h-100 text-center p-4">
                <div class="feature-icon-wrapper mb-3">
                  <span class="feature-icon pharmacy">👨‍⚕️</span>
                </div>
                <h4 class="fw-bold text-primary">Farmácia Confiável</h4>
                <p class="text-muted">Profissionais qualificados e farmacêuticos para melhor atendê-lo 24 horas.</p>
                <div class="mt-3">
                  <small class="text-success fw-semibold">📞 Atendimento Online</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Categories Section -->
      <section class="categories-section bg-light py-5">
        <div class="container">
          <div class="row text-center mb-5">
            <div class="col-12">
              <h2 class="fw-bold text-primary mb-3">Categorias em Destaque</h2>
              <p class="text-muted">Encontre tudo que você precisa por categoria</p>
            </div>
          </div>
          <div class="row g-3">
            <div v-for="(category, index) in categories" :key="category" class="col-md-2 col-6">
              <router-link to="/products" class="text-decoration-none">
                <div class="category-card card border-0 shadow-sm h-100 text-center p-3">
                  <div class="category-icon mb-2">
                    <span class="category-emoji" :class="`category-${index + 1}`">
                      {{ getCategoryIcon(category) }}
                    </span>
                  </div>
                  <h6 class="card-title fw-semibold text-dark mb-0 small">{{ category }}</h6>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </section>

      <!-- CTA Section -->
      <section class="cta-section py-5 bg-primary text-white">
        <div class="container text-center">
          <div class="row justify-content-center">
            <div class="col-lg-8">
              <h3 class="fw-bold mb-3">Pronto para cuidar da sua saúde?</h3>
              <p class="mb-4">Faça seu pedido agora e receba no conforto da sua casa</p>
              <router-link to="/products" class="btn btn-light btn-lg px-5 py-3 fw-semibold">
                🛒 Começar a Comprar
              </router-link>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { placeholderConfig } from '@/utils/placeholder'

export default {
  name: 'Home',
  data() {
    return {
      loading: false,
      featuredData: null,
      heroImage: '/images/hero-bg.svg'
    }
  },
  computed: {
    ...mapGetters(['categories'])
  },
  async mounted() {
    console.log('🏠 Componente Home montado - inicializando...');
    await this.initializeHome();
  },
  methods: {
    handleHeroImageError(event) {
      placeholderConfig.handleImageError(event, '/images/hero-bg.svg');
    },
    
    handleFeatureImageError(event) {
      placeholderConfig.handleImageError(event);
    },
    
    getFeatureImage(imagePath) {
      return placeholderConfig.getHomeImage(imagePath);
    },

    getCategoryIcon(category) {
      const icons = {
        'Medicamentos': '💊',
        'Cosméticos': '🧴',
        'Higiene': '🚿',
        'Vitaminas': '🌿',
        'Maternidade': '👶',
        'Bebês': '🍼'
      };
      return icons[category] || '📦';
    },

    async initializeHome() {
      this.loading = true;
      
      try {
        console.log('📊 Carregando dados da homepage...');
        
        // Verificar se as imagens existem
        await this.preloadImages();
        
        // Simular carregamento de dados adicionais
        await this.loadFeaturedData();
        
        // Analytics
        this.trackHomeView();
        
        console.log('✅ Homepage inicializada com sucesso');
      } catch (error) {
        console.error('❌ Erro ao inicializar homepage:', error);
      } finally {
        this.loading = false;
      }
    },
    
    async loadFeaturedData() {
      return new Promise(resolve => {
        setTimeout(() => {
          this.featuredData = {
            banners: [],
            promotions: []
          };
          resolve();
        }, 500);
      });
    },
    
    trackHomeView() {
      if (window.gtag) {
        window.gtag('event', 'page_view', {
          page_title: 'Página Inicial',
          page_location: '/'
        });
      }
    },
    
    async preloadImages() {
      const imagesToPreload = [
        this.heroImage,
        '/images/feature-delivery.svg',
        '/placeholder-product.svg'
      ];
      
      for (const imageUrl of imagesToPreload) {
        const exists = await placeholderConfig.imageExists(imageUrl);
        if (!exists) {
          console.warn(`⚠️ Imagem não encontrada: ${imageUrl}`);
        }
      }
    }
  }
}
</script>

<style scoped>
.hero-section {
  background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%);
  position: relative;
  overflow: hidden;
}

.min-vh-50 {
  min-height: 60vh;
}

.hero-visual {
  position: relative;
  height: 300px;
}

.floating-pills {
  position: relative;
  height: 200px;
}

.pill {
  position: absolute;
  font-size: 2.5rem;
  animation: float 3s ease-in-out infinite;
}

.pill-1 {
  top: 20px;
  left: 50px;
  animation-delay: 0s;
}

.pill-2 {
  top: 60px;
  right: 80px;
  animation-delay: 0.5s;
}

.pill-3 {
  bottom: 40px;
  left: 80px;
  animation-delay: 1s;
}

.pill-4 {
  bottom: 20px;
  right: 50px;
  animation-delay: 1.5s;
}

.delivery-bike {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
}

.bike-icon {
  font-size: 4rem;
  animation: moveBike 4s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

@keyframes moveBike {
  0%, 100% { transform: translateX(-10px); }
  50% { transform: translateX(10px); }
}

.feature-card {
  transition: all 0.3s ease;
  border-radius: 15px;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.1) !important;
}

.feature-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
}

.feature-icon {
  font-size: 2.5rem;
}

.category-card {
  transition: all 0.3s ease;
  border-radius: 12px;
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1) !important;
}

.category-emoji {
  font-size: 2rem;
  display: block;
}

.category-1 { color: #dc3545; }
.category-2 { color: #fd7e14; }
.category-3 { color: #20c997; }
.category-4 { color: #0dcaf0; }
.category-5 { color: #6f42c1; }
.category-6 { color: #e83e8c; }

.cta-section {
  background: linear-gradient(135deg, #198754 0%, #146c43 100%);
}

.btn-light {
  border-radius: 50px;
  transition: all 0.3s ease;
}

.btn-light:hover {
  transform: scale(1.05);
  box-shadow: 0 5px 15px rgba(255,255,255,0.3);
}

.btn-outline-light:hover {
  transform: scale(1.05);
}
</style>