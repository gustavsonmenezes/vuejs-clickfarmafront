<template>
  <div class="products-page">
    <!-- Header Section -->
    <section class="products-header bg-primary text-white py-4 mb-4">
      <div class="container">
        <div class="row align-items-center">
          <div class="col-md-8">
            <h1 class="display-6 fw-bold mb-2">💊 Nossos Produtos</h1>
            <p class="lead mb-0">Encontre os melhores medicamentos e produtos para sua saúde</p>
          </div>
          <div class="col-md-4 text-md-end">
            <div class="products-count">
              <span class="badge bg-light text-primary fs-6">
                {{ filteredProducts.length || 0 }} produtos encontrados
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div class="container">
      <!-- Filters & Search Section -->
      <div class="filters-section mb-4">
        <div class="row g-3">
          <div class="col-lg-5 col-md-6">
            <div class="search-box">
              <i class="fas fa-search search-icon"></i>
              <input 
                v-model="searchTerm" 
                type="text" 
                class="form-control search-input" 
                placeholder="Buscar produtos por nome ou descrição..."
              >
            </div>
          </div>
          <div class="col-lg-3 col-md-6">
            <div class="filter-group">
              <label class="form-label fw-semibold">📁 Categoria</label>
              <select v-model="filters.category" class="form-select">
                <option value="">Todas as categorias</option>
                <option v-for="cat in categories" :key="cat" :value="cat">
                  {{ getCategoryIcon(cat) }} {{ cat }}
                </option>
              </select>
            </div>
          </div>
          <div class="col-lg-2 col-md-6">
            <div class="filter-group">
              <label class="form-label fw-semibold">🔀 Ordenar</label>
              <select v-model="filters.sortBy" class="form-select">
                <option value="name">📝 Nome A-Z</option>
                <option value="name_desc">📝 Nome Z-A</option>
                <option value="price">💰 Menor preço</option>
                <option value="price_desc">💰 Maior preço</option>
              </select>
            </div>
          </div>
          <div class="col-lg-2 col-md-6">
            <div class="filter-group">
              <label class="form-label fw-semibold">📦 Estoque</label>
              <select v-model="filters.stock" class="form-select">
                <option value="all">Todos</option>
                <option value="in_stock">Em estoque</option>
                <option value="out_of_stock">Fora de estoque</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Active Filters -->
        <div v-if="hasActiveFilters" class="active-filters mt-3">
          <div class="d-flex flex-wrap gap-2 align-items-center">
            <span class="fw-semibold">Filtros ativos:</span>
            <span v-if="searchTerm" class="badge bg-primary">
              Busca: "{{ searchTerm }}"
              <button @click="searchTerm = ''" class="btn-close btn-close-white ms-1" style="font-size: 0.7rem;"></button>
            </span>
            <span v-if="filters.category" class="badge bg-success">
              {{ filters.category }}
              <button @click="filters.category = ''" class="btn-close btn-close-white ms-1" style="font-size: 0.7rem;"></button>
            </span>
            <span v-if="filters.stock !== 'all'" class="badge bg-warning text-dark">
              {{ filters.stock === 'in_stock' ? 'Em estoque' : 'Fora de estoque' }}
              <button @click="filters.stock = 'all'" class="btn-close ms-1" style="font-size: 0.7rem;"></button>
            </span>
            <button @click="clearAllFilters" class="btn btn-sm btn-outline-secondary">
              Limpar todos
            </button>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="text-center py-5">
        <div class="loading-spinner">
          <div class="spinner-border text-primary" style="width: 3rem; height: 3rem;" role="status">
            <span class="visually-hidden">Carregando...</span>
          </div>
          <p class="mt-3 text-muted">Carregando produtos...</p>
        </div>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="error-state text-center py-5">
        <div class="error-icon mb-3">
          <i class="fas fa-exclamation-triangle text-danger" style="font-size: 4rem;"></i>
        </div>
        <h4 class="text-danger mb-3">Ops! Algo deu errado</h4>
        <p class="text-muted mb-4">{{ error }}</p>
        <button @click="retryLoading" class="btn btn-primary btn-lg">
          <i class="fas fa-redo me-2"></i>Tentar Novamente
        </button>
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredProducts.length === 0" class="empty-state text-center py-5">
        <div class="empty-icon mb-3">
          <i class="fas fa-search text-muted" style="font-size: 4rem;"></i>
        </div>
        <h4 class="text-muted mb-3">Nenhum produto encontrado</h4>
        <p class="text-muted mb-4">Tente ajustar os filtros ou termos de busca</p>
        <button @click="clearAllFilters" class="btn btn-primary">
          <i class="fas fa-times me-2"></i>Limpar Filtros
        </button>
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
      <div v-if="!loading && !error && filteredProducts.length > 0" class="quick-actions mt-5 text-center">
        <div class="card border-0 bg-light">
          <div class="card-body py-4">
            <h5 class="mb-3">Precisa de ajuda para encontrar?</h5>
            <div class="d-flex flex-wrap justify-content-center gap-3">
              <button class="btn btn-outline-primary">
                <i class="fas fa-headset me-2"></i>Falar com Farmacêutico
              </button>
              <button class="btn btn-outline-success">
                <i class="fas fa-prescription me-2"></i>Enviar Receita
              </button>
              <button class="btn btn-outline-info">
                <i class="fas fa-question-circle me-2"></i>Tire suas Dúvidas
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapGetters, mapActions } from 'vuex'
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
  computed: {
    ...mapState(['products', 'categories']),
    ...mapGetters(['cartItemsCount']),
    
    filteredProducts() {
      if (!this.products || !Array.isArray(this.products)) {
        return []
      }
      
      let filtered = this.products.filter(product => {
        const matchesSearch = product.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                             product.description.toLowerCase().includes(this.searchTerm.toLowerCase())
        const matchesCategory = !this.filters.category || product.category === this.filters.category
        const matchesStock = this.filters.stock === 'all' || 
                           (this.filters.stock === 'in_stock' && product.inStock) ||
                           (this.filters.stock === 'out_of_stock' && !product.inStock)
        
        return matchesSearch && matchesCategory && matchesStock
      })

      switch (this.filters.sortBy) {
        case 'name_desc':
          filtered.sort((a, b) => b.name.localeCompare(a.name))
          break
        case 'price':
          filtered.sort((a, b) => a.price - b.price)
          break
        case 'price_desc':
          filtered.sort((a, b) => b.price - a.price)
          break
        default: // 'name'
          filtered.sort((a, b) => a.name.localeCompare(b.name))
      }

      return filtered
    },
    
    hasActiveFilters() {
      return this.searchTerm || this.filters.category || this.filters.stock !== 'all'
    }
  },
  async mounted() {
    console.log('🚀 Componente Products montado - inicializando...');
    await this.initializeComponent();
  },
  methods: {
    ...mapActions(['fetchProducts']), // 🔥 REMOVIDO: addToCart daqui
    
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
    
    async initializeComponent() {
      this.loading = true;
      this.error = null;
      
      try {
        console.log('📦 Buscando produtos da API...');
        await this.fetchProducts();
        console.log('✅ Produtos carregados com sucesso');
        console.log('📊 Total de produtos:', this.products?.length || 0);
        this.trackPageView();
      } catch (err) {
        console.error('❌ Erro ao carregar produtos:', err);
        this.error = 'Erro ao carregar produtos. Tente novamente.';
      } finally {
        this.loading = false;
      }
    },
    
    trackPageView() {
      if (window.gtag) {
        window.gtag('event', 'page_view', {
          page_title: 'Página de Produtos',
          page_location: '/products'
        });
      }
    },
    
    // 🔥 CORRIGIDO: Este método não deve chamar addToCart
    // Apenas recebe o evento do ProductCard para tracking
    handleAddToCart(product) {
      console.log(`📦 Products.vue - Produto adicionado via ProductCard: ${product.name}`);
      // Aqui você pode adicionar analytics ou tracking, mas NÃO chamar addToCart
    },
    
    retryLoading() {
      this.initializeComponent();
    },
    
    clearAllFilters() {
      this.searchTerm = '';
      this.filters = {
        category: '',
        sortBy: 'name',
        stock: 'all'
      };
    }
  }
}
</script>

<style scoped>
.products-header {
  background: linear-gradient(135deg, #198754 0%, #146c43 100%);
  border-radius: 0 0 20px 20px;
}

.search-box {
  position: relative;
}

.search-input {
  padding-left: 2.5rem;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
  z-index: 10;
}

.filter-group label {
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.active-filters {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 10px;
  border: 1px solid #e9ecef;
}

.products-grid {
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.loading-spinner {
  padding: 3rem 0;
}

.error-state, .empty-state {
  padding: 4rem 1rem;
}

.quick-actions .card {
  border-radius: 15px;
}

/* Responsividade */
@media (max-width: 768px) {
  .products-header {
    border-radius: 0 0 15px 15px;
    padding: 2rem 0;
  }
  
  .products-header h1 {
    font-size: 1.8rem;
  }
  
  .search-input {
    font-size: 0.9rem;
  }
}
</style>