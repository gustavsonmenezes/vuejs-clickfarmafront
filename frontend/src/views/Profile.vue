<template>
  <div class="profile-page">
    <div class="container py-4">
      <!-- Header com capa -->
      <div class="profile-cover mb-4">
        <div class="cover-gradient"></div>
        <div class="cover-content">
          <div class="d-flex align-items-center gap-4 flex-wrap">
            <div class="profile-avatar">
              <span>{{ userInitials }}</span>
            </div>
            <div class="profile-info">
              <h2 class="mb-1">{{ user?.name || 'Usuário' }}</h2>
              <p class="mb-0 opacity-75">{{ user?.email || '' }}</p>
              <div class="d-flex gap-3 mt-2">
                <span class="cover-badge"><i class="fas fa-calendar me-1"></i>Desde {{ memberSince }}</span>
                <span class="cover-badge"><i class="fas fa-box me-1"></i>{{ dashboard?.resumo?.totalPedidos || totalOrders }} pedidos</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="profile-tabs mb-4">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="activeSection = tab.id"
          class="tab-btn"
          :class="{ active: activeSection === tab.id }"
        >
          <i :class="tab.icon"></i>
          <span>{{ tab.label }}</span>
        </button>
      </div>

      <!-- CONTEÚDO -->
      <div class="profile-content">
        <transition name="fade" mode="out-in">
          <!-- PERFIL -->
          <div v-if="activeSection === 'personal'" key="personal" class="content-section">
            <div class="row g-4">
              <div class="col-lg-8">
                <div class="card cf-card">
                  <div class="card-header cf-card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0"><i class="fas fa-user-edit me-2 text-primary"></i>Informações Pessoais</h5>
                    <button @click="editMode.personal = !editMode.personal" class="btn-edit" :class="{ editing: editMode.personal }">
                      <i :class="editMode.personal ? 'fas fa-times' : 'fas fa-pen'"></i>
                      {{ editMode.personal ? 'Cancelar' : 'Editar' }}
                    </button>
                  </div>
                  <div class="card-body">
                    <form @submit.prevent="updateProfile">
                      <div class="row g-3">
                        <div class="col-md-6">
                          <label class="cf-label">Nome completo</label>
                          <input v-model="profile.name" type="text" class="cf-input" :disabled="!editMode.personal">
                        </div>
                        <div class="col-md-6">
                          <label class="cf-label">Email</label>
                          <input v-model="profile.email" type="email" class="cf-input" :disabled="!editMode.personal">
                        </div>
                        <div class="col-md-6">
                          <label class="cf-label">Telefone</label>
                          <input v-model="profile.phone" type="tel" class="cf-input" :disabled="!editMode.personal">
                        </div>
                        <div class="col-md-6">
                          <label class="cf-label">Data de Nascimento</label>
                          <input v-model="profile.birthDate" type="date" class="cf-input" :disabled="!editMode.personal">
                        </div>
                        <div class="col-12">
                          <label class="cf-label">CPF</label>
                          <input v-model="profile.cpf" type="text" class="cf-input" :disabled="!editMode.personal">
                        </div>
                      </div>
                      <transition name="slide">
                        <div v-show="editMode.personal" class="d-flex gap-2 mt-4">
                          <button type="submit" class="cf-btn cf-btn-primary">
                            <i class="fas fa-check me-1"></i>Salvar Alterações
                          </button>
                          <button type="button" @click="editMode.personal = false" class="cf-btn cf-btn-secondary">
                            Cancelar
                          </button>
                        </div>
                      </transition>
                    </form>
                  </div>
                </div>
              </div>
              <div class="col-lg-4">
                <div class="card cf-card mb-3">
                  <div class="card-header cf-card-header">
                    <h6 class="mb-0"><i class="fas fa-chart-bar me-2 text-primary"></i>Resumo</h6>
                  </div>
                  <div class="card-body">
                    <div class="resumo-item">
                      <span class="resumo-label">Membro desde</span>
                      <span class="resumo-value">{{ memberSince }}</span>
                    </div>
                    <div class="resumo-item">
                      <span class="resumo-label">Pedidos realizados</span>
                      <span class="resumo-value">{{ dashboard?.resumo?.totalPedidos || totalOrders }}</span>
                    </div>
                    <div class="resumo-item">
                      <span class="resumo-label">Endereços salvos</span>
                      <span class="resumo-value">{{ addresses.length }}</span>
                    </div>
                  </div>
                </div>
                <div class="card cf-card">
                  <div class="card-header cf-card-header">
                    <h6 class="mb-0"><i class="fas fa-bolt me-2 text-warning"></i>Ações Rápidas</h6>
                  </div>
                  <div class="card-body p-3">
                    <router-link to="/orders" class="quick-action-btn">
                      <i class="fas fa-box text-primary"></i>
                      <span>Meus Pedidos</span>
                      <i class="fas fa-chevron-right"></i>
                    </router-link>
                    <router-link to="/prescriptions" class="quick-action-btn">
                      <i class="fas fa-file-medical text-success"></i>
                      <span>Minhas Receitas</span>
                      <i class="fas fa-chevron-right"></i>
                    </router-link>
                    <router-link to="/minha-saude" class="quick-action-btn">
                      <i class="fas fa-heartbeat text-danger"></i>
                      <span>Painel de Saúde</span>
                      <i class="fas fa-chevron-right"></i>
                    </router-link>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- MINHA SAÚDE -->
          <div v-else-if="activeSection === 'saude'" key="saude" class="content-section">
            <div v-if="saudeLoading && !dashboard" class="text-center py-5">
              <div class="spinner-border text-primary mb-3" role="status" style="width: 3rem; height: 3rem;"></div>
              <p class="text-muted">Carregando seus dados de saúde...</p>
            </div>
            <div v-else-if="saudeError" class="alert alert-danger d-flex align-items-center gap-2">
              <i class="fas fa-exclamation-triangle"></i>
              <span class="flex-grow-1">{{ saudeError }}</span>
              <button class="btn btn-sm btn-outline-danger" @click="carregarSaude">Tentar novamente</button>
            </div>
            <div v-else-if="!dashboard || !dashboard.resumo || dashboard.resumo.totalPedidos === 0" class="text-center py-5">
              <div class="empty-illustration">
                <i class="fas fa-heartbeat"></i>
              </div>
              <h5 class="text-muted mt-3">Nenhum dado de saúde disponível</h5>
              <p class="text-muted small">Seus dados aparecerão aqui após realizar pedidos.</p>
              <router-link to="/products" class="cf-btn cf-btn-primary mt-2">
                <i class="fas fa-shopping-cart me-1"></i>Ver Produtos
              </router-link>
            </div>
            <div v-else>
              <!-- Stat Cards -->
              <div class="row g-3 mb-4">
                <div class="col-6 col-lg-3">
                  <div class="stat-card-modern gradient-blue">
                    <div class="stat-icon-wrap">
                      <i class="fas fa-dollar-sign"></i>
                    </div>
                    <div class="stat-info">
                      <span class="stat-label">Total Gasto</span>
                      <span class="stat-value">{{ formatCurrency(dashboard.resumo.totalGasto) }}</span>
                    </div>
                  </div>
                </div>
                <div class="col-6 col-lg-3">
                  <div class="stat-card-modern gradient-green">
                    <div class="stat-icon-wrap">
                      <i class="fas fa-shopping-bag"></i>
                    </div>
                    <div class="stat-info">
                      <span class="stat-label">Total Pedidos</span>
                      <span class="stat-value">{{ dashboard.resumo.totalPedidos }}</span>
                    </div>
                  </div>
                </div>
                <div class="col-6 col-lg-3">
                  <div class="stat-card-modern gradient-orange">
                    <div class="stat-icon-wrap">
                      <i class="fas fa-capsules"></i>
                    </div>
                    <div class="stat-info">
                      <span class="stat-label">Mais Comprado</span>
                      <span class="stat-value text-truncate" :title="dashboard.resumo.medicamentoFrequente" style="font-size: 0.9rem;">{{ dashboard.resumo.medicamentoFrequente }}</span>
                    </div>
                  </div>
                </div>
                <div class="col-6 col-lg-3">
                  <div class="stat-card-modern gradient-teal">
                    <div class="stat-icon-wrap">
                      <i class="fas fa-piggy-bank"></i>
                    </div>
                    <div class="stat-info">
                      <span class="stat-label">Economia Est.</span>
                      <span class="stat-value">{{ formatCurrency(dashboard.resumo.economiaEstimada) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Charts Row -->
              <div class="row g-3 mb-4">
                <div class="col-lg-8">
                  <div class="card cf-card h-100">
                    <div class="card-header cf-card-header">
                      <h5 class="mb-0"><i class="fas fa-chart-line me-2 text-primary"></i>Gastos Mensais</h5>
                    </div>
                    <div class="card-body">
                      <canvas ref="gastosChart" height="120"></canvas>
                    </div>
                  </div>
                </div>
                <div class="col-lg-4">
                  <div class="card cf-card h-100">
                    <div class="card-header cf-card-header">
                      <h5 class="mb-0"><i class="fas fa-chart-pie me-2 text-primary"></i>Por Categoria</h5>
                    </div>
                    <div class="card-body d-flex align-items-center justify-content-center">
                      <canvas ref="categoriaChart" height="200"></canvas>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Top Med + Previsão -->
              <div class="row g-3 mb-4">
                <div class="col-lg-5">
                  <div class="card cf-card h-100">
                    <div class="card-header cf-card-header">
                      <h5 class="mb-0"><i class="fas fa-trophy me-2 text-warning"></i>Top Medicamentos</h5>
                    </div>
                    <div class="card-body">
                      <canvas ref="topMedChart" height="180"></canvas>
                    </div>
                  </div>
                </div>
                <div class="col-lg-7">
                  <div class="card cf-card h-100">
                    <div class="card-header cf-card-header">
                      <h5 class="mb-0"><i class="fas fa-clock me-2 text-danger"></i>Previsão de Reposição</h5>
                    </div>
                    <div class="card-body p-0">
                      <div class="table-responsive">
                        <table class="cf-table">
                          <thead>
                            <tr>
                              <th>Produto</th>
                              <th>Categoria</th>
                              <th>Última Compra</th>
                              <th>Dias Rest.</th>
                              <th>Status</th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr v-for="(item, idx) in dashboard.previsaoReposicao" :key="idx">
                              <td class="fw-medium">{{ item.produto }}</td>
                              <td><span class="badge-tag">{{ item.categoria }}</span></td>
                              <td class="text-muted">{{ item.ultimaCompra }}</td>
                              <td>
                                <div class="d-flex align-items-center gap-2">
                                  <div class="progress-bar-mini">
                                    <div class="progress-fill" :class="'fill-' + item.status" :style="{ width: getBarWidth(item.diasRestantes) + '%' }"></div>
                                  </div>
                                  <span class="small fw-bold" :class="getStatusTextColor(item.status)">{{ item.diasRestantes }}d</span>
                                </div>
                              </td>
                              <td>
                                <span class="status-badge" :class="'status-' + item.status">
                                  <i :class="getStatusIcon(item.status)"></i>
                                  {{ getStatusLabel(item.status) }}
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

              <!-- Timeline Preditiva -->
              <div class="card cf-card mb-4">
                <div class="card-header cf-card-header d-flex align-items-center justify-content-between flex-wrap gap-2">
                  <h5 class="mb-0"><i class="fas fa-chart-bar me-2 text-primary"></i>Linha do Tempo Preditiva</h5>
                  <div class="d-flex align-items-center gap-3">
                    <span class="legend-dots">
                      <span class="dot dot-danger"></span>Crítico
                      <span class="dot dot-warning ms-2"></span>Atenção
                      <span class="dot dot-success ms-2"></span>OK
                    </span>
                    <button class="cf-btn cf-btn-outline btn-sm" @click="carregarTimeline" :disabled="timelineLoading">
                      <i :class="timelineLoading ? 'fas fa-sync-alt spin' : 'fas fa-sync-alt'" class="me-1"></i>Recalcular
                    </button>
                  </div>
                </div>
                <div class="card-body">
                  <div v-if="timelineLoading && !timeline" class="text-center py-4">
                    <div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
                    <span class="text-muted">Calculando...</span>
                  </div>
                  <div v-else-if="timelineError" class="alert alert-warning py-2">
                    <i class="fas fa-exclamation-triangle me-1"></i>{{ timelineError }}
                  </div>
                  <div v-else-if="timeline && timeline.medicamentos.length === 0" class="text-center py-4">
                    <i class="fas fa-inbox fa-2x text-muted mb-2"></i>
                    <p class="text-muted mb-0">Nenhum histórico para análise.</p>
                  </div>
                  <div v-else-if="timeline">
                    <div class="row g-2 mb-4">
                      <div class="col-3"><div class="timeline-stat"><span class="timeline-num">{{ timeline.resumo.totalMedicamentos }}</span><span class="timeline-lbl">Total</span></div></div>
                      <div class="col-3"><div class="timeline-stat"><span class="timeline-num text-danger">{{ timeline.resumo.criticos }}</span><span class="timeline-lbl">Críticos</span></div></div>
                      <div class="col-3"><div class="timeline-stat"><span class="timeline-num text-warning">{{ timeline.resumo.atencao }}</span><span class="timeline-lbl">Atenção</span></div></div>
                      <div class="col-3"><div class="timeline-stat"><span class="timeline-num text-success">{{ timeline.resumo.ok }}</span><span class="timeline-lbl">OK</span></div></div>
                    </div>
                    <div v-for="(med, idx) in timeline.medicamentos" :key="idx" class="timeline-item mb-3">
                      <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-1">
                        <div class="d-flex align-items-center gap-2">
                          <span class="status-dot" :class="'dot-' + med.status"></span>
                          <strong class="text-truncate" :title="med.produtoNome">{{ med.produtoNome }}</strong>
                          <span class="badge-tag">{{ med.categoria }}</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                          <small class="text-muted"><i class="fas fa-pills me-1"></i>{{ med.estoqueRestanteEstimado }} und.</small>
                          <small class="text-muted"><i class="fas fa-tachometer-alt me-1"></i>{{ med.consumoDiarioEstimado }}/dia</small>
                          <button v-if="!med.temAgendamentoAtivo && med.status !== 'ok'" class="cf-btn cf-btn-outline btn-sm" @click="agendarRecompra(med)">
                            <i class="fas fa-calendar-plus me-1"></i>Agendar
                          </button>
                          <span v-else-if="med.temAgendamentoAtivo" class="badge-scheduled"><i class="fas fa-check-circle me-1"></i>Agendado</span>
                        </div>
                      </div>
                      <div class="timeline-bar-wrap">
                        <div class="timeline-track">
                          <div v-for="evt in med.historicoCompras" class="tl-dot purchase"
                            :style="{ left: getTimelineDotPosition(evt, med) + '%' }"
                            :title="evt.data + ' - ' + evt.quantidade + ' un.'"></div>
                          <div class="tl-fill" :class="'fill-' + med.status" :style="{ width: getTimelineFillWidth(med) + '%' }"></div>
                          <div class="tl-dot depletion" :class="'depletion-' + med.status"
                            :style="{ left: getDepletionDotPosition(med) + '%' }"
                            :title="'Esgotamento: ' + med.dataPrevisaoEsgotamento">
                            <i class="fas fa-exclamation-triangle"></i>
                          </div>
                        </div>
                        <div class="d-flex justify-content-between mt-1">
                          <small class="text-muted">{{ med.ultimaCompra }}</small>
                          <small class="fw-bold" :class="getStatusTextColor(med.status)">{{ med.dataPrevisaoEsgotamento }}</small>
                        </div>
                      </div>
                      <div v-if="med.recomendacao" class="recomendacao">
                        <i class="fas fa-lightbulb text-warning me-1"></i>{{ med.recomendacao }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- CTA -->
              <div class="card cta-card-modern mb-4">
                <div class="card-body p-4 text-center">
                  <h4 class="mb-2"><i class="fas fa-magic text-warning me-2"></i>Plano Inteligente de Saúde</h4>
                  <p class="text-muted mb-3">Baseado no seu histórico, você pode economizar até <strong>{{ formatCurrency(dashboard.resumo.economiaEstimada) }}</strong> com compras programadas</p>
                  <router-link to="/products" class="cf-btn cf-btn-primary cf-btn-lg">
                    <i class="fas fa-cart-plus me-2"></i>Repor Medicamentos
                  </router-link>
                </div>
              </div>
            </div>
          </div>

          <!-- ENDEREÇOS -->
          <div v-else-if="activeSection === 'addresses'" key="addresses" class="content-section">
            <div class="card cf-card">
              <div class="card-header cf-card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="fas fa-map-marker-alt me-2 text-primary"></i>Meus Endereços</h5>
                <button class="cf-btn cf-btn-primary btn-sm" @click="showAddAddressForm = true">
                  <i class="fas fa-plus me-1"></i>Novo
                </button>
              </div>
              <div class="card-body">
                <div v-if="addresses.length === 0" class="text-center py-4">
                  <div class="empty-illustration"><i class="fas fa-map-marked-alt"></i></div>
                  <p class="text-muted mt-2">Nenhum endereço cadastrado.</p>
                </div>
                <div v-else class="address-list">
                  <div v-for="address in addresses" :key="address.id" class="address-card">
                    <div class="address-info">
                      <div class="d-flex align-items-center gap-2 mb-1">
                        <h6 class="mb-0">{{ address.nickname }}</h6>
                        <span v-if="address.isDefault" class="badge-default">Principal</span>
                      </div>
                      <p class="mb-0 text-muted small">{{ address.street }}, {{ address.number }}{{ address.neighborhood ? ' - ' + address.neighborhood : '' }}</p>
                      <p class="mb-0 text-muted small">{{ address.city }}/{{ address.state }} · CEP: {{ address.zipCode }}</p>
                    </div>
                    <div class="address-actions">
                      <button class="action-btn" title="Editar"><i class="fas fa-pen"></i></button>
                      <button class="action-btn text-danger" title="Excluir"><i class="fas fa-trash"></i></button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- SEGURANÇA -->
          <div v-else-if="activeSection === 'security'" key="security" class="content-section">
            <div class="row g-4">
              <div class="col-lg-8">
                <div class="card cf-card">
                  <div class="card-header cf-card-header">
                    <h5 class="mb-0"><i class="fas fa-lock me-2 text-primary"></i>Alterar Senha</h5>
                  </div>
                  <div class="card-body">
                    <form @submit.prevent="changePassword">
                      <div class="row g-3">
                        <div class="col-12">
                          <label class="cf-label">Senha Atual</label>
                          <input v-model="passwordForm.currentPassword" type="password" class="cf-input" required>
                        </div>
                        <div class="col-md-6">
                          <label class="cf-label">Nova Senha</label>
                          <input v-model="passwordForm.newPassword" type="password" class="cf-input" required>
                        </div>
                        <div class="col-md-6">
                          <label class="cf-label">Confirmar Senha</label>
                          <input v-model="passwordForm.confirmPassword" type="password" class="cf-input" required>
                        </div>
                        <div class="col-12">
                          <button type="submit" class="cf-btn cf-btn-primary">
                            <i class="fas fa-key me-1"></i>Alterar Senha
                          </button>
                        </div>
                      </div>
                    </form>
                  </div>
                </div>
              </div>
              <div class="col-lg-4">
                <div class="card cf-card">
                  <div class="card-header cf-card-header">
                    <h6 class="mb-0"><i class="fas fa-shield-alt me-2 text-success"></i>Sessões</h6>
                  </div>
                  <div class="card-body">
                    <p class="small text-muted mb-2">Você está logado neste dispositivo.</p>
                    <button class="cf-btn cf-btn-danger btn-sm w-100">
                      <i class="fas fa-sign-out-alt me-1"></i>Encerrar Outras Sessões
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </transition>
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
      charts: {},
      timeline: null,
      timelineLoading: false,
      timelineError: null
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
        const [dashResp, timeResp] = await Promise.all([
          api.get(`/dashboard-saude/usuario/${this.usuarioId}`),
          api.get(`/dashboard-saude/timeline/${this.usuarioId}`)
        ])
        this.dashboard = dashResp.data
        this.timeline = timeResp.data
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
    },
    async carregarTimeline() {
      this.timelineLoading = true
      this.timelineError = null
      try {
        const response = await api.get(`/dashboard-saude/timeline/${this.usuarioId}`)
        this.timeline = response.data
      } catch (err) {
        console.error('Erro ao carregar timeline:', err)
        this.timelineError = 'Não foi possível calcular a timeline.'
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
        this.timelineError = 'Erro ao agendar recompra.'
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
/* =========== GLOBAL =========== */
.profile-page {
  background: #f0fdf4;
  min-height: 100vh;
  font-family: 'Fira Sans', system-ui, -apple-system, sans-serif;
}

/* =========== COVER =========== */
.profile-cover {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  min-height: 180px;
}

.cover-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #15803d 0%, #22c55e 50%, #0369a1 100%);
}

.cover-content {
  position: relative;
  padding: 2rem;
  color: white;
}

.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  border: 3px solid rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 700;
  flex-shrink: 0;
}

.profile-info h2 {
  font-size: 1.5rem;
  font-weight: 700;
}

.cover-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(4px);
  padding: 0.2rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
}

/* =========== TABS =========== */
.profile-tabs {
  display: flex;
  gap: 0.25rem;
  background: white;
  border-radius: 14px;
  padding: 0.35rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  overflow-x: auto;
}

.tab-btn {
  flex: 1;
  min-width: 110px;
  padding: 0.6rem 1rem;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #6b7280;
  font-weight: 500;
  font-size: 0.85rem;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  cursor: pointer;
}

.tab-btn:hover {
  background: #f0fdf4;
  color: #15803d;
}

.tab-btn.active {
  background: #15803d;
  color: white;
  box-shadow: 0 2px 8px rgba(21, 128, 61, 0.25);
}

/* =========== CARDS =========== */
.cf-card {
  border: none;
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  transition: box-shadow 0.2s ease;
}

.cf-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.04);
}

