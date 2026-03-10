<template>
  <div class="container mt-4">
    <div v-if="product" class="row">
      <div class="col-md-6">
        <img src="https://via.placeholder.com/400" alt="Product Image" class="img-fluid rounded">
      </div>
      <div class="col-md-6">
        <h2>{{ product.name }}</h2>
        <p class="text-muted">{{ product.category }}</p>
        <p>{{ product.description }}</p>
        <h3 class="text-success">R$ {{ product.price.toFixed(2) }}</h3>
        <p :class="{'text-success': product.inStock, 'text-danger': !product.inStock}">
          {{ product.inStock ? 'Em estoque' : 'Fora de estoque' }}
        </p>
        <button 
          @click="handleAddToCart(product)" 
          class="btn btn-primary btn-lg"
          :disabled="!product.inStock"
        >
          {{ product.inStock ? 'Adicionar ao carrinho' : 'Indisponível' }}
        </button>
        <router-link to="/products" class="btn btn-secondary btn-lg ms-2">Voltar aos Produtos</router-link>
      </div>
    </div>
    <div v-else>
      <p>Produto não encontrado.</p>
      <router-link to="/products" class="btn btn-secondary">Voltar aos Produtos</router-link>
    </div>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex'

export default {
  name: 'ProductDetail',
  data() {
    return {
      product: null,
      lastAddedProduct: null
    }
  },
  computed: {
    ...mapState(['products'])
  },
  created() {
    const productId = parseInt(this.$route.params.id)
    this.product = this.products.find(p => p.id === productId)
    
    // Redirecionar se produto não for encontrado
    if (!this.product) {
      this.$router.push('/products')
    }
  },
  methods: {
    ...mapActions(['addToCart']),
    handleAddToCart(product) {
      this.addToCart(product);
      this.lastAddedProduct = product;
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