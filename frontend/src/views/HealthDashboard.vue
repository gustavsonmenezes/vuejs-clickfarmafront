<template>
  <div class="health-dashboard">
    <div class="container py-4">
      <!-- Header -->
      <div class="dashboard-header mb-4">
        <div class="d-flex align-items-center justify-content-between flex-wrap gap-3">
          <div>
            <h1 class="h3 mb-1 text-primary">
              <i class="fas fa-heartbeat me-2"></i>Minha Saúde
            </h1>
            <p class="text-muted mb-0">Acompanhe seus gastos, medicamentos e reposições</p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="badge bg-success-subtle text-success fs-6">
              <i class="fas fa-shield-check me-1"></i>Dados reais
            </span>
            <button class="btn btn-outline-primary btn-sm" @click="carregarDados" :disabled="loading">
              <i :class="loading ? 'fas fa-sync-alt spin' : 'fas fa-sync-alt'" class="me-1"></i>
              Atualizar
            </button>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading && !dashboard" class="text-center py-5">
        <div class="spinner-border text-primary mb-3" role="status"></div>
        <p class="text-muted">Carregando dados da sua saúde...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="alert alert-danger">
        <i class="fas fa-exclamation-triangle me-2"></i>{{ error }}
        <button class="btn btn-sm btn-outline-danger ms-2" @click="carregarDados">Tentar novamente</button>
      </div>

      <!-- Dashboard Content -->
      <div v-else-if="dashboard" class="dashboard-content">
        <!-- Resumo Cards -->
        <div class="row g-3 mb-4">
          <div class="col-6 col-lg-3">
            <div class="card stat-card border-0 shadow-sm h-100">
              <div class="card-body">
                <div class="stat-icon bg-primary-subtle text-primary rounded-circle d-flex align-items-center justify-content-center mb-2">
                  <i class="fas fa-dollar-sign fs-4"></i>
                </div>
                <p class="text-muted mb-1 small">Total Gasto</p>
                <h3 class="mb-0 fw-bold text-primary">{{ formatCurrency(dashboard.resumo.totalGasto) }}</h3>
              </div>
            </div>
          </div>
          <div class="col-6 col-lg-3">
            <div class="card stat-card border-0 shadow-sm h-100">
              <div class="card-body">
                <div class="stat-icon bg-success-subtle text-success rounded-circle d-flex align-items-center justify-content-center mb-2">
                  <i class="fas fa-shopping-bag fs-4"></i>
                </div>
                <p class="text-muted mb-1 small">Total Pedidos</p>
                <h3 class="mb-0 fw-bold text-success">{{ dashboard.resumo.totalPedidos }}</h3>
              </div>
            </div>
          </div>
          <div class="col-6 col-lg-3">
            <div class="card stat-card border-0 shadow-sm h-100">
              <div class="card-body">
                <div class="stat-icon bg-warning-subtle text-warning rounded-circle d-flex align-items-center justify-content-center mb-2">
                  <i class="fas fa-capsules fs-4"></i>
                </div>
                <p class="text-muted mb-1 small">Mais Comprado</p>
                <h6 class="mb-0 fw-bold text-warning text-truncate" :title="dashboard.resumo.medicamentoFrequente">{{ dashboard.resumo.medicamentoFrequente }}</h6>
              </div>
            </div>
          </div>
          <div class="col-6 col-lg-3">
            <div class="card stat-card border-0 shadow-sm h-100">
              <div class="card-body">
                <div class="stat-icon bg-info-subtle text-info rounded-circle d-flex align-items-center justify-content-center mb-2">
                  <i class="fas fa-piggy-bank fs-4"></i>
                </div>
                <p class="text-muted mb-1 small">Economia Est.</p>
                <h3 class="mb-0 fw-bold text-info">{{ formatCurrency(dashboard.resumo.economiaEstimada) }}</h3>
              </div>
            </div>
          </div>
        </div>

        <!-- Charts Row 1: Gastos Mensais + Categorias -->
        <div class="row g-3 mb-4">
          <div class="col-lg-8">
            <div class="card border-0 shadow-sm h-100">
              <div class="card-header bg-white border-0 py-3">
                <h5 class="mb-0"><i class="fas fa-chart-line me-2 text-primary"></i>Gastos Mensais</h5>
              </div>
              <div class="card-body">
                <canvas ref="gastosMensaisChart" height="120"></canvas>
              </div>
            </div>
          </div>
          <div class="col-lg-4">
            <div class="card border-0 shadow-sm h-100">
              <div class="card-header bg-white border-0 py-3">
                <h5 class="mb-0"><i class="fas fa-chart-pie me-2 text-primary"></i>Por Categoria</h5>
              </div>
              <div class="card-body d-flex align-items-center justify-content-center">
                <canvas ref="categoriaChart" height="200"></canvas>
              </div>
            </div>
          </div>
        </div>

        <!-- Charts Row 2: Top Medicamentos + Previsão Reposição -->
        <div class="row g-3 mb-4">
          <div class="col-lg-5">
            <div class="card border-0 shadow-sm h-100">
              <div class="card-header bg-white border-0 py-3">
                <h5 class="mb-0"><i class="fas fa-trophy me-2 text-warning"></i>Top Medicamentos</h5>
              </div>
              <div class="card-body">
                <canvas ref="topMedicamentosChart" height="180"></canvas>
              </div>
            </div>
          </div>
          <div class="col-lg-7">
            <div class="card border-0 shadow-sm h-100">
              <div class="card-header bg-white border-0 py-3">
                <h5 class="mb-0"><i class="fas fa-clock me-2 text-danger"></i>Previsão de Reposição</h5>
              </div>
              <div class="card-body p-0">
                <div class="table-responsive">
                  <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                      <tr>
                        <th class="ps-3">Produto</th>
                        <th>Categoria</th>
                        <th>Última Compra</th>
                        <th>Dias Restantes</th>
                        <th class="pe-3">Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(item, idx) in dashboard.previsaoReposicao" :key="idx">
                        <td class="ps-3 fw-medium">{{ item.produto }}</td>
                        <td><span class="badge bg-light text-dark">{{ item.categoria }}</span></td>
                        <td>{{ item.ultimaCompra }}</td>
                        <td>
                          <div class="d-flex align-items-center gap-2">
                            <div class="progress flex-grow-1" style="height: 6px; width: 60px;">
                              <div class="progress-bar" :class="getStatusColor(item.status)" :style="{ width: getBarWidth(item.diasRestantes) + '%' }"></div>
                            </div>
                            <span class="small fw-bold" :class="getStatusTextColor(item.status)">{{ item.diasRestantes }}d</span>
                          </div>
                        </td>
                        <td class="pe-3">
                          <span class="badge" :class="getStatusBadge(item.status)">
                            <i :class="getStatusIcon(item.status)" class="me-1"></i>{{ getStatusLabel(item.status) }}
                          </span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Timeline Preditiva Section -->
        <div class="card border-0 shadow-sm mb-4">
          <div class="card-header bg-white border-0 py-3 d-flex align-items-center justify-content-between flex-wrap gap-2">
            <h5 class="mb-0"><i class="fas fa-chart-bar me-2 text-primary"></i>Linha do Tempo Preditiva</h5>
            <div class="d-flex align-items-center gap-3">
              <span class="small text-muted">
                <i class="fas fa-circle text-danger me-1"></i>Crítico
                <i class="fas fa-circle text-warning ms-2 me-1"></i>Atenção
                <i class="fas fa-circle text-success ms-2 me-1"></i>OK
              </span>
              <button class="btn btn-sm btn-outline-primary" @click="carregarTimeline" :disabled="timelineLoading">
                <i :class="timelineLoading ? 'fas fa-sync-alt spin' : 'fas fa-sync-alt'" class="me-1"></i>Recalcular
              </button>
            </div>
          </div>
          <div class="card-body">
            <div v-if="timelineLoading && !timeline" class="text-center py-4">
              <div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
              <span class="text-muted">Calculando timeline preditiva...</span>
            </div>
            <div v-else-if="timelineError" class="alert alert-warning py-2">
              <i class="fas fa-exclamation-triangle me-1"></i>{{ timelineError }}
            </div>
            <div v-else-if="timeline && timeline.medicamentos.length === 0" class="text-center py-4">
              <i class="fas fa-inbox fa-2x text-muted mb-2"></i>
              <p class="text-muted mb-0">Nenhum medicamento no histórico para análise preditiva.</p>
            </div>
            <div v-else-if="timeline" class="timeline-container">
              <div class="row g-3 mb-4">
                <div class="col-4 col-md-3">
                  <div class="timeline-stat">
                    <span class="timeline-stat-number">{{ timeline.resumo.totalMedicamentos }}</span>
                    <span class="timeline-stat-label">Total</span>
                  </div>
                </div>
                <div class="col-4 col-md-3">
                  <div class="timeline-stat">
                    <span class="timeline-stat-number text-danger">{{ timeline.resumo.criticos }}</span>
                    <span class="timeline-stat-label">Críticos</span>
                  </div>
                </div>
                <div class="col-4 col-md-3">
                  <div class="timeline-stat">
                    <span class="timeline-stat-number text-warning">{{ timeline.resumo.atencao }}</span>
                    <span class="timeline-stat-label">Atenção</span>
                  </div>
                </div>
                <div class="col-4 col-md-3">
                  <div class="timeline-stat">
                    <span class="timeline-stat-number text-success">{{ timeline.resumo.ok }}</span>
                    <span class="timeline-stat-label">OK</span>
                  </div>
                </div>
              </div>

              <div v-for="(med, idx) in timeline.medicamentos" :key="idx" class="timeline-item mb-3">
                <div class="timeline-item-header d-flex align-items-center justify-content-between flex-wrap gap-2">
                  <div class="d-flex align-items-center gap-2">
                    <span class="badge" :class="getTimelineBadge(med.status)" style="width: 10px; height: 10px; padding: 0; border-radius: 50%;"></span>
                    <strong class="text-truncate" :title="med.produtoNome">{{ med.produtoNome }}</strong>
                    <span class="badge bg-light text-dark small">{{ med.categoria }}</span>
                  </div>
                  <div class="d-flex align-items-center gap-2">
                    <small class="text-muted">
                      <i class="fas fa-pills me-1"></i>{{ med.estoqueRestanteEstimado }} und.
                    </small>
                    <small class="text-muted">
                      <i class="fas fa-tachometer-alt me-1"></i>{{ med.consumoDiarioEstimado }}/dia
                    </small>
                    <span class="badge" :class="getStatusBadge(med.status)">
                      <i :class="getStatusIcon(med.status)" class="me-1"></i>{{ getStatusLabel(med.status) }}
                    </span>
                    <button v-if="!med.temAgendamentoAtivo && med.status !== 'ok'" class="btn btn-sm btn-outline-primary" @click="agendarRecompra(med)">
                      <i class="fas fa-calendar-plus me-1"></i>Agendar
                    </button>
                    <span v-else-if="med.temAgendamentoAtivo" class="badge bg-info-subtle text-info">
                      <i class="fas fa-check-circle me-1"></i>Agendado
                    </span>
                  </div>
                </div>

                <div class="timeline-bar-wrapper mt-2">
                  <div class="timeline-bar">
                    <div v-for="evt in med.historicoCompras" class="timeline-dot purchase-dot"
                      :style="{ left: getTimelineDotPosition(evt, med) + '%' }"
                      :title="evt.data + ' - ' + evt.quantidade + ' un. (R$ ' + evt.valor?.toFixed(2) + ')'">
                    </div>
                    <div class="timeline-fill" :class="'timeline-fill-' + med.status"
                      :style="{ width: getTimelineFillWidth(med) + '%' }"></div>
                    <div class="timeline-dot depletion-dot"
                      :class="'depletion-' + med.status"
                      :style="{ left: getDepletionDotPosition(med) + '%' }"
                      :title="'Previsão de esgotamento: ' + med.dataPrevisaoEsgotamento">
                      <i class="fas fa-exclamation-triangle"></i>
                    </div>
                  </div>
                  <div class="timeline-labels d-flex justify-content-between mt-1">
                    <small class="text-muted">{{ med.ultimaCompra }}</small>
                    <small class="fw-bold" :class="getStatusTextColor(med.status)">{{ med.dataPrevisaoEsgotamento }}</small>
                  </div>
                </div>

                <div v-if="med.recomendacao" class="timeline-recomendacao mt-1">
                  <small class="text-muted">
                    <i class="fas fa-lightbulb text-warning me-1"></i>{{ med.recomendacao }}
                  </small>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- CTA Card -->
        <div class="card border-0 shadow-sm mb-4 cta-card">
          <div class="card-body p-4 text-center">
            <h4 class="mb-2"><i class="fas fa-magic text-warning me-2"></i>Plano Inteligente de Saúde</h4>
            <p class="text-muted mb-3">Baseado no seu histórico, você pode economizar até <strong>{{ formatCurrency(dashboard.resumo.economiaEstimada) }}</strong> com compras programadas</p>
            <router-link to="/products" class="btn btn-primary btn-lg">
              <i class="fas fa-cart-plus me-2"></i>Repor Medicamentos
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api'
import { Chart, registerables } from 'chart.js'
import { mapState } from 'vuex'
Chart.register(...registerables)

