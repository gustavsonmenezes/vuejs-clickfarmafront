<template>
  <div class="prescription-validation">
    <div class="section-header mb-4">
      <h4 class="fw-semibold mb-0">Validação de Receitas</h4>
      <div class="d-flex gap-2 ms-3">
        <select v-model="statusFilter" class="cf-select" style="width: auto;">
          <option value="">Todas</option>
          <option value="PENDENTE">Pendentes</option>
          <option value="APROVADA">Aprovadas</option>
          <option value="REJEITADA">Rejeitadas</option>
        </select>
      </div>
    </div>

    <div class="cf-card">
      <div class="cf-card-body">
        <div v-if="loading" class="text-center py-4">
          <div class="spinner-border text-primary" role="status"></div>
          <p class="cf-text-muted mt-2 mb-0">Carregando receitas...</p>
        </div>

        <div v-else-if="filteredPrescriptions.length === 0" class="cf-empty-state">
          <i class="fas fa-file-prescription"></i>
          <p>Nenhuma receita encontrada.</p>
        </div>

        <div v-else class="prescriptions-grid">
          <div v-for="rec in filteredPrescriptions" :key="rec.id" class="prescription-card">
            <div class="card-top">
              <div class="d-flex align-items-center gap-2 mb-1">
                <span :class="['cf-badge', statusBadge(rec.status)]">{{ statusLabel(rec.status) }}</span>
                <small class="cf-text-muted">{{ formatDate(rec.dataCriacao) }}</small>
              </div>
              <p class="mb-0 small cf-text-muted">Receita #{{ rec.id }}</p>
            </div>
            <div class="card-body">
              <p class="mb-2 small fw-medium text-dark">Medicamentos extraídos:</p>
              <div v-if="rec.medicamentosExtraidos">
                <div v-for="(med, i) in parseMedicamentos(rec.medicamentosExtraidos)" :key="i" class="med-item">
                  <i class="fas fa-capsules text-primary"></i>
                  <span>{{ med.nome || med.nomeCompleto || med }}</span>
                </div>
              </div>
              <p v-else class="cf-text-muted small mb-0">Nenhum medicamento identificado.</p>
            </div>
            <div class="card-actions" v-if="rec.status === 'PENDENTE'">
              <button @click="aprovar(rec)" class="action-btn success">
                <i class="fas fa-check"></i> Aprovar
              </button>
              <button @click="rejeitar(rec)" class="action-btn danger">
                <i class="fas fa-times"></i> Rejeitar
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { adminService } from '@/services/adminService';

export default {
  name: 'PrescriptionValidation',
  data() {
    return {
      prescriptions: [],
      statusFilter: 'PENDENTE',
      loading: false
    }
  },
  computed: {
    filteredPrescriptions() {
      if (!this.statusFilter) return this.prescriptions;
      return this.prescriptions.filter(r => r.status === this.statusFilter);
    }
  },
  mounted() { this.fetchPrescriptions(); },
  methods: {
    async fetchPrescriptions() {
      this.loading = true;
      try {
        const res = await adminService.getPrescriptions();
        this.prescriptions = res.data || [];
      } catch (e) {
        console.error('Erro ao carregar receitas:', e);
      } finally { this.loading = false; }
    },
    async aprovar(rec) {
      if (!confirm(`Aprovar receita #${rec.id}?`)) return;
      try {
        await adminService.approvePrescription(rec.id);
        await this.fetchPrescriptions();
      } catch (e) {
        alert('Erro: ' + (e.response?.data?.message || e.message));
      }
    },
    async rejeitar(rec) {
      if (!confirm(`Rejeitar receita #${rec.id}?`)) return;
      try {
        await adminService.rejectPrescription(rec.id);
        await this.fetchPrescriptions();
      } catch (e) {
        alert('Erro: ' + (e.response?.data?.message || e.message));
      }
    },
    parseMedicamentos(json) {
      if (!json) return [];
      try {
        const parsed = JSON.parse(json);
        return Array.isArray(parsed) ? parsed : [];
      } catch { return []; }
    },
    formatDate(dateStr) {
      if (!dateStr) return '-';
      return new Date(dateStr).toLocaleDateString('pt-BR');
    },
    statusBadge(s) {
      const m = { 'PENDENTE': 'cf-badge-warning', 'APROVADA': 'cf-badge-success', 'REJEITADA': 'cf-badge-danger' };
      return m[s] || 'cf-badge-primary';
    },
    statusLabel(s) {
      const m = { 'PENDENTE': 'Pendente', 'APROVADA': 'Aprovada', 'REJEITADA': 'Rejeitada' };
      return m[s] || s;
    }
  }
}
</script>

<style scoped>
.prescription-validation { max-width: 1200px; }
.section-header { display: flex; align-items: center; }

.prescriptions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.prescription-card {
  background: white;
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-radius-lg);
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.prescription-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }

.card-top {
  padding: 14px 16px 8px;
  border-bottom: 1px solid var(--cf-border);
}

.card-body { padding: 12px 16px; }

.med-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  font-size: 0.8125rem;
  color: var(--cf-slate-700);
}

.card-actions {
  display: flex;
  gap: 8px;
  padding: 10px 16px;
  border-top: 1px solid var(--cf-border);
  background: var(--cf-slate-50);
}

.action-btn {
  flex: 1;
  padding: 8px 16px;
  border: 1px solid var(--cf-border);
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 0.8125rem;
  font-weight: 500;
  font-family: var(--cf-font);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.15s;
}

.action-btn.success { color: var(--cf-success); }
.action-btn.success:hover { background: var(--cf-success-light); border-color: var(--cf-success); }
.action-btn.danger { color: var(--cf-danger); }
.action-btn.danger:hover { background: var(--cf-danger-light); border-color: var(--cf-danger); }
</style>
