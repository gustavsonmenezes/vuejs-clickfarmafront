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
                  🚚 Entrega em domicílio (R$ 10,00)
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
                {{ deliveryType === 'delivery' ? 'Confirmar Entrega' : 'Confirmar Retirada' }}
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
            <p v-if="deliveryType === 'delivery'">Entrega: R$ 10.00</p>
            <p v-else-if="deliveryType === 'pickup'">Retirada: R$ 0.00</p>
            <hr>
            <h5>Total: R$ {{ finalTotal.toFixed(2) }}</h5>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

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
    finalTotal() {
      return this.deliveryType === 'delivery' ? this.cartTotal + 10 : this.cartTotal;
    }
  },
  methods: {
    ...mapActions(['clearCart']),
    confirmOrder() {
      let message = 'Pedido confirmado com sucesso!\n\n';
      
      if (this.deliveryType === 'delivery') {
        if (!this.deliveryInfo.name || !this.deliveryInfo.address) {
          alert('Por favor, preencha todos os campos de entrega.');
          return;
        }
        message += `Tipo: Entrega em domicílio\nNome: ${this.deliveryInfo.name}\nEndereço: ${this.deliveryInfo.address}`;
      } else {
        message += 'Tipo: Retirada na loja\nEndereço: Rua das Farmácias, 123 - Centro, São Paulo - SP';
      }
      
      alert(message);
      this.clearCart();
      this.$router.push("/payment-method");
    }
  }
}
</script>