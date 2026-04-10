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
      <section class="hero-section text-white py-5">
        <div class="container">
          <div class="row align-items-center min-vh-50">
            <div class="col-lg-6">
              <h1 class="hero-title mb-4">Sua saúde em primeiro lugar</h1>
              <p class="hero-subtitle mb-4">Encontre os melhores medicamentos e produtos com entrega rápida e preços justos.</p>
              <div class="d-flex gap-3 flex-wrap">
                <router-link to="/products" class="btn btn-accent btn-lg px-4 py-3">
                  🛍️ Ver Produtos
                </router-link>
                <router-link to="/promotions" class="btn btn-outline-light btn-lg px-4 py-3">
                  💰 Promoções
                </router-link>
              </div>
              <div class="mt-4 d-flex flex-wrap gap-4 hero-benefits">
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
              <h2 class="section-title mb-3">Por que escolher a ClickFarma?</h2>
              <p class="section-subtitle">Tudo que você precisa para sua saúde e bem-estar</p>
            </div>
          </div>
          <div class="row g-4">
            <div class="col-md-4">
              <div class="feature-card card border-0 h-100 text-center p-4">
                <div class="feature-icon-wrapper mb-3">
                  <span class="feature-icon delivery">🚚</span>
                </div>
                <h4 class="feature-title">Entrega Rápida</h4>
                <p class="feature-text">Receba seus produtos em até 2 horas na região metropolitana com nossa frota própria.</p>
                <div class="mt-3">
                  <small class="feature-badge">⏱️ Entrega Expressa Disponível</small>
                </div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="feature-card card border-0 h-100 text-center p-4">
                <div class="feature-icon-wrapper mb-3">
                  <span class="feature-icon payment">💳</span>
                </div>
                <h4 class="feature-title">Pagamento Seguro</h4>
                <p class="feature-text">Diversas formas de pagamento com total segurança e certificação digital.</p>
                <div class="mt-3">
                  <small class="feature-badge">🔒 Ambiente 100% Seguro</small>
                </div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="feature-card card border-0 h-100 text-center p-4">
                <div class="feature-icon-wrapper mb-3">
                  <span class="feature-icon pharmacy">👨‍⚕️</span>
                </div>
                <h4 class="feature-title">Farmácia Confiável</h4>
                <p class="feature-text">Profissionais qualificados e farmacêuticos para melhor atendê-lo 24 horas.</p>
                <div class="mt-3">
                  <small class="feature-badge">📞 Atendimento Online</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Farmácias Próximas Section -->
      <section class="farmacias-section py-5">
        <div class="container">
          <div class="row justify-content-center">
            <div class="col-lg-8">
              <div class="text-center mb-4">
                <h2 class="section-title mb-3">📍 Encontre Farmácias Próximas</h2>
                <p class="section-subtitle">Localize as farmácias mais perto de você com facilidade e rapidez</p>
              </div>
              <FarmaciasProximas />
            </div>
          </div>
        </div>
      </section>

      <!-- Categories Section -->
      <section class="categories-section py-5">
        <div class="container">
          <div class="row text-center mb-5">
            <div class="col-12">
              <h2 class="section-title mb-3">Categorias em Destaque</h2>
              <p class="section-subtitle">Encontre tudo que você precisa por categoria</p>
            </div>
          </div>
          <div class="row g-3">
            <div v-for="(category, index) in categories" :key="category" class="col-md-2 col-6">
              <router-link to="/products" class="text-decoration-none">
                <div class="category-card card border-0 h-100 text-center p-3">
                  <div class="category-icon mb-2">
                    <span class="category-emoji" :class="`category-${index + 1}`">
                      {{ getCategoryIcon(category) }}
                    </span>
                  </div>
                  <h6 class="category-name fw-semibold mb-0 small">{{ category }}</h6>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </section>

      <!-- CTA Section -->
      <section class="cta-section py-5 text-white">
        <div class="container text-center">
          <div class="row justify-content-center">
            <div class="col-lg-8">
              <h3 class="cta-title fw-bold mb-3">Pronto para cuidar da sua saúde?</h3>
              <p class="cta-subtitle mb-4">Faça seu pedido agora e receba no conforto da sua casa</p>
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
import FarmaciasProximas from '@/components/common/FarmaciasProximas.vue'

