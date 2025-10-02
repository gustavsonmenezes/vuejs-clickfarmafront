<template>
  <div class="delivery-options">
    <div class="card">
      <div class="card-header bg-primary text-white">
        <h5 class="mb-0">
          <i class="fas fa-truck me-2"></i>Opções de Entrega
        </h5>
      </div>
      <div class="card-body">
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
                  <h6 class="mb-1">Entrega em Casa</h6>
                  <p class="mb-0 text-muted">Receba seus produtos no conforto do seu lar</p>
                  <small class="text-success" v-if="deliveryTime">
                    <i class="fas fa-clock me-1"></i>Entrega em {{ deliveryTime }}
                  </small>
                </div>
                <div class="text-end">
                  <span class="fw-bold text-primary" v-if="deliveryPrice > 0">
                    R$ {{ deliveryPrice.toFixed(2) }}
                  </span>
                  <span class="fw-bold text-success" v-else>
                    Grátis
                  </span>
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
            <li>• Pedidos acima de R$ 300,00 têm frete grátis</li>
            <li>• Medicamentos controlados exigem receita médica na retirada</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AddressSelection from './AddressSelection.vue'
import PickupStoreSelection from './PickupStoreSelection.vue'

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
      selectedOption: this.selectedDeliveryOption,
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
    deliveryPrice() {
      const cartTotal = this.$store.getters.cartTotal
      if (cartTotal >= 300) return 0;
      if (cartTotal < 100) return 10.00;
      return 0;
    },
    deliveryTime() {
      return '2-3 dias úteis'
    }
  },
  watch: {
    selectedDeliveryOption(newValue) {
      this.selectedOption = newValue
    }
  },
  methods: {
    updateOption(option) {
      this.selectedOption = option
      this.$emit('update:delivery-option', option)
      
      // Limpar seleções quando mudar a opção
      if (option === 'delivery') {
        this.$emit('update:selected-store', null)
      } else {
        this.$emit('update:selected-address', null)
      }
    },
    handleAddressSelect(address) {
      this.$emit('update:selected-address', address)
    },
    handleStoreSelect(store) {
      this.$emit('update:selected-store', store)
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