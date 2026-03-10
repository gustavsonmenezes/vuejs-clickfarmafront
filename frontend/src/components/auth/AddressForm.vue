<template>
  <div class="address-form">
    <div class="card mb-4">
      <div class="card-header">
        <h6 class="mb-0">{{ address ? 'Editar Endereço' : 'Novo Endereço' }}</h6>
      </div>
      <div class="card-body">
        <form @submit.prevent="handleSubmit">
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label">Apelido do Endereço *</label>
              <input 
                type="text" 
                class="form-control" 
                v-model="formData.nickname"
                placeholder="Ex: Casa, Trabalho"
                required
              >
            </div>
            
            <div class="col-md-6 mb-3">
              <label class="form-label">CEP *</label>
              <input 
                type="text" 
                class="form-control" 
                v-model="formData.zipcode"
                @blur="fetchAddressByZipcode"
                placeholder="00000-000"
                required
              >
            </div>
          </div>
          
          <div class="row">
            <div class="col-md-8 mb-3">
              <label class="form-label">Logradouro *</label>
              <input 
                type="text" 
                class="form-control" 
                v-model="formData.street"
                placeholder="Rua, Avenida, etc."
                required
              >
            </div>
            
            <div class="col-md-4 mb-3">
              <label class="form-label">Número *</label>
              <input 
                type="text" 
                class="form-control" 
                v-model="formData.number"
                required
              >
            </div>
          </div>
          
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label">Bairro *</label>
              <input 
                type="text" 
                class="form-control" 
                v-model="formData.neighborhood"
                required
              >
            </div>
            
            <div class="col-md-6 mb-3">
              <label class="form-label">Complemento</label>
              <input 
                type="text" 
                class="form-control" 
                v-model="formData.complement"
                placeholder="Apartamento, Bloco, etc."
              >
            </div>
          </div>
          
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label">Cidade *</label>
              <input 
                type="text" 
                class="form-control" 
                v-model="formData.city"
                required
              >
            </div>
            
            <div class="col-md-6 mb-3">
              <label class="form-label">Estado *</label>
              <select class="form-select" v-model="formData.state" required>
                <option value="">Selecione</option>
                <option v-for="state in states" :key="state" :value="state">{{ state }}</option>
              </select>
            </div>
          </div>
          
          <div class="mb-3 form-check">
            <input 
              type="checkbox" 
              class="form-check-input" 
              v-model="formData.isDefault"
              :id="'default-' + (address ? address.id : 'new')"
            >
            <label class="form-check-label" :for="'default-' + (address ? address.id : 'new')">
              Definir como endereço principal
            </label>
          </div>
          
          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-secondary" @click="$emit('cancel')">
              Cancelar
            </button>
            <button type="submit" class="btn btn-primary" :disabled="loading">
              <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
              {{ address ? 'Atualizar' : 'Salvar' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AddressForm',
  props: {
    address: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      formData: {
        nickname: '',
        zipcode: '',
        street: '',
        number: '',
        neighborhood: '',
        complement: '',
        city: '',
        state: '',
        isDefault: false
      },
      states: [
        'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 
        'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 
        'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
      ],
      loading: false
    }
  },
  watch: {
    address: {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.formData = { ...newVal }
        } else {
          this.resetForm()
        }
      }
    }
  },
  methods: {
    resetForm() {
      this.formData = {
        nickname: '',
        zipcode: '',
        street: '',
        number: '',
        neighborhood: '',
        complement: '',
        city: '',
        state: '',
        isDefault: false
      }
    },
    
    async fetchAddressByZipcode() {
      // Implementar busca de CEP via API
      if (this.formData.zipcode.replace(/\D/g, '').length === 8) {
        console.log('Buscando endereço para o CEP:', this.formData.zipcode)
        // Simulação de busca de CEP
        const mockAddress = {
          street: 'Rua das Flores',
          neighborhood: 'Centro',
          city: 'Recife',
          state: 'PE'
        }
        
        this.formData = { ...this.formData, ...mockAddress }
      }
    },
    
    handleSubmit() {
      this.$emit('save-address', this.formData)
    }
  }
}
</script>

<style scoped>
.form-check-input {
  margin-top: 0.25rem;
}
</style>