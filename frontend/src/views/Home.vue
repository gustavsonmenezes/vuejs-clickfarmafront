<template>
  <div class="cf-home">

    <!-- ============================
         NAV TOP
         ============================ -->
    <header class="cf-topbar">
      <div class="cf-container cf-topbar-inner">
        <router-link to="/" class="cf-logo">
          <svg class="cf-logo-icon" width="28" height="28" viewBox="0 0 28 28" fill="none">
            <rect width="28" height="28" rx="8" fill="#0D9488"/>
            <path d="M14 7v14M7 14h14" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
          <span class="cf-logo-text">ClickFarma</span>
        </router-link>

        <div class="cf-topbar-right">
          <router-link to="/cart" class="cf-cart-btn">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
            </svg>
            <span v-if="cartItemsCount > 0" class="cf-cart-badge">{{ cartItemsCount }}</span>
          </router-link>
          <router-link to="/profile" class="cf-avatar">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <circle cx="12" cy="8" r="4"/><path d="M4 21v-2a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v2"/>
            </svg>
          </router-link>
        </div>
      </div>
    </header>

    <!-- ============================
         HERO — BUSCA
         ============================ -->
    <section class="cf-hero">
      <div class="cf-container">
        <div class="cf-hero-layout">
          <div class="cf-hero-content">
            <h1 class="cf-hero-title">Sua saúde<br>em boas mãos</h1>
            <p class="cf-hero-sub">Medicamentos, dermocosméticos e cuidados com entrega rápida</p>

            <div class="cf-hero-search">
              <div class="cf-search-box">
                <svg class="cf-search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="7"/><path d="m16.5 16.5 4 4"/>
                </svg>
                <input
                  v-model="searchQuery"
                  type="text"
                  class="cf-search-input"
                  placeholder="Busque por remédio ou sintoma..."
                  @keyup.enter="doSearch"
                />
              </div>
              <button class="cf-btn cf-btn-primary cf-btn-lg cf-search-btn" @click="doSearch">
                Buscar
              </button>
            </div>

            <div class="cf-hero-tags">
              <span class="cf-tag" @click="quickSearch('Dipirona')">Dipirona</span>
              <span class="cf-tag" @click="quickSearch('Dor de cabeça')">Dor de cabeça</span>
              <span class="cf-tag" @click="quickSearch('Amoxicilina')">Amoxicilina</span>
              <span class="cf-tag" @click="quickSearch('Vitamina C')">Vitamina C</span>
            </div>
          </div>

          <div class="cf-hero-visual">
            <img
              src="/images/doctor-hero.svg"
              alt="Médico ClickFarma"
              class="cf-doctor-illustration"
              loading="eager"
            />
          </div>
        </div>
      </div>
      <div class="cf-hero-bg-decor">
        <svg class="cf-hero-blob" viewBox="0 0 600 600" fill="none">
          <circle cx="500" cy="100" r="280" fill="url(#heroGlow)" opacity="0.15"/>
          <defs>
            <radialGradient id="heroGlow" cx="0.5" cy="0.5" r="0.5">
              <stop offset="0%" stop-color="#0D9488"/>
              <stop offset="100%" stop-color="#0284C7" stop-opacity="0"/>
            </radialGradient>
          </defs>
        </svg>
      </div>
    </section>

    <!-- ============================
         CATEGORIAS
         ============================ -->
    <section class="cf-section cf-categories">
      <div class="cf-container">
        <div class="cf-section-head">
          <h2 class="cf-section-title">Categorias</h2>
          <router-link to="/products" class="cf-link-all">Ver todos</router-link>
        </div>
        <div class="cf-cat-grid">
          <router-link
            v-for="cat in categories"
            :key="cat.name"
            :to="`/products?categoria=${cat.name}`"
            class="cf-cat-card"
          >
            <div class="cf-cat-icon" :style="{ background: cat.bg }">
              <span v-html="cat.icon"></span>
            </div>
            <span class="cf-cat-name">{{ cat.name }}</span>
            <span class="cf-cat-count">{{ cat.count }} itens</span>
          </router-link>
        </div>
      </div>
    </section>

    <!-- ============================
         BANNER OFERTAS
         ============================ -->
    <section class="cf-section cf-banner-section">
      <div class="cf-container">
        <div class="cf-banner-card" style="background: linear-gradient(135deg, #0D9488 0%, #0284C7 100%);">
          <div class="cf-banner-content">
            <span class="cf-banner-tag">Oferta especial</span>
              <h2 class="cf-banner-title">Frete grátis na<br/> primeira compra</h2>
            <p class="cf-banner-sub">Use o cupom <strong>BEMVINDO</strong> e ganhe frete grátis em pedidos acima de R$ 79</p>
            <router-link to="/products" class="cf-btn cf-btn-white">Aproveitar</router-link>
          </div>
          <div class="cf-banner-visual">
            <svg width="160" height="160" viewBox="0 0 160 160" fill="none">
              <circle cx="80" cy="80" r="80" fill="rgba(255,255,255,0.08)"/>
              <path d="M56 90c0-8 8-14 16-14h16c8 0 16 6 16 14v6H56v-6z" fill="rgba(255,255,255,0.2)"/>
              <rect x="60" y="76" width="40" height="8" rx="2" fill="rgba(255,255,255,0.2)"/>
              <circle cx="70" cy="94" r="4" fill="rgba(255,255,255,0.3)"/>
              <circle cx="90" cy="94" r="4" fill="rgba(255,255,255,0.3)"/>
              <path d="M48 100h64" stroke="rgba(255,255,255,0.15)" stroke-width="2" stroke-dasharray="4 4"/>
            </svg>
          </div>
        </div>
      </div>
    </section>

    <!-- ============================
         FARMÁCIAS PRÓXIMAS
         ============================ -->
    <section class="cf-section cf-pharmacies">
      <div class="cf-container">
        <div class="cf-section-head">
          <h2 class="cf-section-title">Farmácias parceiras próximas</h2>
          <button class="cf-btn cf-btn-ghost cf-btn-sm" @click="refreshLocation">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2a8 8 0 0 0-8 8c0 5.4 8 12 8 12s8-6.6 8-12a8 8 0 0 0-8-8z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
            {{ userLocation ? 'Atualizar localização' : 'Usar minha localização' }}
          </button>
        </div>
        <div v-if="loadingPharmacies" class="cf-pharmacy-skeleton">
          <div v-for="i in 3" :key="i" class="cf-skeleton" style="height:100px;border-radius:var(--cf-r-lg)"></div>
        </div>
        <div v-else class="cf-pharmacy-grid">
          <div v-for="ph in pharmacies" :key="ph.id" class="cf-pharmacy-card">
            <div class="cf-pharmacy-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--cf-teal)" stroke-width="1.8">
                <rect x="4" y="2" width="16" height="20" rx="2"/><path d="M9 22v-4h6v4"/><path d="M8 12h8"/><path d="M12 8v8"/>
              </svg>
            </div>
            <div class="cf-pharmacy-info">
              <strong class="cf-pharmacy-name">{{ ph.nome }}</strong>
              <span class="cf-pharmacy-addr">{{ ph.endereco }}</span>
            </div>
            <div class="cf-pharmacy-meta">
              <span class="cf-pharmacy-dist">{{ ph.distancia }} km</span>
              <span class="cf-badge" :class="ph.aberto ? 'cf-badge-teal' : 'cf-badge-gray'">
                {{ ph.aberto ? 'Aberto' : 'Fechado' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ============================
         PRODUTOS EM DESTAQUE
         ============================ -->
    <section class="cf-section cf-featured">
      <div class="cf-container">
        <div class="cf-section-head">
          <h2 class="cf-section-title">Mais vendidos</h2>
          <router-link to="/products" class="cf-link-all">Ver todos</router-link>
        </div>
        <div class="cf-featured-grid">
          <div
            v-for="(p, i) in featuredProducts"
            :key="p.id"
            class="cf-feat-card cf-card cf-card-hover"
            :style="{ animationDelay: `${i * 0.08}s` }"
          >
            <div class="cf-feat-visual">
              <div class="cf-feat-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--cf-teal)" stroke-width="1.5">
                  <rect x="4" y="2" width="16" height="20" rx="2"/><path d="M9 22v-4h6v4"/>
                </svg>
              </div>
            </div>
            <div class="cf-feat-body">
              <span v-if="p.tarja" class="cf-stripe" :class="p.tarja === 'PRETA' ? 'cf-stripe-black' : 'cf-stripe-red'">
                {{ p.tarja }}
              </span>
              <h3 class="cf-feat-name">{{ p.nome }}</h3>
              <span class="cf-feat-active">{{ p.principioAtivo }}</span>
              <div class="cf-feat-foot">
                <span class="cf-feat-price">R$ {{ formatPrice(p.preco) }}</span>
                <button class="cf-btn cf-btn-primary cf-btn-sm" @click="addToCart(p)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 5v14m7-7H5"/>
                  </svg>
                  Adicionar
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script>
import { mapState, mapGetters } from 'vuex'

export default {
  name: 'Home',
  data() {
    return {
      searchQuery: '',
      loadingPharmacies: false,
      userLocation: null,
      pharmacies: [
        { id: 1, nome: 'Farmácia Bem Estar', endereco: 'Rua da Consolação, 500', distancia: 0.8, aberto: true },
        { id: 2, nome: 'Drogarias Saúde Total', endereco: 'Av. Paulista, 1200', distancia: 1.5, aberto: true },
        { id: 3, nome: 'Farmácia Popular', endereco: 'Rua Augusta, 300', distancia: 2.1, aberto: false },
      ],
      categories: [
        { name: 'Medicamentos', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="4" y="2" width="16" height="20" rx="2"/><path d="M9 22v-4h6v4"/><path d="M8 12h8"/><path d="M12 8v8"/></svg>', bg: '#F0FDFA', count: 142 },
        { name: 'Dermocosméticos', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="8" r="6"/><path d="M8 14v4a4 4 0 0 0 8 0v-4"/></svg>', bg: '#F0F9FF', count: 89 },
        { name: 'Infantil', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><path d="M9 9h.01"/><path d="M15 9h.01"/></svg>', bg: '#FFFBEB', count: 56 },
        { name: 'Vitaminas', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 2v20"/><path d="M6 8h12"/><path d="M6 12h12"/></svg>', bg: '#F0FDF4', count: 73 },
        { name: 'Higiene', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M20 10c0-4-4-8-8-8S4 6 4 10c0 6 8 12 8 12s8-6 8-12z"/></svg>', bg: '#FEF2F2', count: 45 },
        { name: 'Bem-Estar', icon: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>', bg: '#F5F3FF', count: 38 },
      ],
      featuredProducts: [
        { id: 1, nome: 'Dipirona Sódica 500mg', principioAtivo: 'Dipirona monoidratada', preco: 8.90, tarja: null },
        { id: 2, nome: 'Amoxicilina 500mg', principioAtivo: 'Amoxicilina tri-hidratada', preco: 19.50, tarja: 'VERMELHA' },
        { id: 3, nome: 'Clonazepam 2mg', principioAtivo: 'Clonazepam', preco: 14.30, tarja: 'PRETA' },
        { id: 4, nome: 'Vitamina C 1000mg', principioAtivo: 'Ácido ascórbico', preco: 25.90, tarja: null },
      ]
    }
  },
  computed: {
    ...mapState(['cart']),
    ...mapGetters(['cartItemsCount'])
  },
  mounted() {
    this.detectLocation()
  },
  methods: {
    formatPrice(v) { return (v || 0).toFixed(2).replace('.', ',') },
    doSearch() {
      if (this.searchQuery.trim()) {
        this.$router.push({ path: '/products', query: { search: this.searchQuery } })
      }
    },
    quickSearch(term) {
      this.searchQuery = term
      this.doSearch()
    },
    addToCart(p) {
      this.$store.dispatch('addToCart', p)
    },
    detectLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (pos) => { this.userLocation = { lat: pos.coords.latitude, lng: pos.coords.longitude } },
          () => { this.userLocation = { lat: -23.5505, lng: -46.6333 } },
          { timeout: 5000 }
        )
      } else {
        this.userLocation = { lat: -23.5505, lng: -46.6333 }
      }
    },
    refreshLocation() {
      this.loadingPharmacies = true
      this.detectLocation()
      setTimeout(() => { this.loadingPharmacies = false }, 800)
    }
  }
}
</script>

<style scoped>
/* ============================
   TOPBAR
   ============================ */
.cf-topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--cf-gray-200);
}
.cf-topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}
.cf-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}
.cf-logo-text {
  font-family: var(--cf-heading);
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--cf-gray-900);
}
.cf-topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.cf-cart-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--cf-r-full);
  color: var(--cf-gray-600);
  transition: all var(--cf-fast);
  text-decoration: none;
}
.cf-cart-btn:hover { background: var(--cf-gray-100); color: var(--cf-gray-900); }
.cf-cart-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 18px;
  height: 18px;
  background: var(--cf-teal);
  color: white;
  font-size: 0.6rem;
  font-weight: 700;
  border-radius: var(--cf-r-full);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}
