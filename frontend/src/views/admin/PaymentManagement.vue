<template>
  <div class="cf-mgmt">
    <div class="cf-mgmt-header">
      <h4 class="cf-page-title"><span class="cf-dot gold"></span>Gestão de Repasses e Pagamentos</h4>
      <div class="cf-mgmt-actions">
        <button class="cf-btn-primary" @click="showGenerateModal = true">
          <i class="fas fa-plus me-2"></i>Gerar Novo Repasse
        </button>
      </div>
    </div>

    <!-- Filtros/Abas -->
    <div class="cf-tabs mb-4">
      <button class="cf-tab" :class="{ active: tab === 'TODOS' }" @click="tab = 'TODOS'">Todos</button>
      <button class="cf-tab" :class="{ active: tab === 'PENDENTE' }" @click="tab = 'PENDENTE'">Pendentes</button>
      <button class="cf-tab" :class="{ active: tab === 'PAGO' }" @click="tab = 'PAGO'">Pagos</button>
    </div>

    <div class="cf-table-card">
      <div v-if="isLoading" class="cf-loading-row">
        <div class="cf-spinner"></div><span>Carregando repasses...</span>
      </div>
      <div v-else class="cf-table-wrap">
        <table class="cf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Destinatário</th>
              <th>Tipo</th>
              <th>Período</th>
              <th>Valor Líquido</th>
              <th>Chave PIX</th>
              <th>Status</th>
              <th class="text-center">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in filteredPagamentos" :key="p.id">
              <td class="cf-td-muted">#{{ p.id }}</td>
              <td>
                <div class="cf-td-bold">{{ p.farmacia?.nome || p.motoboy?.nome }}</div>
                <div class="cf-td-muted small">{{ p.tipo }}</div>
              </td>
              <td>
                <span class="badge" :class="p.tipo === 'FARMACIA' ? 'bg-success-subtle text-success' : 'bg-info-subtle text-info'">
                  {{ p.tipo }}
                </span>
              </td>
              <td class="cf-td-muted">{{ p.referenciaPeriodo }}</td>
              <td class="cf-td-bold text-success">R$ {{ Number(p.valorLiquido || 0).toFixed(2) }}</td>
              <td>
                <div class="cf-pix-box" v-if="p.chavePix" @click="copyPix(p.chavePix)">
                  <span class="small fw-bold text-muted">{{ p.tipoChavePix }}:</span>
                  <span class="ms-1">{{ p.chavePix }}</span>
                  <i class="fas fa-copy ms-2 opacity-50"></i>
                </div>
                <span v-else class="text-muted italic small">Não cadastrada</span>
              </td>
              <td>
                <span class="cf-status-badge" :class="statusClass(p.status)">
                  {{ p.status }}
                </span>
              </td>
              <td class="text-center">
                <button v-if="p.status === 'PENDENTE'" class="cf-btn-pago" @click="prepararPagamento(p)">
                  Confirmar Pagamento
                </button>
                <button class="cf-icon-btn ms-1" @click="viewDetails(p)">
                  <i class="fas fa-info-circle"></i>
                </button>
              </td>
            </tr>
            <tr v-if="filteredPagamentos.length === 0">
              <td colspan="8" class="cf-empty">Nenhum registro encontrado.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Gerar Repasse -->
    <div v-if="showGenerateModal" class="cf-modal-overlay" @click.self="showGenerateModal = false">
      <div class="cf-modal-box animate__animated animate__fadeInDown" style="width: 500px">
        <div class="cf-modal-header">
          <h5 class="mb-0 fw-bold">Gerar Novo Repasse</h5>
          <button class="btn-close-custom" @click="showGenerateModal = false"><i class="fas fa-times"></i></button>
        </div>
        <div class="cf-modal-body p-4">
          <div class="row g-3">
            <div class="col-12">
              <label class="cf-label-premium">Tipo de Destinatário</label>
              <select v-model="gen.tipo" class="cf-input-premium">
                <option value="FARMACIA">Farmácia</option>
                <option value="MOTOBOY">Entregador (Motoboy)</option>
              </select>
            </div>
            <div class="col-12">
              <label class="cf-label-premium">Destinatário</label>
              <select v-model="gen.id" class="cf-input-premium">
                <template v-if="gen.tipo === 'FARMACIA'">
                  <option v-for="f in farmacias" :key="f.id" :value="f.id">{{ f.nome }}</option>
                </template>
                <template v-else>
                  <option v-for="m in motoboys" :key="m.id" :value="m.id">{{ m.nome }}</option>
                </template>
              </select>
            </div>
            <div class="col-12">
              <label class="cf-label-premium">Período de Referência (AAAA-MM)</label>
              <input v-model="gen.periodo" type="month" class="cf-input-premium">
            </div>
          </div>
        </div>
        <div class="cf-modal-footer">
          <button class="btn btn-light" @click="showGenerateModal = false">Cancelar</button>
          <button class="cf-btn-primary" @click="gerarPagamento" :disabled="isGenerating">
            <span v-if="isGenerating" class="spinner-border spinner-border-sm me-2"></span>
            Gerar Repasse
          </button>
        </div>
      </div>
    </div>

    <!-- Modal Confirmar Pagamento -->
    <div v-if="pagando" class="cf-modal-overlay" @click.self="pagando = null">
      <div class="cf-modal-box animate__animated animate__fadeInUp" style="width: 450px">
        <div class="cf-modal-header bg-success-subtle">
          <h5 class="mb-0 fw-bold text-success">Confirmar Pagamento</h5>
        </div>
        <div class="cf-modal-body p-4 text-center">
          <p>Você confirma que realizou o PIX de</p>
          <h3 class="fw-bold text-success">R$ {{ Number(pagando.valorLiquido).toFixed(2) }}</h3>
          <p class="text-muted">para <strong>{{ pagando.farmacia?.nome || pagando.motoboy?.nome }}</strong>?</p>
          
          <div class="cf-pix-display mb-4">
            <span class="cf-label-premium">Chave PIX ({{ pagando.tipoChavePix }})</span>
            <div class="h5 fw-bold">{{ pagando.chavePix }}</div>
          </div>

          <label class="cf-label-premium text-start">Observações/Comprovante</label>
          <textarea v-model="obs" class="cf-input-premium" rows="2" placeholder="Opcional..."></textarea>
        </div>
        <div class="cf-modal-footer">
          <button class="btn btn-light flex-grow-1" @click="pagando = null">Cancelar</button>
          <button class="btn btn-success flex-grow-1 fw-bold" @click="confirmarPagamento" :disabled="isPaying">
            Confirmar e Baixar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import paymentService from '@/services/paymentService';
