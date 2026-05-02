<template>
  <div class="prescription-validation">
    <!-- Header -->
    <div class="section-header mb-4">
      <h5 class="mb-0 fw-semibold">Receitas Pendentes</h5>
      <span v-if="pendingCount > 0" class="cf-badge cf-badge-warning">{{ pendingCount }} {{ pendingCount === 1 ? 'pendente' : 'pendentes' }}</span>
    </div>

    <!-- Prescriptions List -->
    <div class="prescriptions-list">
      <div v-for="prescription in prescriptions" :key="prescription.id" class="prescription-card cf-card" :class="{ 'pending': prescription.status === 'Pendente' }">
        <div class="prescription-header">
          <div class="d-flex align-items-center gap-3">
            <div class="prescription-icon" :class="prescription.status.toLowerCase()">
              <i :class="getStatusIcon(prescription.status)"></i>
            </div>
            <div>
              <h6 class="mb-0 fw-semibold">{{ prescription.medication }}</h6>
              <p class="mb-0 cf-text-muted small">{{ prescription.customerName }} · Pedido #{{ prescription.orderId }}</p>
            </div>
          </div>
          <span :class="['cf-badge', getStatusBadge(prescription.status)]">{{ prescription.status }}</span>
        </div>
        
        <div class="prescription-body">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">Data</span>
              <span class="info-value">{{ prescription.date }}</span>
            </div>
            <div class="info-item" v-if="prescription.validatedAt">
              <span class="info-label">Validado em</span>
              <span class="info-value">{{ prescription.validatedAt }}</span>
            </div>
          </div>
          
          <div v-if="prescription.fileUrl" class="file-link">
            <a :href="prescription.fileUrl" target="_blank" class="file-link-btn">
              <i class="fas fa-file-medical"></i>
              Ver Receita
            </a>
          </div>
        </div>

        <div v-if="prescription.status === 'Pendente'" class="prescription-actions">
          <button @click="validatePrescription(prescription.id, 'Aprovada')" class="cf-btn cf-btn-primary cf-btn-sm">
            <i class="fas fa-check me-1"></i>
            Aprovar
          </button>
          <button @click="validatePrescription(prescription.id, 'Rejeitada')" class="cf-btn cf-btn-danger cf-btn-sm">
            <i class="fas fa-xmark me-1"></i>
            Rejeitar
          </button>
          <button @click="uploadReplacement(prescription.id)" class="cf-btn cf-btn-secondary cf-btn-sm">
            <i class="fas fa-upload me-1"></i>
            Substituir
          </button>
        </div>
      </div>

      <div v-if="!prescriptions.length" class="cf-card">
        <div class="cf-empty-state">
          <i class="fas fa-file-medical"></i>
          <p>Nenhuma receita para validar.</p>
        </div>
      </div>
    </div>

    <!-- Upload Modal -->
    <div v-if="uploadingPrescriptionId" class="modal-backdrop" @click.self="cancelUpload">
      <div class="modal-panel">
        <div class="modal-header">
          <h5 class="mb-0 fw-semibold">Substituir Receita</h5>
          <button @click="cancelUpload" class="btn-close-modal"><i class="fas fa-xmark"></i></button>
        </div>
        <div class="modal-body">
          <p class="mb-3 cf-text-muted small">Receita #{{ uploadingPrescriptionId }}</p>
          <div class="upload-area">
            <input type="file" @change="handleFileUpload" accept="image/*,application/pdf" class="form-control" />
            <p v-if="selectedFile" class="mt-2 mb-0 small fw-medium">
              <i class="fas fa-paperclip me-1"></i> {{ selectedFile.name }}
            </p>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cf-btn cf-btn-secondary" @click="cancelUpload">Cancelar</button>
          <button class="cf-btn cf-btn-primary" @click="submitUpload" :disabled="!selectedFile">Enviar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PrescriptionValidation',
  data() {
    return {
      prescriptions: [],
      uploadingPrescriptionId: null,
      selectedFile: null
    }
  },
  computed: {
    pendingCount() {
      return this.prescriptions.filter(p => p.status === 'Pendente').length;
    }
  },
  mounted() { this.fetchPrescriptions(); },
  methods: {
    async fetchPrescriptions() {
      this.prescriptions = [
        { id: 1, orderId: 1001, customerName: 'João Silva', medication: 'Antibiótico Amoxicilina', date: '2023-10-01', status: 'Pendente', fileUrl: '/mock/receita1.pdf', validatedAt: null },
        { id: 2, orderId: 1002, customerName: 'Maria Oliveira', medication: 'Paracetamol Controlado', date: '2023-10-02', status: 'Aprovada', fileUrl: '/mock/receita2.pdf', validatedAt: '2023-10-02 14:30' },
        { id: 3, orderId: 1003, customerName: 'Carlos Santos', medication: 'Dipirona Sódica', date: '2023-10-03', status: 'Pendente', fileUrl: '/mock/receita3.pdf', validatedAt: null }
      ];
    },
    getStatusIcon(status) {
      const map = { 'Pendente': 'fas fa-clock', 'Aprovada': 'fas fa-check-circle', 'Rejeitada': 'fas fa-times-circle' };
      return map[status] || 'fas fa-file';
    },
    getStatusBadge(status) {
      const map = { 'Pendente': 'cf-badge-warning', 'Aprovada': 'cf-badge-success', 'Rejeitada': 'cf-badge-danger' };
      return map[status] || 'cf-badge-primary';
    },
    async validatePrescription(id, status) {
      if (confirm(`Confirmar ${status.toLowerCase()} da receita?`)) {
        const prescription = this.prescriptions.find(p => p.id === id);
        if (prescription) {
          prescription.status = status;
          prescription.validatedAt = new Date().toLocaleString('pt-BR');
        }
      }
    },
    uploadReplacement(id) {
      this.uploadingPrescriptionId = id;
      this.selectedFile = null;
    },
    handleFileUpload(event) {
      this.selectedFile = event.target.files[0];
    },
    async submitUpload() {
      if (!this.selectedFile) return;
      const prescription = this.prescriptions.find(p => p.id === this.uploadingPrescriptionId);
      if (prescription) {
        prescription.fileUrl = URL.createObjectURL(this.selectedFile);
        prescription.status = 'Pendente';
        prescription.validatedAt = null;
      }
      this.cancelUpload();
    },
    cancelUpload() {
      this.uploadingPrescriptionId = null;
      this.selectedFile = null;
    }
  }
}
</script>

