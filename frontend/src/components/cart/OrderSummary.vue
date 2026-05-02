<template>
  <div class="order-summary p-4 bg-white rounded shadow-sm border">
    <h4 class="mb-4 font-weight-bold">Resumo do Pedido</h4>

    <!-- BARRA DE FRETE GRÁTIS -->
    <div class="free-shipping-container mb-4">
      <div class="d-flex justify-content-between mb-2">
        <span class="small font-weight-bold text-muted">
          {{ ganhouFrete ? '🎉 Frete Grátis Garantido!' : `Faltam R$ ${faltaQuanto.toFixed(2)} para Frete Grátis` }}
        </span>
        <span class="small font-weight-bold">{{ porcentagem }}%</span>
      </div>
      <div class="progress" style="height: 10px; border-radius: 10px;">
        <div
            class="progress-bar progress-bar-striped progress-bar-animated"
            role="progressbar"
            :class="ganhouFrete ? 'bg-success' : 'bg-primary'"
            :style="{ width: porcentagem + '%' }"
        ></div>
      </div>
    </div>

    <!-- VALORES DETALHADOS -->
    <div class="summary-details mb-4">
      <div class="d-flex justify-content-between mb-2">
        <span class="text-muted">Subtotal ({{ itemsCount }} itens):</span>
        <span>R$ {{ total.toFixed(2) }}</span>
      </div>

      <div class="d-flex justify-content-between mb-2">
        <span class="text-muted">Frete:</span>
        <span :class="ganhouFrete ? 'text-success font-weight-bold' : 'text-muted'">
          {{ ganhouFrete ? 'GRÁTIS' : 'R$ ' + valorFretePadrao.toFixed(2) }}
        </span>
      </div>

      <hr>

      <div class="d-flex justify-content-between mt-3">
        <h5 class="font-weight-bold">Total Final:</h5>
        <h5 class="font-weight-bold text-primary">R$ {{ totalFinal.toFixed(2) }}</h5>
      </div>
    </div>

    <!-- BOTÃO DE AÇÃO -->
    <router-link
        to="/checkout"
        class="btn btn-primary btn-block btn-lg py-3 shadow-sm"
        :class="{ 'disabled': itemsCount === 0 }"
    >
      Prosseguir para Pagamento
    </router-link>
  </div>
</template>

<script>
export default {
  name: 'OrderSummary',
  props: {
    itemsCount: { type: Number, required: true },
    total: { type: Number, required: true }
  },
  data() {
    return {
      threshold: 150.00, // Meta para frete grátis
      valorFretePadrao: 15.00 // Valor do frete se não atingir a meta
    }
  },
  computed: {
    faltaQuanto() {
      const valor = this.threshold - this.total;
      return valor > 0 ? valor : 0;
    },
    porcentagem() {
      const p = (this.total / this.threshold) * 100;
      return Math.min(p, 100).toFixed(0);
    },
    ganhouFrete() {
      return this.total >= this.threshold;
    },
    totalFinal() {
      return this.ganhouFrete ? this.total : this.total + this.valorFretePadrao;
    }
  }
}
</script>

<style scoped>
.order-summary { border-color: #f1f1f1 !important; }
.progress { background-color: #f8f9fa; }
.btn-primary { background-color: #2c3e50; border: none; }
.btn-primary:hover { background-color: #34495e; transform: translateY(-1px); }
.disabled { pointer-events: none; opacity: 0.6; }
</style>
