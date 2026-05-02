<template>
  <div class="product-management">
    <!-- Notification -->
    <div v-if="notification.show" :class="['notification', notification.type]" role="alert">
      <i :class="notification.type === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-circle'"></i>
      <span>{{ notification.message }}</span>
      <button class="btn-close" @click="hideNotification"><i class="fas fa-xmark"></i></button>
    </div>

    <!-- Header with Actions -->
    <div class="section-header mb-4">
      <div class="d-flex gap-2 flex-grow-1">
        <div class="search-box flex-grow-1">
          <i class="fas fa-search"></i>
          <input v-model="search" class="cf-input ps-4" placeholder="Buscar produtos..." />
        </div>
      </div>
      <button @click="openCreateModal" class="cf-btn cf-btn-primary ms-3">
        <i class="fas fa-plus"></i>
        Novo Produto
      </button>
    </div>

    <!-- Products Table -->
    <div class="cf-card">
      <div class="cf-card-body p-0">
        <table class="cf-table" v-if="filteredProducts.length">
          <thead>
            <tr>
              <th class="ps-4">Produto</th>
              <th>Descrição</th>
              <th class="text-center">Preço</th>
              <th class="text-center">Estoque</th>
              <th class="text-center">Receita</th>
              <th class="text-end pe-4" style="width: 140px;">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in paginatedProducts" :key="product.id">
              <td class="ps-4">
                <div class="fw-semibold text-dark">{{ product.nome }}</div>
              </td>
              <td>
                <span class="cf-text-muted small">{{ product.descricao || '-' }}</span>
              </td>
              <td class="text-center fw-semibold">R$ {{ formatPrice(product.preco) }}</td>
              <td class="text-center">
                <span :class="['cf-badge', product.estoque < 10 ? 'cf-badge-warning' : 'cf-badge-success']">
                  {{ product.estoque || 0 }}
                </span>
              </td>
              <td class="text-center">
                <span v-if="product.receita" class="cf-badge cf-badge-info">Sim</span>
                <span v-else class="cf-text-muted small">Não</span>
              </td>
              <td class="text-end pe-4">
                <div class="actions">
                  <button @click="openEditModal(product)" class="action-btn" title="Editar">
                    <i class="fas fa-pen"></i>
                  </button>
                  <button @click="showDeleteModal(product)" class="action-btn danger" title="Excluir">
                    <i class="fas fa-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="cf-empty-state">
          <i class="fas fa-pills"></i>
          <p>Nenhum produto encontrado.</p>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="pagination-bar mt-3">
      <span class="cf-text-muted">
        Mostrando {{ startIndex + 1 }} - {{ endIndex }} de {{ totalProducts }}
      </span>
      <div class="d-flex gap-2">
        <button @click="previousPage" :disabled="currentPage === 1" class="cf-btn cf-btn-secondary cf-btn-sm">
          Anterior
        </button>
        <button @click="nextPage" :disabled="currentPage === totalPages" class="cf-btn cf-btn-secondary cf-btn-sm">
          Próximo
        </button>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
      <div class="modal-panel">
        <div class="modal-header">
          <h5 class="mb-0 fw-semibold">{{ isEditing ? 'Editar Produto' : 'Novo Produto' }}</h5>
          <button @click="closeModal" class="btn-close-modal"><i class="fas fa-xmark"></i></button>
        </div>
        <form @submit.prevent="submitProduct" class="modal-body">
          <div class="form-group mb-3">
            <label class="form-label">Nome do Medicamento</label>
            <input v-model="productForm.name" class="cf-input" :class="{ 'is-invalid': errors.name }" placeholder="Ex: Dipirona 500mg" required />
            <div v-if="errors.name" class="invalid-feedback">{{ errors.name }}</div>
          </div>
          
          <div class="row g-3 mb-3">
            <div class="col-sm-6">
              <label class="form-label">Preço (R$)</label>
              <input v-model="productForm.price" type="number" step="0.01" min="0.01" class="cf-input" :class="{ 'is-invalid': errors.price }" placeholder="0,00" required />
              <div v-if="errors.price" class="invalid-feedback">{{ errors.price }}</div>
            </div>
            <div class="col-sm-6">
              <label class="form-label">Estoque Inicial</label>
              <input v-model="productForm.stock" type="number" min="0" class="cf-input" placeholder="0" />
            </div>
          </div>
          
          <div class="form-group mb-3">
            <label class="form-label">Descrição</label>
            <input v-model="productForm.description" class="cf-input" :class="{ 'is-invalid': errors.description }" placeholder="Princípio ativo, dosagem..." maxlength="255" />
            <div v-if="errors.description" class="invalid-feedback">{{ errors.description }}</div>
          </div>
          
          <div class="form-group mb-4">
            <label class="form-label">Requer Receita?</label>
            <select v-model="productForm.requiresPrescription" class="cf-select">
              <option value="false">Não</option>
              <option value="true">Sim</option>
            </select>
          </div>
          
          <div class="modal-footer">
            <button type="button" @click="closeModal" class="cf-btn cf-btn-secondary">Cancelar</button>
            <button type="submit" class="cf-btn cf-btn-primary">
              {{ isEditing ? 'Salvar Alterações' : 'Criar Produto' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteConfirm" class="modal-backdrop" @click.self="hideDeleteModal">
      <div class="modal-panel modal-sm">
        <div class="modal-header">
          <h5 class="mb-0 fw-semibold">Confirmar Exclusão</h5>
          <button @click="hideDeleteModal" class="btn-close-modal"><i class="fas fa-xmark"></i></button>
        </div>
        <div class="modal-body">
          <p class="mb-1">Tem certeza que deseja excluir o produto <strong>{{ productToDelete?.nome }}</strong>?</p>
          <p class="cf-text-muted mb-0 small">Esta ação não pode ser desfeita.</p>
        </div>
        <div class="modal-footer">
          <button class="cf-btn cf-btn-secondary" @click="hideDeleteModal">Cancelar</button>
          <button class="cf-btn cf-btn-danger" @click="confirmDelete">Excluir</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { adminService } from '@/services/adminService';

export default {
  name: 'ProductManagement',
  data() {
    return {
      products: [],
      search: '',
      productForm: { id: null, name: '', price: 0, description: '', requiresPrescription: false, stock: 0 },
      errors: {},
      notification: { show: false, message: '', type: 'success' },
      isEditing: false,
      showModal: false,
      showDeleteConfirm: false,
      productToDelete: null,
      currentPage: 1,
      itemsPerPage: 10,
      loading: false
    }
  },
  computed: {
    filteredProducts() {
      if (!this.search) return this.products;
      const q = this.search.toLowerCase();
      return this.products.filter(p => 
        p.nome.toLowerCase().includes(q) || 
        (p.descricao && p.descricao.toLowerCase().includes(q))
      );
    },
    totalProducts() { return this.filteredProducts.length; },
    totalPages() { return Math.ceil(this.totalProducts / this.itemsPerPage) || 1; },
    startIndex() { return (this.currentPage - 1) * this.itemsPerPage; },
    endIndex() { return Math.min(this.startIndex + this.itemsPerPage, this.totalProducts); },
    paginatedProducts() { return this.filteredProducts.slice(this.startIndex, this.endIndex); }
  },
  mounted() { this.fetchProducts(); },
  methods: {
    async fetchProducts() {
      this.loading = true;
      try {
        const res = await adminService.getProducts();
        this.products = res.data || [];
      } catch (e) {
        this.showNotification('Erro ao carregar produtos', 'error');
      } finally { this.loading = false; }
    },
    openCreateModal() {
      this.resetForm();
      this.showModal = true;
    },
    openEditModal(product) {
      this.productForm = {
        id: product.id,
        name: product.nome,
        price: product.preco,
        description: product.descricao,
        stock: product.estoque,
        requiresPrescription: product.receita
      };
      this.isEditing = true;
      this.errors = {};
      this.showModal = true;
    },
    closeModal() {
      this.showModal = false;
      this.resetForm();
    },
    validateForm() {
      this.errors = {};
      if (!this.productForm.name.trim()) this.errors.name = 'Nome é obrigatório';
      if (!this.productForm.price || this.productForm.price <= 0) this.errors.price = 'Preço deve ser maior que zero';
      if (this.productForm.description && this.productForm.description.length > 255) this.errors.description = 'Máximo 255 caracteres';
      return Object.keys(this.errors).length === 0;
    },
    async submitProduct() {
      if (!this.validateForm()) return;
      try {
        const payload = {
          nome: this.productForm.name,
          preco: parseFloat(this.productForm.price),
          descricao: this.productForm.description,
          estoque: parseInt(this.productForm.stock) || 0
        };
        if (this.isEditing) {
          await adminService.updateProduct(this.productForm.id, payload);
          this.showNotification('Produto atualizado com sucesso!', 'success');
        } else {
          await adminService.createProduct(payload);
          this.showNotification('Produto criado com sucesso!', 'success');
        }
        this.closeModal();
        await this.fetchProducts();
      } catch (e) {
        this.showNotification('Erro: ' + (e.response?.data?.message || e.message), 'error');
      }
    },
    resetForm() {
      this.productForm = { id: null, name: '', price: 0, description: '', requiresPrescription: false, stock: 0 };
      this.isEditing = false;
      this.errors = {};
    },
    showDeleteModal(product) {
      this.productToDelete = product;
      this.showDeleteConfirm = true;
    },
    hideDeleteModal() {
      this.productToDelete = null;
      this.showDeleteConfirm = false;
    },
    async confirmDelete() {
      if (!this.productToDelete) return;
      try {
        await adminService.deleteProduct(this.productToDelete.id);
        this.showNotification('Produto excluído com sucesso!', 'success');
        this.hideDeleteModal();
        await this.fetchProducts();
      } catch (e) {
        this.showNotification('Erro ao excluir produto', 'error');
      }
      if (this.paginatedProducts.length === 0 && this.currentPage > 1) this.currentPage--;
    },
    showNotification(message, type = 'success') {
      this.notification = { show: true, message, type };
      setTimeout(() => { this.hideNotification(); }, 5000);
    },
    hideNotification() { this.notification.show = false; },
    previousPage() { if (this.currentPage > 1) this.currentPage--; },
    nextPage() { if (this.currentPage < this.totalPages) this.currentPage++; },
    formatPrice(val) { return val ? parseFloat(val).toFixed(2).replace('.', ',') : '0,00'; }
  }
}
</script>

<style scoped>
.product-management { max-width: 1200px; }

.section-header { display: flex; align-items: center; }

.search-box {
  position: relative;
  max-width: 320px;
}

.search-box i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--cf-slate-400);
  font-size: 0.875rem;
}

