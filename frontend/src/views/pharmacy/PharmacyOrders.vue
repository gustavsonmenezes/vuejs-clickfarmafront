<template>
  <div class="cf-orders">
    <div class="cf-orders-header">
      <h4 class="cf-page-title">
        <span class="cf-dot gold"></span>Gestão de Pedidos
        <span v-if="pedidosPendentes > 0" class="cf-badge-danger ms-2">{{ pedidosPendentes }} pendentes</span>
      </h4>
      <button class="cf-btn-outline-sm" @click="carregar">
        <i class="fas fa-sync-alt me-1"></i>Atualizar
      </button>
    </div>

    <!-- Abas -->
    <div class="cf-tabs">
      <button v-for="aba in abas" :key="aba.status"
        class="cf-tab" :class="{ active: abaAtiva === aba.status }"
        @click="abaAtiva = aba.status">
        <i :class="aba.icon"></i>
        {{ aba.label }}
        <span v-if="contarStatus(aba.status) > 0" class="cf-tab-count">{{ contarStatus(aba.status) }}</span>
      </button>
    </div>

    <!-- Loading -->
    <div v-if="isLoading" class="cf-loading-row">
      <div class="cf-spinner"></div><span>Carregando pedidos...</span>
    </div>

    <!-- Vazio -->
    <div v-else-if="!pedidosFiltrados.length" class="cf-empty-box">
      <i class="fas fa-inbox cf-empty-icon"></i>
      <p>Nenhum pedido nesta categoria.</p>
    </div>

    <!-- Grid -->
    <div v-else class="cf-pedidos-grid">
      <div v-for="p in pedidosFiltrados" :key="p.id" class="cf-pedido-card">
        <div class="cf-pedido-head">
          <div class="d-flex align-items-center gap-2">
            <span class="cf-pedido-codigo">#{{ p.codigoPedido || p.id }}</span>
            <span class="cf-status-badge" :class="statusClass(p.status)">{{ p.status?.replace(/_/g,' ') }}</span>
          </div>
          <span class="cf-pedido-data">{{ dt(p.dataPedido) }}</span>
        </div>

        <div class="cf-pedido-body">
          <div class="cf-pedido-info"><i class="fas fa-user me-1 cf-info-icon"></i>{{ p.nomeCliente || p.usuario?.nome || 'Cliente #' + p.usuarioId }}</div>
          <div class="cf-pedido-info mt-1" v-if="p.enderecoEntrega">
            <i class="fas fa-map-marker-alt me-1 cf-info-icon"></i>
            <span class="cf-td-muted">{{ p.enderecoEntrega }}</span>
          </div>
          <div class="cf-pedido-itens" v-if="p.itens?.length">
            <div v-for="it in p.itens" :key="it.id" class="cf-pedido-item">
              <span class="cf-item-qty">{{ it.quantidade }}×</span>
              {{ it.nomeProduto || it.produto?.nome }}
            </div>
          </div>
        </div>

        <div class="cf-pedido-foot">
          <span class="cf-pedido-valor">R$ {{ Number(p.valorTotal || 0).toFixed(2) }}</span>
          <div class="cf-pedido-acoes">
            <template v-if="p.status === 'AGUARDANDO_PAGAMENTO' || p.status === 'PAGO'">
              <button class="cf-action-btn green" @click="mudarStatus(p.id, 'EM_PREPARACAO')">
                <i class="fas fa-fire me-1"></i>Iniciar Preparo
              </button>
            </template>
            <template v-else-if="p.status === 'EM_PREPARACAO'">
              <button class="cf-action-btn blue" @click="mudarStatus(p.id, 'ENVIADO')">
                <i class="fas fa-motorcycle me-1"></i>Marcar Enviado
              </button>
            </template>
            <template v-else-if="p.status === 'ENTREGUE'">
              <span class="cf-status-ok"><i class="fas fa-check-circle me-1"></i>Concluído</span>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import pedidoService from '@/services/pedidoService';

