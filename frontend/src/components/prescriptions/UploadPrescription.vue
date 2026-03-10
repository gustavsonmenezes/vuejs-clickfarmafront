<template>
  <div class="upload-prescription">
    <div class="card">
      <div class="card-header bg-light">
        <h6 class="mb-0">
          <i class="fas fa-upload me-2"></i>
          {{ editing ? 'Editar Receita' : 'Nova Receita' }}
        </h6>
      </div>
      <div class="card-body">
        <form @submit.prevent="handleSubmit">
          <!-- Upload de Arquivo -->
          <div class="mb-4">
            <label class="form-label">Upload da Receita *</label>
            <div class="file-upload-area" @click="triggerFileInput" @dragover="handleDragOver" @drop="handleDrop">
              <div class="text-center">
                <i class="fas fa-cloud-upload-alt fa-3x text-primary mb-3"></i>
                <p class="mb-2">Arraste e solte o arquivo aqui ou</p>
                <button type="button" class="btn btn-outline-primary">
                  Selecione o arquivo
                </button>
                <input 
                  type="file" 
                  ref="fileInput"
                  @change="handleFileSelect"
                  accept=".jpg,.jpeg,.png,.pdf,.doc,.docx"
                  class="d-none"
                >
              </div>
            </div>
            
            <div v-if="selectedFile" class="selected-file mt-3">
              <div class="d-flex align-items-center justify-content-between p-3 bg-light rounded">
                <div>
                  <i class="fas fa-file me-2"></i>
                  <span>{{ selectedFile.name }}</span>
                  <small class="text-muted d-block">({{ formatFileSize(selectedFile.size) }})</small>
                </div>
                <button type="button" class="btn btn-sm btn-outline-danger" @click="removeFile">
                  <i class="fas fa-times"></i>
                </button>
              </div>
            </div>
            
            <div class="form-text">
              Formatos aceitos: JPG, PNG, PDF, DOC, DOCX. Tamanho máximo: 10MB
            </div>
          </div>

          <!-- Informações da Receita -->
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label">Médico *</label>
              <input 
                type="text" 
                class="form-control" 
                v-model="formData.doctorName"
                placeholder="Nome do médico"
                required
              >
            </div>
            
            <div class="col-md-6 mb-3">
              <label class="form-label">CRM *</label>
              <input 
                type="text" 
                class="form-control" 
                v-model="formData.doctorCrm"
                placeholder="Número do CRM"
                required
              >
            </div>
          </div>

          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label">Data da Receita *</label>
              <input 
                type="date" 
                class="form-control" 
                v-model="formData.prescriptionDate"
                required
              >
            </div>
            
            <div class="col-md-6 mb-3">
              <label class="form-label">Validade *</label>
              <input 
                type="date" 
                class="form-control" 
                v-model="formData.expiryDate"
                required
              >
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label">Medicamentos Prescritos *</label>
            <textarea 
              class="form-control" 
              v-model="formData.medications"
              placeholder="Lista de medicamentos prescritos..."
              rows="3"
              required
            ></textarea>
          </div>

          <div class="mb-3">
            <label class="form-label">Observações</label>
            <textarea 
              class="form-control" 
              v-model="formData.notes"
              placeholder="Observações adicionais..."
              rows="2"
            ></textarea>
          </div>

          <!-- Ações -->
          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-secondary" @click="$emit('cancel')">
              Cancelar
            </button>
            <button type="submit" class="btn btn-primary" :disabled="uploading || !selectedFile">
              <span v-if="uploading" class="spinner-border spinner-border-sm me-1"></span>
              {{ uploading ? 'Enviando...' : (editing ? 'Atualizar' : 'Enviar Receita') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'UploadPrescription',
  props: {
    prescription: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      formData: {
        doctorName: '',
        doctorCrm: '',
        prescriptionDate: '',
        expiryDate: '',
        medications: '',
        notes: ''
      },
      selectedFile: null,
      uploading: false,
      isDragging: false
    }
  },
  computed: {
    editing() {
      return this.prescription !== null
    }
  },
  methods: {
    ...mapActions(['uploadPrescription', 'updatePrescription']),
    
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    
    handleFileSelect(event) {
      const file = event.target.files[0]
      if (file) {
        this.validateFile(file)
      }
    },
    
    handleDragOver(event) {
      event.preventDefault()
      this.isDragging = true
    },
    
    handleDrop(event) {
      event.preventDefault()
      this.isDragging = false
      
      const files = event.dataTransfer.files
      if (files.length > 0) {
        this.validateFile(files[0])
      }
    },
    
    validateFile(file) {
      // Validar tamanho do arquivo (10MB máximo)
      const maxSize = 10 * 1024 * 1024
      if (file.size > maxSize) {
        this.$toast.error('O arquivo deve ter no máximo 10MB')
        return
      }
      
      // Validar tipo do arquivo
      const validTypes = ['image/jpeg', 'image/png', 'application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
      if (!validTypes.includes(file.type)) {
        this.$toast.error('Tipo de arquivo não suportado')
        return
      }
      
      this.selectedFile = file
    },
    
    removeFile() {
      this.selectedFile = null
      this.$refs.fileInput.value = ''
    },
    
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },
    
    async handleSubmit() {
      if (!this.selectedFile) {
        this.$toast.error('Selecione um arquivo para upload')
        return
      }
      
      this.uploading = true
      
      try {
        const formData = new FormData()
        formData.append('file', this.selectedFile)
        formData.append('doctorName', this.formData.doctorName)
        formData.append('doctorCrm', this.formData.doctorCrm)
        formData.append('prescriptionDate', this.formData.prescriptionDate)
        formData.append('expiryDate', this.formData.expiryDate)
        formData.append('medications', this.formData.medications)
        formData.append('notes', this.formData.notes)
        
        if (this.editing) {
          formData.append('id', this.prescription.id)
          await this.updatePrescription(formData)
        } else {
          await this.uploadPrescription(formData)
        }
        
        this.$emit('upload-complete')
        
      } catch (error) {
        console.error('Erro ao enviar receita:', error)
        this.$toast.error('Erro ao enviar receita. Tente novamente.')
      } finally {
        this.uploading = false
      }
    },
    
    resetForm() {
      this.formData = {
        doctorName: '',
        doctorCrm: '',
        prescriptionDate: '',
        expiryDate: '',
        medications: '',
        notes: ''
      }
      this.removeFile()
    }
  },
  watch: {
    prescription: {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.formData = { ...newVal }
        } else {
          this.resetForm()
        }
      }
    }
  }
}
</script>

<style scoped>
.file-upload-area {
  border: 2px dashed #dee2e6;
  border-radius: 8px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.file-upload-area:hover {
  border-color: #0d6efd;
  background-color: #f8f9fa;
}

.file-upload-area.dragging {
  border-color: #0d6efd;
  background-color: #e3f2fd;
}

.selected-file {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>