.cf-avatar {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--cf-r-full);
  color: var(--cf-gray-600);
  transition: all var(--cf-fast);
  text-decoration: none;
}
.cf-avatar:hover { background: var(--cf-gray-100); color: var(--cf-gray-900); }

/* ============================
   HERO
   ============================ */
.cf-hero {
  position: relative;
  padding: 3rem 0 2.5rem;
  overflow: hidden;
}
.cf-hero-content { position: relative; z-index: 2; }
.cf-hero-layout {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
}
.cf-hero-visual {
  flex-shrink: 0;
  position: relative;
  z-index: 2;
  display: none;
}
.cf-doctor-illustration {
  width: 320px;
  height: auto;
  display: block;
  animation: cf-float 4s ease-in-out infinite;
}
@keyframes cf-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@media (min-width: 900px) {
  .cf-hero-visual { display: block; }
  .cf-hero-content { max-width: 540px; }
}
.cf-hero-title {
  font-family: var(--cf-heading);
  font-size: clamp(2.2rem, 5vw, 3.5rem);
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: -0.03em;
  color: var(--cf-gray-900);
  margin-bottom: 0.75rem;
}
.cf-hero-sub {
  font-size: 1.1rem;
  color: var(--cf-gray-500);
  margin-bottom: 1.75rem;
  line-height: 1.6;
}
.cf-hero-search {
  display: flex;
  gap: 10px;
  margin-bottom: 1rem;
}
.cf-search-box {
  flex: 1;
  position: relative;
}
.cf-search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--cf-gray-400);
  pointer-events: none;
}
.cf-search-input {
  width: 100%;
  height: 52px;
  padding: 0 1rem 0 3rem;
  border: 1.5px solid var(--cf-gray-200);
  border-radius: var(--cf-r-lg);
  font-family: var(--cf-body);
  font-size: 0.95rem;
  color: var(--cf-gray-900);
  background: var(--cf-white);
  transition: all var(--cf-fast);
}
.cf-search-input:focus {
  outline: none;
  border-color: var(--cf-teal);
  box-shadow: 0 0 0 4px rgba(13,148,136,0.1);
}
.cf-search-input::placeholder { color: var(--cf-gray-400); }
.cf-search-btn { flex-shrink: 0; }
.cf-hero-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.cf-tag {
  display: inline-block;
  padding: 0.35rem 0.85rem;
  border-radius: var(--cf-r-full);
  background: var(--cf-gray-100);
  color: var(--cf-gray-600);
  font-size: 0.8rem;
  cursor: pointer;
  transition: all var(--cf-fast);
  font-family: var(--cf-body);
}
.cf-tag:hover {
  background: var(--cf-teal-xlight);
  color: var(--cf-teal-dark);
}
.cf-hero-bg-decor {
  position: absolute;
  top: 0;
  right: 0;
  width: 50%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}