export default {
  name: 'Home',
  components: {
    FarmaciasProximas
  },
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
/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  position: relative;
  overflow: hidden;
}

.hero-title {
  font-family: 'Cormorant', serif;
  font-size: 3.5rem;
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1.1;
  margin-bottom: 1.5rem;
}

.hero-subtitle {
  font-size: 1.25rem;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 2rem;
}

.hero-benefits {
  color: rgba(255, 255, 255, 0.8);
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

/* Features Section */
.features-section {
  background-color: #ffffff;
}

.section-title {
  font-family: 'Cormorant', serif;
  font-size: 2.5rem;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: -0.02em;
}

.section-subtitle {
  font-size: 1rem;
  color: #999999;
  font-weight: 500;
}

.feature-card {
  transition: all 250ms ease-in-out;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
  border-color: #d4af37;
}

.feature-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1), rgba(212, 175, 55, 0.05));
}

.feature-icon {
  font-size: 2.5rem;
}

.feature-title {
  font-family: 'Cormorant', serif;
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 1rem;
}

.feature-text {
  color: #666666;
  font-size: 0.95rem;
  line-height: 1.8;
}

.feature-badge {
  display: inline-block;
  background: rgba(212, 175, 55, 0.15);
  color: #d4af37;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-size: 0.75rem;
}

/* Farmácias Section */
.farmacias-section {
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
}

/* Categories Section */
.categories-section {
  background-color: #ffffff;
}

.category-card {
  transition: all 250ms ease-in-out;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.category-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #d4af37;
}

.category-emoji {
  font-size: 2.5rem;
  display: block;
  transition: transform 250ms ease-in-out;
}

.category-card:hover .category-emoji {
  transform: scale(1.1);
}

.category-name {
  font-size: 0.95rem;
  color: #1a1a1a;
  font-weight: 600;
}

.category-1 { color: #d4af37; }
.category-2 { color: #2d5016; }
.category-3 { color: #0066cc; }
.category-4 { color: #c41e3a; }
.category-5 { color: #d4af37; }
.category-6 { color: #2d5016; }

/* CTA Section */
.cta-section {
  background: linear-gradient(135deg, #d4af37 0%, #e8c547 100%);
  color: #1a1a1a;
}

.cta-title {
  font-family: 'Cormorant', serif;
  font-size: 2.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #1a1a1a;
}

.cta-subtitle {
  font-size: 1.1rem;
  color: rgba(26, 26, 26, 0.8);
  line-height: 1.8;
}

.btn-light {
  border-radius: 50px;
  transition: all 250ms ease-in-out;
  background-color: #ffffff;
  color: #1a1a1a;
  font-weight: 600;
}

.btn-light:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  background-color: #f5f5f5;
}

.btn-outline-light {
  border-color: rgba(255, 255, 255, 0.5);
  color: rgba(255, 255, 255, 0.9);
  border-radius: 50px;
  transition: all 250ms ease-in-out;
}

.btn-outline-light:hover {
  background-color: rgba(255, 255, 255, 0.15);
  border-color: white;
  color: white;
  transform: translateY(-2px);
}

/* Deep selectors para customização de componentes filhos */
:deep(.farmacias-proximas .card) {
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 250ms ease-in-out;
}

:deep(.farmacias-proximas .card:hover) {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #d4af37;
}

:deep(.farmacias-proximas .btn-primary) {
  border-radius: 25px;
  transition: all 250ms ease-in-out;
  background-color: #1a1a1a;
}

:deep(.farmacias-proximas .btn-primary:hover) {
  transform: translateY(-2px);
  background-color: #2d2d2d;
}

:deep(.farmacias-proximas .btn-outline-primary) {
  border-radius: 20px;
  transition: all 250ms ease-in-out;
  border-color: #1a1a1a;
  color: #1a1a1a;
}

:deep(.farmacias-proximas .btn-outline-primary:hover) {
  background-color: #1a1a1a;
  color: white;
  transform: translateY(-2px);
}

/* Responsividade */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-subtitle {
    font-size: 1rem;
  }
  
  .section-title {
    font-size: 1.75rem;
  }
  
  .cta-title {
    font-size: 1.75rem;
  }
}
</style>
