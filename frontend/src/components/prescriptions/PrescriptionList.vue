<template>
  <div class="prescription-list">
    <div v-if="prescriptions.length === 0" class="text-center py-5">
      <i class="fas fa-file-medical fa-4x text-muted mb-3"></i>
      <h5 class="text-muted">Nenhuma receita encontrada</h5>
      <p class="text-muted">Envie sua primeira receita para começar</p>
      <button class="btn btn-primary" @click="$emit('new-prescription')">
        <i class="fas fa-upload me-1"></i>Enviar Receita
      </button>
    </div>
    
    <div v-else class="row">
      <div v-for="prescription in prescriptions" :key="prescription.id" class="col-md-6 mb-4">
        <prescription-card 
          :prescription="prescription"
          @edit="$emit('edit-prescription', prescription)"
          @delete="$emit('delete-prescription', prescription.id)"
        />
      </div>
    </div>

    <!-- Filtros e Ordenação -->
    <div v-if="prescriptions.length > 0" class="row mb-4">
      <div class="col-md-6">
        <div class="input-group">
          <span class="input-group-text">
            <i class="fas fa-search"></i>
          </span>
          <input 
            type="text" 
            class="form-control" 
            placeholder="Buscar receitas..."
            v-model="searchTerm"
          >
        </div>
      </div>
      <div class="col-md-6">
        <select class="form-select" v-model="sortBy">
          <option value="date">Ordenar por Data</option>
          <option value="status">Ordenar por Status</option>
          <option value="doctor">Ordenar por Médico</option>
        </select>
      </div>
    </div>

    <!-- Estatísticas -->
    <div v-if="prescriptions.length > 0" class="row mb-4">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h6 class="card-title">Estatísticas das Receitas</h6>
            <div class="d-flex justify-content-around text-center">
              <div>
                <div class="text-primary fw-bold">{{ stats.total }}</div>
                <small class="text-muted">Total</small>
              </div>
              <div>
                <div class="text-success fw-bold">{{ stats.approved }}</div>
                <small class="text-muted">Aprovadas</small>
              </div>
              <div>
                <div class="text-warning fw-bold">{{ stats.pending }}</div>
                <small class="text-muted">Pendentes</small>
              </div>
              <div>
                <div class="text-danger fw-bold">{{ stats.rejected }}</div>
                <small class="text-muted">Rejeitadas</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import PrescriptionCard from './PrescriptionCard.vue'

export default {
  name: 'PrescriptionList',
  components: {
    PrescriptionCard
  },
  props: {
    prescriptions: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      searchTerm: '',
      sortBy: 'date'
    }
  },
  computed: {
    filteredPrescriptions() {
      let filtered = this.prescriptions
      
      // Filtro de busca
      if (this.searchTerm) {
        const term = this.searchTerm.toLowerCase()
        filtered = filtered.filter(p => 
          p.doctorName.toLowerCase().includes(term) ||
          p.medications.toLowerCase().includes(term) ||
          p.status.toLowerCase().includes(term)
        )
      }
      
      // Ordenação
      switch (this.sortBy) {
        case 'date':
          filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
          break
        case 'status':
          filtered.sort((a, b) => a.status.localeCompare(b.status))
          break
        case 'doctor':
          filtered.sort((a, b) => a.doctorName.localeCompare(b.doctorName))
          break
      }
      
      return filtered
    },
    
    stats() {
      return {
        total: this.prescriptions.length,
        approved: this.prescriptions.filter(p => p.status === 'approved').length,
        pending: this.prescriptions.filter(p => p.status === 'pending').length,
        rejected: this.prescriptions.filter(p => p.status === 'rejected').length
      }
    }
  },
  emits: ['new-prescription', 'edit-prescription', 'delete-prescription']
}
</script>

<style scoped>
.prescription-list {
  min-height: 400px;
}

.input-group-text {
  background-color: #f8f9fa;
}

.card {
  border: none;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.card-body {
  padding: 1rem;
}
</style>