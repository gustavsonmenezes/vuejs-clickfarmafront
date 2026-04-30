<template>
  <div class="cf-mgmt">
    <div class="cf-mgmt-header">
      <h4 class="cf-page-title"><span class="cf-dot gold"></span>Gestão Global de Pedidos</h4>
      <div class="cf-mgmt-actions">
        <div class="cf-search-wrap">
          <i class="fas fa-search cf-search-icon"></i>
          <input v-model="search" type="text" class="cf-search" placeholder="ID ou Cliente...">
        </div>
        <button class="cf-btn-outline-sm" @click="fetchOrders">
          <i class="fas fa-sync-alt me-1"></i>Atualizar
        </button>
      </div>
    </div>

    <div class="cf-table-card">
      <div v-if="isLoading" class="cf-loading-row">
        <div class="cf-spinner"></div><span>Carregando pedidos...</span>
      </div>
      <div v-else class="cf-table-wrap">
        <table class="cf-table">
          <thead>
            <tr>
              <th>ID / Código</th>
              <th>Cliente</th>
              <th>Data</th>
              <th>Total</th>
              <th>Status</th>
              <th class="text-center">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredOrders" :key="order.id">
              <td class="cf-mono">#{{ order.codigoPedido || order.id }}</td>
              <td>
                <div class="cf-td-bold">{{ order.nomeCliente || order.usuario?.nome || 'Cliente #' + order.usuarioId }}</div>
              </td>
              <td class="cf-td-muted">{{ formatDate(order.dataPedido) }}</td>
              <td class="cf-td-bold text-success">R$ {{ Number(order.valorTotal || 0).toFixed(2) }}</td>
              <td>
                <span class="cf-status-badge" :class="statusClass(order.status)">
                  {{ order.status?.replace(/_/g, ' ') }}
                </span>
              </td>
              <td class="text-center">
                <button class="cf-icon-btn" @click="viewDetails(order)" title="Ver Detalhes">
                  <i class="fas fa-eye"></i>
                </button>
              </td>
            </tr>
            <tr v-if="filteredOrders.length === 0">
              <td colspan="6" class="cf-empty">Nenhum pedido encontrado.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal de Detalhes -->
    <div v-if="selectedOrder" class="cf-modal-overlay" @click.self="selectedOrder = null">
      <div class="cf-modal-box animate__animated animate__fadeInUp">
        <div class="cf-modal-header">
          <div class="d-flex align-items-center gap-3">
            <div class="modal-icon-wrap bg-gold-light">
              <i class="fas fa-shopping-bag text-warning"></i>
            </div>
            <div>
              <h5 class="mb-0 fw-bold">Detalhes do Pedido</h5>
              <p class="mb-0 text-muted small">#{{ selectedOrder.codigoPedido || selectedOrder.id }}</p>
            </div>
          </div>
          <button class="btn-close-custom" @click="selectedOrder = null"><i class="fas fa-times"></i></button>
        </div>

        <div class="cf-modal-body p-4">
          <div class="row g-4">
            <div class="col-md-6">
              <label class="cf-label-premium">Informações do Cliente</label>
              <div class="cf-detail-box">
                <p class="mb-1"><strong>Nome:</strong> {{ selectedOrder.nomeCliente || selectedOrder.usuario?.nome }}</p>
                <p class="mb-0"><strong>Email:</strong> {{ selectedOrder.usuario?.email || '—' }}</p>
              </div>
            </div>
            <div class="col-md-6">
              <label class="cf-label-premium">Status Atual</label>
              <div class="d-flex align-items-center gap-2 mb-3">
                <span class="cf-status-badge" :class="statusClass(selectedOrder.status)">
                  {{ selectedOrder.status?.replace(/_/g, ' ') }}
                </span>
              </div>
              <div class="input-group input-group-sm">
                <select v-model="newStatus" class="form-select form-select-sm">
                  <option v-for="s in statusOptions" :key="s" :value="s">{{ s.replace(/_/g, ' ') }}</option>
                </select>
                <button class="btn btn-primary btn-sm" @click="updateStatus" :disabled="isUpdating">
                  Atualizar
                </button>
              </div>
            </div>

            <div class="col-12">
              <label class="cf-label-premium">Itens do Pedido</label>
              <div class="cf-table-card border">
                <table class="cf-table">
                  <thead class="bg-light">
                    <tr><th>Produto</th><th>Qtd</th><th>Unitário</th><th>Subtotal</th></tr>
                  </thead>
                  <tbody>
                    <tr v-for="it in selectedOrder.itens" :key="it.id">
                      <td>{{ it.nomeProduto || it.produto?.nome }}</td>
                      <td>{{ it.quantidade }}</td>
                      <td>R$ {{ Number(it.precoUnitario || 0).toFixed(2) }}</td>
                      <td class="fw-bold">R$ {{ Number((it.precoUnitario * it.quantidade) || 0).toFixed(2) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="col-12 text-end">
              <div class="h5 mb-0">Total: <span class="text-success fw-bold">R$ {{ Number(selectedOrder.valorTotal || 0).toFixed(2) }}</span></div>
            </div>
          </div>
        </div>

        <div class="cf-modal-footer">
          <button class="btn btn-light fw-bold px-4" @click="selectedOrder = null">Fechar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import pedidoService from '@/services/pedidoService';

export default {
  name: 'OrderManagement',
  data() {
    return {
      orders: [],
      isLoading: true,
      search: '',
      selectedOrder: null,
      newStatus: '',
      isUpdating: false,
      statusOptions: [
        'AGUARDANDO_PAGAMENTO', 'PAGO', 'EM_PREPARACAO', 
        'ENVIADO', 'EM_TRANSITO', 'ENTREGUE', 'CANCELADO'
      ]
    };
  },
  computed: {
    filteredOrders() {
      if (!this.search) return this.orders;
      const t = this.search.toLowerCase();
      return this.orders.filter(o => 
        o.id.toString().includes(t) || 
        o.codigoPedido?.toLowerCase().includes(t) ||
        (o.nomeCliente || o.usuario?.nome)?.toLowerCase().includes(t)
      );
    }
  },
  mounted() {
    this.fetchOrders();
  },
  methods: {
    async fetchOrders() {
      this.isLoading = true;
      try {
        const { data } = await pedidoService.listarTodos();
        this.orders = data;
      } catch (err) {
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    formatDate(d) {
      if (!d) return '—';
      return new Date(d).toLocaleString('pt-BR');
    },
    statusClass(s) {
      return { 
        AGUARDANDO_PAGAMENTO:'s-pending', PAGO:'s-paid', 
        EM_PREPARACAO:'s-prep', ENVIADO:'s-sent', 
        EM_TRANSITO:'s-transit', ENTREGUE:'s-done', 
        CANCELADO:'s-cancelled' 
      }[s] || '';
    },
    viewDetails(order) {
      this.selectedOrder = order;
      this.newStatus = order.status;
    },
    async updateStatus() {
      if (!this.selectedOrder) return;
      this.isUpdating = true;
      try {
        await pedidoService.atualizarStatus(this.selectedOrder.id, this.newStatus);
        this.selectedOrder.status = this.newStatus;
        await this.fetchOrders();
        alert('Status atualizado com sucesso!');
      } catch (err) {
        alert('Erro ao atualizar status.');
      } finally {
        this.isUpdating = false;
      }
    }
  }
};
</script>

<style scoped>
.cf-mgmt { padding-bottom: 2rem; }
.cf-mgmt-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
.cf-page-title { margin: 0; font-size: 1.1rem; font-weight: 700; color: var(--cf-text-dark); display: flex; align-items: center; gap: 0.6rem; }
.cf-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.cf-dot.gold { background: var(--cf-gold); }
.cf-mgmt-actions { display: flex; gap: 0.75rem; align-items: center; }

.cf-search-wrap { position: relative; }
.cf-search-icon { position: absolute; left: 0.85rem; top: 50%; transform: translateY(-50%); color: var(--cf-text-faint); font-size: 0.85rem; }
.cf-search { padding: 0.55rem 1rem 0.55rem 2.4rem; border: 1px solid var(--cf-border-mid); border-radius: 12px; font-family: var(--cf-sans); font-size: 0.88rem; color: var(--cf-text-dark); background: var(--cf-white); outline: none; transition: all 0.2s; width: 240px; }
.cf-search:focus { border-color: var(--cf-green); box-shadow: 0 0 0 4px rgba(42,92,69,0.06); }

.cf-btn-outline-sm { padding: 0.45rem 0.9rem; background: none; border: 1px solid var(--cf-border-mid); border-radius: 10px; font-family: var(--cf-sans); font-size: 0.78rem; color: var(--cf-text-muted); cursor: pointer; transition: all 0.18s; }
.cf-btn-outline-sm:hover { border-color: var(--cf-green); color: var(--cf-green); }

.cf-table-card { background: var(--cf-white); border: 1px solid var(--cf-border); border-radius: 16px; box-shadow: var(--cf-shadow-sm); overflow: hidden; }
.cf-table-wrap { overflow-x: auto; scrollbar-width: thin; }
.cf-table { width: 100%; border-collapse: collapse; min-width: 900px; }
.cf-table th { font-size: 0.68rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-muted); font-weight: 600; padding: 1rem; border-bottom: 1px solid var(--cf-border); background: var(--cf-ivory); white-space: nowrap; }
.cf-table td { padding: 1rem; border-bottom: 1px solid var(--cf-border); font-size: 0.88rem; vertical-align: middle; }
.cf-table tbody tr:last-child td { border-bottom: none; }
.cf-table tbody tr:hover td { background: rgba(42,92,69,0.02); }

.cf-td-bold { font-weight: 600; color: var(--cf-text-dark); }
.cf-td-muted { color: var(--cf-text-muted); font-size: 0.8rem; }
.cf-mono { font-family: 'DM Mono', monospace !important; font-size: 0.8rem; color: var(--cf-text-mid); }

.cf-icon-btn { background: var(--cf-white); border: 1px solid var(--cf-border-mid); border-radius: 8px; width: 34px; height: 34px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer; color: var(--cf-text-muted); transition: all 0.2s; }
.cf-icon-btn:hover { border-color: var(--cf-green); color: var(--cf-green); background: var(--cf-green-xlight); }

/* Status Badges */
.cf-status-badge { font-size: 0.65rem; font-weight: 600; letter-spacing: 0.08em; padding: 0.25rem 0.8rem; border-radius: 20px; text-transform: uppercase; }
.s-pending   { background: #FFF8EC; color: #9A6700; }
.s-paid      { background: var(--cf-green-xlight); color: var(--cf-green); }
.s-prep      { background: #FFF0E6; color: #9A4500; }
.s-sent      { background: #EAF1FB; color: #2a6099; }
.s-transit   { background: #F0EAFB; color: #5a2a99; }
.s-done      { background: var(--cf-green-xlight); color: var(--cf-green-dark); }
.s-cancelled { background: #F9EDED; color: var(--cf-danger); }

/* Modal */
.cf-modal-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.7); z-index: 2000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(6px); padding: 20px; }
.cf-modal-box { background: white; border-radius: 24px; width: 800px; max-width: 100%; max-height: calc(100vh - 60px); box-shadow: 0 25px 50px -12px rgba(0,0,0,0.4); display: flex; flex-direction: column; overflow: hidden; }
.cf-modal-header { padding: 1.25rem 1.5rem; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: center; flex-shrink: 0; }
.modal-icon-wrap { width: 44px; height: 44px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; }
.cf-modal-body { flex: 1; overflow-y: auto; background: #fafafa; }
.cf-modal-footer { padding: 1.25rem 1.5rem; border-top: 1px solid #f1f5f9; display: flex; justify-content: flex-end; gap: 1rem; background: #fff; flex-shrink: 0; }

.cf-detail-box { background: #fff; border: 1px solid var(--cf-border); border-radius: 12px; padding: 1rem; }
.cf-label-premium { font-size: 0.72rem; font-weight: 600; color: var(--cf-text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.45rem; display: block; }

.btn-close-custom { background: var(--cf-ivory); border: none; width: 34px; height: 34px; border-radius: 50%; color: var(--cf-text-muted); cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
.btn-close-custom:hover { background: var(--cf-cream); color: var(--cf-text-dark); transform: rotate(90deg); }

.cf-loading-row { display: flex; align-items: center; justify-content: center; gap: 1rem; padding: 5rem 2rem; color: var(--cf-text-muted); }
.cf-spinner { width: 28px; height: 28px; border: 3px solid var(--cf-border); border-top-color: var(--cf-green); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .cf-modal-overlay { padding: 0; }
  .cf-modal-box { border-radius: 0; height: 100vh; max-height: 100vh; }
}
</style>