.search-box input { padding-left: 36px; }

/* Notification */
.notification {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: var(--cf-radius-md);
  margin-bottom: 16px;
  animation: slideDown 0.25s ease-out;
}

.notification.success { background: var(--cf-success-light); color: var(--cf-success); border: 1px solid rgba(5, 150, 105, 0.2); }
.notification.error { background: var(--cf-danger-light); color: var(--cf-danger); border: 1px solid rgba(220, 38, 38, 0.2); }

.btn-close {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  opacity: 0.7;
  margin-left: auto;
}

.btn-close:hover { opacity: 1; }

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Table Actions */
.actions { display: inline-flex; gap: 4px; }

.action-btn {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--cf-slate-500);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.action-btn:hover { background: var(--cf-slate-100); color: var(--cf-slate-700); }
.action-btn.danger:hover { background: var(--cf-danger-light); color: var(--cf-danger); }

/* Pagination */
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* Modal */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.15s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-panel {
  background: white;
  border-radius: var(--cf-radius-lg);
  width: 100%;
  max-width: 480px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  animation: scaleIn 0.2s ease;
}

.modal-sm { max-width: 400px; }

@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--cf-border);
}

.btn-close-modal {
  background: none;
  border: none;
  color: var(--cf-slate-400);
  cursor: pointer;
  font-size: 1.1rem;
  padding: 4px;
}

.btn-close-modal:hover { color: var(--cf-slate-700); }

.modal-body { padding: 24px; }
.modal-footer { padding: 16px 24px; border-top: 1px solid var(--cf-border); display: flex; justify-content: flex-end; gap: 8px; }

/* Forms */
.form-label {
  display: block;
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--cf-slate-700);
  margin-bottom: 6px;
}

.is-invalid { border-color: var(--cf-danger); }
.invalid-feedback { font-size: 0.75rem; color: var(--cf-danger); margin-top: 4px; }
</style>
