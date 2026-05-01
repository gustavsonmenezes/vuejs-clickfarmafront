<template>
  <div class="payment-method-container">
    <h4 class="mb-4 font-weight-bold text-dark">💳 Forma de Pagamento</h4>

    <div class="options-list">
      <!-- OPÇÃO MERCADO PAGO (SELECIONADA POR PADRÃO) -->
      <div
          class="payment-option p-4 mb-3 border rounded shadow-sm d-flex align-items-center"
          :class="{ 'selected-border bg-light': selectedMethod === 'MERCADO_PAGO' }"
          @click="selectMethod('MERCADO_PAGO')"
          style="cursor: pointer; transition: all 0.2s ease-in-out;"
      >
        <div class="form-check mb-0">
          <input
              class="form-check-input"
              type="radio"
              value="MERCADO_PAGO"
              v-model="selectedMethod"
          >
        </div>
        <div class="ms-3">
          <h6 class="mb-1 font-weight-bold">Mercado Pago</h6>
          <p class="small text-muted mb-0">Cartão de Crédito, PIX ou Boleto com segurança total.</p>
        </div>
        <img src="https://logodownload.org/wp-content/uploads/2019/06/mercado-pago-logo-0.png" height="25" class="ms-auto">
      </div>

      <!-- OPÇÃO SIMULADA (PARA TESTE/DEMO) -->
      <div
          class="payment-option p-4 mb-3 border rounded shadow-sm d-flex align-items-center"
          :class="{ 'selected-border bg-light': selectedMethod === 'SIMULADO' }"
          @click="selectMethod('SIMULADO')"
          style="cursor: pointer; transition: all 0.2s ease-in-out;"
      >
        <div class="form-check mb-0">
          <input
              class="form-check-input"
              type="radio"
              value="SIMULADO"
              v-model="selectedMethod"
          >
        </div>
        <div class="ms-3">
          <h6 class="mb-1 font-weight-bold">Pagamento Simulado (Demo)</h6>
          <p class="small text-muted mb-0">Finaliza o pedido no sistema e já libera rastreio, sem redirecionar.</p>
        </div>
        <i class="fas fa-vial fa-2x ms-auto text-muted"></i>
      </div>
    </div>

<<<<<<< main
    <div class="cf-alert-info mt-4 d-flex align-items-center p-3">
      <i class="fas fa-shield-alt fa-lg me-3 cf-text-green"></i>
      <p class="small mb-0">Você será redirecionado para o ambiente seguro do <strong>Mercado Pago</strong> ao finalizar.</p>
=======
    <div class="alert alert-primary border-0 shadow-sm mt-4 d-flex align-items-center p-3">
      <i class="fas fa-shield-alt fa-lg me-3 text-primary"></i>
      <p class="small mb-0">
        No <strong>Mercado Pago</strong>, você será redirecionado para o ambiente seguro ao finalizar.
        No modo <strong>Simulado</strong>, o pedido é finalizado no próprio sistema.
      </p>
>>>>>>> main
    </div>
  </div>
</template>

<script>
export default {
  name: 'PaymentMethod',
  data() { return { selectedMethod: 'MERCADO_PAGO' }; },
  methods: {
    selectMethod(method) {
      this.selectedMethod = method;
      this.$emit('update-method', method); // Avisa o Checkout.vue
    }
  },
  mounted() { this.$emit('update-method', this.selectedMethod); }
}
</script>

<style scoped>
.payment-option { border: 2px solid #f0f0f0; border-radius: 12px; }
.payment-option:hover { border-color: var(--cf-green); }
.selected-border { border-color: var(--cf-green) !important; background-color: var(--cf-green-xlight) !important; }
.form-check-input:checked { background-color: var(--cf-green); border-color: var(--cf-green); }
.cf-alert-info { background: var(--cf-green-xlight); border-radius: 8px; border: 1px solid var(--cf-green-light); color: var(--cf-green); }
.cf-text-green { color: var(--cf-green); }
</style>
