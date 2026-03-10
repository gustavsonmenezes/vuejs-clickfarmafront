<template>
  <div class="checkout-page">
    <div class="container mt-4">
      <!-- Steps -->
      <checkout-steps :current-step="1" />
      
      <div class="row">
        <!-- Coluna Principal -->
        <div class="col-lg-8">
          <!-- Opções de Entrega -->
          <delivery-options
            :addresses="userAddresses"
            :selected-delivery-option="deliveryOption"
            :selected-address="selectedAddress"
            :selected-store="selectedStore"
            @update:delivery-option="updateDeliveryOption"
            @update:selected-address="updateSelectedAddress"
            @update:selected-store="updateSelectedStore"
          />
          
          <!-- Botão de Continuar -->
          <div class="text-end mt-4">
            <button 
              class="btn btn-primary btn-lg"
              @click="proceedToPayment"
              :disabled="!canProceed"
            >
              Continuar para Pagamento <i class="fas fa-arrow-right ms-2"></i>
            </button>
          </div>
        </div>
        
        <!-- Resumo -->
        <div class="col-lg-4">
          <checkout-summary
            :cart="cart"
            :delivery-option="deliveryOption"
            :selected-address="selectedAddress"
            :selected-store="selectedStore"
            :selected-payment-method="''"
            :current-step="1"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapState } from 'vuex'
import CheckoutSteps from '@/components/checkout/CheckoutSteps.vue'
import DeliveryOptions from '@/components/checkout/DeliveryOptions.vue'
import CheckoutSummary from '@/components/checkout/CheckoutSummary.vue'

export default {
  name: 'Checkout',
  components: {
    CheckoutSteps,
    DeliveryOptions,
    CheckoutSummary
  },
  data() {
    return {
      deliveryOption: 'delivery',
      selectedAddress: null,
      selectedStore: null
    }
  },
  computed: {
    ...mapGetters(['cartItemsCount', 'cartTotal']),
    ...mapState(['cart', 'user']),
    
    userAddresses() {
      // Endereços mockados - em produção viriam da API
      return [
        {
          id: 1,
          nickname: 'Casa',
          street: 'Rua das Flores',
          number: '123',
          complement: 'Apto 101',
          neighborhood: 'Centro',
          city: 'Recife',
          state: 'PE',
          zipcode: '50000-000',
          isDefault: true
        },
        {
          id: 2,
          nickname: 'Trabalho',
          street: 'Av. Boa Viagem',
          number: '456',
          complement: 'Sala 501',
          neighborhood: 'Boa Viagem',
          city: 'Recife',
          state: 'PE',
          zipcode: '51000-000',
          isDefault: false
        }
      ]
    },
    
    canProceed() {
      if (this.deliveryOption === 'delivery') {
        return this.selectedAddress !== null
      } else if (this.deliveryOption === 'pickup') {
        return this.selectedStore !== null
      }
      return false
    }
  },
  methods: {
    updateDeliveryOption(option) {
      this.deliveryOption = option
    },
    
    updateSelectedAddress(address) {
      this.selectedAddress = address
    },
    
    updateSelectedStore(store) {
      this.selectedStore = store
    },
    
    proceedToPayment() {
      if (!this.canProceed) {
        alert('Por favor, selecione uma opção de entrega válida.')
        return
      }
      
      // Salvar dados do checkout com estrutura consistente
      const checkoutData = {
        deliveryOption: this.deliveryOption,
        deliveryType: this.deliveryOption, // ← ADICIONADO: alias para consistência
        selectedAddress: this.selectedAddress,
        selectedStore: this.selectedStore,
        deliveryInfo: this.selectedAddress || this.selectedStore, // ← ADICIONADO: estrutura unificada
        timestamp: new Date().toISOString()
      }
      
      console.log('🚚 Dados de checkout salvos:', checkoutData) // ← DEBUG
      
      localStorage.setItem('checkoutData', JSON.stringify(checkoutData))
      
      // Ir para pagamento
      this.$router.push('/payment-method')
    }
  },
  
  mounted() {
    // Verificar se há itens no carrinho
    if (this.cart.length === 0) {
      alert('Seu carrinho está vazio!')
      this.$router.push('/products')
      return
    }
    
    // Carregar dados salvos se existirem
    const savedCheckoutData = localStorage.getItem('checkoutData')
    if (savedCheckoutData) {
      try {
        const data = JSON.parse(savedCheckoutData)
        this.deliveryOption = data.deliveryOption || 'delivery'
        this.selectedAddress = data.selectedAddress || null
        this.selectedStore = data.selectedStore || null
      } catch (error) {
        console.error('Erro ao carregar dados do checkout:', error)
      }
    }
  }
}
</script>

<style scoped>
.checkout-page {
  min-height: 100vh;
  background-color: #f8f9fa;
}
</style>