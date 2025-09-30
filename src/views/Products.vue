<template>
  <div class="container mt-4">
    <h2>📦 Nossos Produtos</h2>
    
    <!-- Loading State -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
      <p class="mt-2">Carregando produtos...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="alert alert-danger">
      {{ error }}
      <button @click="retryLoading" class="btn btn-sm btn-outline-danger ms-2">
        Tentar Novamente
      </button>
    </div>

    <!-- Content -->
    <div v-else>
      <div class="row mb-4">
        <div class="col-md-6">
          <input 
            v-model="searchTerm" 
            type="text" 
            class="form-control" 
            placeholder="Buscar produtos..."
          >
        </div>
        <div class="col-md-3">
          <select v-model="filters.category" class="form-select">
            <option value="">Todas categorias</option>
            <option v-for="cat in categories" :key="cat" :value="cat">
              {{ cat }}
            </option>
          </select>
        </div>
        <div class="col-md-3">
          <select v-model="filters.sortBy" class="form-select">
            <option value="name">Ordenar por nome</option>
            <option value="price">Ordenar por preço</option>
          </select>
        </div>
      </div>

      <div class="row">
        <div v-for="product in filteredProducts" :key="product.id" class="col-md-4 mb-4">
          <div class="card h-100">
            <div class="card-body text-center">
              <span style="font-size: 4rem;">💊</span>
              <router-link :to="`/products/${product.id}`" class="text-decoration-none text-dark">
                <h5 class="card-title">{{ product.name }}</h5>
              </router-link>
              <p class="card-text">{{ product.description }}</p>
              <p class="text-success fw-bold">R$ {{ product.price.toFixed(2) }}</p>
              <p :class="{'text-success': product.inStock, 'text-danger': !product.inStock}"> <!-- inteporla -->
                {{ product.inStock ? 'Em estoque' : 'Fora de estoque' }}
              </p>
            </div>
            <div class="card-footer">
              <button 
                @click="handleAddToCart(product)" 
                class="btn btn-primary w-100"
                :disabled="!product.inStock"
              >
                {{ product.inStock ? 'Adicionar ao carrinho' : 'Indisponível' }}
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

export default {
  name: 'Products',
  data() {
    return {
      searchTerm: '',
      filters: {
        category: '',
        sortBy: 'name'
      },
      lastAddedProduct: null,
      loading: false,
      error: null
    }
  },
  computed: {
    ...mapState(['products', 'categories']),
    filteredProducts() {
      let filtered = this.products.filter(product => {
        const matchesSearch = product.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                             product.description.toLowerCase().includes(this.searchTerm.toLowerCase())
        const matchesCategory = !this.filters.category || product.category === this.filters.category
        
        return matchesSearch && matchesCategory
      })

      if (this.filters.sortBy === 'price') {
        filtered.sort((a, b) => a.price - b.price)
      } else {
        filtered.sort((a, b) => a.name.localeCompare(b.name))
      }

      return filtered
    }
  },
  async mounted() {
    console.log('🚀 Componente Products montado - inicializando...');
    await this.initializeComponent();
  },
  methods: {
    ...mapActions(['fetchProducts', 'addToCart']),
    
    async initializeComponent() {
      this.loading = true;
      this.error = null;
      
      try {
        console.log('📦 Buscando produtos da API...');
        await this.fetchProducts();
        console.log('✅ Produtos carregados com sucesso');
        
        // Analytics ou tracking
        this.trackPageView();
        
      } catch (err) {
        console.error('❌ Erro ao carregar produtos:', err);
        this.error = 'Erro ao carregar produtos. Tente novamente.';
      } finally {
        this.loading = false;
      }
    },
    
    trackPageView() {
      // Simulação de tracking
      if (window.gtag) {
        window.gtag('event', 'page_view', {
          page_title: 'Página de Produtos',
          page_location: '/products'
        });
      }
    },
    
    handleAddToCart(product) {
      this.addToCart(product);
      this.lastAddedProduct = product;
    },
    
    retryLoading() {
      this.initializeComponent();
    }
  },
  watch: {
    lastAddedProduct(newProduct) {
      if (newProduct) {
        alert(`${newProduct.name} adicionado ao carrinho!`);
        this.lastAddedProduct = null;
      }
    }
  }
}
</script>