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
            @update:delivery-cost="updateDeliveryCost"
            @update:delivery-details="updateDeliveryDetails"
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
            :delivery-cost="deliveryCost"
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
      selectedStore: null,
      deliveryCost: 0,
      deliveryDetails: null,
      dynamicAddresses: []
    }
  },
  computed: {
    ...mapGetters(['cartItemsCount', 'cartTotal']),
    ...mapState(['cart', 'user']),
    
    userAddresses() {
      // Retorna a lista de endereços carregada do localStorage,
      // Ou se estiver vazio, retorna um endereço padrão baseado no perfil (fallback opcional)
      if (this.dynamicAddresses && this.dynamicAddresses.length > 0) {
         // Converte as chaves do formato do Addresses.vue para o esperado pelo DeliveryOptions
         return this.dynamicAddresses.map(addr => ({
            id: addr.id,
            nickname: addr.label,
            street: addr.street,
            number: addr.number,
            complement: addr.complement,
            neighborhood: addr.neighborhood || '',
            city: addr.city,
            state: addr.state,
            zipcode: addr.zip,
            isDefault: addr.isDefault
         }));
      }

      // Fallback
      if (this.user && this.user.endereco) {
         return [
           {
             id: 1,
             nickname: 'Meu Endereço de Cadastro',
             street: this.user.endereco,
             number: '',
             complement: '',
             neighborhood: '',
             city: 'N/A',
             state: 'N/A',
             zipcode: '01001000', // CEP padrão
             isDefault: true
           }
         ];
      }
      return [];
    },
    
    canProceed() {
      if (this.deliveryOption === 'delivery') {
        return this.selectedAddress !== null && this.deliveryDetails !== null
      } else if (this.deliveryOption === 'pickup') {
        return this.selectedStore !== null
      }
      return false
    }
  },
  methods: {
    loadUserAddresses() {
       const saved = localStorage.getItem('userAddressesList');
       if (saved) {
         try {
           this.dynamicAddresses = JSON.parse(saved);
         } catch(e) {
           console.error("Erro ao ler endereços do usuario", e);
         }
       }
    },

    updateDeliveryOption(option) {
      this.deliveryOption = option
      if (option === 'pickup') {
        this.deliveryCost = 0
        this.deliveryDetails = null
      }
    },
    
    updateSelectedAddress(address) {
      this.selectedAddress = address
    },
    
    updateSelectedStore(store) {
      this.selectedStore = store
    },

    updateDeliveryCost(cost) {
      this.deliveryCost = cost
    },

    updateDeliveryDetails(details) {
      this.deliveryDetails = details
    },
    
    proceedToPayment() {
      if (!this.canProceed) {
        alert('Por favor, selecione uma opção de entrega válida e escolha o frete.')
        return
      }
      
      const checkoutData = {
        deliveryOption: this.deliveryOption,
        deliveryType: this.deliveryOption,
        selectedAddress: this.selectedAddress,
        selectedStore: this.selectedStore,
        deliveryInfo: this.selectedAddress || this.selectedStore,
        deliveryCost: this.deliveryCost,
        deliveryDetails: this.deliveryDetails,
        timestamp: new Date().toISOString()
      }
      
      console.log('🚚 Dados de checkout salvos:', checkoutData)
      
      localStorage.setItem('checkoutData', JSON.stringify(checkoutData))

      this.$router.push('/payment-method')
    }
  },
  
  mounted() {
    this.loadUserAddresses();

    // Verificar se há itens no carrinho
    if (!this.cart || this.cart.length === 0) {
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
        this.deliveryCost = data.deliveryCost || 0
        this.deliveryDetails = data.deliveryDetails || null
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
