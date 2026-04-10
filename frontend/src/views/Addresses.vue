<template>
  <div class="container mt-4">
    <h2>📍 Meus Endereços</h2>
    
    <div class="row">
      <div class="col-md-8">
        <div v-for="address in addresses" :key="address.id" class="card mb-3">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h6 class="card-title">{{ address.label }}</h6>
                <p class="card-text">
                  {{ address.street }}, {{ address.number }}<br>
                  {{ address.city }} - {{ address.state }}<br>
                  CEP: {{ address.zip }}
                </p>
                <span v-if="address.isDefault" class="badge bg-primary">Endereço Principal</span>
              </div>
              <div>
                <button @click="editAddress(address)" class="btn btn-sm btn-outline-primary me-2">
                  Editar
                </button>
                <button @click="deleteAddress(address.id)" class="btn btn-sm btn-outline-danger">
                  Excluir
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="addresses.length === 0" class="text-center py-5">
          <p class="text-muted">Nenhum endereço cadastrado.</p>
        </div>
      </div>
      
      <div class="col-md-4">
        <div class="card">
          <div class="card-header">
            <h5>{{ editingAddress ? 'Editar Endereço' : 'Novo Endereço' }}</h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="saveAddress">
              <div class="mb-3">
                <label class="form-label">Rótulo</label>
                <input v-model="addressForm.label" type="text" class="form-control" placeholder="Ex: Casa, Trabalho" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Rua</label>
                <input v-model="addressForm.street" type="text" class="form-control" required>
              </div>
              <div class="row">
                <div class="col-md-8 mb-3">
                  <label class="form-label">Número</label>
                  <input v-model="addressForm.number" type="text" class="form-control" required>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="form-label">Complemento</label>
                  <input v-model="addressForm.complement" type="text" class="form-control">
                </div>
              </div>
              <div class="row">
                <div class="col-md-8 mb-3">
                  <label class="form-label">Cidade</label>
                  <input v-model="addressForm.city" type="text" class="form-control" required>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="form-label">Estado</label>
                  <input v-model="addressForm.state" type="text" class="form-control" required>
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label">CEP</label>
                <input v-model="addressForm.zip" type="text" class="form-control" required>
              </div>
              <div class="mb-3 form-check">
                <input v-model="addressForm.isDefault" type="checkbox" class="form-check-input" id="defaultAddress">
                <label class="form-check-label" for="defaultAddress">
                  Definir como endereço principal
                </label>
              </div>
              <button type="submit" class="btn btn-primary w-100">
                {{ editingAddress ? 'Atualizar' : 'Adicionar' }} Endereço
              </button>
              <button v-if="editingAddress" @click="cancelEdit" type="button" class="btn btn-secondary w-100 mt-2">
                Cancelar
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Addresses',
  data() {
    return {
      addresses: [],
      addressForm: {
        label: '',
        street: '',
        number: '',
        complement: '',
        city: '',
        state: '',
        zip: '',
        isDefault: false
      },
      editingAddress: null
    }
  },
  mounted() {
    this.loadAddresses();
  },
  methods: {
    loadAddresses() {
      // Tenta carregar do LocalStorage
      const savedAddresses = localStorage.getItem('userAddressesList');
      if (savedAddresses) {
        try {
          this.addresses = JSON.parse(savedAddresses);
        } catch(e) {
          console.error("Erro ao ler endereços", e);
        }
      }
    },
    saveAddressesToStorage() {
      localStorage.setItem('userAddressesList', JSON.stringify(this.addresses));
    },
    saveAddress() {
      let savedId;
      if (this.editingAddress) {
        // Atualizar endereço existente
        const index = this.addresses.findIndex(addr => addr.id === this.editingAddress.id);
        if (index !== -1) {
          this.addresses[index] = { ...this.addressForm, id: this.editingAddress.id };
        }
        savedId = this.editingAddress.id;
        this.editingAddress = null;
      } else {
        // Adicionar novo endereço
        const newAddress = {
          ...this.addressForm,
          id: Date.now()
        };
        this.addresses.push(newAddress);
        savedId = newAddress.id;
      }
      
      // Se este endereço foi marcado como principal, desmarcar os outros
      if (this.addressForm.isDefault) {
        this.addresses.forEach(addr => {
          if (addr.id !== savedId) {
            addr.isDefault = false;
          }
        });
      }
      
      this.saveAddressesToStorage();
      this.resetForm();
      alert('Endereço salvo com sucesso!');
    },
    
    editAddress(address) {
      this.editingAddress = address;
      this.addressForm = { ...address };
    },
    
    deleteAddress(addressId) {
      if (confirm('Tem certeza que deseja excluir este endereço?')) {
        this.addresses = this.addresses.filter(addr => addr.id !== addressId);
        this.saveAddressesToStorage();
        alert('Endereço excluído com sucesso!');
      }
    },
    
    cancelEdit() {
      this.editingAddress = null;
      this.resetForm();
    },
    
    resetForm() {
      this.addressForm = {
        label: '',
        street: '',
        number: '',
        complement: '',
        city: '',
        state: '',
        zip: '',
        isDefault: false
      };
    }
  }
}
</script>
