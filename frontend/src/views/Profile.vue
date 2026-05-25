<template>
  <div class="profile-page">
    <div class="container py-4">
      <!-- Header -->
      <div class="profile-header mb-4">
        <div class="d-flex align-items-center gap-3">
          <div class="profile-avatar">
            <span>{{ userInitials }}</span>
          </div>
          <div>
            <h2 class="h4 mb-1">{{ user?.name || 'Usuário' }}</h2>
            <p class="text-muted mb-0">{{ user?.email || '' }}</p>
          </div>
        </div>
      </div>

      <!-- Tabs de navegação -->
      <div class="profile-tabs mb-4">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="activeSection = tab.id"
          class="tab-btn"
          :class="{ active: activeSection === tab.id }"
        >
          <i :class="tab.icon" class="me-1"></i>
          <span>{{ tab.label }}</span>
        </button>
      </div>

      <!-- Conteúdo -->
      <div class="profile-content">
        <!-- INFORMAÇÕES PESSOAIS -->
        <div v-show="activeSection === 'personal'" class="content-section">
          <div class="row g-4">
            <div class="col-lg-8">
              <div class="card border-0 shadow-sm">
                <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
                  <h5 class="mb-0"><i class="fas fa-user-edit me-2 text-primary"></i>Informações Pessoais</h5>
                  <button
                    @click="editMode.personal = !editMode.personal"
                    class="btn btn-sm"
                    :class="editMode.personal ? 'btn-secondary' : 'btn-outline-primary'"
                  >
                    {{ editMode.personal ? 'Cancelar' : 'Editar' }}
                  </button>
                </div>
                <div class="card-body">
                  <form @submit.prevent="updateProfile">
                    <div class="row g-3">
                      <div class="col-md-6">
                        <label class="form-label small text-muted">Nome completo</label>
                        <input v-model="profile.name" type="text" class="form-control" :disabled="!editMode.personal">
                      </div>
                      <div class="col-md-6">
                        <label class="form-label small text-muted">Email</label>
                        <input v-model="profile.email" type="email" class="form-control" :disabled="!editMode.personal">
                      </div>
                      <div class="col-md-6">
                        <label class="form-label small text-muted">Telefone</label>
                        <input v-model="profile.phone" type="tel" class="form-control" :disabled="!editMode.personal">
                      </div>
                      <div class="col-md-6">
                        <label class="form-label small text-muted">Data de Nascimento</label>
                        <input v-model="profile.birthDate" type="date" class="form-control" :disabled="!editMode.personal">
                      </div>
                      <div class="col-12">
                        <label class="form-label small text-muted">CPF</label>
                        <input v-model="profile.cpf" type="text" class="form-control" :disabled="!editMode.personal">
                      </div>
                    </div>
                    <div v-show="editMode.personal" class="d-flex gap-2 mt-3">
                      <button type="submit" class="btn btn-success btn-sm">
                        <i class="fas fa-save me-1"></i>Salvar
                      </button>
                      <button type="button" @click="editMode.personal = false" class="btn btn-secondary btn-sm">Cancelar</button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
            <div class="col-lg-4">
              <div class="card border-0 shadow-sm mb-3">
                <div class="card-header bg-white py-3">
                  <h6 class="mb-0"><i class="fas fa-chart-bar me-2 text-primary"></i>Resumo</h6>
                </div>
                <div class="card-body">
                  <p class="mb-2 small"><strong>Membro desde:</strong> {{ memberSince }}</p>
                  <p class="mb-2 small"><strong>Pedidos:</strong> {{ dashboard?.resumo?.totalPedidos || totalOrders }}</p>
                  <p class="mb-0 small"><strong>Endereços:</strong> {{ addresses.length }}</p>
                </div>
              </div>
              <div class="card border-0 shadow-sm">
                <div class="card-header bg-white py-3">
                  <h6 class="mb-0"><i class="fas fa-bolt me-2 text-warning"></i>Ações Rápidas</h6>
                </div>
                <div class="card-body p-2">
                  <router-link to="/orders" class="btn btn-outline-primary btn-sm w-100 mb-2 text-start">
                    <i class="fas fa-box me-1"></i>Meus Pedidos
                  </router-link>
                  <router-link to="/prescriptions" class="btn btn-outline-primary btn-sm w-100 text-start">
                    <i class="fas fa-file-medical me-1"></i>Minhas Receitas
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- MINHA SAÚDE -->
        <div v-show="activeSection === 'saude'" class="content-section">
          <!-- Loading -->
          <div v-if="saudeLoading && !dashboard" class="text-center py-5">
            <div class="spinner-border text-primary mb-3"></div>
            <p class="text-muted">Carregando seus dados de saúde...</p>
          </div>

          <!-- Error -->
          <div v-else-if="saudeError" class="alert alert-danger">
            <i class="fas fa-exclamation-triangle me-2"></i>{{ saudeError }}
            <button class="btn btn-sm btn-outline-danger ms-2" @click="carregarSaude">Tentar novamente</button>
          </div>

          <!-- Sem dados -->
          <div v-else-if="!dashboard || !dashboard.resumo || dashboard.resumo.totalPedidos === 0" class="text-center py-5">
            <i class="fas fa-heartbeat fa-3x text-muted mb-3"></i>
            <h5 class="text-muted">Nenhum dado de saúde disponível</h5>
            <p class="text-muted small">Seus dados aparecerão aqui após realizar pedidos.</p>
            <router-link to="/products" class="btn btn-primary">
              <i class="fas fa-shopping-cart me-1"></i>Ver Produtos
            </router-link>
          </div>

          <!-- Dashboard completo -->
          <div v-else>
            <!-- Cards resumo -->
            <div class="row g-3 mb-4">
              <div class="col-6 col-lg-3">
                <div class="card stat-card border-0 shadow-sm h-100">
                  <div class="card-body">
                    <div class="stat-icon bg-primary bg-opacity-10 text-primary">
                      <i class="fas fa-dollar-sign"></i>
                    </div>
                    <p class="text-muted mb-1 small">Total Gasto</p>
                    <h3 class="mb-0 fw-bold text-primary">{{ formatCurrency(dashboard.resumo.totalGasto) }}</h3>
                  </div>
                </div>
              </div>
              <div class="col-6 col-lg-3">
                <div class="card stat-card border-0 shadow-sm h-100">
                  <div class="card-body">
                    <div class="stat-icon bg-success bg-opacity-10 text-success">
                      <i class="fas fa-shopping-bag"></i>
                    </div>
                    <p class="text-muted mb-1 small">Total Pedidos</p>
                    <h3 class="mb-0 fw-bold text-success">{{ dashboard.resumo.totalPedidos }}</h3>
                  </div>
                </div>
              </div>
              <div class="col-6 col-lg-3">
                <div class="card stat-card border-0 shadow-sm h-100">
                  <div class="card-body">
                    <div class="stat-icon bg-warning bg-opacity-10 text-warning">
                      <i class="fas fa-capsules"></i>
                    </div>
                    <p class="text-muted mb-1 small">Mais Comprado</p>
                    <h6 class="mb-0 fw-bold text-warning text-truncate" :title="dashboard.resumo.medicamentoFrequente">{{ dashboard.resumo.medicamentoFrequente }}</h6>
                  </div>
                </div>
              </div>
              <div class="col-6 col-lg-3">
                <div class="card stat-card border-0 shadow-sm h-100">
                  <div class="card-body">
                    <div class="stat-icon bg-info bg-opacity-10 text-info">
                      <i class="fas fa-piggy-bank"></i>
                    </div>
                    <p class="text-muted mb-1 small">Economia Est.</p>
                    <h3 class="mb-0 fw-bold text-info">{{ formatCurrency(dashboard.resumo.economiaEstimada) }}</h3>
                  </div>
                </div>
              </div>
            </div>

            <!-- Gráficos: Gastos + Categorias -->
            <div class="row g-3 mb-4">
              <div class="col-lg-8">
                <div class="card border-0 shadow-sm h-100">
                  <div class="card-header bg-white py-3">
                    <h5 class="mb-0"><i class="fas fa-chart-line me-2 text-primary"></i>Gastos Mensais</h5>
                  </div>
                  <div class="card-body">
                    <canvas ref="gastosChart" height="120"></canvas>
                  </div>
                </div>
              </div>
              <div class="col-lg-4">
                <div class="card border-0 shadow-sm h-100">
                  <div class="card-header bg-white py-3">
                    <h5 class="mb-0"><i class="fas fa-chart-pie me-2 text-primary"></i>Por Categoria</h5>
                  </div>
                  <div class="card-body d-flex align-items-center justify-content-center">
                    <canvas ref="categoriaChart" height="200"></canvas>
                  </div>
                </div>
              </div>
            </div>

            <!-- Top Medicamentos + Reposição -->
            <div class="row g-3 mb-4">
              <div class="col-lg-5">
                <div class="card border-0 shadow-sm h-100">
                  <div class="card-header bg-white py-3">
                    <h5 class="mb-0"><i class="fas fa-trophy me-2 text-warning"></i>Top Medicamentos</h5>
                  </div>
                  <div class="card-body">
                    <canvas ref="topMedChart" height="180"></canvas>
                  </div>
                </div>
              </div>
              <div class="col-lg-7">
                <div class="card border-0 shadow-sm h-100">
                  <div class="card-header bg-white py-3">
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
                            <th>Dias Rest.</th>
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
                                <div class="progress flex-grow-1" style="height: 6px; width: 50px;">
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

            <!-- CTA -->
            <div class="card border-0 shadow-sm cta-card mb-4">
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

        <!-- ENDEREÇOS -->
        <div v-show="activeSection === 'addresses'" class="content-section">
          <div class="card border-0 shadow-sm">
            <div class="card-header bg-white py-3">
              <h5 class="mb-0"><i class="fas fa-map-marker-alt me-2 text-primary"></i>Meus Endereços</h5>
            </div>
            <div class="card-body">
              <div v-if="addresses.length === 0" class="text-center py-4">
                <i class="fas fa-map-marked-alt fa-2x text-muted mb-3"></i>
                <p class="text-muted">Nenhum endereço cadastrado.</p>
                <button class="btn btn-primary btn-sm" @click="showAddAddressForm = true">
                  <i class="fas fa-plus me-1"></i>Adicionar Endereço
                </button>
              </div>
              <div v-else>
                <div v-for="address in addresses" :key="address.id" class="address-item border rounded p-3 mb-3">
                  <div class="d-flex justify-content-between align-items-start">
                    <div>
                      <h6 class="mb-1">{{ address.nickname }}</h6>
                      <p class="mb-1 small text-muted">{{ address.street }}, {{ address.number }} - {{ address.neighborhood }}</p>
                      <p class="mb-0 small text-muted">{{ address.city }}/{{ address.state }} · CEP: {{ address.zipCode }}</p>
                      <span v-if="address.isDefault" class="badge bg-primary mt-1">Principal</span>
                    </div>
                    <div class="d-flex gap-1">
                      <button class="btn btn-sm btn-outline-primary">Editar</button>
                      <button class="btn btn-sm btn-outline-danger">Excluir</button>
                    </div>
                  </div>
                </div>
                <button v-show="!showAddAddressForm" class="btn btn-outline-primary btn-sm" @click="showAddAddressForm = true">
                  <i class="fas fa-plus me-1"></i>Novo Endereço
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- SEGURANÇA -->
        <div v-show="activeSection === 'security'" class="content-section">
          <div class="row g-4">
            <div class="col-lg-8">
              <div class="card border-0 shadow-sm">
                <div class="card-header bg-white py-3">
                  <h5 class="mb-0"><i class="fas fa-lock me-2 text-primary"></i>Alterar Senha</h5>
                </div>
                <div class="card-body">
                  <form @submit.prevent="changePassword" class="row g-3">
                    <div class="col-12">
                      <label class="form-label small text-muted">Senha Atual</label>
                      <input v-model="passwordForm.currentPassword" type="password" class="form-control" required>
                    </div>
                    <div class="col-md-6">
                      <label class="form-label small text-muted">Nova Senha</label>
                      <input v-model="passwordForm.newPassword" type="password" class="form-control" required>
                    </div>
                    <div class="col-md-6">
                      <label class="form-label small text-muted">Confirmar Senha</label>
                      <input v-model="passwordForm.confirmPassword" type="password" class="form-control" required>
                    </div>
                    <div class="col-12">
                      <button type="submit" class="btn btn-primary btn-sm">
                        <i class="fas fa-key me-1"></i>Alterar Senha
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
            <div class="col-lg-4">
              <div class="card border-0 shadow-sm">
                <div class="card-header bg-white py-3">
                  <h6 class="mb-0"><i class="fas fa-shield-alt me-2 text-success"></i>Sessões</h6>
                </div>
                <div class="card-body">
                  <p class="small text-muted mb-2">Você está logado neste dispositivo.</p>
                  <button class="btn btn-outline-danger btn-sm w-100">
                    <i class="fas fa-sign-out-alt me-1"></i>Encerrar Outras Sessões
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import api from '@/services/api'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

