<template>
  <div class="delivery-options">
    <div class="cf-card delivery-card">
      <div class="cf-card-header">
        <i class="fas fa-truck me-2 text-primary"></i>
        Opções de Entrega
      </div>
      <div class="cf-card-body">
        <div class="delivery-option mb-4">
          <label class="option-label" :class="{ active: selectedOption === 'delivery' }">
            <input type="radio" value="delivery" :checked="selectedOption === 'delivery'" @change="updateOption('delivery')" />
            <div class="option-content">
              <div class="option-info">
                <h6 class="mb-1">Entrega em Casa</h6>
                <p class="mb-0 text-muted small">Receba no conforto do seu lar</p>
                <small class="text-success" v-if="deliveryTime">
                  <i class="fas fa-clock me-1"></i>Entrega em {{ deliveryTime }}
                </small>
              </div>
              <div class="option-price">
                <span class="fw-bold" :class="deliveryPrice > 0 ? 'text-primary' : 'text-success'">
                  {{ deliveryPrice > 0 ? 'R$ ' + deliveryPrice.toFixed(2) : 'Grátis' }}
                </span>
              </div>
            </div>
          </label>
          <div v-if="selectedOption === 'delivery'" class="mt-3 ps-4">
            <address-selection
              :addresses="addresses"
              :selected-address="selectedAddress"
              @select-address="handleAddressSelect"
            />
          </div>
        </div>

        <div class="delivery-option">
          <label class="option-label" :class="{ active: selectedOption === 'pickup' }">
            <input type="radio" value="pickup" :checked="selectedOption === 'pickup'" @change="updateOption('pickup')" />
            <div class="option-content">
              <div class="option-info">
                <h6 class="mb-1">Retirada na Loja</h6>
                <p class="mb-0 text-muted small">Retire na farmácia selecionada</p>
                <small class="text-success">
                  <i class="fas fa-clock me-1"></i>Disponível em 1 hora
                </small>
              </div>
              <div class="option-price">
                <span class="fw-bold text-success">Grátis</span>
              </div>
            </div>
          </label>
          <div v-if="selectedOption === 'pickup'" class="mt-3 ps-4">
            <div class="store-card" v-if="farmacia">
              <div class="store-name">{{ farmacia.nome }}</div>
              <div class="store-address">
                <i class="fas fa-map-marker-alt me-1"></i>{{ farmacia.endereco || farmacia.enderecoCompleto }}
              </div>
              <div class="store-hours text-success">
                <i class="fas fa-clock me-1"></i>Seg-Sáb 8h-20h
              </div>
            </div>
            <p v-else class="cf-text-muted small mb-0">
              <i class="fas fa-info-circle me-1"></i>
              Selecione uma farmácia na busca de produtos para ver o endereço de retirada.
            </p>
          </div>
        </div>

        <div class="delivery-info mt-4 p-3 rounded">
          <h6 class="mb-2 small fw-semibold">
            <i class="fas fa-info-circle me-2 text-primary"></i>Informações Importantes
          </h6>
          <ul class="list-unstyled mb-0 small cf-text-muted">
            <li v-if="selectedOption === 'delivery'">• Entregas de segunda a sábado, das 8h às 18h</li>
            <li v-if="selectedOption === 'pickup' && farmacia">• Retirada na {{ farmacia.nome }} — apresente documento com foto</li>
            <li v-if="farmacia && farmacia.valorFrete">• Frete: R$ {{ parseFloat(farmacia.valorFrete).toFixed(2) }} por km</li>
            <li>• Medicamentos controlados exigem receita médica na retirada</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AddressSelection from './AddressSelection.vue'
import { mapState } from 'vuex';

export default {
  name: 'DeliveryOptions',
  components: { AddressSelection },
  props: {
    addresses: { type: Array, default: () => [] },
    selectedDeliveryOption: { type: String, default: '' },
    selectedAddress: { type: Object, default: null },
    selectedStore: { type: Object, default: null }
  },
  data() {
    return { selectedOption: this.selectedDeliveryOption }
  },
  computed: {
    ...mapState(['selectedFarmacia']),
    farmacia() { return this.selectedFarmacia; },
    deliveryPrice() {
      const cartTotal = this.$store.getters.cartTotal;
      if (cartTotal >= 300) return 0;
      const frete = this.farmacia?.valorFrete ? parseFloat(this.farmacia.valorFrete) : 10;
      return cartTotal >= 100 ? frete : frete + 5;
    },
    deliveryTime() {
      return this.farmacia ? '1-2 dias úteis' : '2-3 dias úteis';
    }
  },
  watch: {
    selectedDeliveryOption(n) { this.selectedOption = n; }
  },
  methods: {
    updateOption(opt) {
      this.selectedOption = opt;
      this.$emit('update:delivery-option', opt);
      if (opt === 'delivery') this.$emit('update:selected-store', null);
      else this.$emit('update:selected-address', null);
    },
    handleAddressSelect(addr) { this.$emit('update:selected-address', addr); }
  }
}
</script>

<style scoped>
.delivery-card { border: 1px solid var(--cf-border); }

.option-label {
  display: flex;
  cursor: pointer;
  padding: 16px;
  border: 2px solid var(--cf-border);
  border-radius: var(--cf-radius-md);
  transition: all 0.15s;
}

.option-label.active { border-color: var(--cf-primary-500); background: var(--cf-primary-50); }

.option-label input { display: none; }

.option-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-left: 12px;
}

.option-info h6 { font-size: 0.9375rem; font-weight: 600; color: var(--cf-slate-900); }

.store-card {
  background: var(--cf-slate-50);
  border-radius: 8px;
  padding: 14px 16px;
  border: 1px solid var(--cf-border);
}

.store-name { font-weight: 600; font-size: 0.875rem; color: var(--cf-slate-900); margin-bottom: 4px; }
.store-address { font-size: 0.8125rem; color: var(--cf-slate-600); margin-bottom: 2px; }
.store-hours { font-size: 0.8125rem; }

.delivery-info { border-left: 4px solid var(--cf-primary-500); background: var(--cf-primary-50); }

.delivery-info ul li { padding: 2px 0; }
</style>
