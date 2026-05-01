<template>
  <div class="container mt-5">
    <div class="card text-center">
      <div class="card-body">
        <div class="mb-4">
          <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
        </div>
        <h1 class="card-title">Pagamento Confirmado!</h1>
        <p class="card-text">Seu pedido foi processado com sucesso.</p>
        <p class="card-text">
          Número do pedido:
          <strong>#{{ codigoPedido || pedidoId }}</strong>
        </p>
        <router-link
          v-if="codigoPedido"
          :to="`/tracking-backend/${encodeURIComponent(codigoPedido)}`"
          class="btn btn-outline-primary me-2"
        >
          Rastrear pedido
        </router-link>
        <router-link to="/orders" class="btn btn-primary">
          Ver meus pedidos
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      pedidoId: localStorage.getItem('ultimoPedidoId') || '',
      codigoPedido: localStorage.getItem('ultimoCodigoPedido') || ''
    }
  },
  mounted() {
    // Limpar carrinho
    localStorage.removeItem('cart');
    localStorage.removeItem('ultimoPedidoId');
    localStorage.removeItem('ultimoCodigoPedido');
    this.$store.dispatch('clearCart');
  }
}
</script>