export default {
  name: 'Profile',
  data() {
    return {
      activeSection: 'personal',
      tabs: [
        { id: 'personal', label: 'Perfil', icon: 'fas fa-user' },
        { id: 'saude', label: 'Minha Saúde', icon: 'fas fa-heartbeat' },
        { id: 'addresses', label: 'Endereços', icon: 'fas fa-map-marker-alt' },
        { id: 'security', label: 'Segurança', icon: 'fas fa-lock' }
      ],
      editMode: { personal: false },
      showAddAddressForm: false,
      profile: { name: '', email: '', phone: '', birthDate: '', cpf: '' },
      addresses: [
        { id: 1, nickname: 'Casa', street: 'Rua das Flores', number: '123', neighborhood: 'Centro', city: 'São Paulo', state: 'SP', zipCode: '01234-567', isDefault: true }
      ],
      passwordForm: { currentPassword: '', newPassword: '', confirmPassword: '' },
      memberSince: 'Jan 2024',
      totalOrders: 5,
      // Saúde
      dashboard: null,
      saudeLoading: false,
      saudeError: null,
      charts: {}
    }
  },
  computed: {
    ...mapState(['user']),
    userInitials() {
      const name = this.user?.name || ''
      return name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase() || 'U'
    },
    usuarioId() {
      return this.user?.id || 1
    }
  },
  watch: {
    activeSection(newVal) {
      if (newVal === 'saude' && !this.dashboard && !this.saudeLoading) {
        this.carregarSaude()
      }
    }
  },
  mounted() {
    if (this.user) {
      this.profile.name = this.user.name || ''
      this.profile.email = this.user.email || ''
    }
    const savedSection = sessionStorage.getItem('profileSection')
    if (savedSection && this.tabs.find(t => t.id === savedSection)) {
      this.activeSection = savedSection
      sessionStorage.removeItem('profileSection')
    }
    if (this.activeSection === 'saude') {
      this.carregarSaude()
    }
  },
  beforeUnmount() {
    Object.values(this.charts).forEach(c => c.destroy())
  },
  methods: {
    async carregarSaude() {
      this.saudeLoading = true
      this.saudeError = null
      try {
        const response = await api.get(`/dashboard-saude/usuario/${this.usuarioId}`)
        this.dashboard = response.data
        this.$nextTick(() => {
          this.renderCharts()
        })
      } catch (err) {
        console.error('Erro ao carregar saúde:', err)
        this.saudeError = 'Não foi possível carregar os dados.'
      } finally {
        this.saudeLoading = false
      }
    },
    renderCharts() {
      if (!this.dashboard) return
      this.renderGastos()
      this.renderCategoria()
      this.renderTopMed()
    },
    renderGastos() {
      if (this.charts.gastos) this.charts.gastos.destroy()
      const ctx = this.$refs.gastosChart
      if (!ctx || !this.dashboard.gastosMensais?.length) return
      const meses = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez']
      const sorted = [...this.dashboard.gastosMensais].sort((a, b) => {
        const [mA, yA] = a.mes.split('/')
        const [mB, yB] = b.mes.split('/')
        return (meses.indexOf(mA.slice(0, 3)) + parseInt(yA) * 12) - (meses.indexOf(mB.slice(0, 3)) + parseInt(yB) * 12)
      })
      this.charts.gastos = new Chart(ctx, {
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
          plugins: { legend: { display: false }, tooltip: { callbacks: { label: ctx => `R$ ${ctx.parsed.y.toFixed(2)}` } } },
          scales: { y: { beginAtZero: true, ticks: { callback: v => `R$ ${v}` } } }
        }
      })
    },
    renderCategoria() {
      if (this.charts.categoria) this.charts.categoria.destroy()
      const ctx = this.$refs.categoriaChart
      if (!ctx || !this.dashboard.distribuicaoCategoria?.length) return
      const cores = ['rgba(13,110,253,0.8)', 'rgba(25,135,84,0.8)', 'rgba(255,193,7,0.8)', 'rgba(220,53,69,0.8)', 'rgba(111,66,193,0.8)', 'rgba(13,202,240,0.8)']
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
            legend: { position: 'bottom', labels: { padding: 12, usePointStyle: true, font: { size: 11 } } },
            tooltip: { callbacks: { label: ctx => { const i = this.dashboard.distribuicaoCategoria[ctx.dataIndex]; return ` ${i.categoria}: R$ ${i.valor.toFixed(2)} (${i.percentual}%)` } } }
          },
          cutout: '65%'
        }
      })
    },
    renderTopMed() {
      if (this.charts.topMed) this.charts.topMed.destroy()
      const ctx = this.$refs.topMedChart
      if (!ctx || !this.dashboard.topMedicamentos?.length) return
      const cores = ['rgba(255,193,7,0.8)', 'rgba(108,117,125,0.8)', 'rgba(205,133,63,0.8)', 'rgba(13,110,253,0.7)', 'rgba(25,135,84,0.7)']
      this.charts.topMed = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.dashboard.topMedicamentos.map(m => m.nome.length > 18 ? m.nome.substring(0, 18) + '...' : m.nome),
          datasets: [{
            label: 'Qtd',
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
            tooltip: { callbacks: { title: items => this.dashboard.topMedicamentos[items[0].dataIndex].nome, label: ctx => [`Qtd: ${ctx.parsed.x}`, `Gasto: R$ ${this.dashboard.topMedicamentos[ctx.dataIndex].totalGasto.toFixed(2)}`, `Última: ${this.dashboard.topMedicamentos[ctx.dataIndex].ultimaCompra}`] } }
          },
          scales: { x: { beginAtZero: true } }
        }
      })
    },
    formatCurrency(value) {
      return value ? `R$ ${Number(value).toFixed(2).replace('.', ',')}` : 'R$ 0,00'
    },
    getStatusBadge(s) {
      return { critico: 'bg-danger bg-opacity-10 text-danger', atencao: 'bg-warning bg-opacity-10 text-warning', ok: 'bg-success bg-opacity-10 text-success' }[s] || ''
    },
    getStatusColor(s) {
      return { critico: 'bg-danger', atencao: 'bg-warning', ok: 'bg-success' }[s] || ''
    },
    getStatusTextColor(s) {
      return { critico: 'text-danger', atencao: 'text-warning', ok: 'text-success' }[s] || ''
    },
    getStatusLabel(s) {
      return { critico: 'Crítico', atencao: 'Atenção', ok: 'OK' }[s] || s
    },
    getStatusIcon(s) {
      return { critico: 'fas fa-times-circle', atencao: 'fas fa-exclamation-triangle', ok: 'fas fa-check-circle' }[s] || ''
    },
    getBarWidth(d) { return Math.min((d / 30) * 100, 100) },
    updateProfile() {
      alert('Perfil atualizado com sucesso!')
      this.editMode.personal = false
    },
    changePassword() {
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) { alert('As senhas não coincidem!'); return }
      alert('Senha alterada com sucesso!')
      this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' }
    }
  }
}
</script>