.cf-card-header {
  background: white;
  border-bottom: 1px solid #f0fdf4;
  padding: 1rem 1.25rem;
}

.cf-card-header h5, .cf-card-header h6 {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1f2937;
}

/* =========== FORM ELEMENTS =========== */
.cf-label {
  display: block;
  font-size: 0.8rem;
  font-weight: 500;
  color: #6b7280;
  margin-bottom: 0.35rem;
}

.cf-input {
  width: 100%;
  padding: 0.6rem 0.85rem;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  font-size: 0.9rem;
  color: #1f2937;
  background: white;
  transition: border-color 0.2s, box-shadow 0.2s;
  outline: none;
}

.cf-input:focus:not(:disabled) {
  border-color: #15803d;
  box-shadow: 0 0 0 3px rgba(21, 128, 61, 0.12);
}

.cf-input:disabled {
  background: #f9fafb;
  color: #6b7280;
  cursor: not-allowed;
}

/* =========== BUTTONS =========== */
.btn-edit {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.9rem;
  border: 1.5px solid #15803d;
  border-radius: 8px;
  background: transparent;
  color: #15803d;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-edit:hover { background: #f0fdf4; }
.btn-edit.editing { background: #ef4444; border-color: #ef4444; color: white; }
.btn-edit.editing:hover { background: #dc2626; }

.cf-btn {
  display: inline-flex;
  align-items: center;
  padding: 0.5rem 1.25rem;
  border: none;
  border-radius: 10px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
}

.cf-btn-primary {
  background: #15803d;
  color: white;
}
.cf-btn-primary:hover { background: #166534; color: white; }

.cf-btn-secondary {
  background: #f3f4f6;
  color: #374151;
}
.cf-btn-secondary:hover { background: #e5e7eb; }

.cf-btn-danger {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}
.cf-btn-danger:hover { background: #fee2e2; }

.cf-btn-outline {
  background: transparent;
  color: #15803d;
  border: 1.5px solid #15803d;
}
.cf-btn-outline:hover { background: #f0fdf4; }

.cf-btn-lg {
  padding: 0.7rem 1.75rem;
  font-size: 1rem;
}

/* =========== RESUMO SIDEBAR =========== */
.resumo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.6rem 0;
  border-bottom: 1px solid #f3f4f6;
}
.resumo-item:last-child { border-bottom: none; }

.resumo-label { font-size: 0.8rem; color: #6b7280; }
.resumo-value { font-size: 0.85rem; font-weight: 600; color: #1f2937; }

/* =========== QUICK ACTIONS =========== */
.quick-action-btn {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.65rem 0.75rem;
  border-radius: 10px;
  text-decoration: none;
  color: #374151;
  font-size: 0.85rem;
  transition: background 0.15s;
  margin-bottom: 0.25rem;
}
.quick-action-btn:last-child { margin-bottom: 0; }
.quick-action-btn:hover { background: #f9fafb; color: #374151; }
.quick-action-btn i:last-child { margin-left: auto; font-size: 0.7rem; color: #9ca3af; }

/* =========== STAT CARDS MODERN =========== */
.stat-card-modern {
  border-radius: 14px;
  padding: 1.25rem;
  color: white;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card-modern:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.stat-info { min-width: 0; }
.stat-label { display: block; font-size: 0.75rem; opacity: 0.85; margin-bottom: 0.15rem; }
.stat-value { display: block; font-size: 1.25rem; font-weight: 700; }

.gradient-blue { background: linear-gradient(135deg, #2563eb, #3b82f6); }
.gradient-green { background: linear-gradient(135deg, #15803d, #22c55e); }
.gradient-orange { background: linear-gradient(135deg, #d97706, #f59e0b); }
.gradient-teal { background: linear-gradient(135deg, #0d9488, #14b8a6); }

/* =========== BADGES & TAGS =========== */
.badge-tag {
  display: inline-block;
  padding: 0.15rem 0.6rem;
  background: #f3f4f6;
  color: #6b7280;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.2rem 0.7rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
}

.status-critico { background: #fef2f2; color: #dc2626; }
.status-atencao { background: #fffbeb; color: #d97706; }
.status-ok { background: #f0fdf4; color: #15803d; }

.badge-default {
  display: inline-block;
  padding: 0.1rem 0.5rem;
  background: #15803d;
  color: white;
  border-radius: 6px;
  font-size: 0.7rem;
  font-weight: 500;
}

/* =========== CUSTOM TABLE =========== */
.cf-table {
  width: 100%;
  border-collapse: collapse;
}

.cf-table th {
  padding: 0.75rem 0.85rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.cf-table td {
  padding: 0.7rem 0.85rem;
  font-size: 0.85rem;
  border-bottom: 1px solid #f3f4f6;
  vertical-align: middle;
}

.cf-table tbody tr {
  transition: background 0.15s;
}

.cf-table tbody tr:hover {
  background: #f0fdf4;
}

.cf-table tbody tr:last-child td {
  border-bottom: none;
}

/* =========== PROGRESS BAR MINI =========== */
.progress-bar-mini {
  width: 50px;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.fill-critico { background: #dc2626; }
.fill-atencao { background: #f59e0b; }
.fill-ok { background: #22c55e; }

/* =========== CTA CARD =========== */
.cta-card-modern {
  background: linear-gradient(135deg, #f0fdf4 0%, #e0f2fe 100%);
  border: 1px solid rgba(21, 128, 61, 0.15) !important;
  border-radius: 14px;
}

/* =========== ADDRESS CARD =========== */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.address-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.address-card:hover {
  border-color: #bbf7d0;
  box-shadow: 0 2px 8px rgba(21, 128, 61, 0.06);
}

.address-info { flex: 1; min-width: 0; }
.address-info h6 { font-size: 0.9rem; font-weight: 600; }

.address-actions {
  display: flex;
  gap: 0.25rem;
  flex-shrink: 0;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover { background: #f3f4f6; color: #374151; }

/* =========== TIMELINE =========== */
.timeline-stat {
  text-align: center;
  padding: 0.65rem;
  background: #f9fafb;
  border-radius: 10px;
}

.timeline-num { display: block; font-size: 1.35rem; font-weight: 700; line-height: 1.2; }
.timeline-lbl { display: block; font-size: 0.7rem; color: #6b7280; text-transform: uppercase; letter-spacing: 0.5px; }

.legend-dots { font-size: 0.78rem; color: #6b7280; white-space: nowrap; }
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 3px; }
.dot-danger { background: #dc2626; }
.dot-warning { background: #f59e0b; }
.dot-success { background: #22c55e; }

.status-dot {
  display: inline-block;
  width: 10px; height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-critico { background: #dc2626; }
.dot-atencao { background: #f59e0b; }
.dot-ok { background: #22c55e; }

.timeline-item {
  background: #f9fafb;
  border-radius: 10px;
  padding: 0.75rem 1rem;
  border: 1px solid #e5e7eb;
  transition: box-shadow 0.15s;
}
.timeline-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.05); }

.timeline-bar-wrap { margin-top: 0.5rem; }

.timeline-track {
  position: relative;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: visible;
}

.tl-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}
.tl-fill.fill-critico { background: linear-gradient(90deg, #dc2626, #ef4444); }
.tl-fill.fill-atencao { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.tl-fill.fill-ok { background: linear-gradient(90deg, #22c55e, #4ade80); }

.tl-dot {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
  border-radius: 50%;
}

.tl-dot.purchase {
  width: 12px; height: 12px;
  background: white;
  border: 2px solid #15803d;
  cursor: help;
  transition: transform 0.15s;
}
.tl-dot.purchase:hover { transform: translate(-50%, -50%) scale(1.4); }

.tl-dot.depletion {
  width: 20px; height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.55rem;
  cursor: help;
}
.depletion-critico { background: #dc2626; color: white; border: 2px solid white; box-shadow: 0 0 0 2px #dc2626; }
.depletion-atencao { background: #f59e0b; color: black; border: 2px solid white; box-shadow: 0 0 0 2px #f59e0b; }
.depletion-ok { background: #22c55e; color: white; border: 2px solid white; box-shadow: 0 0 0 2px #22c55e; }

.recomendacao {
  background: white;
  border-radius: 6px;
  padding: 0.3rem 0.6rem;
  font-size: 0.78rem;
  color: #6b7280;
  margin-top: 0.35rem;
}

.badge-scheduled {
  display: inline-flex;
  align-items: center;
  padding: 0.2rem 0.6rem;
  background: #eff6ff;
  color: #2563eb;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

/* =========== EMPTY STATE =========== */
.empty-illustration {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  background: #f3f4f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #9ca3af;
}

/* =========== TRANSITIONS =========== */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.fade-enter-from { opacity: 0; transform: translateY(6px); }
.fade-leave-to { opacity: 0; transform: translateY(-6px); }

.slide-enter-active, .slide-leave-active {
  transition: all 0.2s ease;
}
.slide-enter-from, .slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* =========== ANIMATIONS =========== */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.spin { animation: spin 1s linear infinite; }

/* =========== RESPONSIVE =========== */
@media (max-width: 768px) {
  .profile-cover { min-height: 140px; }
  .cover-content { padding: 1.25rem; }
  .profile-avatar { width: 56px; height: 56px; font-size: 1.2rem; }
  .profile-info h2 { font-size: 1.15rem; }
  .tab-btn { min-width: 90px; padding: 0.45rem 0.6rem; font-size: 0.75rem; }
  .stat-value { font-size: 1rem; }
  .cf-table th, .cf-table td { padding: 0.5rem 0.6rem; font-size: 0.78rem; }
}
</style>