.cf-hero-blob { width: 100%; height: 100%; }

/* ============================
   SECTION HEAD
   ============================ */
.cf-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
}
.cf-link-all {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--cf-teal);
  text-decoration: none;
  transition: color var(--cf-fast);
}
.cf-link-all:hover { color: var(--cf-teal-dark); text-decoration: underline; }

/* ============================
   CATEGORIAS
   ============================ */
.cf-cat-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}
.cf-cat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 1.25rem 0.75rem;
  border-radius: var(--cf-r-lg);
  border: 1px solid var(--cf-gray-200);
  background: var(--cf-white);
  text-decoration: none;
  transition: all var(--cf-base);
}
.cf-cat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--cf-shadow-md);
  border-color: var(--cf-teal-light);
}
.cf-cat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--cf-r-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cf-gray-700);
}
.cf-cat-name {
  font-family: var(--cf-heading);
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--cf-gray-800);
  text-align: center;
}
.cf-cat-count {
  font-size: 0.72rem;
  color: var(--cf-gray-400);
}

/* ============================
   BANNER
   ============================ */
.cf-banner-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: var(--cf-r-xl);
  padding: 2.5rem 3rem;
  overflow: hidden;
  position: relative;
}
.cf-banner-content { position: relative; z-index: 2; max-width: 480px; }
.cf-banner-tag {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: rgba(255,255,255,0.2);
  color: white;
  border-radius: var(--cf-r-full);
  font-size: 0.72rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.75rem;
}
.cf-banner-title {
  font-family: var(--cf-heading);
  font-size: clamp(1.5rem, 3vw, 2.2rem);
  font-weight: 700;
  color: white;
  line-height: 1.15;
  margin-bottom: 0.75rem;
}
.cf-banner-sub {
  font-size: 0.95rem;
  color: rgba(255,255,255,0.85);
  margin-bottom: 1.25rem;
}
.cf-btn-white {
  background: white;
  color: var(--cf-teal);
  font-weight: 600;
}
.cf-btn-white:hover {
  background: var(--cf-gray-100);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}
