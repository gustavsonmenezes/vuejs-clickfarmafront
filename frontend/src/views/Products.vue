<template>
  <div class="products-page">
    <!-- Header Section -->
    <section class="products-header text-white py-5 mb-4">
      <div class="container">
        <div class="row align-items-center">
          <div class="col-md-8">
            <h1 class="display-5 fw-bold mb-2">💊 Nossos Produtos</h1>
            <p class="lead mb-0 opacity-75">Encontre os melhores medicamentos e produtos para sua saúde</p>
          </div>
          <div class="col-md-4 text-md-end mt-3 mt-md-0">
            <span class="badge bg-white text-success fs-6 px-3 py-2 shadow-sm">
              {{ filteredProducts.length }} produtos encontrados
            </span>
          </div>
        </div>
      </div>
    </section>

    <div class="container">
      <!-- Smart Search Box -->
      <div class="smart-search-card mb-4 p-3 bg-white rounded-3 shadow-sm border">
        <div class="row g-2 align-items-center">
          <div class="col-md-8 position-relative">
            <i class="fas fa-search search-icon"></i>
            <input
                v-model="searchTerm"
                @keyup.enter="fetchProducts"
                type="text"
                class="form-control form-control-lg ps-5 border-0 bg-light"
                placeholder="O que você procura? (Ex: Dor de cabeça, Dipirona...)"
            >
          </div>
          <div class="col-md-4 d-flex gap-2 justify-content-end">
            <button
                class="btn btn-outline-secondary btn-lg"
                @click="clearAllFilters"
                v-if="hasActiveFilters"
            >
              <i class="fas fa-times"></i> Limpar
            </button>
            <button
                class="btn btn-ai btn-lg px-4"
                @click="buscarComIA"
                :disabled="aiLoading || !searchTerm"
            >
              <span v-if="aiLoading" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else class="fas fa-wand-magic-sparkles me-2"></i>
              {{ aiLoading ? 'Pensando...' : 'Buscar com IA' }}
            </button>
          </div>
        </div>
      </div>

      <!-- AI Search Banner -->
      <div v-if="aiSintoma" class="ai-banner mb-4">
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <i class="fas fa-robot me-2 text-primary"></i>
            <strong>Busca Inteligente:</strong> Recomendado para <em>"{{ aiSintoma }}"</em>
            <span class="badge bg-primary ms-2">{{ filteredProducts.length }} produto(s)</span>
          </div>
          <button @click="limparBuscaIA" class="btn btn-sm btn-outline-secondary">
            Voltar para catálogo
          </button>
        </div>
      </div>

      <!-- Filters Bar -->
      <div class="filters-bar mb-4 d-flex flex-wrap gap-3 align-items-center justify-content-between">
        <div class="d-flex gap-2 flex-wrap">
          <select v-model="filters.category" class="form-select form-select-sm w-auto">
            <option value="">📁 Categoria</option>
            <option v-for="cat in categoriesList" :key="cat" :value="cat">{{ cat }}</option>
          </select>
          <select v-model="filters.stock" class="form-select form-select-sm w-auto">
            <option value="all">📦 Estoque</option>
            <option value="in_stock">Em estoque</option>
            <option value="out_of_stock">Sem estoque</option>
          </select>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="text-muted small">Ordenar por:</span>
          <select v-model="filters.sortBy" class="form-select form-select-sm w-auto">
            <option value="name">Nome A-Z</option>
            <option value="price">Menor Preço</option>
            <option value="price_desc">Maior Preço</option>
          </select>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-success" style="width: 3rem; height: 3rem;" role="status"></div>
        <p class="mt-3 text-muted">Carregando produtos...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="error-state text-center py-5">
        <i class="fas fa-exclamation-circle text-danger mb-3" style="font-size: 3rem;"></i>
        <h4 class="text-danger mb-3">Ops! Algo deu errado</h4>
        <p class="text-muted mb-4">{{ error }}</p>
        <button @click="retryLoading" class="btn btn-success">Tentar Novamente</button>
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredProducts.length === 0" class="empty-state text-center py-5">
        <i class="fas fa-box-open text-muted mb-3" style="font-size: 4rem;"></i>
        <h4 class="text-muted mb-3">Nenhum produto encontrado</h4>
        <button @click="clearAllFilters" class="btn btn-primary">Limpar Filtros</button>
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
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import ProductCard from '@/components/products/ProductCard.vue'

