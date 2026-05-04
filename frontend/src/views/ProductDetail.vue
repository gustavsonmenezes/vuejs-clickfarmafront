<template>
  <div class="container mt-4">
    <div v-if="product" class="row">
      <div class="col-md-6">
        <img src="https://via.placeholder.com/400" alt="Product Image" class="img-fluid rounded">
      </div>
      <div class="col-md-6">
        <h2>{{ product.nome }}</h2>
        <p class="text-muted">{{ product.categoriaNome }}</p>
        <p>{{ product.descricao }}</p>
        <h3 class="text-success">R$ {{ (product.preco || 0).toFixed(2) }}</h3>
        <p :class="{'text-success': isInStock, 'text-danger': !isInStock}">
          {{ isInStock ? 'Em estoque' : 'Fora de estoque' }}
        </p>
        <button 
          @click="handleAddToCart(product)" 
          class="btn btn-primary btn-lg"
          :disabled="!isInStock"
        >
          {{ isInStock ? 'Adicionar ao carrinho' : 'Indisponível' }}
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
    ...mapState(['products']),
    isInStock() {
      return (this.product?.estoque || 0) > 0
    }
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
        alert(`${newProduct.nome} adicionado ao carrinho!`);
        this.lastAddedProduct = null;
      }
    }
  }
}
</script>