.cf-banner-visual { position: relative; z-index: 2; opacity: 0.6; }

/* ============================
   FARMÁCIAS
   ============================ */
.cf-pharmacy-grid {
  display: grid;
  gap: 10px;
}
.cf-pharmacy-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 1rem 1.25rem;
  border-radius: var(--cf-r-lg);
  border: 1px solid var(--cf-gray-200);
  background: var(--cf-white);
  transition: all var(--cf-fast);
}
.cf-pharmacy-card:hover {
  border-color: var(--cf-teal-light);
  box-shadow: var(--cf-shadow-sm);
}
.cf-pharmacy-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--cf-r-lg);
  background: var(--cf-teal-xlight);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.cf-pharmacy-info { flex: 1; min-width: 0; }
.cf-pharmacy-name {
  font-size: 0.9rem;
  color: var(--cf-gray-900);
  display: block;
}
.cf-pharmacy-addr {
  font-size: 0.78rem;
  color: var(--cf-gray-500);
}
.cf-pharmacy-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}
.cf-pharmacy-dist {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--cf-gray-600);
}
.cf-pharmacy-skeleton {
  display: grid;
  gap: 10px;
}

/* ============================
   DESTAQUE / FEATURED
   ============================ */
.cf-featured-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.cf-feat-card {
  display: flex;
  flex-direction: column;
  animation: cf-fadeInUp 0.5s var(--cf-ease) both;
}
.cf-feat-visual {
  background: var(--cf-teal-xlight);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
}
.cf-feat-icon { opacity: 0.5; }
.cf-feat-body {
  padding: 0.9rem 1rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}
