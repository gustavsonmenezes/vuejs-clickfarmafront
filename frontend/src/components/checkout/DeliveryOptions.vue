<template>
  <div class="delivery-options">
    <div class="card">
      <div class="card-header bg-primary text-white">
        <h5 class="mb-0">
          <i class="fas fa-truck me-2"></i>Opções de Entrega
        </h5>
      </div>
      <div class="card-body">

        <!-- Alerta de erro da API de frete -->
        <div v-if="deliveryError" class="alert alert-danger">
          {{ deliveryError }}
        </div>

        <!-- Alerta de carregamento -->
        <div v-if="loadingDelivery" class="alert alert-info text-center">
          <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
          Calculando melhores opções de entrega para você...
        </div>

        <!-- Opção de Entrega -->
        <div class="delivery-option mb-4">
          <div class="form-check">
            <input 
              class="form-check-input" 
              type="radio" 
              id="delivery-home" 
              value="delivery" 
              :checked="selectedOption === 'delivery'"
              @change="updateOption('delivery')"
            >
            <label class="form-check-label w-100" for="delivery-home">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <h6 class="mb-1">Entrega no Endereço</h6>
                  <p class="mb-0 text-muted">Receba seus produtos no conforto do seu lar</p>
                </div>
              </div>
            </label>
          </div>

          <!-- Seleção de Endereço (aparece quando entrega está selecionada) -->
          <div v-if="selectedOption === 'delivery'" class="mt-3 ps-4">
            <address-selection 
              :addresses="addresses"
              :selected-address="selectedAddress"
              @select-address="handleAddressSelect"
            />

            <!-- Mostra opções retornadas pela API APÓS selecionar endereço -->
            <div v-if="selectedAddress && !loadingDelivery && apiDeliveryOptions.length > 0" class="mt-3">
              <h6>Escolha a modalidade de envio:</h6>
              <div
                v-for="opt in apiDeliveryOptions.filter(o => o.tipo !== 'RETIRADA')"
                :key="opt.tipo"
                class="card mb-2 p-2"
                :class="{ 'border-primary': selectedApiOption === opt.tipo }"
                @click="selectApiOption(opt)"
                style="cursor: pointer;"
              >
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <i class="fas fa-shipping-fast text-primary me-2"></i>
                    <strong>{{ opt.nome }}</strong>
                    <div class="text-muted small mt-1">{{ opt.prazoEstimado }}</div>
                  </div>
                  <div>
                    <span v-if="opt.valor === 0" class="text-success fw-bold">Grátis</span>
                    <span v-else class="fw-bold">R$ {{ opt.valor.toFixed(2).replace('.', ',') }}</span>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>

        <!-- Opção de Retirada -->
        <div class="delivery-option">
          <div class="form-check">
            <input 
              class="form-check-input" 
              type="radio" 
              id="pickup-store" 
              value="pickup" 
              :checked="selectedOption === 'pickup'"
              @change="updateOption('pickup')"
            >
            <label class="form-check-label w-100" for="pickup-store">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <h6 class="mb-1">Retirada na Loja</h6>
                  <p class="mb-0 text-muted">Retire seus produtos em uma de nossas lojas</p>
                  <small class="text-success">
                    <i class="fas fa-clock me-1"></i>Disponível em 1 hora
                  </small>
                </div>
                <div class="text-end">
                  <span class="fw-bold text-success">Grátis</span>
                </div>
              </div>
            </label>
          </div>

          <!-- Seleção de Loja (aparece quando retirada está selecionada) -->
          <div v-if="selectedOption === 'pickup'" class="mt-3 ps-4">
            <pickup-store-selection 
              :stores="stores"
              :selected-store="selectedStore"
              @select-store="handleStoreSelect"
            />
          </div>
        </div>

        <!-- Informações de Entrega -->
        <div class="delivery-info mt-4 p-3 bg-light rounded">
          <h6 class="mb-2">
            <i class="fas fa-info-circle me-2 text-primary"></i>Informações Importantes
          </h6>
          <ul class="list-unstyled mb-0 small">
            <li v-if="selectedOption === 'delivery'">
              • Entregas realizadas de segunda a sábado, das 8h às 18h
            </li>
            <li v-if="selectedOption === 'pickup'">
              • Horário de funcionamento: Segunda a Sábado, 8h às 20h
            </li>
            <li>• Pedidos acima de R$ 100,00 podem ter frete grátis na modalidade PAC</li>
            <li>• Medicamentos controlados exigem retenção de receita médica na entrega ou retirada</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AddressSelection from './AddressSelection.vue'
import PickupStoreSelection from './PickupStoreSelection.vue'
import { DeliveryService } from '@/services/deliveryService'

