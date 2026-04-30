<template>
  <div class="cf-pharm-dash">
    <!-- KPI Cards -->
    <div class="row g-3 mb-4">
      <div class="col-xl-3 col-md-6">
        <div class="cf-kpi accent-green">
          <div class="cf-kpi-icon"><i class="fas fa-dollar-sign"></i></div>
          <div class="cf-kpi-body">
            <span class="cf-kpi-lbl">Faturamento Bruto</span>
            <div class="cf-kpi-val">R$ {{ n(stats.faturamentoBruto) }}</div>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-md-6">
        <div class="cf-kpi accent-gold">
          <div class="cf-kpi-icon"><i class="fas fa-hand-holding-usd"></i></div>
          <div class="cf-kpi-body">
            <span class="cf-kpi-lbl">Faturamento Líquido</span>
            <div class="cf-kpi-val">R$ {{ n(stats.faturamentoLiquido) }}</div>
            <span class="cf-kpi-sub">Taxa: R$ {{ n(stats.comissaoPaga) }}</span>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-sm-6">
        <div class="cf-kpi accent-neutral">
          <div class="cf-kpi-icon"><i class="fas fa-box-open"></i></div>
          <div class="cf-kpi-body">
            <span class="cf-kpi-lbl">Total Pedidos</span>
            <div class="cf-kpi-val">{{ stats.totalPedidos }}</div>
          </div>
        </div>
      </div>
      <div class="col-xl-3 col-sm-6">
        <div class="cf-kpi" :class="stats.pedidosPendentes > 0 ? 'accent-danger' : 'accent-neutral'">
          <div class="cf-kpi-icon"><i class="fas fa-clock"></i></div>
          <div class="cf-kpi-body">
            <span class="cf-kpi-lbl">Pendentes</span>
            <div class="cf-kpi-val" :class="stats.pedidosPendentes > 0 ? 'cf-danger' : ''">
              {{ stats.pedidosPendentes }}
            </div>
          </div>
          <div v-if="stats.pedidosPendentes > 0" class="cf-kpi-pulse"></div>
        </div>
      </div>
    </div>

    <div class="row g-4">
      <!-- Gráfico semanal -->
      <div class="col-xl-7">
        <div class="cf-card">
          <div class="cf-card-head">
            <h5 class="cf-card-title"><span class="cf-dot green"></span>Vendas da Semana</h5>
            <span class="cf-eyebrow">semana atual</span>
          </div>
          <div class="cf-card-body">
            <canvas id="lineChart" height="130"></canvas>
          </div>
        </div>
      </div>

      <!-- Central de pedidos -->
      <div class="col-xl-5">
        <div class="cf-card h-100">
          <div class="cf-card-head">
            <h5 class="cf-card-title"><span class="cf-dot gold"></span>Central de Pedidos</h5>
            <span v-if="stats.pedidosPendentes > 0" class="cf-badge-danger">
              {{ stats.pedidosPendentes }} pendentes
            </span>
          </div>
          <div class="cf-card-body d-flex flex-column justify-content-center text-center py-4">
            <template v-if="stats.pedidosPendentes > 0">
              <div class="cf-bell-anim mb-3"><i class="fas fa-bell"></i></div>
              <h4 class="cf-alert-heading">{{ stats.pedidosPendentes }} pedido(s) aguardando</h4>
              <p class="cf-alert-sub">Acesse os pedidos para confirmar e preparar os envios.</p>
              <router-link to="/pharmacy/orders" class="cf-btn-primary mt-2">
                <i class="fas fa-arrow-right me-2"></i>Ver Pedidos
              </router-link>
            </template>
            <template v-else>
              <div class="cf-all-clear">
                <i class="fas fa-check-circle"></i>
                <h5 class="mt-3">Tudo em dia!</h5>
                <p>Nenhum pedido pendente no momento.</p>
                <router-link to="/pharmacy/orders" class="cf-btn-outline mt-2">Ver histórico</router-link>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'PharmacyDashboard',
  data() {
    return {
      stats: { faturamentoBruto:0, faturamentoLiquido:0, comissaoPaga:0, totalPedidos:0, pedidosPendentes:0, totalProdutos:0 },
      lineChart: null
    };
  },
  async mounted() {
    await this.carregar();
  },
  methods: {
    n(v) { return Number(v || 0).toFixed(2); },
    async carregar() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const { data: farmacias } = await api.get('/farmacias');
        const farmacia = farmacias.find(f => f.email === user.email);
        if (!farmacia) return;

        const { data: s } = await api.get(`/dashboard/farmacia/${farmacia.id}`);
        this.stats = s;
        await this.carregarGrafico(farmacia.id);
      } catch (e) { console.error(e); }
    },
    async carregarGrafico(id) {
      await this.$nextTick();
      const Chart = window.Chart;
      if (!Chart) return;
      try {
        const { data } = await api.get(`/dashboard/farmacia/${id}/vendas-semana`);
        const ctx = document.getElementById('lineChart');
        if (!ctx) return;
        if (this.lineChart) this.lineChart.destroy();
        this.lineChart = new Chart(ctx.getContext('2d'), {
          type: 'line',
          data: {
            labels: data.labels || [],
            datasets: [{
              label: 'Vendas (R$)',
              data: data.valores || [],
              borderColor: '#2A5C45',
              backgroundColor: 'rgba(42,92,69,0.07)',
              tension: 0.4, fill: true,
              pointBackgroundColor: '#2A5C45',
              pointRadius: 4, pointHoverRadius: 6,
              borderWidth: 2
            }]
          },
          options: {
            responsive: true, maintainAspectRatio: true,
            plugins: { legend: { display: false } },
            scales: {
              x: { grid: { display: false }, ticks: { font: { family: "'DM Sans', sans-serif", size: 11 } } },
              y: { beginAtZero: true, grid: { color: 'rgba(28,28,26,0.05)' },
                   ticks: { callback: v => `R$${v}`, font: { family: "'DM Sans', sans-serif", size: 10 } } }
            }
          }
        });
      } catch (e) { console.error(e); }
    }
  }
};
</script>