.cf-feat-name {
  font-family: var(--cf-heading);
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--cf-gray-900);
  line-height: 1.25;
}
.cf-feat-active {
  font-size: 0.75rem;
  color: var(--cf-gray-500);
  font-style: italic;
}
.cf-feat-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 0.75rem;
  border-top: 1px solid var(--cf-gray-100);
}
.cf-feat-price {
  font-family: var(--cf-heading);
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--cf-gray-900);
}

/* ============================
   RESPONSIVO
   ============================ */
@media (max-width: 1024px) {
  .cf-cat-grid { grid-template-columns: repeat(3, 1fr); }
  .cf-featured-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .cf-cat-grid { grid-template-columns: repeat(3, 1fr); }
  .cf-featured-grid { grid-template-columns: repeat(2, 1fr); }
  .cf-hero { padding: 2rem 0 1.5rem; }
  .cf-hero-search { flex-direction: column; }
  .cf-banner-card { flex-direction: column; text-align: center; padding: 2rem 1.5rem; }
  .cf-banner-visual { display: none; }
  .cf-pharmacy-card { flex-wrap: wrap; }
  .cf-section-head { flex-direction: column; align-items: flex-start; gap: 8px; }
}
@media (max-width: 480px) {
  .cf-cat-grid { grid-template-columns: repeat(2, 1fr); }
  .cf-featured-grid { grid-template-columns: 1fr; }
}
</style>
