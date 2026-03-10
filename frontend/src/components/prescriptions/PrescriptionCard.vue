<template>
  <div class="prescription-card">
    <div class="card h-100">
      <div class="card-header d-flex justify-content-between align-items-center">
        <span class="badge" :class="statusClass">
          {{ statusText }}
        </span>
        <small class="text-muted">{{ formatDate(prescription.createdAt) }}</small>
      </div>
      
      <div class="card-body">
        <!-- Informações do Médico -->
        <div class="doctor-info mb-3">
          <h6 class="card-title">
            <i class="fas fa-user-md me-2 text-primary"></i>
            {{ prescription.doctorName }}
          </h6>
          <p class="card-text text-muted small mb-1">
            CRM: {{ prescription.doctorCrm }}
          </p>
          <p class="card-text text-muted small">
            Data da receita: {{ formatDate(prescription.prescriptionDate) }}
          </p>
        </div>

        <!-- Medicamentos -->
        <div class="medications-info mb-3">
          <h6 class="fw-bold">Medicamentos:</h6>
          <p class="card-text small">{{ truncateText(prescription.medications, 100) }}</p>
        </div>

        <!-- Validade -->
        <div class="expiry-info mb-3">
          <h6 class="fw-bold">Validade:</h6>
          <p class="card-text small" :class="{'text-danger': isExpired}">
            {{ formatDate(prescription.expiryDate) }}
            <span v-if="isExpired" class="badge bg-danger ms-2">Expirada</span>
          </p>
        </div>

        <!-- Observações -->
        <div v-if="prescription.notes" class="notes-info mb-3">
          <h6 class="fw-bold">Observações:</h6>
          <p class="card-text small">{{ truncateText(prescription.notes, 80) }}</p>
        </div>

        <!-- Ações -->
        <div class="actions">
          <button 
            class="btn btn-outline-primary btn-sm me-2"
            @click="$emit('edit', prescription)"
            :disabled="prescription.status !== 'pending'"
          >
            <i class="fas fa-edit me-1"></i>Editar
          </button>
          
          <button 
            class="btn btn-outline-danger btn-sm"
            @click="$emit('delete', prescription.id)"
          >
            <i class="fas fa-trash me-1"></i>Excluir
          </button>

          <button 
            v-if="prescription.fileUrl"
            class="btn btn-outline-secondary btn-sm ms-2"
            @click="downloadFile(prescription.fileUrl)"
          >
            <i class="fas fa-download me-1"></i>Download
          </button>
        </div>
      </div>

      <!-- Footer com informações adicionais -->
      <div class="card-footer bg-transparent">
        <small class="text-muted">
          <i class="fas fa-history me-1"></i>
          {{ timeAgo(prescription.createdAt) }}
        </small>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PrescriptionCard',
  props: {
    prescription: {
      type: Object,
      required: true
    }
  },
  computed: {
    statusClass() {
      const classes = {
        'pending': 'bg-warning',
        'approved': 'bg-success',
        'rejected': 'bg-danger',
        'expired': 'bg-secondary'
      }
      return `badge ${classes[this.prescription.status] || 'bg-info'}`
    },
    
    statusText() {
      const texts = {
        'pending': 'Pendente',
        'approved': 'Aprovada',
        'rejected': 'Rejeitada',
        'expired': 'Expirada'
      }
      return texts[this.prescription.status] || this.prescription.status
    },
    
    isExpired() {
      if (!this.prescription.expiryDate) return false
      return new Date(this.prescription.expiryDate) < new Date()
    }
  },
  methods: {
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('pt-BR')
    },
    
    timeAgo(dateString) {
      const date = new Date(dateString)
      const now = new Date()
      const diff = now - date
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      
      if (days === 0) return 'Hoje'
      if (days === 1) return 'Ontem'
      if (days < 7) return `Há ${days} dias`
      if (days < 30) return `Há ${Math.floor(days / 7)} semanas`
      return `Há ${Math.floor(days / 30)} meses`
    },
    
    truncateText(text, maxLength) {
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    },
    
    downloadFile(url) {
      window.open(url, '_blank')
    }
  },
  emits: ['edit', 'delete']
}
</script>

<style scoped>
.prescription-card .card {
  border: none;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.prescription-card .card:hover {
  transform: translateY(-2px);
}

.card-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.badge {
  font-size: 0.75rem;
  padding: 0.4rem 0.6rem;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
}

.card-footer {
  border-top: 1px solid #e9ecef;
  padding: 0.5rem 1rem;
}
</style>