<style scoped>
/* KPIs */
.cf-kpi {
  background: var(--cf-white); border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg); padding: 1.25rem 1.1rem;
  display: flex; align-items: center; gap: 0.9rem;
  box-shadow: var(--cf-shadow-xs); position: relative; overflow: hidden;
  border-top: 3px solid transparent; transition: all 0.2s var(--cf-ease);
}
.cf-kpi:hover { transform: translateY(-2px); box-shadow: var(--cf-shadow-md); }
.cf-kpi.accent-green  { border-top-color: var(--cf-green); }
.cf-kpi.accent-gold   { border-top-color: var(--cf-gold); }
.cf-kpi.accent-danger { border-top-color: var(--cf-danger); }
.cf-kpi.accent-neutral { border-top-color: var(--cf-border-mid); }

.cf-kpi-icon {
  width: 44px; height: 44px; border-radius: var(--cf-r-md);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.1rem; flex-shrink: 0;
}
.accent-green  .cf-kpi-icon { background: var(--cf-green-xlight); color: var(--cf-green); }
.accent-gold   .cf-kpi-icon { background: var(--cf-gold-light);   color: var(--cf-gold); }
.accent-danger .cf-kpi-icon { background: #F9EDED;                color: var(--cf-danger); }
.accent-neutral .cf-kpi-icon { background: var(--cf-cream);       color: var(--cf-text-mid); }

.cf-kpi-body { flex: 1; min-width: 0; }
.cf-kpi-lbl { font-size: 0.62rem; text-transform: uppercase; letter-spacing: 0.12em; color: var(--cf-text-muted); display: block; margin-bottom: 3px; font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cf-kpi-val { font-size: 1.4rem; font-weight: 700; color: var(--cf-text-dark); line-height: 1.1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cf-kpi-sub { font-size: 0.7rem; color: var(--cf-text-muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; }
.cf-danger  { color: var(--cf-danger) !important; }

.cf-kpi-pulse {
  position: absolute; top: 10px; right: 10px;
  width: 10px; height: 10px; border-radius: 50%;
  background: var(--cf-danger);
  animation: kpi-pulse 1.5s ease-in-out infinite;
}
@keyframes kpi-pulse { 0%,100% { transform:scale(1); opacity:1; } 50% { transform:scale(1.6); opacity:0.4; } }

/* Cards */
.cf-card { background: var(--cf-white); border: 1px solid var(--cf-border); border-radius: var(--cf-r-lg); box-shadow: var(--cf-shadow-xs); overflow: hidden; }
.cf-card-head { padding: 0.95rem 1.3rem; border-bottom: 1px solid var(--cf-border); display: flex; align-items: center; justify-content: space-between; background: var(--cf-ivory); }
.cf-card-title { margin: 0; font-size: 0.87rem; font-weight: 500; color: var(--cf-text-dark); display: flex; align-items: center; gap: 0.5rem; }
.cf-dot { width: 7px; height: 7px; border-radius: 50%; display: inline-block; flex-shrink: 0; }
.cf-dot.green { background: var(--cf-green); }
.cf-dot.gold  { background: var(--cf-gold); }
.cf-eyebrow { font-size: 0.6rem; text-transform: uppercase; letter-spacing: 0.14em; color: var(--cf-text-faint); font-weight: 500; }
.cf-card-body { padding: 1.2rem; }

.cf-badge-danger { font-size: 0.65rem; font-weight: 700; background: #F9EDED; color: var(--cf-danger); padding: 0.22rem 0.75rem; border-radius: 20px; }

/* Central de pedidos */
.cf-bell-anim { font-size: 2.8rem; color: var(--cf-gold); animation: bell-shake 1.2s ease-in-out infinite; }
@keyframes bell-shake { 0%,100%{transform:rotate(0)} 20%{transform:rotate(15deg)} 40%{transform:rotate(-15deg)} 60%{transform:rotate(8deg)} 80%{transform:rotate(-8deg)} }
.cf-alert-heading { font-size: 1.05rem; font-weight: 600; color: var(--cf-text-dark); }
.cf-alert-sub { font-size: 0.82rem; color: var(--cf-text-muted); }

.cf-all-clear i { font-size: 2.8rem; color: var(--cf-green); }
.cf-all-clear h5 { font-size: 1rem; font-weight: 600; color: var(--cf-text-dark); }
.cf-all-clear p { font-size: 0.82rem; color: var(--cf-text-muted); }

.cf-btn-primary {
  display: inline-flex; align-items: center; justify-content: center;
  background: var(--cf-green); color: #fff; border: none; cursor: pointer;
  padding: 0.62rem 1.4rem; border-radius: var(--cf-r-md);
  font-size: 0.78rem; font-weight: 500; letter-spacing: 0.06em;
  text-decoration: none; transition: all 0.2s var(--cf-ease);
}
.cf-btn-primary:hover { background: var(--cf-green-dark); color: #fff; transform: translateY(-1px); box-shadow: 0 4px 14px rgba(42,92,69,0.2); }

.cf-btn-outline {
  display: inline-flex; align-items: center; justify-content: center;
  background: transparent; color: var(--cf-green);
  border: 1px solid var(--cf-green); cursor: pointer;
  padding: 0.55rem 1.2rem; border-radius: var(--cf-r-md);
  font-size: 0.78rem; font-weight: 500; text-decoration: none;
  transition: all 0.2s var(--cf-ease);
}
.cf-btn-outline:hover { background: var(--cf-green); color: #fff; }
</style>
