<template>
  <div class="product-management container-fluid">
    <h2 class="mb-4">Gerenciar Produtos</h2>
    
    <!-- Notificação de Sucesso/Erro -->
    <div v-if="notification.show" :class="['alert', notification.type === 'success' ? 'alert-success' : 'alert-danger', 'alert-dismissible']" role="alert">
      {{ notification.message }}
      <button type="button" class="btn-close" @click="hideNotification" aria-label="Close"></button>
    </div>

    <!-- Formulário de Adição/Edição de Produtos -->
    <form @submit.prevent="submitProduct" class="mb-4">
      <div class="row">
        <div class="col-md-3">
          <input 
            v-model="productForm.name" 
            class="form-control" 
            :class="{ 'is-invalid': errors.name }"
            placeholder="Nome do Medicamento" 
            required 
          />
          <div v-if="errors.name" class="invalid-feedback">{{ errors.name }}</div>
        </div>
        <div class="col-md-2">
          <input 
            v-model="productForm.price" 
            type="number" 
            step="0.01" 
            min="0.01"
            class="form-control" 
            :class="{ 'is-invalid': errors.price }"
            placeholder="Preço (R$)" 
            required 
          />
          <div v-if="errors.price" class="invalid-feedback">{{ errors.price }}</div>
        </div>
        <div class="col-md-3">
          <input 
            v-model="productForm.description" 
            class="form-control" 
            :class="{ 'is-invalid': errors.description }"
            placeholder="Descrição/Princípio Ativo" 
            maxlength="255"
          />
          <div v-if="errors.description" class="invalid-feedback">{{ errors.description }}</div>
        </div>
        <div class="col-md-2">
          <select v-model="productForm.requiresPrescription" class="form-control">
            <option value="false">Sem Receita</option>
            <option value="true">Com Receita</option>
          </select>
        </div>
        <div class="col-md-2">
          <button type="submit" class="btn w-100" :class="isEditing ? 'btn-warning' : 'btn-success'">
            {{ isEditing ? 'Atualizar' : 'Adicionar' }}
          </button>
          <button v-if="isEditing" type="button" @click="cancelEdit" class="btn btn-secondary w-100 mt-1">
            Cancelar
          </button>
        </div>
      </div>
    </form>

    <!-- Paginação -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <span>Mostrando {{ startIndex + 1 }} - {{ endIndex }} de {{ totalProducts }} produtos</span>
      </div>
      <div>
        <button 
          @click="previousPage" 
          :disabled="currentPage === 1" 
          class="btn btn-outline-primary me-2"
        >
          Anterior
        </button>
        <span class="me-2">Página {{ currentPage }} de {{ totalPages }}</span>
        <button 
          @click="nextPage" 
          :disabled="currentPage === totalPages" 
          class="btn btn-outline-primary"
        >
          Próxima
        </button>
      </div>
    </div>

    <!-- Lista de Produtos -->
    <div class="row">
      <div v-for="product in paginatedProducts" :key="product.id" class="col-md-4 mb-3">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">{{ product.name }}</h5>
            <p class="card-text">Preço: R$ {{ product.price.toFixed(2) }}</p>
            <p class="card-text">{{ product.description }}</p>
            <p class="card-text">Receita: {{ product.requiresPrescription ? 'Sim' : 'Não' }}</p>
            <p class="card-text">Estoque: {{ product.stock }} unidades</p>
            <div class="d-flex gap-2">
              <button @click="editProduct(product)" class="btn btn-warning btn-sm">Editar</button>
              <button @click="showDeleteModal(product)" class="btn btn-danger btn-sm">Excluir</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    
    <div v-if="showDeleteConfirm" class="modal d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Confirmar Exclusão</h5>
            <button type="button" class="btn-close" @click="hideDeleteModal"></button>
          </div>
          <div class="modal-body">
            <p>Tem certeza que deseja excluir o produto <strong>{{ productToDelete?.name }}</strong>?</p>
            <p class="text-muted">Esta ação não pode ser desfeita.</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="hideDeleteModal">Cancelar</button>
            <button type="button" class="btn btn-danger" @click="confirmDelete">Excluir</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductManagement',
  data() {
    return {
      products: [],
      productForm: { 
        id: null,
        name: '', 
        price: 0, 
        description: '', 
        requiresPrescription: false 
      },
      errors: {},
      notification: {
        show: false,
        message: '',
        type: 'success'
      },
      isEditing: false,
      showDeleteConfirm: false,
      productToDelete: null,
      currentPage: 1,
      itemsPerPage: 6
    }
  },
  computed: {
    totalProducts() {
      return this.products.length
    },
    totalPages() {
      return Math.ceil(this.totalProducts / this.itemsPerPage)
    },
    startIndex() {
      return (this.currentPage - 1) * this.itemsPerPage
    },
    endIndex() {
      return Math.min(this.startIndex + this.itemsPerPage, this.totalProducts)
    },
    paginatedProducts() {
      return this.products.slice(this.startIndex, this.endIndex)
    }
  },
  mounted() {
    this.fetchProducts()
  },
  methods: {
    async fetchProducts() {
      
      this.products = [
        { id: 1, name: 'Paracetamol 500mg', price: 10.50, description: 'Analgésico comum', requiresPrescription: false, stock: 50 },
        { id: 2, name: 'Amoxicilina 500mg', price: 25.00, description: 'Antibiótico para infecções', requiresPrescription: true, stock: 30 },
        { id: 3, name: 'Dipirona 500mg', price: 8.75, description: 'Analgésico e antitérmico', requiresPrescription: false, stock: 45 },
        { id: 4, name: 'Omeprazol 20mg', price: 15.30, description: 'Protetor gástrico', requiresPrescription: false, stock: 60 },
        { id: 5, name: 'Losartana 50mg', price: 12.90, description: 'Anti-hipertensivo', requiresPrescription: true, stock: 25 },
        { id: 6, name: 'Metformina 850mg', price: 18.50, description: 'Antidiabético', requiresPrescription: true, stock: 35 },
        { id: 7, name: 'Ibuprofeno 600mg', price: 14.20, description: 'Anti-inflamatório', requiresPrescription: false, stock: 40 }
      ]
      this.showNotification('Produtos carregados com sucesso!', 'success')
    },

    validateForm() {
      this.errors = {}
      
      if (!this.productForm.name.trim()) {
        this.errors.name = 'Nome é obrigatório'
      } else if (this.productForm.name.length < 3) {
        this.errors.name = 'Nome deve ter pelo menos 3 caracteres'
      }
      
      if (!this.productForm.price || this.productForm.price <= 0) {
        this.errors.price = 'Preço deve ser maior que zero'
      }
      
      if (this.productForm.description && this.productForm.description.length > 255) {
        this.errors.description = 'Descrição deve ter no máximo 255 caracteres'
      }
      
      return Object.keys(this.errors).length === 0
    },

    async submitProduct() {
      if (!this.validateForm()) {
        return
      }

      if (this.isEditing) {
        await this.updateProduct()
      } else {
        await this.addProduct()
      }
    },

    async addProduct() {
      
      const newProduct = {
        ...this.productForm,
        id: Date.now(), 
        price: parseFloat(this.productForm.price),
        requiresPrescription: this.productForm.requiresPrescription === 'true',
        stock: 0 
      }
      
      this.products.push(newProduct)
      this.resetForm()
      this.showNotification('Produto adicionado com sucesso!', 'success')
    },

    async updateProduct() {
      
      const index = this.products.findIndex(p => p.id === this.productForm.id)
      if (index !== -1) {
        this.products[index] = {
          ...this.productForm,
          price: parseFloat(this.productForm.price),
          requiresPrescription: this.productForm.requiresPrescription === 'true'
        }
      }
      this.resetForm()
      this.showNotification('Produto atualizado com sucesso!', 'success')
    },

    editProduct(product) {
      this.productForm = { ...product }
      this.isEditing = true
      this.errors = {}
    },

    cancelEdit() {
      this.resetForm()
    },

    resetForm() {
      this.productForm = { 
        id: null,
        name: '', 
        price: 0, 
        description: '', 
        requiresPrescription: false 
      }
      this.isEditing = false
      this.errors = {}
    },

    showDeleteModal(product) {
      this.productToDelete = product
      this.showDeleteConfirm = true
    },

    hideDeleteModal() {
      this.productToDelete = null
      this.showDeleteConfirm = false
    },

    async confirmDelete() {
      if (!this.productToDelete) return

      // Remove produto mockado
      this.products = this.products.filter(p => p.id !== this.productToDelete.id)
      this.showNotification('Produto excluído com sucesso!', 'success')
      this.hideDeleteModal()
      
      // Ajusta páginação se necessário
      if (this.paginatedProducts.length === 0 && this.currentPage > 1) {
        this.currentPage--
      }
    },

    showNotification(message, type = 'success') {
      this.notification = {
        show: true,
        message,
        type
      }
      setTimeout(() => {
        this.hideNotification()
      }, 5000)
    },

    hideNotification() {
      this.notification.show = false
    },

    previousPage() {
      if (this.currentPage > 1) {
        this.currentPage--
      }
    },

    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
      }
    }
  }
}
</script>

<style scoped>
.card { 
  box-shadow: 0 2px 4px rgba(0,0,0,0.1); 
  transition: transform 0.2s ease-in-out;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.modal {
  z-index: 1050;
}

.alert {
  position: relative;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.btn {
  transition: all 0.2s ease-in-out;
}

.btn:hover {
  transform: translateY(-1px);
}

.is-invalid {
  border-color: #dc3545;
}

.invalid-feedback {
  display: block;
}
</style>