<style scoped>
.prescription-validation { max-width: 800px; }

.section-header { display: flex; align-items: center; gap: 12px; }

/* Prescription Cards */
.prescriptions-list { display: flex; flex-direction: column; gap: 12px; }

.prescription-card {
  padding: 20px;
  transition: box-shadow 0.15s ease;
}

.prescription-card.pending {
  border-left: 3px solid var(--cf-warning);
}

.prescription-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.prescription-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

.prescription-icon.pendente { background: var(--cf-warning-light); color: var(--cf-warning); }
.prescription-icon.aprovada { background: var(--cf-success-light); color: var(--cf-success); }
.prescription-icon.rejeitada { background: var(--cf-danger-light); color: var(--cf-danger); }

.prescription-body {
  padding-top: 16px;
  border-top: 1px solid var(--cf-border);
}

.info-grid {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 0.6875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--cf-slate-400);
  font-weight: 500;
}

.info-value {
  font-size: 0.875rem;
  color: var(--cf-slate-700);
  font-weight: 500;
}

.file-link { margin-top: 12px; }

.file-link-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--cf-slate-50);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-radius-md);
  color: var(--cf-primary-600);
  font-size: 0.8125rem;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.15s ease;
}

.file-link-btn:hover {
  background: var(--cf-primary-50);
  border-color: var(--cf-primary-200);
}

.prescription-actions {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--cf-border);
  display: flex;
  gap: 8px;
}

/* Upload Area */
.upload-area {
  padding: 20px;
  border: 2px dashed var(--cf-border);
  border-radius: var(--cf-radius-md);
  text-align: center;
}

/* Modal */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(4px);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.15s ease;
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.modal-panel {
  background: white;
  border-radius: var(--cf-radius-lg);
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
  animation: scaleIn 0.2s ease;
}

@keyframes scaleIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--cf-border);
}

.btn-close-modal { background: none; border: none; color: var(--cf-slate-400); cursor: pointer; font-size: 1.1rem; padding: 4px; }
.btn-close-modal:hover { color: var(--cf-slate-700); }

.modal-body { padding: 24px; }
.modal-footer { padding: 16px 24px; border-top: 1px solid var(--cf-border); display: flex; justify-content: flex-end; gap: 8px; }
</style>
