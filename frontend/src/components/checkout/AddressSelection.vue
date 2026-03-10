<template>
  <div class="address-selection">
    <h6 class="mb-3">Selecione o endereço de entrega:</h6>
    
    <div v-if="addresses.length === 0" class="alert alert-warning">
      <i class="fas fa-exclamation-triangle me-2"></i>
      Nenhum endereço cadastrado. 
      <router-link to="/addresses" class="alert-link">Cadastrar endereço</router-link>
    </div>

    <div class="address-list">
      <div 
        v-for="address in addresses" 
        :key="address.id" 
        class="address-item card mb-2"
        :class="{ 'border-primary': isSelected(address) }"
        @click="selectAddress(address)"
      >
        <div class="card-body py-2">
          <div class="form-check">
            <input 
              class="form-check-input" 
              type="radio" 
              :checked="isSelected(address)"
              @change="selectAddress(address)"
            >
            <label class="form-check-label w-100">
              <div class="d-flex justify-content-between align-items-start">
                <div>
                  <strong>{{ address.nickname }}</strong>
                  <p class="mb-0 small">
                    {{ address.street }}, {{ address.number }}<br>
                    {{ address.neighborhood }} - {{ address.city }}/{{ address.state }}<br>
                    CEP: {{ address.zipcode }}
                    <span v-if="address.complement"> - {{ address.complement }}</span>
                  </p>
                </div>
                <span v-if="address.isDefault" class="badge bg-primary">Principal</span>
              </div>
            </label>
          </div>
        </div>
      </div>
    </div>

    <div class="text-end mt-3">
      <router-link to="/addresses" class="btn btn-outline-primary btn-sm">
        <i class="fas fa-plus me-1"></i>Gerenciar Endereços
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AddressSelection',
  props: {
    addresses: {
      type: Array,
      default: () => []
    },
    selectedAddress: {
      type: Object,
      default: null
    }
  },
  methods: {
    isSelected(address) {
      return this.selectedAddress && this.selectedAddress.id === address.id
    },
    selectAddress(address) {
      this.$emit('select-address', address)
    }
  }
}
</script>

<style scoped>
.address-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.address-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.address-item.border-primary {
  border-width: 2px;
}

.form-check-input {
  margin-top: 0.3rem;
}
</style>