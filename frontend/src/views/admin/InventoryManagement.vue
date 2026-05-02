<template>
  <div class="inventory-management">
    <!-- Low Stock Alert Banner -->
    <div v-if="lowStockProducts.length > 0" class="alert-banner mb-4">
      <div class="alert-icon">
        <i class="fas fa-triangle-exclamation"></i>
      </div>
      <div class="alert-content">
        <h6 class="mb-1 fw-semibold">{{ lowStockProducts.length }} {{ lowStockProducts.length === 1 ? 'produto com estoque baixo' : 'produtos com estoque baixo' }}</h6>
        <p class="mb-0 small">{{ lowStockProducts.map(p => p.nome).join(', ') }}</p>
      </div>
    </div>

    <!-- Header -->
    <div class="section-header mb-4">
      <div class="search-box">
        <i class="fas fa-search"></i>
        <input v-model="search" class="cf-input ps-4" placeholder="Buscar no estoque..." />
      </div>
      <select v-model="stockFilter" class="cf-select ms-3" style="width: auto;">
        <option value="">Todo Estoque</option>
        <option value="low">Estoque Baixo (&lt; 10)</option>
        <option value="out">Sem Estoque</option>
      </select>
    </div>

    <!-- Inventory Table -->
    <div class="cf-card">
      <div class="cf-card-body p-0">
        <table class="cf-table" v-if="filteredProducts.length">
          <thead>
            <tr>
              <th class="ps-4">Produto</th>
              <th class="text-center">Estoque Atual</th>
              <th class="text-center">Status</th>
              <th class="text-center">Atualizar</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in filteredProducts" :key="product.id" :class="{ 'row-warning': product.estoque < 10 }">
              <td class="ps-4">
                <div class="fw-semibold text-dark">{{ product.nome }}</div>
                <div class="cf-text-muted small">{{ product.descricao || '-' }}</div>
              </td>
              <td class="text-center">
                <div class="stock-value">{{ product.estoque || 0 }}</div>
              </td>
              <td class="text-center">
                <span v-if="product.estoque === 0" class="cf-badge cf-badge-danger">Sem estoque</span>
                <span v-else-if="product.estoque < 10" class="cf-badge cf-badge-warning">Baixo</span>
                <span v-else class="cf-badge cf-badge-success">Normal</span>
              </td>
              <td class="text-center">
                <div class="stock-update">
                  <input v-model.number="stockUpdates[product.id]" type="number" class="cf-input stock-input" placeholder="Novo valor" min="0" />
                  <button @click="updateStock(product)" class="cf-btn cf-btn-primary cf-btn-sm" :disabled="!stockUpdates[product.id] && stockUpdates[product.id] !== 0">
                    Salvar
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else-if="loading" class="cf-empty-state">
          <i class="fas fa-spinner fa-spin"></i>
          <p>Carregando...</p>
        </div>
        <div v-else class="cf-empty-state">
          <i class="fas fa-warehouse"></i>
          <p>Nenhum produto no estoque.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { adminService } from '@/services/adminService';

export default {
  name: 'InventoryManagement',
  data() {
    return {
      products: [],
      search: '',
      stockFilter: '',
      stockUpdates: {},
      lowStockThreshold: 10,
      loading: false
    }
  },
  computed: {
    lowStockProducts() {
      return this.products.filter(p => p.estoque < this.lowStockThreshold)
    },
    filteredProducts() {
      let filtered = this.products;
      
      if (this.search) {
        const q = this.search.toLowerCase();
        filtered = filtered.filter(p => 
          p.nome.toLowerCase().includes(q) || 
          (p.descricao && p.descricao.toLowerCase().includes(q))
        );
      }
      
      if (this.stockFilter === 'low') {
        filtered = filtered.filter(p => p.estoque > 0 && p.estoque < this.lowStockThreshold);
      } else if (this.stockFilter === 'out') {
        filtered = filtered.filter(p => p.estoque === 0);
      }
      
      return filtered;
    }
  },
  mounted() { this.fetchProducts(); },
  methods: {
    async fetchProducts() {
      this.loading = true;
      try {
        const res = await adminService.getProducts();
        this.products = res.data || [];
      } catch (e) {
        console.error('Erro ao carregar estoque:', e);
      } finally { this.loading = false; }
    },
    async updateStock(product) {
      const newStock = this.stockUpdates[product.id];
      if (newStock === undefined || newStock === '' || newStock < 0) {
        return;
      }

      try {
        await adminService.updateProduct(product.id, { ...product, estoque: parseInt(newStock) });
        this.stockUpdates[product.id] = undefined;
        await this.fetchProducts();
      } catch (e) {
        alert('Erro ao atualizar: ' + (e.response?.data?.message || e.message));
      }
    }
  }
}
</script>

<style scoped>
.inventory-management { max-width: 1200px; }

.section-header { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; }

.search-box {
  position: relative;
  max-width: 280px;
}

.search-box i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--cf-slate-400);
}

/* Alert Banner */
.alert-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: var(--cf-warning-light);
  border: 1px solid rgba(217, 119, 6, 0.2);
  border-radius: var(--cf-radius-md);
}

.alert-icon {
  width: 36px;
  height: 36px;
  background: rgba(217, 119, 6, 0.15);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cf-warning);
}

.alert-content h6 { color: var(--cf-warning); }
.alert-content p { color: var(--cf-slate-600); }

/* Table */
.row-warning { background: rgba(254, 243, 199, 0.4); }
.row-warning:hover { background: rgba(254, 243, 199, 0.7); }

.stock-value {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--cf-slate-900);
}

.stock-update {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.stock-input {
  width: 100px;
  padding: 6px 10px;
  font-size: 0.8125rem;
}
</style>