import api from '@/services/api';

export default {
  name: 'PaymentManagement',
  data() {
    return {
      pagamentos: [], farmacias: [], motoboys: [],
      isLoading: true, tab: 'TODOS',
      showGenerateModal: false, isGenerating: false,
      gen: { tipo: 'FARMACIA', id: null, periodo: new Date().toISOString().slice(0, 7) },
      pagando: null, isPaying: false, obs: ''
    };
  },
  computed: {
    filteredPagamentos() {
      if (this.tab === 'TODOS') return this.pagamentos;
      return this.pagamentos.filter(p => p.status === this.tab);
    }
  },
  async mounted() {
    await this.fetchInitialData();
    await this.fetchPagamentos();
  },
  methods: {
    async fetchInitialData() {
      try {
        const { data: f } = await api.get('/farmacias');
        this.farmacias = f;
        const { data: m } = await api.get('/motoboys');
        this.motoboys = m;
      } catch (err) { console.error(err); }
    },
    async fetchPagamentos() {
      this.isLoading = true;
      try {
        const { data } = await paymentService.listarTodos();
        this.pagamentos = data;
      } catch (err) { console.error(err); }
      finally { this.isLoading = false; }
    },
    statusClass(s) {
      return { PENDENTE: 's-pending', PAGO: 's-paid', CANCELADO: 's-cancelled' }[s] || '';
    },
    async copyPix(pix) {
      try {
        await navigator.clipboard.writeText(pix);
        alert('Chave PIX copiada!');
      } catch { alert('Erro ao copiar.'); }
    },
    async gerarPagamento() {
      if (!this.gen.id) return alert('Selecione o destinatário');
      this.isGenerating = true;
      try {
        if (this.gen.tipo === 'FARMACIA') {
          await paymentService.gerarPagamentoFarmacia(this.gen.id, this.gen.periodo);
        } else {
          await paymentService.gerarPagamentoMotoboy(this.gen.id, this.gen.periodo);
        }
        await this.fetchPagamentos();
        this.showGenerateModal = false;
        alert('Repasse gerado com sucesso!');
      } catch (err) {
        alert(err.response?.data?.erro || 'Erro ao gerar repasse. Verifique se já existe repasse para este período.');
      } finally { this.isGenerating = false; }
    },
    prepararPagamento(p) { this.pagando = p; this.obs = ''; },
    async confirmarPagamento() {
      this.isPaying = true;
      try {
        await paymentService.marcarComoPago(this.pagando.id, this.obs);
        await this.fetchPagamentos();
        this.pagando = null;
        alert('Pagamento confirmado!');
      } catch (err) { alert('Erro ao confirmar pagamento.'); }
      finally { this.isPaying = false; }
    },
    viewDetails(p) {
       alert(`Observações: ${p.observacoes || 'Nenhuma'}\nCriado em: ${new Date(p.dataCriacao).toLocaleString()}`);
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

.cf-btn-primary { background: var(--cf-green); color: #fff; border: none; padding: 0.6rem 1.2rem; border-radius: 12px; font-weight: 600; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; display: inline-flex; align-items: center; }
.cf-btn-primary:hover { background: var(--cf-green-dark); transform: translateY(-1px); }

/* Tabs */
.cf-tabs { display: flex; gap: 0.5rem; }
.cf-tab { padding: 0.4rem 1.2rem; border-radius: 20px; border: 1px solid var(--cf-border-mid); background: #fff; color: var(--cf-text-muted); font-size: 0.8rem; cursor: pointer; transition: all 0.2s; }
.cf-tab.active { background: var(--cf-gold); border-color: var(--cf-gold); color: #fff; font-weight: 600; }

.cf-table-card { background: #fff; border: 1px solid var(--cf-border); border-radius: 16px; box-shadow: var(--cf-shadow-sm); overflow: hidden; }
.cf-table-wrap { overflow-x: auto; }
.cf-table { width: 100%; border-collapse: collapse; min-width: 900px; }
.cf-table th { font-size: 0.65rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-muted); padding: 1rem; border-bottom: 1px solid var(--cf-border); background: var(--cf-ivory); }
.cf-table td { padding: 1rem; border-bottom: 1px solid var(--cf-border); font-size: 0.85rem; vertical-align: middle; }

.cf-td-bold { font-weight: 600; color: var(--cf-text-dark); }
.cf-td-muted { color: var(--cf-text-muted); font-size: 0.78rem; }

.cf-pix-box { display: inline-flex; align-items: center; padding: 0.35rem 0.7rem; background: var(--cf-ivory); border-radius: 8px; border: 1px dashed var(--cf-border-mid); cursor: pointer; transition: all 0.2s; }
.cf-pix-box:hover { border-color: var(--cf-gold); background: var(--cf-gold-light); }

.cf-btn-pago { background: var(--cf-green-xlight); color: var(--cf-green); border: 1px solid rgba(42,92,69,0.2); padding: 0.35rem 0.75rem; border-radius: 8px; font-size: 0.75rem; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.cf-btn-pago:hover { background: var(--cf-green); color: #fff; }

.cf-status-badge { font-size: 0.62rem; font-weight: 600; padding: 0.2rem 0.6rem; border-radius: 20px; text-transform: uppercase; }
.s-pending { background: #FFF8EC; color: #9A6700; }
.s-paid { background: var(--cf-green-xlight); color: var(--cf-green); }
.s-cancelled { background: #F9EDED; color: var(--cf-danger); }

/* Modal */
.cf-modal-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.7); z-index: 2000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(6px); padding: 20px; }
.cf-modal-box { background: white; border-radius: 24px; max-width: 100%; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.4); display: flex; flex-direction: column; overflow: hidden; }
.cf-modal-header { padding: 1.25rem 1.5rem; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: center; }
.cf-modal-body { flex: 1; overflow-y: auto; }
.cf-modal-footer { padding: 1.25rem 1.5rem; border-top: 1px solid #f1f5f9; display: flex; justify-content: flex-end; gap: 0.75rem; background: #fff; }

.cf-input-premium { width: 100%; padding: 0.7rem 1rem; border-radius: 10px; border: 1px solid var(--cf-border-mid); background: #fff; font-size: 0.9rem; outline: none; transition: all 0.2s; }
.cf-input-premium:focus { border-color: var(--cf-green); box-shadow: 0 0 0 4px rgba(42,92,69,0.06); }
.cf-label-premium { font-size: 0.7rem; font-weight: 600; color: var(--cf-text-muted); text-transform: uppercase; margin-bottom: 0.3rem; display: block; }

.cf-pix-display { background: var(--cf-ivory); border: 1px solid var(--cf-border); border-radius: 12px; padding: 1rem; }

.btn-close-custom { background: var(--cf-ivory); border: none; width: 32px; height: 32px; border-radius: 50%; color: var(--cf-text-muted); cursor: pointer; display: flex; align-items: center; justify-content: center; }

.cf-loading-row { display: flex; align-items: center; justify-content: center; gap: 1rem; padding: 4rem; color: var(--cf-text-muted); }
.cf-spinner { width: 24px; height: 24px; border: 2px solid var(--cf-border); border-top-color: var(--cf-green); border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
