<template>
  <div class="text-center py-5 empty-cart">
    <div class="empty-icon">🛒</div>
    <p class="text-muted mt-3">Seu carrinho está vazio</p>
    <router-link to="/products" class="btn btn-primary mt-3">
      Ver Produtos
    </router-link>
  </div>
</template>

<script>
export default {
  name: 'EmptyCart'
}
</script>

<style scoped>
.empty-cart {
  background-color: #f8f9fa;
  border-radius: 0.5rem;
}

.empty-icon {
  font-size: 4rem;
  opacity: 0.5;
}
</style>