export default {
  name: 'Products',
  components: { ProductCard },
  data() {
    return {
      searchTerm: '',
      filters: { category: '', sortBy: 'name', stock: 'all' },
      loading: false,
      error: null,
      aiLoading: false,
      aiSintoma: '',
      aiProducts: null
    }
  },
  computed: {
    ...mapState(['products']),
    categoriesList() {
      if (!this.products) return []
      return [...new Set(this.products.map(p => p.categoriaNome || p.categoria).filter(Boolean))].sort()
    },
    filteredProducts() {
      let base = this.aiProducts || this.products
      if (!base || !Array.isArray(base)) return []

      let result = base.filter(p => {
        // Se é busca IA, não aplica filtro textual (já foi filtrado pelo backend)
        const isIaSearch = !!this.aiSintoma
        if (isIaSearch) {
          const matchCat = !this.filters.category || (p.categoriaNome === this.filters.category) || (p.categoria === this.filters.category)
          const stock = p.estoque !== undefined ? p.estoque : (p.stock || 0)
          const matchStock = this.filters.stock === 'all' || 
                             (this.filters.stock === 'in_stock' && stock > 0) || 
                             (this.filters.stock === 'out_of_stock' && stock === 0)
          return matchCat && matchStock
        }

        const nome = (p.nome || p.name || '').toLowerCase()
        const desc = (p.descricao || p.description || '').toLowerCase()
        const term = this.searchTerm.toLowerCase()
        
        const matchSearch = !this.searchTerm || nome.includes(term) || desc.includes(term)
        const matchCat = !this.filters.category || (p.categoriaNome === this.filters.category) || (p.categoria === this.filters.category)
        
        const stock = p.estoque !== undefined ? p.estoque : (p.stock || 0)
        const matchStock = this.filters.stock === 'all' || 
                           (this.filters.stock === 'in_stock' && stock > 0) || 
                           (this.filters.stock === 'out_of_stock' && stock === 0)
        
        return matchSearch && matchCat && matchStock
      })

      // Sorting
      result.sort((a, b) => {
        const nomeA = a.nome || a.name || ''
        const nomeB = b.nome || b.name || ''
        const priceA = a.preco || a.price || 0
        const priceB = b.preco || b.price || 0

        if (this.filters.sortBy === 'price') return priceA - priceB
        if (this.filters.sortBy === 'price_desc') return priceB - priceA
        return nomeA.localeCompare(nomeB)
      })

      return result
    },
    hasActiveFilters() {
      return this.searchTerm || this.filters.category || this.filters.stock !== 'all' || this.aiSintoma
    }
  },
  async mounted() {
    await this.initializeComponent()
  },
  methods: {
    ...mapActions(['fetchProducts']),
    async initializeComponent() {
      this.loading = true
      this.error = null
      try {
        await this.fetchProducts()
      } catch (err) {
        this.error = 'Erro ao carregar produtos.'
      } finally {
        this.loading = false
      }
    },
    handleAddToCart(product) {
      this.$emit('add-to-cart', product)
    },
    retryLoading() {
      this.initializeComponent()
    },
    clearAllFilters() {
      this.searchTerm = ''
      this.filters = { category: '', sortBy: 'name', stock: 'all' }
      this.limparBuscaIA()
    },
    async buscarComIA() {
      if (!this.searchTerm) return
      this.aiLoading = true
      this.error = null
      try {
        const res = await fetch('/api/produtos/busca-inteligente', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ sintoma: this.searchTerm })
        })
        if (!res.ok) throw new Error('Falha na IA')
        const data = await res.json()
        this.aiSintoma = this.searchTerm
        this.aiProducts = data.produtos || []
        if (this.aiProducts.length === 0) this.error = `A IA não encontrou produtos para "${this.searchTerm}"`
      } catch (err) {
        this.error = 'Erro na busca inteligente.'
      } finally {
        this.aiLoading = false
      }
    },
    limparBuscaIA() {
      this.aiSintoma = ''
      this.aiProducts = null
      this.error = null
    }
  }
}
</script>

<style scoped>
.products-header {
  background: linear-gradient(135deg, #198754 0%, #0f5132 100%);
  border-radius: 0 0 30px 30px;
}
.smart-search-card input {
  font-size: 1.1rem;
}
.search-icon {
  position: absolute;
  left: 1.2rem;
  top: 50%;
  transform: translateY(-50%);
  color: #adb5bd;
  z-index: 10;
}
.btn-ai {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-weight: 600;
  border: none;
}
.btn-ai:hover:not(:disabled) {
  color: white;
  opacity: 0.9;
  transform: translateY(-1px);
}
.btn-ai:disabled {
  background: #adb5bd;
  color: white;
}
.ai-banner {
  background: #f8f9fa;
  border-left: 4px solid #667eea;
  padding: 1rem 1.5rem;
  border-radius: 0 8px 8px 0;
}
.filters-bar select {
  border-color: #e9ecef;
  background-color: #f8f9fa;
}
.products-grid {
  animation: fadeIn 0.4s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>