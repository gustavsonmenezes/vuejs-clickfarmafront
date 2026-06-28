<template>
  <div class="container mt-4">
    <div v-if="product" class="row">
      <div class="col-md-6">
        <img :src="product.imagem || 'https://via.placeholder.com/400?text=Sem+Imagem'" :alt="product.nome" class="img-fluid rounded" style="width:100%;max-height:400px;object-fit:cover;">
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

        <!-- Preços por Farmácia -->
        <div v-if="farmaciaOfertas.length > 0" class="mt-4 p-3 bg-light rounded">
          <h5 class="mb-3">
            <i class="fas fa-store me-2 text-success"></i>
            Disponível em:
          </h5>
          <div class="table-responsive">
            <table class="table table-sm table-hover align-middle mb-0">
              <thead class="table-success">
                <tr>
                  <th>Farmácia</th>
                  <th>Preço</th>
                  <th>Frete</th>
                  <th>Total</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(o, i) in farmaciaOfertas" :key="i">
                  <td><strong>{{ o.farmaciaNome }}</strong></td>
                  <td class="text-success fw-bold">R$ {{ o.preco.toFixed(2) }}</td>
                  <td>R$ {{ o.valorFrete.toFixed(2) }}</td>
                  <td class="fw-bold">R$ {{ o.valorTotal.toFixed(2) }}</td>
                  <td>
                    <button class="btn btn-sm btn-outline-success" @click="addDaFarmacia(o)">
                      <i class="fas fa-cart-plus"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
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
import farmaciasService from '@/services/farmaciasService'

export default {
  name: 'ProductDetail',
  data() {
    return {
      product: null,
      lastAddedProduct: null,
      farmaciaOfertas: []
    }
  },
  computed: {
    ...mapState(['products']),
    isInStock() {
      return (this.product?.estoque || 0) > 0
    }
  },
  async created() {
    const productId = parseInt(this.$route.params.id)
    this.product = this.products.find(p => p.id === productId)
    
    if (!this.product) {
      this.$router.push('/products')
      return
    }

    await this.carregarOfertas()
  },
  methods: {
    ...mapActions(['addToCart']),
    handleAddToCart(product) {
      this.addToCart(product);
      this.lastAddedProduct = product;
    },
    async carregarOfertas() {
      try {
        const loc = { lat: -8.6845, lng: -35.5898 }
        if (navigator.geolocation) {
          try {
            const pos = await new Promise((res, rej) => navigator.geolocation.getCurrentPosition(res, rej, { timeout: 3000 }))
            loc.lat = pos.coords.latitude
            loc.lng = pos.coords.longitude
          } catch (e) {}
        }
        const res = await farmaciasService.buscarProdutos(this.product.nome, loc.lat, loc.lng)
        this.farmaciaOfertas = (Array.isArray(res.data) ? res.data : [])
          .filter(r => r.produtoId === this.product.id)
          .sort((a, b) => a.valorTotal - b.valorTotal)
      } catch (e) {
        console.error('Erro ao carregar ofertas de farmácias:', e)
      }
    },
    addDaFarmacia(oferta) {
      const p = {
        ...this.product,
        preco: oferta.preco,
        farmaciaId: oferta.farmaciaId,
        farmaciaNome: oferta.farmaciaNome,
        valorFrete: oferta.valorFrete
      }
      this.addToCart(p)
      this.$store.commit('SET_SELECTED_FARMACIA', {
        id: oferta.farmaciaId,
        nome: oferta.farmaciaNome,
        valorFrete: oferta.valorFrete
      })
      this.lastAddedProduct = p
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
