<template>
  <div class="order-management">
    <!-- Header -->
    <div class="section-header mb-4">
      <div class="search-box">
        <i class="fas fa-search"></i>
        <input v-model="search" class="cf-input ps-4" placeholder="Buscar por ID ou cliente..." />
      </div>
      <div class="d-flex gap-2 ms-3">
        <select v-model="statusFilter" class="cf-select" style="width: auto;">
          <option value="">Todos os Status</option>
          <option value="AGUARDANDO_PAGAMENTO">Aguardando Pagamento</option>
          <option value="PAGO">Pago</option>
          <option value="EM_PREPARACAO">Em Preparação</option>
          <option value="ENVIADO">Enviado</option>
          <option value="EM_TRANSITO">Em Trânsito</option>
          <option value="ENTREGUE">Entregue</option>
          <option value="CANCELADO">Cancelado</option>
        </select>
      </div>
    </div>

    <!-- Orders Table -->
    <div class="cf-card">
      <div class="cf-card-body p-0">
        <table class="cf-table" v-if="filteredOrders.length">
          <thead>
            <tr>
              <th class="ps-4">Pedido</th>
              <th>Cliente</th>
              <th>Data</th>
              <th class="text-center">Status</th>
              <th class="text-end">Total</th>
              <th class="text-end pe-4">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredOrders" :key="order.id">
              <td class="ps-4">
                <div class="fw-semibold text-dark">#{{ order.codigoPedido }}</div>
              </td>
              <td>
                <div class="fw-medium">{{ order.usuarioNome }}</div>
              </td>
              <td class="cf-text-muted">{{ formatDate(order.dataPedido) }}</td>
              <td class="text-center">
                <span :class="['cf-badge', getStatusBadge(order.status)]">
                  {{ statusMap[order.status] }}
                </span>
              </td>
              <td class="text-end fw-semibold">R$ {{ formatValue(order.valorTotal) }}</td>
              <td class="text-end pe-4">
                <button @click="openStatusModal(order)" class="cf-btn cf-btn-secondary cf-btn-sm">
                  <i class="fas fa-arrow-right-arrow-left me-1"></i>
                  Status
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="cf-empty-state">
          <i class="fas fa-bag-shopping"></i>
          <p>Nenhum pedido encontrado.</p>
        </div>
      </div>
    </div>

    <!-- Status Update Modal -->
    <div v-if="showStatusModal" class="modal-backdrop" @click.self="closeStatusModal">
      <div class="modal-panel">
        <div class="modal-header">
          <h5 class="mb-0 fw-semibold">Atualizar Status</h5>
          <button @click="closeStatusModal" class="btn-close-modal"><i class="fas fa-xmark"></i></button>
        </div>
        <div class="modal-body">
          <p class="mb-3">
            Pedido <strong>#{{ selectedOrder?.codigoPedido }}</strong> - 
            <span class="cf-text-muted">{{ selectedOrder?.usuarioNome }}</span>
          </p>
          <label class="form-label">Novo Status</label>
          <select v-model="newStatus" class="cf-select mb-3">
            <option value="AGUARDANDO_PAGAMENTO">Aguardando Pagamento</option>
            <option value="PAGO">Pago</option>
            <option value="EM_PREPARACAO">Em Preparação</option>
            <option value="ENVIADO">Enviado</option>
            <option value="EM_TRANSITO">Em Trânsito</option>
            <option value="ENTREGUE">Entregue</option>
            <option value="CANCELADO">Cancelado</option>
          </select>
          <div class="d-flex gap-2 justify-content-end">
            <button class="cf-btn cf-btn-secondary" @click="closeStatusModal">Cancelar</button>
            <button class="cf-btn cf-btn-primary" @click="updateStatus">Confirmar</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { adminService } from '@/services/adminService';

export default {
  name: 'OrderManagement',
  data() {
    return {
      orders: [],
      search: '',
      statusFilter: '',
      statusMap: {
        'AGUARDANDO_PAGAMENTO': 'Aguardando',
        'PAGO': 'Pago',
        'EM_PREPARACAO': 'Preparação',
        'ENVIADO': 'Enviado',
        'EM_TRANSITO': 'Trânsito',
        'ENTREGUE': 'Entregue',
        'CANCELADO': 'Cancelado'
      },
      showStatusModal: false,
      selectedOrder: null,
      newStatus: 'PAGO'
    }
  },
  computed: {
    filteredOrders() {
      let filtered = this.orders.filter(order => {
        const matchesSearch = order.codigoPedido.toLowerCase().includes(this.search.toLowerCase()) ||
          (order.usuarioNome && order.usuarioNome.toLowerCase().includes(this.search.toLowerCase()));
        const matchesStatus = !this.statusFilter || order.status === this.statusFilter;
        return matchesSearch && matchesStatus;
      });
      return filtered.sort((a, b) => new Date(b.dataPedido) - new Date(a.dataPedido));
    }
  },
  mounted() { this.fetchOrders(); },
  methods: {
    async fetchOrders() {
      try {
        const res = await adminService.getOrders();
        this.orders = (res.data || []).map(o => ({
          ...o,
          usuarioNome: o.usuario?.nome || 'Desconhecido'
        }));
      } catch (e) {
        console.error('Erro ao buscar pedidos:', e);
      }
    },
    getStatusBadge(status) {
      const map = {
        'AGUARDANDO_PAGAMENTO': 'cf-badge-warning',
        'PAGO': 'cf-badge-info',
        'EM_PREPARACAO': 'cf-badge-primary',
        'ENVIADO': 'cf-badge-success',
        'ENTREGUE': 'cf-badge-success',
        'CANCELADO': 'cf-badge-danger'
      };
      return map[status] || 'cf-badge-primary';
    },
    openStatusModal(order) {
      this.selectedOrder = order;
      this.newStatus = order.status;
      this.showStatusModal = true;
    },
    closeStatusModal() {
      this.showStatusModal = false;
      this.selectedOrder = null;
    },
    async updateStatus() {
      if (!this.selectedOrder) return;
      try {
        await adminService.updateOrderStatus(this.selectedOrder.id, this.newStatus);
        await this.fetchOrders();
        this.closeStatusModal();
      } catch (e) {
        alert('Erro ao atualizar: ' + (e.response?.data?.message || e.message));
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return '-';
      return new Date(dateStr).toLocaleDateString('pt-BR');
    },
    formatValue(val) {
      return val ? parseFloat(val).toFixed(2).replace('.', ',') : '0,00';
    }
  }
}
</script>

<style scoped>
.order-management { max-width: 1200px; }

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

.form-label { display: block; font-size: 0.8125rem; font-weight: 500; color: var(--cf-slate-700); margin-bottom: 6px; }
</style>
