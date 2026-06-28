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
              <td class="text-end pe-4 d-flex gap-1 justify-content-end">
                <button @click="openDetailModal(order)" class="action-btn" title="Ver detalhes">
                  <i class="fas fa-eye"></i>
                </button>
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

    <!-- Order Detail Modal -->
    <div v-if="showDetailModal" class="modal-backdrop" @click.self="closeDetailModal">
      <div class="modal-panel modal-lg">
        <div class="modal-header">
          <h5 class="mb-0 fw-semibold">Pedido #{{ detailOrder?.codigoPedido }}</h5>
          <button @click="closeDetailModal" class="btn-close-modal"><i class="fas fa-xmark"></i></button>
        </div>
        <div class="modal-body" v-if="detailOrder">
          <div class="detail-grid mb-4">
            <div>
              <span class="detail-label">Cliente</span>
              <span class="detail-value">{{ detailOrder.usuarioNome }}</span>
            </div>
            <div>
              <span class="detail-label">Data</span>
              <span class="detail-value">{{ formatDate(detailOrder.dataPedido) }}</span>
            </div>
            <div>
              <span class="detail-label">Status</span>
              <span :class="['cf-badge', getStatusBadge(detailOrder.status)]">{{ statusMap[detailOrder.status] }}</span>
            </div>
            <div>
              <span class="detail-label">Total</span>
              <span class="detail-value fw-bold">R$ {{ formatValue(detailOrder.valorTotal) }}</span>
            </div>
            <div v-if="detailOrder.formaPagamento">
              <span class="detail-label">Pagamento</span>
              <span class="detail-value">{{ detailOrder.formaPagamento }}</span>
            </div>
            <div v-if="detailOrder.farmaciaNome">
              <span class="detail-label">Farmácia</span>
              <span class="detail-value">{{ detailOrder.farmaciaNome }}</span>
            </div>
          </div>
          <h6 class="fw-semibold mb-2">Itens do Pedido</h6>
          <table class="cf-table" v-if="detailOrder.itens && detailOrder.itens.length">
            <thead>
              <tr>
                <th class="ps-3">Produto</th>
                <th class="text-center">Qtd</th>
                <th class="text-end">Preço</th>
                <th class="text-end pe-3">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in detailOrder.itens" :key="item.id">
                <td class="ps-3">{{ item.produtoNome || 'Produto' }}</td>
                <td class="text-center">{{ item.quantidade }}</td>
                <td class="text-end">R$ {{ formatValue(item.precoUnitario) }}</td>
                <td class="text-end pe-3 fw-medium">R$ {{ formatValue(item.subtotal) }}</td>
              </tr>
            </tbody>
            <tfoot>
              <tr>
                <td colspan="3" class="text-end pe-3 fw-semibold">Total</td>
                <td class="text-end pe-3 fw-bold">R$ {{ formatValue(detailOrder.valorTotal) }}</td>
              </tr>
            </tfoot>
          </table>
          <p v-else class="cf-text-muted small">Nenhum item disponível.</p>
        </div>
        <div class="modal-footer">
          <button class="cf-btn cf-btn-secondary" @click="closeDetailModal">Fechar</button>
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
          <div v-if="whatsappFeedback" :class="['alert', whatsappFeedback.success ? 'alert-success' : 'alert-info', 'd-flex', 'align-items-center', 'gap-2', 'py-2', 'px-3', 'mb-3']">
            <i :class="whatsappFeedback.success ? 'fas fa-check-circle' : 'fas fa-info-circle'"></i>
            <span>{{ whatsappFeedback.message }}</span>
          </div>
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
      newStatus: '',
      whatsappFeedback: null,
      showDetailModal: false,
      detailOrder: null
    };
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
          usuarioNome: o.usuario?.nome || 'Desconhecido',
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
    openDetailModal(order) {
      this.detailOrder = order;
      this.showDetailModal = true;
    },
    closeDetailModal() {
      this.showDetailModal = false;
      this.detailOrder = null;
    },
    openStatusModal(order) {
      this.selectedOrder = order;
      this.newStatus = order.status;
      this.whatsappFeedback = null;
      this.showStatusModal = true;
    },
    closeStatusModal() {
      this.showStatusModal = false;
      this.selectedOrder = null;
      this.whatsappFeedback = null;
    },
    async updateStatus() {
      if (!this.selectedOrder) return;
      try {
        this.whatsappFeedback = null;
        const resp = await adminService.updateOrderStatus(this.selectedOrder.id, this.newStatus);
        await this.fetchOrders();
        const dados = resp.data?.dados;
        if (dados?.whatsappMensagem) {
          this.whatsappFeedback = {
            success: dados.whatsappEnviado,
            message: dados.whatsappMensagem
          };
        } else {
          this.closeStatusModal();
        }
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

/* Detail Grid */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  background: var(--cf-slate-50);
  border-radius: 8px;
  padding: 16px;
}

.detail-label {
  display: block;
  font-size: 0.75rem;
  color: var(--cf-slate-500);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 2px;
}

.detail-value {
  font-size: 0.875rem;
  color: var(--cf-slate-900);
}

.action-btn {
  width: 32px; height: 32px; display: inline-flex;
  align-items: center; justify-content: center; border: none;
  background: transparent; color: var(--cf-slate-500);
  border-radius: 6px; cursor: pointer;
  transition: all 0.15s ease;
}
.action-btn:hover { background: var(--cf-slate-100); color: var(--cf-slate-700); }

.modal-lg { max-width: 640px !important; }

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