export default {
  name: 'HealthDashboard',
  data() {
    return {
      dashboard: null,
      loading: false,
      error: null,
      charts: {},
      timeline: null,
      timelineLoading: false,
      timelineError: null
    }
  },
  computed: {
    ...mapState(['user']),
    usuarioId() {
      return this.user?.id || 1
    }
  },
  mounted() {
    this.carregarDados()
  },
  beforeUnmount() {
    Object.values(this.charts).forEach(chart => chart.destroy())
  },
  methods: {
    async carregarDados() {
      this.loading = true
      this.error = null
      try {
        const [dashboardResp, timelineResp] = await Promise.all([
          api.get(`/dashboard-saude/usuario/${this.usuarioId}`),
          api.get(`/dashboard-saude/timeline/${this.usuarioId}`)
        ])
        this.dashboard = dashboardResp.data
        this.timeline = timelineResp.data
        this.$nextTick(() => {
          this.renderCharts()
        })
      } catch (err) {
        console.error('Erro ao carregar dashboard:', err)
        this.error = 'Não foi possível carregar os dados. Tente novamente mais tarde.'
      } finally {
        this.loading = false
      }
    },
    renderCharts() {
      this.renderGastosMensais()
      this.renderCategoria()
      this.renderTopMedicamentos()
    },
    renderGastosMensais() {
      if (this.charts.gastosMensais) this.charts.gastosMensais.destroy()
      const ctx = this.$refs.gastosMensaisChart
      if (!ctx || !this.dashboard.gastosMensais.length) return

      const sorted = [...this.dashboard.gastosMensais].sort((a, b) => {
        const meses = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez']
        const [mA, yA] = a.mes.split('/')
        const [mB, yB] = b.mes.split('/')
        const dateA = meses.indexOf(mA.slice(0, 3)) + parseInt(yA) * 12
        const dateB = meses.indexOf(mB.slice(0, 3)) + parseInt(yB) * 12
        return dateA - dateB
      })

      this.charts.gastosMensais = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: sorted.map(g => g.mes),
          datasets: [{
            label: 'Gastos (R$)',
            data: sorted.map(g => g.valor),
            backgroundColor: 'rgba(13, 110, 253, 0.7)',
            borderColor: 'rgba(13, 110, 253, 1)',
            borderWidth: 1,
            borderRadius: 6,
            maxBarThickness: 50
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: { display: false },
            tooltip: {
              callbacks: {
                label: (ctx) => `R$ ${ctx.parsed.y.toFixed(2)}`
              }
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                callback: (val) => `R$ ${val}`
              }
            }
          }
        }
      })
    },
    renderCategoria() {
      if (this.charts.categoria) this.charts.categoria.destroy()
      const ctx = this.$refs.categoriaChart
      if (!ctx || !this.dashboard.distribuicaoCategoria.length) return

      const cores = [
        'rgba(13, 110, 253, 0.8)',
        'rgba(25, 135, 84, 0.8)',
        'rgba(255, 193, 7, 0.8)',
        'rgba(220, 53, 69, 0.8)',
        'rgba(111, 66, 193, 0.8)',
        'rgba(13, 202, 240, 0.8)'
      ]

      this.charts.categoria = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: this.dashboard.distribuicaoCategoria.map(c => c.categoria),
          datasets: [{
            data: this.dashboard.distribuicaoCategoria.map(c => c.valor),
            backgroundColor: cores.slice(0, this.dashboard.distribuicaoCategoria.length),
            borderWidth: 2,
            borderColor: '#fff'
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: {
              position: 'bottom',
              labels: { padding: 12, usePointStyle: true, font: { size: 11 } }
            },
            tooltip: {
              callbacks: {
                label: (ctx) => {
                  const item = this.dashboard.distribuicaoCategoria[ctx.dataIndex]
                  return ` ${item.categoria}: R$ ${item.valor.toFixed(2)} (${item.percentual}%)`
                }
              }
            }
          },
          cutout: '65%'
        }
      })
    },
    renderTopMedicamentos() {
      if (this.charts.topMedicamentos) this.charts.topMedicamentos.destroy()
      const ctx = this.$refs.topMedicamentosChart
      if (!ctx || !this.dashboard.topMedicamentos.length) return

      const cores = [
        'rgba(255, 193, 7, 0.8)',
        'rgba(108, 117, 125, 0.8)',
        'rgba(205, 133, 63, 0.8)',
        'rgba(13, 110, 253, 0.7)',
        'rgba(25, 135, 84, 0.7)'
      ]

      this.charts.topMedicamentos = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.dashboard.topMedicamentos.map(m => {
            const nome = m.nome.length > 18 ? m.nome.substring(0, 18) + '...' : m.nome
            return nome
          }),
          datasets: [{
            label: 'Qtd Comprada',
            data: this.dashboard.topMedicamentos.map(m => m.quantidadeComprada),
            backgroundColor: cores.slice(0, this.dashboard.topMedicamentos.length),
            borderRadius: 6,
            maxBarThickness: 40
          }]
        },
        options: {
          indexAxis: 'y',
          responsive: true,
          plugins: {
            legend: { display: false },
            tooltip: {
              callbacks: {
                title: (items) => this.dashboard.topMedicamentos[items[0].dataIndex].nome,
                label: (ctx) => [
                  `Qtd: ${ctx.parsed.x} unidades`,
                  `Gasto: R$ ${this.dashboard.topMedicamentos[ctx.dataIndex].totalGasto.toFixed(2)}`,
                  `Última: ${this.dashboard.topMedicamentos[ctx.dataIndex].ultimaCompra}`
                ]
              }
            }
          },
          scales: {
            x: { beginAtZero: true }
          }
        }
      })
    },
    formatCurrency(value) {
      if (!value) return 'R$ 0,00'
      return `R$ ${Number(value).toFixed(2).replace('.', ',')}`
    },
    getStatusBadge(status) {
      return {
        'bg-danger-subtle text-danger': status === 'critico',
        'bg-warning-subtle text-warning': status === 'atencao',
        'bg-success-subtle text-success': status === 'ok'
      }
    },
    getStatusColor(status) {
      return {
        'bg-danger': status === 'critico',
        'bg-warning': status === 'atencao',
        'bg-success': status === 'ok'
      }
    },
    getStatusTextColor(status) {
      return {
        'text-danger': status === 'critico',
        'text-warning': status === 'atencao',
        'text-success': status === 'ok'
      }
    },
    getStatusLabel(status) {
      return { critico: 'Crítico', atencao: 'Atenção', ok: 'OK' }[status] || status
    },
    getStatusIcon(status) {
      return { critico: 'fas fa-times-circle', atencao: 'fas fa-exclamation-triangle', ok: 'fas fa-check-circle' }[status] || ''
    },
    getBarWidth(dias) {
      return Math.min((dias / 30) * 100, 100)
    },
    async carregarTimeline() {
      this.timelineLoading = true
      this.timelineError = null
      try {
        const response = await api.get(`/dashboard-saude/timeline/${this.usuarioId}`)
        this.timeline = response.data
      } catch (err) {
        console.error('Erro ao carregar timeline:', err)
        this.timelineError = 'Não foi possível calcular a timeline preditiva.'
      } finally {
        this.timelineLoading = false
      }
    },
    async agendarRecompra(med) {
      if (!med || !med.produtoId) return
      try {
        await api.post('/recompra', {
          usuarioId: this.usuarioId,
          produtoId: med.produtoId,
          posologiaTexto: 'Uso contínuo - 1 unidade por dia (estimado)',
          diasDuracao: 30,
          status: 'PENDENTE'
        })
        med.temAgendamentoAtivo = true
      } catch (err) {
        console.error('Erro ao agendar recompra:', err)
        this.timelineError = 'Erro ao agendar recompra. Tente novamente.'
      }
    },
    getTimelineBadge(status) {
      return { critico: 'bg-danger', atencao: 'bg-warning', ok: 'bg-success' }[status] || 'bg-secondary'
    },
    getTimelineDotPosition(evt, med) {
      if (!med.historicoCompras || med.historicoCompras.length < 2) return 10
      const dates = med.historicoCompras.map(e => new Date(e.data.split('/').reverse().join('-')))
      const min = new Date(Math.min(...dates))
      const max = new Date(Math.max(...dates))
      const range = max - min || 1
      const evtDate = new Date(evt.data.split('/').reverse().join('-'))
      return ((evtDate - min) / range) * 80 + 10
    },
    getTimelineFillWidth(med) {
      const total = med.diasAteEsgotar + (med.historicoCompras ? med.historicoCompras.length * 5 : 30)
      const maxWidth = 80
      const fill = total > 0 ? (med.diasAteEsgotar / Math.max(total, 30)) * maxWidth : 0
      return Math.min(fill, maxWidth)
    },
    getDepletionDotPosition(med) {
      const timelineEnd = med.diasAteEsgotar + 90
      return timelineEnd > 0 ? (med.diasAteEsgotar / Math.max(timelineEnd, 30)) * 90 + 10 : 90
    }
  }
}
</script>

