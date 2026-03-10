<template>
  <div class="address-list">
    <div v-if="addresses.length === 0" class="text-center py-4">
      <i class="fas fa-map-marker-alt fa-3x text-muted mb-3"></i>
      <p class="text-muted">Nenhum endereço cadastrado</p>
      <button class="btn btn-primary" @click="$emit('add-address')">
        <i class="fas fa-plus me-1"></i>Adicionar Endereço
      </button>
    </div>
    
    <div v-else class="row">
      <div v-for="address in addresses" :key="address.id" class="col-md-6 mb-3">
        <div class="card h-100" :class="{ 'border-primary': address.isDefault }">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <h6 class="card-title">{{ address.nickname }}</h6>
              <span v-if="address.isDefault" class="badge bg-primary">Principal</span>
            </div>
            
            <p class="card-text">
              {{ address.street }}, {{ address.number }}<br>
              {{ address.neighborhood }}<br>
              {{ address.city }} - {{ address.state }}<br>
              CEP: {{ address.zipcode }}<br>
              <span v-if="address.complement">Complemento: {{ address.complement }}</span>
            </p>
          </div>
          
          <div class="card-footer bg-transparent d-flex justify-content-between">
            <div>
              <button 
                v-if="!address.isDefault"
                class="btn btn-sm btn-outline-primary me-2"
                @click="$emit('set-default', address.id)"
                title="Definir como principal"
              >
                <i class="fas fa-star"></i>
              </button>
            </div>
            
            <div>
              <button 
                class="btn btn-sm btn-outline-secondary me-2"
                @click="$emit('edit-address', address)"
                title="Editar endereço"
              >
                <i class="fas fa-edit"></i>
              </button>
              
              <button 
                class="btn btn-sm btn-outline-danger"
                @click="confirmDelete(address)"
                title="Excluir endereço"
              >
                <i class="fas fa-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AddressList',
  props: {
    addresses: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    confirmDelete(address) {
      if (confirm(`Tem certeza que deseja excluir o endereço "${address.nickname}"?`)) {
        this.$emit('delete-address', address.id)
      }
    }
  }
}
</script>

<style scoped>
.card {
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-2px);
}

.badge {
  font-size: 0.7rem;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
}
</style>