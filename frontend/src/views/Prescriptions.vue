<template>
  <div class="container mt-4">
    <h2>📄 Minhas Receitas</h2>
    
    <div class="row">
      <div class="col-md-8">
        <div class="card mb-4">
          <div class="card-header">
            <h5>📤 Enviar Nova Receita</h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="uploadPrescription">
              <div class="mb-3">
                <label class="form-label">Arquivo da Receita</label>
                <input 
                  @change="handleFileSelect" 
                  type="file" 
                  class="form-control" 
                  accept="image/*,.pdf"
                  required
                >
                <div class="form-text">Formatos aceitos: JPG, PNG, PDF (máx. 5MB)</div>
              </div>
              <div class="mb-3">
                <label class="form-label">Observações (opcional)</label>
                <textarea 
                  v-model="uploadForm.notes" 
                  class="form-control" 
                  rows="3"
                  placeholder="Adicione observações sobre a receita..."
                ></textarea>
              </div>
              <button type="submit" class="btn btn-primary" :disabled="!selectedFile">
                Enviar Receita
              </button>
            </form>
          </div>
        </div>
        
        <div class="card">
          <div class="card-header">
            <h5>📋 Histórico de Receitas</h5>
          </div>
          <div class="card-body">
            <div v-if="prescriptions.length === 0" class="text-center py-4">
              <p class="text-muted">Nenhuma receita enviada ainda.</p>
            </div>
            <div v-else>
              <div v-for="prescription in prescriptions" :key="prescription.id" class="border-bottom pb-3 mb-3">
                <div class="d-flex justify-content-between align-items-start">
                  <div>
                    <h6>{{ prescription.fileName }}</h6>
                    <p class="text-muted small mb-1">
                      Enviado em: {{ formatDate(prescription.uploadDate) }}
                    </p>
                    
                    <!-- Botão para expandir detalhes -->
                    <button 
                      @click="togglePrescriptionDetails(prescription.id)" 
                      class="btn btn-sm btn-outline-secondary mb-2"
                    >
                      {{ prescription.showDetails ? '▲ Ocultar' : '▼ Mostrar' }} Detalhes
                    </button>
                    
                    <!-- Detalhes da receita (v-show) -->
                    <div v-show="prescription.showDetails" class="prescription-details">
                      <p v-if="prescription.notes" class="small mb-2">
                        <strong>Observações:</strong> {{ prescription.notes }}
                      </p>
                      <p class="small text-muted">
                        <strong>Tipo:</strong> {{ prescription.fileType || 'Imagem/PDF' }}
                      </p>
                      <p class="small text-muted">
                        <strong>Tamanho:</strong> {{ prescription.fileSize || 'N/A' }}
                      </p>
                    </div>
                    
                    <span :class="getStatusClass(prescription.status)" class="badge">
                      {{ getStatusText(prescription.status) }}
                    </span>
                  </div>
                  <div>
                    <button @click="viewPrescription(prescription)" class="btn btn-sm btn-outline-primary">
                      Ver Detalhes
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Sidebar de Informações com toggle -->
      <div class="col-md-4">
        <!-- Botão para mostrar/ocultar informações -->
        <div class="d-grid mb-3">
          <button 
            @click="showInfoSection = !showInfoSection" 
            class="btn btn-info"
          >
            ℹ️ {{ showInfoSection ? 'Ocultar' : 'Mostrar' }} Informações
          </button>
        </div>
        
        <!-- Seção de informações (v-show) -->
        <div v-show="showInfoSection">
          <div class="card">
            <div class="card-header">
              <h5>ℹ️ Informações</h5>
            </div>
            <div class="card-body">
              <h6>Como funciona?</h6>
              <ol class="small">
                <li>Envie a foto ou PDF da sua receita</li>
                <li>Nossa equipe irá validar a receita</li>
                <li>Você será notificado sobre o status</li>
                <li>Após aprovada, os medicamentos estarão disponíveis para compra</li>
              </ol>
              
              <h6 class="mt-3">Status das Receitas:</h6>
              <ul class="small">
                <li><span class="badge bg-warning">Pendente</span> - Aguardando análise</li>
                <li><span class="badge bg-primary">Em Análise</span> - Sendo validada</li>
                <li><span class="badge bg-success">Aprovada</span> - Receita válida</li>
                <li><span class="badge bg-danger">Rejeitada</span> - Receita inválida</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Prescriptions',
  data() {
    return {
      selectedFile: null,
      uploadForm: {
        notes: ''
      },
      showInfoSection: true, // Controla visibilidade da sidebar
      prescriptions: [
        {
          id: 1,
          fileName: 'receita_antibiotico.jpg',
          uploadDate: new Date('2024-01-15'),
          status: 'approved',
          notes: 'Receita para tratamento de infecção',
          showDetails: false, // Controla detalhes expandíveis
          fileType: 'JPEG',
          fileSize: '2.3 MB'
        },
        {
          id: 2,
          fileName: 'receita_vitaminas.pdf',
          uploadDate: new Date('2024-01-10'),
          status: 'pending',
          notes: '',
          showDetails: false,
          fileType: 'PDF',
          fileSize: '1.1 MB'
        }
      ]
    }
  },
  methods: {
    handleFileSelect(event) {
      this.selectedFile = event.target.files[0];
    },
    
    uploadPrescription() {
      if (!this.selectedFile) {
        alert('Por favor, selecione um arquivo.');
        return;
      }
      
      // Simular upload
      const newPrescription = {
        id: Date.now(),
        fileName: this.selectedFile.name,
        uploadDate: new Date(),
        status: 'pending',
        notes: this.uploadForm.notes,
        showDetails: false,
        fileType: this.selectedFile.type,
        fileSize: this.formatFileSize(this.selectedFile.size)
      };
      
      this.prescriptions.unshift(newPrescription);
      
      // Reset form
      this.selectedFile = null;
      this.uploadForm.notes = '';
      document.querySelector('input[type="file"]').value = '';
      
      alert('Receita enviada com sucesso! Você será notificado sobre o status da análise.');
    },
    
    // Alternar detalhes da receita
    togglePrescriptionDetails(prescriptionId) {
      const prescription = this.prescriptions.find(p => p.id === prescriptionId);
      if (prescription) {
        prescription.showDetails = !prescription.showDetails;
      }
    },
    
    viewPrescription(prescription) {
      alert(`Detalhes da Receita:\n\nArquivo: ${prescription.fileName}\nStatus: ${this.getStatusText(prescription.status)}\nData: ${this.formatDate(prescription.uploadDate)}\nObservações: ${prescription.notes || 'Nenhuma'}`);
    },
    
    formatDate(date) {
      return date.toLocaleDateString('pt-BR');
    },
    
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    
    getStatusText(status) {
      const statusMap = {
        'pending': 'Pendente',
        'analyzing': 'Em Análise',
        'approved': 'Aprovada',
        'rejected': 'Rejeitada'
      };
      return statusMap[status] || status;
    },
    
    getStatusClass(status) {
      const classMap = {
        'pending': 'bg-warning',
        'analyzing': 'bg-primary',
        'approved': 'bg-success',
        'rejected': 'bg-danger'
      };
      return classMap[status] || 'bg-secondary';
    }
  }
}
</script>

<style scoped>
.prescription-details {
  background-color: #f8f9fa;
  padding: 10px;
  border-radius: 5px;
  margin-top: 5px;
  border-left: 4px solid #0d6efd;
}

.btn-info {
  background-color: #0dcaf0;
  border-color: #0dcaf0;
}

/* Transição suave para os elementos com v-show */
[v-show] {
  transition: opacity 0.3s ease-in-out;
}
</style>