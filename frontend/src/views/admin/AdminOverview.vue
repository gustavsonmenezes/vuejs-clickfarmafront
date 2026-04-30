<template>
  <div class="cf-overview">
    <!-- KPI Cards -->
    <div class="row g-3 mb-4">
      <div class="col-xl-3 col-sm-6" v-for="kpi in kpis" :key="kpi.label">
        <div class="cf-kpi-card" :class="kpi.accent">
          <div class="cf-kpi-icon"><i :class="kpi.icon"></i></div>
          <div class="cf-kpi-body">
            <span class="cf-kpi-label">{{ kpi.label }}</span>
            <div class="cf-kpi-value">{{ kpi.value }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Gráficos -->
    <div class="row g-4 mb-4">
      <div class="col-xl-7">
        <div class="cf-card">
          <div class="cf-card-head">
            <h5 class="cf-card-title">
              <span class="cf-card-dot green"></span>
              Vendas da Semana (R$)
            </h5>
            <span class="cf-eyebrow">semana atual</span>
          </div>
          <div class="cf-card-body">
            <canvas id="barChart" height="130"></canvas>
          </div>
        </div>
      </div>

      <div class="col-xl-5">
        <div class="cf-card h-100">
          <div class="cf-card-head">
            <h5 class="cf-card-title">
              <span class="cf-card-dot gold"></span>
              Status dos Pedidos
            </h5>
          </div>
          <div class="cf-card-body d-flex align-items-center justify-content-center">
            <canvas id="pieChart" height="180" style="max-height:210px"></canvas>
          </div>
        </div>
      </div>
    </div>

    <!-- Rankings -->
    <div class="row g-4 mb-4">
      <div class="col-xl-4">
        <div class="cf-card h-100">
          <div class="cf-card-head">
            <h5 class="cf-card-title"><span class="cf-card-dot green"></span>Top Farmácias</h5>
            <span class="cf-eyebrow">por faturamento</span>
          </div>
          <div class="cf-card-body p-0">
            <div v-if="loadingRankings" class="cf-loading"><div class="cf-spinner"></div></div>
            <table v-else class="cf-table">
              <thead>
                <tr><th>#</th><th>Farmácia</th><th>Faturamento</th></tr>
              </thead>
              <tbody>
                <tr v-for="(f, i) in topFarmacias" :key="f.id">
                  <td><span class="cf-rank" :class="`cf-rank-${i+1}`">{{ i+1 }}</span></td>
                  <td class="cf-td-bold text-truncate" style="max-width: 120px;">{{ f.nome }}</td>
                  <td class="cf-td-green">R$ {{ num(f.faturamento) }}</td>
                </tr>
                <tr v-if="!topFarmacias.length">
                  <td colspan="3" class="cf-empty">Nenhum dado</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-xl-4">
        <div class="cf-card h-100">
          <div class="cf-card-head">
            <h5 class="cf-card-title"><span class="cf-card-dot gold"></span>Top Clientes</h5>
            <span class="cf-eyebrow">por valor gasto</span>
          </div>
          <div class="cf-card-body p-0">
            <div v-if="loadingRankings" class="cf-loading"><div class="cf-spinner"></div></div>
            <table v-else class="cf-table">
              <thead>
                <tr><th>#</th><th>Cliente</th><th>Total Gasto</th></tr>
              </thead>
              <tbody>
                <tr v-for="(c, i) in topClientes" :key="c.usuarioId">
                  <td><span class="cf-rank" :class="`cf-rank-${i+1}`">{{ i+1 }}</span></td>
                  <td class="cf-td-bold text-truncate" style="max-width: 120px;">{{ c.nome || 'Cliente #' + c.usuarioId }}</td>
                  <td class="cf-td-gold">R$ {{ num(c.totalGasto) }}</td>
                </tr>
                <tr v-if="!topClientes.length">
                  <td colspan="3" class="cf-empty">Nenhum dado</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-xl-4">
        <div class="cf-card h-100">
          <div class="cf-card-head">
            <h5 class="cf-card-title"><span class="cf-card-dot dark"></span>Top Entregadores</h5>
            <span class="cf-eyebrow">por entregas</span>
          </div>
          <div class="cf-card-body p-0">
            <div v-if="loadingRankings" class="cf-loading"><div class="cf-spinner"></div></div>
            <table v-else class="cf-table">
              <thead>
                <tr><th>#</th><th>Nome</th><th>Entregas</th></tr>
              </thead>
              <tbody>
                <tr v-for="(m, i) in topMotoboys" :key="m.motoboyId">
                  <td><span class="cf-rank" :class="`cf-rank-${i+1}`">{{ i+1 }}</span></td>
                  <td class="cf-td-bold">{{ m.nome || 'Motoboy #' + m.motoboyId }}</td>
                  <td class="cf-td-muted fw-bold">{{ m.entregas }} envios</td>
                </tr>
                <tr v-if="!topMotoboys.length">
                  <td colspan="3" class="cf-empty">Nenhum dado</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'AdminOverview',
  data() {
    return {
      kpis: [],
      topFarmacias: [],
      topClientes: [],
      topMotoboys: [],
      loadingRankings: true,
      barChart: null,
      pieChart: null
    };
  },
  async mounted() {
    await Promise.all([this.loadKpis(), this.loadRankings(), this.loadCharts()]);
  },
  methods: {
    num(v) { return Number(v || 0).toFixed(2); },

    async loadKpis() {
      try {
        const { data: d } = await api.get('/dashboard/admin');
        this.kpis = [
          { label: 'Faturamento Total',   value: `R$ ${this.num(d.faturamentoTotal)}`,  icon: 'fas fa-dollar-sign', accent: 'accent-green' },
          { label: 'Comissão ClickFarma', value: `R$ ${this.num(d.comissaoTotal)}`,     icon: 'fas fa-percentage', accent: 'accent-gold' },
          { label: 'Total de Pedidos',    value: d.totalPedidos ?? 0,                   icon: 'fas fa-shopping-bag', accent: 'accent-neutral' },
          { label: 'Clientes Ativos',     value: d.totalClientes ?? 0,                  icon: 'fas fa-users', accent: 'accent-neutral' }
        ];
      } catch (e) { console.error(e); }
    },

    async loadRankings() {
      try {
        const { data } = await api.get('/dashboard/admin/rankings');
        this.topFarmacias = data.topFarmacias || [];
        this.topClientes  = data.topClientes  || [];
        this.topMotoboys  = data.topMotoboys  || [];
      } catch (e) { console.error(e); }
      finally { this.loadingRankings = false; }
    },

    async loadCharts() {
      await this.$nextTick();
      const Chart = window.Chart;
      if (!Chart) return;
      try {
        // ─── Barras — vendas da semana ───
        const { data: s } = await api.get('/dashboard/admin/vendas-semana');
        const ctxBar = document.getElementById('barChart');
        if (ctxBar) {
          if (this.barChart) this.barChart.destroy();
          this.barChart = new Chart(ctxBar.getContext('2d'), {
            type: 'bar',
            data: {
              labels: s.labels || [],
              datasets: [{
                label: 'Vendas (R$)',
                data: s.valores || [],
                backgroundColor: 'rgba(42,92,69,0.18)',
                borderColor: '#2A5C45',
                borderWidth: 2,
                borderRadius: 6,
                hoverBackgroundColor: 'rgba(42,92,69,0.35)'
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
        }

        // ─── Rosca — status de pedidos ───
        const { data: st } = await api.get('/dashboard/admin/pedidos-por-status');
        const labels = Object.keys(st.porStatus || {}).map(k => k.replace(/_/g,' '));
        const vals   = Object.values(st.porStatus || {});
        const ctxPie = document.getElementById('pieChart');
        if (ctxPie && labels.length) {
          if (this.pieChart) this.pieChart.destroy();
          this.pieChart = new Chart(ctxPie.getContext('2d'), {
            type: 'doughnut',
            data: {
              labels,
              datasets: [{
                data: vals,
                backgroundColor: ['#2A5C45','#B89550','#3D7A5E','#8B3A3A','#B0AFA9','#1C3D2E'],
                borderWidth: 0
              }]
            },
            options: {
              responsive: true, cutout: '68%',
              plugins: {
                legend: { position: 'bottom', labels: { font: { family: "'DM Sans', sans-serif", size: 10 }, padding: 12, boxWidth: 10, usePointStyle: true } }
              }
            }
          });
        }
      } catch (e) { console.error(e); }
    }
  }
};
</script>

<style scoped>
/* KPIs */
.cf-kpi-card {
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg);
  padding: 1.35rem 1.25rem;
  display: flex; align-items: center; gap: 1rem;
  box-shadow: var(--cf-shadow-xs);
  transition: all 0.22s var(--cf-ease);
  border-top: 3px solid transparent;
}
.cf-kpi-card:hover { transform: translateY(-3px); box-shadow: var(--cf-shadow-md); }
.cf-kpi-card.accent-green  { border-top-color: var(--cf-green); }
.cf-kpi-card.accent-gold   { border-top-color: var(--cf-gold); }
.cf-kpi-card.accent-neutral { border-top-color: var(--cf-border-mid); }

.cf-kpi-icon {
  width: 48px; height: 48px; border-radius: var(--cf-r-md);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.2rem; flex-shrink: 0;
}
.accent-green  .cf-kpi-icon { background: var(--cf-green-xlight); color: var(--cf-green); }
.accent-gold   .cf-kpi-icon { background: var(--cf-gold-light);   color: var(--cf-gold); }
.accent-neutral .cf-kpi-icon { background: var(--cf-cream);       color: var(--cf-text-mid); }

.cf-kpi-label {
  font-size: 0.65rem; text-transform: uppercase; letter-spacing: 0.12em;
  color: var(--cf-text-muted); display: block; margin-bottom: 4px; font-weight: 500;
}
.cf-kpi-value { 
  font-size: clamp(1.2rem, 4vw, 1.5rem); 
  font-weight: 700; 
  color: var(--cf-text-dark); 
  line-height: 1.1; 
  font-family: var(--cf-sans);
  word-break: break-all;
}

@media (max-width: 576px) {
  .cf-kpi-card { padding: 1rem; gap: 0.75rem; }
  .cf-kpi-icon { width: 40px; height: 40px; font-size: 1rem; }
}

/* Cards */
.cf-card {
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg);
  box-shadow: var(--cf-shadow-xs);
  overflow: hidden;
}
.cf-card-head {
  padding: 1rem 1.35rem;
  border-bottom: 1px solid var(--cf-border);
  display: flex; align-items: center; justify-content: space-between;
  background: var(--cf-ivory);
}
.cf-card-title {
  margin: 0; font-size: 0.88rem; font-weight: 500;
  color: var(--cf-text-dark); display: flex; align-items: center; gap: 0.5rem;
}
.cf-card-dot {
  width: 8px; height: 8px; border-radius: 50%;
  display: inline-block; flex-shrink: 0;
}
.cf-card-dot.green { background: var(--cf-green); }
.cf-card-dot.gold  { background: var(--cf-gold); }
.cf-card-dot.dark  { background: var(--cf-green-dark); }
.cf-eyebrow {
  font-size: 0.6rem; text-transform: uppercase; letter-spacing: 0.14em;
  color: var(--cf-text-faint); font-weight: 500;
}
.cf-card-body { padding: 1.25rem; }

/* Tabelas */
.cf-table { width: 100%; border-collapse: collapse; }
.cf-table thead tr { background: var(--cf-ivory); }
.cf-table th {
  font-size: 0.62rem; text-transform: uppercase; letter-spacing: 0.12em;
  color: var(--cf-text-muted); font-weight: 500;
  padding: 0.6rem 1.1rem; border-bottom: 1px solid var(--cf-border);
}
.cf-table td { padding: 0.7rem 1.1rem; border-bottom: 1px solid var(--cf-border); font-size: 0.855rem; }
.cf-table tbody tr:last-child td { border-bottom: none; }
.cf-table tbody tr:hover td { background: var(--cf-ivory); }

.cf-td-bold   { font-weight: 500; color: var(--cf-text-dark); }
.cf-td-muted  { color: var(--cf-text-muted); font-size: 0.8rem; }
.cf-td-green  { color: var(--cf-green); font-weight: 600; }
.cf-td-gold   { color: var(--cf-gold);  font-weight: 600; }

.cf-rank {
  display: inline-flex; align-items: center; justify-content: center;
  width: 24px; height: 24px; border-radius: 50%;
  font-size: 0.72rem; font-weight: 700;
  background: var(--cf-cream); color: var(--cf-text-muted);
}
.cf-rank-1 { background: var(--cf-gold-light); color: var(--cf-gold); }
.cf-rank-2 { background: var(--cf-cream); color: var(--cf-text-mid); }
.cf-rank-3 { background: var(--cf-green-xlight); color: var(--cf-green); }

.cf-empty { text-align: center; color: var(--cf-text-faint); padding: 2rem; font-size: 0.82rem; }
.cf-loading { display: flex; align-items: center; justify-content: center; padding: 2.5rem; }
.cf-spinner {
  width: 26px; height: 26px; border: 2.5px solid var(--cf-border);
  border-top-color: var(--cf-green); border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
