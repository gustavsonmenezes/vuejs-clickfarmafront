<template>
  <div class="card order-summary">
    <div class="card-header">
      <h5 class="summary-title">Resumo do Pedido</h5>
    </div>
    <div class="card-body">
      <div class="summary-details">
        <div class="d-flex justify-content-between mb-2">
          <span>Itens ({{ itemsCount }}):</span>
          <span>R$ {{ subtotal.toFixed(2) }}</span>
        </div>
        <div class="d-flex justify-content-between mb-2">
          <span>Frete:</span>
          <span>{{ shippingCost > 0 ? 'R$ ' + shippingCost.toFixed(2) : 'Grátis' }}</span>
        </div>
        <hr>
        <div class="d-flex justify-content-between total-row">
          <strong>Total:</strong>
          <strong>R$ {{ totalWithShipping.toFixed(2) }}</strong>
        </div>
      </div>
      
      <router-link 
        to="/checkout" 
        class="btn btn-primary w-100 mt-4 checkout-btn"
      >
        Finalizar Compra
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderSummary',
  props: {
    itemsCount: {
      type: Number,
      required: true
    },
    total: {
      type: Number,
      required: true
    }
  },
  computed: {
    subtotal() {
      return this.total
    },
    shippingCost() {
      // Frete grátis para compras acima de R$ 100
      return this.total > 100 ? 0 : 10
    },
    totalWithShipping() {
      return this.total + this.shippingCost
    }
  }
}
</script>

<style scoped>
.order-summary {
  position: sticky;
  top: 20px;
}

.summary-title {
  margin-bottom: 0;
  color: #2c3e50;
}

.summary-details {
  font-size: 1rem;
}

.total-row {
  font-size: 1.2rem;
  margin-top: 1rem;
}

.checkout-btn {
  padding: 0.75rem;
  font-weight: 600;
}
</style>