<style scoped>
.health-dashboard {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  min-height: 100vh;
}

.dashboard-header {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.stat-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border-radius: 12px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1) !important;
}

.stat-icon {
  width: 48px;
  height: 48px;
}

.card {
  border-radius: 12px;
}

.card-header {
  border-radius: 12px 12px 0 0 !important;
}

.cta-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e7f1ff 100%);
  border: 1px solid rgba(13, 110, 253, 0.15) !important;
}

.table tbody tr {
  transition: background-color 0.15s ease;
}

.progress {
  background-color: #e9ecef;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.spin {
  animation: spin 1s linear infinite;
}

.timeline-stat {
  text-align: center;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.timeline-stat-number {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.2;
}

.timeline-stat-label {
  display: block;
  font-size: 0.75rem;
  color: #6c757d;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.timeline-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  border: 1px solid #e9ecef;
  transition: box-shadow 0.15s ease;
}

.timeline-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.timeline-bar-wrapper {
  position: relative;
}

.timeline-bar {
  position: relative;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: visible;
}

.timeline-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.timeline-fill-critico {
  background: linear-gradient(90deg, #dc3545, #ff6b6b);
}

.timeline-fill-atencao {
  background: linear-gradient(90deg, #ffc107, #ffd43b);
}

.timeline-fill-ok {
  background: linear-gradient(90deg, #198754, #51cf66);
}

.timeline-dot {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
  border-radius: 50%;
}

.purchase-dot {
  width: 12px;
  height: 12px;
  background: #fff;
  border: 2px solid #0d6efd;
  cursor: help;
  transition: transform 0.15s ease;
}

.purchase-dot:hover {
  transform: translate(-50%, -50%) scale(1.5);
}

.depletion-dot {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.6rem;
  cursor: help;
}

.depletion-critico {
  background: #dc3545;
  color: #fff;
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px #dc3545;
}

.depletion-atencao {
  background: #ffc107;
  color: #000;
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px #ffc107;
}

.depletion-ok {
  background: #198754;
  color: #fff;
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px #198754;
}

.timeline-labels {
  padding: 0 2px;
}

.timeline-recomendacao {
  background: #fff;
  border-radius: 4px;
  padding: 0.25rem 0.5rem;
  font-size: 0.8rem;
}

@media (max-width: 768px) {
  .stat-card h3 {
    font-size: 1.25rem;
  }
  .stat-card h6 {
    font-size: 0.85rem;
  }
  .timeline-stat-number {
    font-size: 1.1rem;
  }
}
</style>