export default {
  name: 'DeliveryOptions',
  components: {
    AddressSelection,
    PickupStoreSelection
  },
  props: {
    addresses: {
      type: Array,
      default: () => []
    },
    selectedDeliveryOption: {
      type: String,
      default: ''
    },
    selectedAddress: {
      type: Object,
      default: null
    },
    selectedStore: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      selectedOption: this.selectedDeliveryOption || 'delivery',
      apiDeliveryOptions: [],
      selectedApiOption: null,
      loadingDelivery: false,
      deliveryError: null,
      stores: [
        {
          id: 1,
          name: 'ClickFarma Centro',
          address: 'Rua do Sol, 123 - Centro',
          city: 'Recife',
          state: 'PE',
          phone: '(81) 3333-3333',
          hours: 'Seg-Sáb: 8h-20h, Dom: 9h-14h',
          distance: '1.2 km'
        },
        {
          id: 2,
          name: 'ClickFarma Boa Viagem',
          address: 'Av. Boa Viagem, 456 - Boa Viagem',
          city: 'Recife',
          state: 'PE',
          phone: '(81) 4444-4444',
          hours: 'Seg-Sáb: 8h-20h, Dom: 9h-14h',
          distance: '3.5 km'
        },
        {
          id: 3,
          name: 'ClickFarma Casa Forte',
          address: 'Rua Madre Loyola, 789 - Casa Forte',
          city: 'Recife',
          state: 'PE',
          phone: '(81) 5555-5555',
          hours: 'Seg-Sáb: 8h-20h, Dom: 9h-14h',
          distance: '2.8 km'
        }
      ]
    }
  },
  computed: {
    cartTotal() {
      return this.$store.getters.cartTotal;
    }
  },
  watch: {
    selectedDeliveryOption(newValue) {
      if (newValue) {
        this.selectedOption = newValue;
      }
    }
  },
  methods: {
    updateOption(option) {
      this.selectedOption = option
      this.$emit('update:delivery-option', option)
      
      // Limpar seleções quando mudar a opção principal
      if (option === 'delivery') {
        this.$emit('update:selected-store', null)
        // Se já tiver endereço selecionado, busca o frete
        if (this.selectedAddress) {
           this.fetchDeliveryRates(this.selectedAddress.zipcode);
        }
      } else {
        this.$emit('update:selected-address', null)
        this.$store.commit('SET_DELIVERY_COST', 0); // Zera o frete no total se for retirada
      }
    },

    async handleAddressSelect(address) {
      this.$emit('update:selected-address', address);
      if (address && address.zipcode) {
        await this.fetchDeliveryRates(address.zipcode);
      }
    },

    handleStoreSelect(store) {
      this.$emit('update:selected-store', store);
      // Aqui podemos zerar a taxa de entrega no state do Vuex, caso futuramente seja integrado no CheckoutSummary
    },

    async fetchDeliveryRates(cep) {
      this.loadingDelivery = true;
      this.deliveryError = null;
      this.apiDeliveryOptions = [];
      this.selectedApiOption = null;

      try {
        const response = await DeliveryService.calculateDelivery(cep, this.cartTotal);
        this.apiDeliveryOptions = response;

        // Auto-selecionar a primeira opção (a mais barata ou normal geralmente)
        if (this.apiDeliveryOptions.length > 0) {
          const defaultOpt = this.apiDeliveryOptions.find(o => o.tipo !== 'RETIRADA');
          if(defaultOpt) {
            this.selectApiOption(defaultOpt);
          }
        }
      } catch (error) {
        this.deliveryError = error.response?.data?.mensagem || "Não foi possível calcular o frete para este CEP.";
      } finally {
        this.loadingDelivery = false;
      }
    },

    selectApiOption(opt) {
      this.selectedApiOption = opt.tipo;
      // Aqui idealmente emitimos esse valor para o componente pai Checkout.vue atualizar o Total.
      this.$emit('update:delivery-cost', opt.valor);
      this.$emit('update:delivery-details', opt);
    }
  },
  mounted() {
    // Se a página carregar e já houver endereço (porque o usuário voltou na navegação), busca
    if (this.selectedOption === 'delivery' && this.selectedAddress && this.selectedAddress.zipcode) {
      this.fetchDeliveryRates(this.selectedAddress.zipcode);
    }
  }
}
</script>

<style scoped>
.delivery-options .card {
  border: none;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.form-check-input {
  transform: scale(1.2);
  margin-right: 10px;
}

.form-check-label {
  cursor: pointer;
  padding: 15px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.form-check-input:checked + .form-check-label {
  border-color: #0d6efd;
  background-color: #f8f9fa;
}

.delivery-info {
  border-left: 4px solid #0d6efd;
}
</style>