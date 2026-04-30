<template>
  <div class="products-page">
    <!-- Header Section — Limpo e Editorial -->


    <div class="filters-wrapper">
      <div class="container">
        <div class="filters-section">
        <div class="row g-3">
          <div class="col-lg-4 col-md-6">
            <div class="filter-group">
              <label class="form-label">📂 Categoria</label>
              <select v-model="filters.category" class="form-select">
                <option value="">Todas as especialidades</option>
                <option v-for="cat in categoriesList" :key="cat" :value="cat">
                  {{ cat }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-lg-3 col-md-6">
            <div class="filter-group">
              <label class="form-label">💰 Ordenar por</label>
              <select v-model="filters.sortBy" class="form-select">
                <optgroup label="Valor">
                  <option value="price">Menor Preço</option>
                  <option value="price_desc">Maior Preço</option>
                </optgroup>
                <optgroup label="Ordem">
                  <option value="name">A - Z</option>
                  <option value="name_desc">Z - A</option>
                </optgroup>
                <optgroup label="Prioridade">
                  <option value="relevance">Relevância</option>
                </optgroup>
              </select>
            </div>
        </div>
      </div>
    </div>
  </div>
</div>

<div class="container mt-4">
      <!-- Active Filters -->
      <div v-if="hasActiveFilters" class="active-filters">
        <span class="fw-medium small me-2">Filtrado por:</span>
        <div class="d-flex flex-wrap gap-2">
          <span v-if="searchTerm" class="filter-tag">
            "{{ searchTerm }}"
            <i class="fas fa-times" @click="searchTerm = ''" style="cursor:pointer"></i>
          </span>
          <span v-if="filters.category" class="filter-tag">
            {{ filters.category }}
            <i class="fas fa-times" @click="filters.category = ''" style="cursor:pointer"></i>
          </span>
          <button @click="clearAllFilters" class="clear-all">Limpar filtros</button>
        </div>
      </div>
    </div>

    <div class="container">

      <!-- Loading State -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border cf-spinner" style="width: 2.5rem; height: 2.5rem;" role="status"></div>
        <p class="mt-3 text-muted fw-light">Organizando catálogo...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">⚠️</div>
        <h4 class="mb-3">Conexão interrompida</h4>
        <p class="text-muted mb-4">{{ error }}</p>
        <button @click="retryLoading" class="btn btn-primary">
          Tentar novamente
        </button>
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredProducts.length === 0" class="empty-state">
        <div class="empty-icon">🔍</div>
        <h4 class="mb-3">Nada encontrado</h4>
        <p class="text-muted mb-4">Tente outros termos ou remova os filtros ativos.</p>
        <button @click="clearAllFilters" class="btn btn-primary">Ver Tudo</button>
      </div>

      <!-- Products Grid -->
      <div v-else class="products-grid">
        <div class="row g-4">
          <div
              v-for="product in filteredProducts"
              :key="product.id"
              class="col-xl-3 col-lg-4 col-md-6"
          >
            <ProductCard
                :product="product"
                @add-to-cart="handleAddToCart"
            />
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div v-if="!loading && !error && filteredProducts.length > 0" class="mt-5 pt-lg-4">
        <div class="quick-actions-card text-center">
          <h2 class="qa-title mb-4">Ainda com dúvidas sobre sua prescrição?</h2>
          <div class="d-flex flex-wrap justify-content-center gap-3">
            <button class="btn btn-outline-primary shadow-sm bg-white">
              <i class="fas fa-headset me-2"></i>Chamar Farmacêutico
            </button>
            <button class="btn btn-primary">
              <i class="fas fa-prescription me-2"></i>Enviar Receita
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import ProductCard from '@/components/products/ProductCard.vue'

export default {
  name: 'Products',
  components: {
    ProductCard
  },
  data() {
    return {
      searchTerm: '',
      filters: {
        category: '',
        sortBy: 'name',
        stock: 'all'
      },
      loading: false,
      error: null
    }
  },
  watch: {
    '$route.query.q': {
      immediate: true,
      handler(newVal) {
        this.searchTerm = newVal || '';
      }
    },
    '$route.query.category': {
      immediate: true,
      handler(newVal) {
        if (newVal) this.filters.category = newVal;
      }
    }
  },
  computed: {
    ...mapState(['products']),

    categoriesList() {
      if (!this.products || !Array.isArray(this.products)) {
        return []
      }
      const categories = [...new Set(this.products.map(p => p.category))].filter(Boolean)
      return categories.sort()
    },

    filteredProducts() {
      if (!this.products || !Array.isArray(this.products)) {
        return []
      }

      let filtered = this.products.filter(product => {
        const searchTermLower = this.searchTerm.toLowerCase()
        const matchesSearch = !this.searchTerm ||
            (product.name && product.name.toLowerCase().includes(searchTermLower)) ||
            (product.description && product.description.toLowerCase().includes(searchTermLower))

        const matchesCategory = !this.filters.category || product.category === this.filters.category

        // CORREÇÃO: Usa estoque ao invés de inStock
        const matchesStock = this.filters.stock === 'all' ||
            (this.filters.stock === 'in_stock' && product.estoque > 0) ||
            (this.filters.stock === 'out_of_stock' && product.estoque === 0)

        return matchesSearch && matchesCategory && matchesStock
      })

      // Ordenação
      switch (this.filters.sortBy) {
        case 'name_desc':
          filtered.sort((a, b) => (b.name || '').localeCompare(a.name || ''))
          break
        case 'price':
          filtered.sort((a, b) => (a.price || 0) - (b.price || 0))
          break
        case 'price_desc':
          filtered.sort((a, b) => (b.price || 0) - (a.price || 0))
          break
        case 'relevance':
          filtered.sort((a, b) => {
            const stockA = a.estoque || 0;
            const stockB = b.estoque || 0;
            if (stockA > 0 && stockB === 0) return -1;
            if (stockA === 0 && stockB > 0) return 1;
            return (a.name || '').localeCompare(b.name || '');
          })
          break
        default: // 'name'
          filtered.sort((a, b) => (a.name || '').localeCompare(b.name || ''))
          break
      }

      return filtered
    },

    hasActiveFilters() {
      return this.searchTerm || this.filters.category || this.filters.stock !== 'all'
    }
  },
  async mounted() {
    console.log('🚀 Componente Products montado - inicializando...')
    await this.initializeComponent()
  },
  methods: {
    ...mapActions(['fetchProducts']),

    getCategoryIcon(category) {
      const icons = {
        'Medicamentos': '💊',
        'Cosméticos': '🧴',
        'Higiene': '🚿',
        'Vitaminas': '🌿',
        'Maternidade': '👶'
      }
      return icons[category] || '📦'
    },

    async initializeComponent() {
      this.loading = true
      this.error = null

      try {
        console.log('📦 Buscando produtos da API...')
        await this.fetchProducts()
        console.log('✅ Produtos carregados com sucesso')
        console.log('📊 Total de produtos:', this.products?.length || 0)
        if (this.products && this.products.length > 0) {
          console.log('📋 Exemplo do primeiro produto:', this.products[0])
        }
        this.trackPageView()
      } catch (err) {
        console.error('❌ Erro ao carregar produtos:', err)
        this.error = 'Erro ao carregar produtos. Tente novamente.'
      } finally {
        this.loading = false
      }
    },

    trackPageView() {
      if (window.gtag) {
        window.gtag('event', 'page_view', {
          page_title: 'Página de Produtos',
          page_location: '/products'
        })
      }
    },

    handleAddToCart(product) {
      console.log(`📦 Produto adicionado ao carrinho: ${product.name}`)
      // Dispara evento para adicionar ao carrinho
      this.$emit('add-to-cart', product)
    },

    retryLoading() {
      this.initializeComponent()
    },

    clearAllFilters() {
      this.searchTerm = ''
      this.filters = {
        category: '',
        sortBy: 'name',
        stock: 'all'
      }
    }
  }
}
</script>

<style scoped>
.products-page {
  background: var(--cf-ivory);
  min-height: 100vh;
  padding-bottom: 4rem;
}

.products-header {
  background: var(--cf-white);
  border-bottom: 1px solid var(--cf-border);
  padding: 3rem 0;
  margin-bottom: 3rem;
  position: relative;
}

.products-header h1 {
  font-family: var(--cf-serif);
  font-size: clamp(2.2rem, 4vw, 3rem);
  color: var(--cf-text-dark);
  font-weight: 400;
  margin-bottom: 0.5rem;
}

.products-header p {
  color: var(--cf-text-muted);
  font-weight: 300;
  font-size: 1.1rem;
}

.count-badge {
  background: var(--cf-green-xlight);
  color: var(--cf-green);
  padding: 0.5rem 1.2rem;
  border-radius: 100px;
  font-size: 0.75rem;
  letter-spacing: 0.05em;
  font-weight: 500;
  border: 1px solid var(--cf-green-light);
}

/* SEARCH & FILTERS */
.filters-wrapper {
  background: var(--cf-white);
  border-bottom: 1px solid var(--cf-border);
  z-index: 1000;
  position: sticky;
  top: 81px; /* Navbar (66px) + Top Strip (~15px) */
  transition: all 0.3s ease;
}

.filters-section {
  padding: 1rem 0;
}

@media (max-width: 991px) {
  .filters-wrapper {
    top: 60px;
  }
}

.search-box { position: relative; }
.search-input {
  padding-left: 2.8rem;
  background: var(--cf-ivory);
  border: 1px solid var(--cf-border-mid);
}
.search-input:focus {
  background: var(--cf-white);
  border-color: var(--cf-green);
}
.search-icon {
  position: absolute;
  left: 1.1rem; top: 50%;
  transform: translateY(-50%);
  color: var(--cf-text-faint);
  font-size: 1rem;
}

.filter-group label {
  font-size: 0.68rem;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--cf-text-muted);
  margin-bottom: 0.4rem;
  font-weight: 500;
}

