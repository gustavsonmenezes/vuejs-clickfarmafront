<template>
  <div class="container mt-4">
    <h2>💰 Finalizar Compra</h2>
    <div class="row">
      <div class="col-md-8">
        <div class="card">
          <div class="card-header">
            <h5>Informações de Entrega</h5>
          </div>
          <div class="card-body">
            <div class="mb-4">
              <h6>Tipo de Entrega</h6>
              <div class="form-check">
                <input v-model="deliveryType" class="form-check-input" type="radio" value="delivery" id="delivery">
                <label class="form-check-label" for="delivery">
                  🚚 Entrega em domicílio {{ getDeliveryLabel() }}
                </label>
              </div>
              <div class="form-check">
                <input v-model="deliveryType" class="form-check-input" type="radio" value="pickup" id="pickup">
                <label class="form-check-label" for="pickup">
                  🏪 Retirada na loja (Grátis)
                </label>
              </div>
            </div>
            
            <form v-if="deliveryType === 'delivery'" @submit.prevent="confirmOrder">
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Nome completo</label>
                  <input v-model="deliveryInfo.name" type="text" class="form-control" required>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">CPF</label>
                  <input v-model="deliveryInfo.cpf" type="text" class="form-control" required>
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label">Endereço</label>
                <input v-model="deliveryInfo.address" type="text" class="form-control" required>
              </div>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Cidade</label>
                  <input v-model="deliveryInfo.city" type="text" class="form-control" required>
                </div>
                <div class="col-md-3 mb-3">
                  <label class="form-label">Estado</label>
                  <input v-model="deliveryInfo.state" type="text" class="form-control" required>
                </div>
                <div class="col-md-3 mb-3">
                  <label class="form-label">CEP</label>
                  <input v-model="deliveryInfo.zip" type="text" class="form-control" required>
                </div>
              </div>
            </form>
            
            <div v-if="deliveryType === 'pickup'" class="alert alert-info">
              <h6>📍 Endereço da Loja:</h6>
              <p class="mb-0">
                Rua das Farmácias, 123 - Centro<br>
                São Paulo - SP, CEP: 01234-567<br>
                <strong>Horário:</strong> Segunda a Sexta: 8h às 18h | Sábado: 8h às 12h
              </p>
            </div>
            
            <div v-if="deliveryType" class="mt-3">
              <button @click="confirmOrder" class="btn btn-success">
                {{ deliveryType === 'delivery' ? 'Continuar para Pagamento' : 'Continuar para Pagamento' }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card">
          <div class="card-header">
            <h5>Resumo</h5>
          </div>
          <div class="card-body">
            <p>Itens: {{ cartItemsCount }}</p>
            <p>Subtotal: R$ {{ cartTotal.toFixed(2) }}</p>
            <p v-if="deliveryType === 'delivery'">Entrega: {{ getDeliveryCostLabel() }}</p>
            <p v-else-if="deliveryType === 'pickup'">Retirada: Grátis</p>
            <hr>
            <h5>Total: R$ {{ finalTotal.toFixed(2) }}</h5>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapState } from 'vuex'

export default {
  name: 'Checkout',
  data() {
    return {
      deliveryType: 'delivery',
      deliveryInfo: {
        name: '',
        cpf: '',
        address: '',
        city: '',
        state: '',
        zip: ''
      }
    }
  },
  computed: {
    ...mapGetters(['cartItemsCount', 'cartTotal']),
    ...mapState(['cart']),
    finalTotal() {
      const deliveryCost = this.deliveryType === 'delivery' ? this.getDeliveryCost() : 0;
      return this.cartTotal + deliveryCost;
    }
  },
  methods: {
    getDeliveryCost() {
      if (this.cartTotal >= 300) return 0;
      if (this.cartTotal < 100) return 10.00;
      return 0; // Entre R$ 100 e R$ 299,99 é grátis também
    },
    
    getDeliveryLabel() {
      if (this.cartTotal >= 300) return '(Grátis)';
      if (this.cartTotal < 100) return '(R$ 10,00)';
      return '(Grátis)';
    },
    
    getDeliveryCostLabel() {
      const cost = this.getDeliveryCost();
      return cost > 0 ? `R$ ${cost.toFixed(2)}` : 'Grátis';
    },
    
    confirmOrder() {
      if (this.deliveryType === 'delivery') {
        if (!this.deliveryInfo.name || !this.deliveryInfo.address) {
          alert('Por favor, preencha todos os campos de entrega.');
          return;
        }
      }
      
      // Salvar dados no localStorage antes de ir para o pagamento
      this.saveCheckoutData();
      
      // Ir para a página de pagamento
      this.$router.push("/payment-method");
    },
    
    saveCheckoutData() {
      // Salvar carrinho no localStorage
      localStorage.setItem('cart', JSON.stringify(this.cart));
      localStorage.setItem('cartTotal', this.cartTotal.toString());
      localStorage.setItem('finalTotal', this.finalTotal.toString());
      
      // Salvar informações de entrega
      const checkoutData = {
        deliveryType: this.deliveryType,
        deliveryInfo: this.deliveryInfo,
        finalTotal: this.finalTotal
      };
      localStorage.setItem('checkoutData', JSON.stringify(checkoutData));
    }
  }
}
</script>