export default {
  name: 'PharmacyOrders',
  data() {
    return {
      pedidos: [], isLoading: true, abaAtiva: 'AGUARDANDO_PAGAMENTO',
      farmaciaId: null, pollingInterval: null, lastCount: 0,
      abas: [
        { status: 'AGUARDANDO_PAGAMENTO', label: 'Aguardando', icon: 'fas fa-clock' },
        { status: 'PAGO',                 label: 'Pagos',      icon: 'fas fa-check-circle' },
        { status: 'EM_PREPARACAO',        label: 'Em Preparo', icon: 'fas fa-fire' },
        { status: 'ENVIADO',              label: 'Enviados',   icon: 'fas fa-motorcycle' },
        { status: 'EM_TRANSITO',          label: 'Em Trânsito',icon: 'fas fa-route' },
        { status: 'ENTREGUE',             label: 'Entregues',  icon: 'fas fa-check-double' }
      ]
    };
  },
  computed: {
    pedidosFiltrados() { return this.pedidos.filter(p => p.status === this.abaAtiva); },
    pedidosPendentes() { return this.pedidos.filter(p => p.status === 'AGUARDANDO_PAGAMENTO' || p.status === 'PAGO').length; }
  },
  async mounted() {
    await this.carregarFarmacia();
    await this.carregar();
    this.pollingInterval = setInterval(() => this.carregarSilencioso(), 30000);
  },
  beforeUnmount() { clearInterval(this.pollingInterval); },
  methods: {
    async carregarFarmacia() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const { data } = await api.get('/farmacias');
        const f = data.find(x => x.email === user.email);
        if (f) this.farmaciaId = f.id;
      } catch {}
    },
    async carregar() {
      if (!this.farmaciaId) return;
      this.isLoading = true;
      try {
        const { data } = await pedidoService.listarPorFarmacia(this.farmaciaId);
        this.pedidos = data; this.lastCount = this.pedidosPendentes;
      } catch (e) { console.error(e); }
      finally { this.isLoading = false; }
    },
    async carregarSilencioso() {
      if (!this.farmaciaId) return;
      try {
        const { data } = await pedidoService.listarPorFarmacia(this.farmaciaId);
        const novo = data.filter(p => p.status === 'AGUARDANDO_PAGAMENTO' || p.status === 'PAGO').length;
        if (novo > this.lastCount) { this.tocarSom(); this.notificar(novo - this.lastCount); }
        this.pedidos = data; this.lastCount = novo;
      } catch {}
    },
    tocarSom() {
      try {
        const ctx = new (window.AudioContext || window.webkitAudioContext)();
        const o = ctx.createOscillator(); const g = ctx.createGain();
        o.connect(g); g.connect(ctx.destination);
        o.frequency.value = 880; g.gain.setValueAtTime(0.25, ctx.currentTime);
        g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.4);
        o.start(ctx.currentTime); o.stop(ctx.currentTime + 0.4);
      } catch {}
    },
    notificar(n) {
      if ('Notification' in window && Notification.permission === 'granted')
        new Notification('ClickFarma', { body: `${n} novo(s) pedido(s)!` });
    },
    async mudarStatus(id, status) {
      try { await pedidoService.atualizarStatus(id, status); await this.carregar(); }
      catch { alert('Erro ao atualizar status.'); }
    },
    contarStatus(s) { return this.pedidos.filter(p => p.status === s).length; },
    statusClass(s) {
      return { AGUARDANDO_PAGAMENTO:'s-pending', PAGO:'s-paid', EM_PREPARACAO:'s-prep', ENVIADO:'s-sent', EM_TRANSITO:'s-transit', ENTREGUE:'s-done', CANCELADO:'s-cancelled' }[s] || '';
    },
    dt(d) { if (!d) return '—'; return new Date(d).toLocaleString('pt-BR',{day:'2-digit',month:'2-digit',hour:'2-digit',minute:'2-digit'}); }
  }
};
</script>

