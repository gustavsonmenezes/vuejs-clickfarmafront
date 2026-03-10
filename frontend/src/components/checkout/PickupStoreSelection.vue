<template>
  <div class="pickup-store-selection">
    <h6 class="mb-3">Selecione a loja para retirada:</h6>
    
    <div class="store-list">
      <div 
        v-for="store in stores" 
        :key="store.id" 
        class="store-item card mb-3"
        :class="{ 'border-primary': isSelected(store) }"
        @click="selectStore(store)"
      >
        <div class="card-body">
          <div class="form-check">
            <input 
              class="form-check-input" 
              type="radio" 
              :checked="isSelected(store)"
              @change="selectStore(store)"
            >
            <label class="form-check-label w-100">
              <div class="d-flex justify-content-between align-items-start mb-2">
                <div>
                  <strong>{{ store.name }}</strong>
                  <p class="mb-1 small text-muted">
                    <i class="fas fa-map-marker-alt me-1"></i>
                    {{ store.address }} - {{ store.city }}/{{ store.state }}
                  </p>
                  <p class="mb-1 small text-muted">
                    <i class="fas fa-phone me-1"></i>{{ store.phone }}
                  </p>
                  <p class="mb-1 small text-muted">
                    <i class="fas fa-clock me-1"></i>{{ store.hours }}
                  </p>
                </div>
                <span class="badge bg-info">{{ store.distance }}</span>
              </div>
              
              <div class="store-features">
                <span class="badge bg-light text-dark me-1 small">
                  <i class="fas fa-parking text-success me-1"></i>Estacionamento
                </span>
                <span class="badge bg-light text-dark me-1 small">
                  <i class="fas fa-wheelchair text-primary me-1"></i>Acessível
                </span>
                <span class="badge bg-light text-dark small">
                  <i class="fas fa-prescription text-warning me-1"></i>Farmácia
                </span>
              </div>
            </label>
          </div>
        </div>
      </div>
    </div>

    <div class="store-map mt-4">
      <div class="card">
        <div class="card-header bg-light">
          <h6 class="mb-0">
            <i class="fas fa-map me-2"></i>Localização das Lojas
          </h6>
        </div>
        <div class="card-body text-center p-4">
          <i class="fas fa-map-marked-alt fa-3x text-muted mb-3"></i>
          <p class="text-muted mb-0">Mapa interativo das lojas ClickFarma</p>
          <small class="text-muted">(Integração com Google Maps disponível em produção)</small>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PickupStoreSelection',
  props: {
    stores: {
      type: Array,
      default: () => []
    },
    selectedStore: {
      type: Object,
      default: null
    }
  },
  methods: {
    isSelected(store) {
      return this.selectedStore && this.selectedStore.id === store.id
    },
    selectStore(store) {
      this.$emit('select-store', store)
    }
  }
}
</script>

<style scoped>
.store-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.store-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.store-item.border-primary {
  border-width: 2px;
}

.form-check-input {
  margin-top: 0.3rem;
}

.store-features {
  margin-top: 10px;
}
</style>