<style scoped>
.profile-page {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  min-height: 100vh;
}

.profile-header {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.profile-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0d6efd, #0dcaf0);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  font-weight: 600;
}

/* Tabs */
.profile-tabs {
  display: flex;
  gap: 0.5rem;
  background: white;
  border-radius: 12px;
  padding: 0.5rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow-x: auto;
}

.tab-btn {
  flex: 1;
  min-width: 120px;
  padding: 0.7rem 1rem;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #6c757d;
  font-weight: 500;
  font-size: 0.85rem;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
}

.tab-btn:hover {
  background: #f8f9fa;
  color: #0d6efd;
}

.tab-btn.active {
  background: #0d6efd;
  color: white;
}

/* Stat Cards */
.stat-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border-radius: 12px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1) !important;
}

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 0.75rem;
}

.card {
  border-radius: 12px;
}

.cta-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e7f1ff 100%);
  border: 1px solid rgba(13, 110, 253, 0.15) !important;
}

.address-item {
  transition: background-color 0.2s;
}

.address-item:hover {
  background-color: #f8f9fa;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.spin {
  animation: spin 1s linear infinite;
}

@media (max-width: 768px) {
  .profile-tabs {
    flex-wrap: nowrap;
  }
  .tab-btn {
    min-width: 100px;
    padding: 0.5rem 0.75rem;
    font-size: 0.78rem;
  }
  .stat-card h3 {
    font-size: 1.1rem;
  }
}
</style>
