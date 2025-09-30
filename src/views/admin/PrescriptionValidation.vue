<template>
  <div class="prescription-validation container-fluid">
    <h2>Validar Receitas</h2>
    <div class="row">
      <div v-for="prescription in prescriptions" :key="prescription.id" class="col-md-6 mb-3">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Receita #{{ prescription.id }} - Pedido {{ prescription.orderId }}</h5>
            <p class="card-text">Cliente: {{ prescription.customerName }}</p>
            <p class="card-text">Medicamento: {{ prescription.medication }}</p>
            <p class="card-text">Data: {{ prescription.date }}</p>
            <p class="card-text">Status: <span :class="getStatusClass(prescription.status)">{{ prescription.status }}</span></p>
            <p v-if="prescription.fileUrl" class="card-text">
              Arquivo: <a :href="prescription.fileUrl" target="_blank">Ver Receita (PDF/Imagem)</a>
            </p>
            <div class="btn-group" v-if="prescription.status === 'Pendente'">
              <button @click="validatePrescription(prescription.id, 'Aprovada')" class="btn btn-success btn-sm">Aprovar</button>
              <button @click="validatePrescription(prescription.id, 'Rejeitada')" class="btn btn-danger btn-sm">Rejeitar</button>
              <button @click="uploadReplacement(prescription.id)" class="btn btn-info btn-sm">Substituir Arquivo</button>
            </div>
            <p v-if="prescription.status !== 'Pendente'" class="text-muted small">Validação realizada em {{ prescription.validatedAt || 'N/A' }}</p>
          </div>
        </div>
      </div>
    </div>
    <!-- Input para upload de nova receita (exemplo para substituição) -->
    <div v-if="uploadingPrescriptionId" class="mt-3">
      <h5>Substituir Receita #{{ uploadingPrescriptionId }}</h5>
      <input type="file" @change="handleFileUpload" accept="image/*,application/pdf" class="form-control mb-2" />
      <button @click="submitUpload" class="btn btn-primary">Enviar Nova Receita</button>
      <button @click="cancelUpload" class="btn btn-secondary ml-2">Cancelar</button>
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
  mounted() {
    this.fetchPrescriptions()
  },
  methods: {
    async fetchPrescriptions() {
      // Dados mockados
      this.prescriptions = [
        { 
          id: 1, 
          orderId: 1001, 
          customerName: 'João Silva', 
          medication: 'Antibiótico Amoxicilina', 
          date: '2023-10-01', 
          status: 'Pendente', 
          fileUrl: '/mock/receita1.pdf',
          validatedAt: null 
        },
        { 
          id: 2, 
          orderId: 1002, 
          customerName: 'Maria Oliveira', 
          medication: 'Paracetamol Controlado', 
          date: '2023-10-02', 
          status: 'Aprovada', 
          fileUrl: '/mock/receita2.pdf',
          validatedAt: '2023-10-02 14:30' 
        },
        { 
          id: 3, 
          orderId: 1003, 
          customerName: 'Carlos Santos', 
          medication: 'Dipirona Sódica', 
          date: '2023-10-03', 
          status: 'Pendente', 
          fileUrl: '/mock/receita3.pdf',
          validatedAt: null 
        }
      ]
    },

    getStatusClass(status) {
      return {
        'text-warning': status === 'Pendente',
        'text-success': status === 'Aprovada',
        'text-danger': status === 'Rejeitada'
      }
    },

    async validatePrescription(id, status) {
      if (confirm(`Confirmar ${status.toLowerCase()} da receita?`)) {
        const prescription = this.prescriptions.find(p => p.id === id)
        if (prescription) {
          prescription.status = status
          prescription.validatedAt = new Date().toLocaleString('pt-BR')
        }
        alert(`Receita ${status.toLowerCase()} com sucesso!`)
      }
    },

    uploadReplacement(id) {
      this.uploadingPrescriptionId = id
      this.selectedFile = null
    },

    handleFileUpload(event) {
      this.selectedFile = event.target.files[0]
    },

    async submitUpload() {
      if (!this.selectedFile) {
        alert('Selecione um arquivo')
        return
      }

      // Simula upload mockado
      const prescription = this.prescriptions.find(p => p.id === this.uploadingPrescriptionId)
      if (prescription) {
        prescription.fileUrl = URL.createObjectURL(this.selectedFile) // Mock URL
        prescription.status = 'Pendente' // Volta para pendente após upload
        prescription.validatedAt = null
      }
      
      this.cancelUpload()
      alert('Nova receita enviada para validação!')
    },

    cancelUpload() {
      this.uploadingPrescriptionId = null
      this.selectedFile = null
    }
  }
}
</script>

<style scoped>
.card { box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.btn-group { margin-top: 10px; }
.ml-2 { margin-left: 0.5rem; }
</style>