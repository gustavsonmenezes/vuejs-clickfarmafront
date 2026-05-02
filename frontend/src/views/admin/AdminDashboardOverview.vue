<template>
  <div class="admin-dashboard-view">
    <!-- Welcome Header -->
    <div class="page-header mb-4">
      <div>
        <h2 class="mb-1">Visão Geral</h2>
        <p class="text-muted mb-0">Acompanhe as métricas principais da sua farmácia.</p>
      </div>
      <button class="btn-refresh" @click="fetchDashboardData" :disabled="loading">
        <i class="fas fa-arrows-rotate" :class="{ 'fa-spin': loading }"></i>
        Atualizar
      </button>
    </div>

    <!-- Stat Cards -->
    <div class="stats-grid mb-4">
      <div class="cf-stat-card">
        <div class="stat-icon bg-primary-subtle">
          <i class="fas fa-bag-shopping"></i>
        </div>
        <div class="stat-content">
          <p class="stat-label">Total de Pedidos</p>
          <p class="stat-value">{{ stats.totalPedidos || 0 }}</p>
        </div>
      </div>
      
      <div class="cf-stat-card">
        <div class="stat-icon bg-success-subtle">
          <i class="fas fa-chart-line"></i>
        </div>
        <div class="stat-content">
          <p class="stat-label">Faturamento</p>
          <p class="stat-value">R$ {{ stats.faturamentoTotal || '0,00' }}</p>
        </div>
      </div>
      
      <div class="cf-stat-card">
        <div class="stat-icon bg-info-subtle">
          <i class="fas fa-pills"></i>
        </div>
        <div class="stat-content">
          <p class="stat-label">Produtos Ativos</p>
          <p class="stat-value">{{ stats.totalProdutos || 0 }}</p>
        </div>
      </div>
      
      <div class="cf-stat-card">
        <div class="stat-icon bg-warning-subtle">
          <i class="fas fa-users"></i>
        </div>
        <div class="stat-content">
          <p class="stat-label">Clientes</p>
          <p class="stat-value">{{ stats.totalUsuarios || 0 }}</p>
        </div>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="charts-row mb-4">
      <!-- Top Products Bar Chart -->
      <div class="cf-card chart-card">
        <div class="cf-card-header">
          <h5 class="mb-0 fw-semibold">Top Produtos por Receita</h5>
        </div>
        <div class="cf-card-body">
          <BarChart v-if="chartData.bar" :data="chartData.bar" :options="barOptions" />
          <div v-else class="cf-empty-state py-4">
            <i class="fas fa-spinner fa-spin"></i>
            <p>Carregando...</p>
          </div>
        </div>
      </div>

      <!-- Order Status Doughnut -->
      <div class="cf-card chart-card">
        <div class="cf-card-header">
          <h5 class="mb-0 fw-semibold">Distribuição de Pedidos</h5>
        </div>
        <div class="cf-card-body">
          <DoughnutChart v-if="chartData.doughnut" :data="chartData.doughnut" :options="doughnutOptions" />
          <div v-else class="cf-empty-state py-4">
            <i class="fas fa-spinner fa-spin"></i>
            <p>Carregando...</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Row: Table + Sales by Period -->
    <div class="content-grid">
      <!-- Top Products Table -->
      <div class="cf-card">
        <div class="cf-card-header d-flex justify-content-between align-items-center">
          <h5 class="mb-0 fw-semibold">Detalhamento de Vendas</h5>
          <span class="cf-badge cf-badge-primary">Top 5</span>
        </div>
        <div class="cf-card-body p-0">
          <table class="cf-table" v-if="popularProducts.length">
            <thead>
              <tr>
                <th class="ps-4">#</th>
                <th>Produto</th>
                <th class="text-center">Qtd.</th>
                <th class="text-end pe-4">Receita</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(prod, i) in popularProducts" :key="i">
                <td class="ps-4">
                  <div class="rank-badge">{{ i + 1 }}</div>
                </td>
                <td>
                  <div class="fw-medium text-dark">{{ prod.nome }}</div>
                </td>
                <td class="text-center">
                  <span class="cf-badge cf-badge-info">{{ prod.quantidade }}</span>
                </td>
                <td class="text-end pe-4 fw-semibold text-success">R$ {{ prod.receita }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="cf-empty-state">
            <i class="fas fa-box-open"></i>
            <p>Sem dados de vendas ainda.</p>
          </div>
        </div>
      </div>
      
      <!-- Order Status Breakdown -->
      <div class="cf-card">
        <div class="cf-card-header">
          <h5 class="mb-0 fw-semibold">Status dos Pedidos</h5>
        </div>
        <div class="cf-card-body">
          <div v-for="(count, status) in statusCounts" :key="status" class="status-row mb-3">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <span class="small fw-medium">{{ formatStatus(status) }}</span>
              <span class="cf-badge cf-badge-primary">{{ count }}</span>
            </div>
            <div class="cf-progress">
              <div class="cf-progress-bar" :style="{ width: (count / totalPedidosStatus * 100) + '%', backgroundColor: getStatusColor(status) }"></div>
            </div>
          </div>
          <div v-if="!Object.keys(statusCounts).length" class="cf-empty-state py-5">
            <i class="fas fa-chart-pie"></i>
            <p>Nenhum pedido registrado.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { BarElement, CategoryScale, Chart as ChartJS, Legend, LinearScale, Title, Tooltip, ArcElement } from 'chart.js'
import { Bar } from 'vue-chartjs'
import { Doughnut } from 'vue-chartjs'
import { adminService } from '@/services/adminService';

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, ArcElement);