<style scoped>
.cf-orders-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.25rem; flex-wrap: wrap; gap: 0.75rem; }
.cf-page-title { margin: 0; font-size: 1rem; font-weight: 600; color: var(--cf-text-dark); display: flex; align-items: center; gap: 0.5rem; }
.cf-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.cf-dot.gold { background: var(--cf-gold); }
.cf-badge-danger { font-size: 0.65rem; font-weight: 700; background: #F9EDED; color: var(--cf-danger); padding: 0.22rem 0.75rem; border-radius: 20px; border: 1px solid rgba(139,58,58,0.12); }

.cf-btn-outline-sm { padding: 0.45rem 0.9rem; background: none; border: 1px solid var(--cf-border-mid); border-radius: var(--cf-r-md); font-family: var(--cf-sans); font-size: 0.78rem; color: var(--cf-text-muted); cursor: pointer; transition: all 0.18s; }
.cf-btn-outline-sm:hover { border-color: var(--cf-green); color: var(--cf-green); }

/* Abas */
.cf-tabs { display: flex; gap: 0.4rem; flex-wrap: wrap; margin-bottom: 1.25rem; }
.cf-tab {
  display: flex; align-items: center; gap: 0.4rem;
  padding: 0.45rem 0.95rem; border-radius: 20px;
  border: 1px solid var(--cf-border-mid); background: var(--cf-white);
  color: var(--cf-text-muted); font-family: var(--cf-sans); font-size: 0.78rem; font-weight: 400;
  cursor: pointer; transition: all 0.18s; white-space: nowrap;
}
.cf-tab:hover { border-color: var(--cf-gold); color: var(--cf-gold); }
.cf-tab.active { background: var(--cf-gold); border-color: var(--cf-gold); color: #fff; font-weight: 500; }
.cf-tab-count {
  font-size: 0.7rem; font-weight: 700; padding: 0 6px; border-radius: 10px;
  background: rgba(255,255,255,0.25); color: inherit;
}
.cf-tab:not(.active) .cf-tab-count { background: var(--cf-cream); color: var(--cf-text-mid); }

/* Grid de pedidos */
.cf-pedidos-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1rem; }

.cf-pedido-card { background: var(--cf-white); border: 1px solid var(--cf-border); border-radius: var(--cf-r-lg); box-shadow: var(--cf-shadow-xs); overflow: hidden; transition: all 0.2s var(--cf-ease); }
.cf-pedido-card:hover { transform: translateY(-2px); box-shadow: var(--cf-shadow-md); border-color: rgba(184,149,80,0.2); }

.cf-pedido-head { padding: 0.85rem 1rem; background: var(--cf-ivory); border-bottom: 1px solid var(--cf-border); display: flex; justify-content: space-between; align-items: center; }
.cf-pedido-codigo { font-size: 0.85rem; font-weight: 600; color: var(--cf-text-dark); }
.cf-pedido-data { font-size: 0.7rem; color: var(--cf-text-faint); }

.cf-pedido-body { padding: 0.9rem 1rem; }
.cf-pedido-info { font-size: 0.83rem; color: var(--cf-text-mid); }
.cf-info-icon { color: var(--cf-text-faint); width: 14px; }
.cf-td-muted { color: var(--cf-text-muted); font-size: 0.78rem; }
.cf-pedido-itens { margin-top: 0.6rem; padding-top: 0.6rem; border-top: 1px solid var(--cf-border); }
.cf-pedido-item { font-size: 0.78rem; color: var(--cf-text-muted); padding: 1px 0; }
.cf-item-qty { font-weight: 700; color: var(--cf-green); margin-right: 3px; }

.cf-pedido-foot { padding: 0.75rem 1rem; border-top: 1px solid var(--cf-border); display: flex; justify-content: space-between; align-items: center; background: var(--cf-ivory); }
.cf-pedido-valor { font-size: 1rem; font-weight: 700; color: var(--cf-green); font-family: var(--cf-sans); }

.cf-action-btn { padding: 0.38rem 0.85rem; border: none; border-radius: var(--cf-r-sm); font-family: var(--cf-sans); font-size: 0.74rem; font-weight: 500; cursor: pointer; transition: all 0.18s; }
.cf-action-btn.green { background: var(--cf-green-xlight); color: var(--cf-green); border: 1px solid rgba(42,92,69,0.2); }
.cf-action-btn.green:hover { background: var(--cf-green); color: #fff; }
.cf-action-btn.blue { background: #EAF1FB; color: #2a6099; border: 1px solid rgba(42,96,153,0.2); }
.cf-action-btn.blue:hover { background: #2a6099; color: #fff; }
.cf-status-ok { font-size: 0.78rem; color: var(--cf-green); font-weight: 500; }

/* Status badges */
.cf-status-badge { font-size: 0.62rem; font-weight: 600; letter-spacing: 0.06em; padding: 0.2rem 0.65rem; border-radius: 20px; text-transform: uppercase; }
.s-pending   { background: #FFF8EC; color: #9A6700; }
.s-paid      { background: var(--cf-green-xlight); color: var(--cf-green); }
.s-prep      { background: #FFF0E6; color: #9A4500; }
.s-sent      { background: #EAF1FB; color: #2a6099; }
.s-transit   { background: #F0EAFB; color: #5a2a99; }
.s-done      { background: var(--cf-green-xlight); color: var(--cf-green-dark); }
.s-cancelled { background: #F9EDED; color: var(--cf-danger); }

/* Estados */
.cf-loading-row { display: flex; align-items: center; justify-content: center; gap: 0.75rem; padding: 4rem; color: var(--cf-text-muted); font-size: 0.85rem; }
.cf-spinner { width: 22px; height: 22px; border: 2px solid var(--cf-border); border-top-color: var(--cf-green); border-radius: 50%; animation: spin 0.65s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.cf-empty-box { text-align: center; padding: 4rem 2rem; }
.cf-empty-icon { font-size: 2.5rem; color: var(--cf-text-faint); opacity: 0.3; display: block; margin-bottom: 0.75rem; }
.cf-empty-box p { color: var(--cf-text-muted); font-size: 0.88rem; }
</style>