.active-filters {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0.8rem 1rem;
  background: var(--cf-green-xlight);
  border-radius: var(--cf-r-md);
  margin-top: 1rem;
}
.filter-tag {
  background: var(--cf-white);
  color: var(--cf-green);
  border: 1px solid var(--cf-green-light);
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.75rem;
  display: flex;
  align-items: center;
  gap: 6px;
}
.clear-all {
  font-size: 0.72rem;
  color: var(--cf-text-muted);
  text-decoration: underline;
  background: none; border: none;
  cursor: pointer;
}

/* GRID */
.products-grid { animation: fadeInUp 0.6s var(--cf-ease) both; }

/* STATES */
.cf-spinner { color: var(--cf-green); }

.empty-state, .error-state {
  padding: 5rem 1rem;
  text-align: center;
}
.empty-icon, .error-icon {
  font-size: 4rem;
  color: var(--cf-cream);
  margin-bottom: 1.5rem;
}

/* QUICK ACTIONS */
.quick-actions-card {
  background: var(--cf-cream);
  border: 1px dashed var(--cf-border-mid);
  border-radius: var(--cf-r-xl);
  padding: 3rem 2rem;
}
.qa-title { font-family: var(--cf-sans); font-size: 1.6rem; font-weight: 600; color: var(--cf-text-dark); }

/* RESPONSIVO */
@media (max-width: 768px) {
  .products-header { padding: 2.5rem 0; text-align: center; }
  .products-header .col-md-4 { margin-top: 1.5rem; }
}
</style>