const BarChart = Bar;
const DoughnutChart = Doughnut;

export default {
  name: 'AdminDashboardOverview',
  components: { BarChart, DoughnutChart },
  data() {
    return {
      stats: {},
      popularProducts: [],
      statusCounts: {},
      totalPedidosStatus: 0,
      loading: false,
      chartData: { bar: null, doughnut: null },
      barOptions: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#1e293b',
            titleFont: { family: 'Inter', size: 13, weight: '600' },
            bodyFont: { family: 'Inter', size: 12 },
            padding: 12,
            cornerRadius: 8,
            callbacks: {
              label: (ctx) => `Receita: R$ ${ctx.parsed.y}`
            }
          }
        },
        scales: {
          x: {
            grid: { display: false },
            ticks: { font: { family: 'Inter', size: 11, weight: '500' }, color: '#94a3b8' }
          },
          y: {
            grid: { color: '#f1f5f9' },
            ticks: { font: { family: 'Inter', size: 11 }, color: '#94a3b8' }
          }
        }
      },
      doughnutOptions: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '65%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 16,
              usePointStyle: true,
              pointStyleWidth: 8,
              font: { family: 'Inter', size: 12, weight: '500' },
              color: '#475569'
            }
          },
          tooltip: {
            backgroundColor: '#1e293b',
            padding: 12,
            cornerRadius: 8,
            titleFont: { family: 'Inter', size: 13, weight: '600' },
            bodyFont: { family: 'Inter', size: 12 }
          }
        }
      }
    }
  },
  mounted() {
    this.fetchDashboardData()
  },
  methods: {
    async fetchDashboardData() {
      this.loading = true;
      try {
        const [resumoRes, popRes, statusRes] = await Promise.all([
          adminService.getResumo(),
          adminService.getProdutosPopulares(),
          adminService.getPedidosPorStatus()
        ]);
        
        this.stats = resumoRes.data || {};
        this.popularProducts = popRes.data.produtos || [];
        this.statusCounts = statusRes.data.porStatus || {};
        this.totalPedidosStatus = Object.values(this.statusCounts).reduce((a, b) => a + b, 0);
        
        this.buildCharts();
      } catch (e) {
        console.error('Erro ao carregar dados:', e);
      } finally {
        this.loading = false;
      }
    },
    buildCharts() {
      // Bar chart - top products by revenue
      if (this.popularProducts.length) {
        this.chartData.bar = {
          labels: this.popularProducts.map(p => p.nome.length > 20 ? p.nome.substring(0, 20) + '...' : p.nome),
          datasets: [{
            data: this.popularProducts.map(p => parseFloat(p.receita.replace(',', '.'))),
            backgroundColor: ['#3b82f6', '#059669', '#f59e0b', '#8b5cf6', '#ec4899'],
            borderRadius: 6,
            borderSkipped: false,
            barThickness: 36
          }]
        };
      }
      
      // Doughnut chart - order status distribution
      if (Object.keys(this.statusCounts).length) {
        this.chartData.doughnut = {
          labels: Object.keys(this.statusCounts).map(s => this.formatStatus(s)),
          datasets: [{
            data: Object.values(this.statusCounts),
            backgroundColor: ['#94a3b8', '#3b82f6', '#f59e0b', '#8b5cf6', '#059669', '#06b6d4', '#ef4444'],
            borderWidth: 0,
            spacing: 2
          }]
        };
      }
    },
    formatStatus(status) {
      const map = {
        'AGUARDANDO_PAGAMENTO': 'Aguardando',
        'PAGO': 'Pago',
        'EM_PREPARACAO': 'Preparação',
        'ENVIADO': 'Enviado',
        'EM_TRANSITO': 'Trânsito',
        'ENTREGUE': 'Entregue',
        'CANCELADO': 'Cancelado'
      };
      return map[status] || status;
    },
    getStatusColor(status) {
      const map = {
        'AGUARDANDO_PAGAMENTO': '#f59e0b',
        'PAGO': '#3b82f6',
        'EM_PREPARACAO': '#8b5cf6',
        'ENVIADO': '#059669',
        'EM_TRANSITO': '#06b6d4',
        'ENTREGUE': '#475569',
        'CANCELADO': '#ef4444'
      };
      return map[status] || '#94a3b8';
    }
  }
}
</script>

<style scoped>
.admin-dashboard-view {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--cf-slate-900);
  letter-spacing: -0.02em;
}

.btn-refresh {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: white;
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-radius-md);
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--cf-slate-600);
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-refresh:hover {
  background: var(--cf-slate-50);
  border-color: var(--cf-slate-300);
}

.btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.cf-stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.125rem;
}

.bg-primary-subtle { background: var(--cf-primary-100); color: var(--cf-primary-600); }
.bg-success-subtle { background: var(--cf-success-light); color: var(--cf-success); }
.bg-info-subtle { background: var(--cf-info-light); color: var(--cf-info); }
.bg-warning-subtle { background: var(--cf-warning-light); color: var(--cf-warning); }

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 0.8125rem;
  color: var(--cf-slate-500);
  margin: 0 0 4px 0;
  font-weight: 500;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--cf-slate-900);
  margin: 0;
  letter-spacing: -0.02em;
}

/* Charts Row */
.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.chart-card .cf-card-body {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: var(--cf-slate-100);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.75rem;
  color: var(--cf-slate-600);
}

.status-row:last-child {
  margin-bottom: 0;
}

/* Responsive */
@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row,
  .content-grid { grid-template-columns: 1fr; }
  .chart-card .cf-card-body { height: 280px; }
}

@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr; }
  .page-header { flex-direction: column; gap: 12px; }
}
</style>
