<template>
  <div class="courier-management fade-in-up">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="mb-0 fw-bold"><i class="fas fa-motorcycle me-2 text-primary"></i>Entregadores (Motoboys)</h4>
      <input v-model="busca" type="text" class="form-control form-control-sm" style="max-width:260px" placeholder="🔍 Buscar...">
    </div>

    <div class="dash-card">
      <div v-if="isLoading" class="text-center py-5">
        <div class="spinner-border text-primary"></div>
      </div>
      <div v-else class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th>#ID</th><th>Motoboy</th><th>CPF</th><th>Chave PIX</th><th>Cadastro</th><th class="text-center">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in motoboysFiltrados" :key="m.id">
              <td class="text-muted small">#{{ m.id }}</td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="courier-avatar"><i class="fas fa-motorcycle"></i></div>
                  <span class="fw-semibold">{{ m.nome }}</span>
                </div>
              </td>
              <td class="font-monospace small">{{ m.cpf }}</td>
              <td>
                <div v-if="m.chavePix" class="small">
                  <span class="badge bg-light text-dark border me-1">{{ m.tipoChavePix }}</span>
                  <span class="text-muted">{{ m.chavePix }}</span>
                </div>
                <span v-else class="text-muted small italic">Não informada</span>
              </td>
              <td class="text-muted small">{{ formatarData(m.dataCadastro) }}</td>
              <td class="text-center">
                <div class="btn-group btn-group-sm">
                  <button class="btn btn-outline-primary" @click="abrirEdicao(m)">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button class="btn btn-outline-danger" @click="confirmarDelete(m)">
                    <i class="fas fa-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="motoboysFiltrados.length === 0">
              <td colspan="6" class="text-center py-5 text-muted">
                <i class="fas fa-motorcycle fa-2x d-block mb-3 opacity-25"></i>
                {{ busca ? 'Nenhum entregador encontrado.' : 'Nenhum entregador cadastrado.' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal de Edição -->
    <div v-if="modalEdicao" class="cf-modal-overlay" @click.self="fecharModal">
      <div class="cf-modal-box animate__animated animate__fadeInDown">
        <div class="cf-modal-header">
          <div class="d-flex align-items-center gap-3">
            <div class="modal-icon-wrap bg-primary-subtle">
              <i class="fas fa-motorcycle text-primary"></i>
            </div>
            <div>
              <h5 class="mb-0 fw-bold">Editar Entregador</h5>
              <p class="mb-0 text-muted small">ID: #{{ editandoId }}</p>
            </div>
          </div>
          <button class="btn-close-custom" @click="fecharModal"><i class="fas fa-times"></i></button>
        </div>
        
        <div class="cf-modal-body p-4">
          <div class="row g-4">
            <div class="col-md-6">
              <label class="cf-label-premium">Nome Completo</label>
              <input v-model="form.nome" class="cf-input-premium">
            </div>
            <div class="col-md-6">
              <label class="cf-label-premium">CPF</label>
              <input v-model="form.cpf" class="cf-input-premium">
            </div>
            <div class="col-md-6">
              <label class="cf-label-premium">Telefone</label>
              <input v-model="form.telefone" class="cf-input-premium">
            </div>
            <div class="col-md-3">
              <label class="cf-label-premium">Tipo PIX</label>
              <select v-model="form.tipoChavePix" class="cf-input-premium">
                <option value="CPF">CPF</option>
                <option value="EMAIL">E-mail</option>
                <option value="TELEFONE">Telefone</option>
                <option value="ALEATORIA">Chave Aleatória</option>
              </select>
            </div>
            <div class="col-md-9">
              <label class="cf-label-premium">Chave PIX</label>
              <input v-model="form.chavePix" class="cf-input-premium" placeholder="Chave para repasse">
            </div>
          </div>
        </div>
        
        <div class="cf-modal-footer">
          <button class="btn btn-light fw-bold px-4" @click="fecharModal">Cancelar</button>
          <button class="btn btn-primary fw-bold px-4" @click="salvar" :disabled="salvando">
            <span v-if="salvando" class="spinner-border spinner-border-sm me-2"></span>
            Salvar Alterações
          </button>
        </div>
      </div>
    </div>

    <!-- Confirmação Delete -->
    <div v-if="paraDeletar" class="cf-modal-overlay" @click.self="paraDeletar = null">
      <div class="cf-modal-box animate__animated animate__headShake" style="max-width:400px">
        <div class="cf-modal-header bg-danger-subtle py-3">
          <h5 class="mb-0 text-danger fw-bold"><i class="fas fa-exclamation-triangle me-2"></i>Confirmar Remoção</h5>
        </div>
        <div class="cf-modal-body text-center p-4">
          <p class="mb-2">Deseja remover permanentemente o entregador:</p>
          <h5 class="fw-bold text-dark mb-3">{{ paraDeletar.nome }}</h5>
          <p class="text-muted small mb-0">Esta ação não pode ser desfeita e removerá o acesso do motoboy.</p>
        </div>
        <div class="cf-modal-footer bg-light border-0">
          <button class="btn btn-light fw-bold flex-grow-1" @click="paraDeletar = null">Cancelar</button>
          <button class="btn btn-danger fw-bold flex-grow-1" @click="deletar">Confirmar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'CourierManagement',
  data() {
    return { 
      motoboys: [], isLoading: true, busca: '', paraDeletar: null,
      modalEdicao: false, form: {}, editandoId: null, salvando: false
    };
  },
  computed: {
    motoboysFiltrados() {
      if (!this.busca) return this.motoboys;
      const t = this.busca.toLowerCase();
      return this.motoboys.filter(m =>
        m.nome?.toLowerCase().includes(t) || m.cpf?.includes(t)
      );
    }
  },
  async mounted() {
    await this.carregar();
  },
  methods: {
    async carregar() {
      this.isLoading = true;
      try {
        const res = await api.get('/motoboys');
        this.motoboys = res.data;
      } catch (err) {
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    abrirEdicao(m) { this.editandoId = m.id; this.form = { ...m }; this.modalEdicao = true; },
    fecharModal() { this.modalEdicao = false; this.form = {}; this.editandoId = null; },
    async salvar() {
      this.salvando = true;
      try { 
        await api.put(`/motoboys/${this.editandoId}`, this.form); 
        await this.carregar(); 
        this.fecharModal(); 
      }
      catch { alert('Erro ao salvar.'); }
      finally { this.salvando = false; }
    },
    confirmarDelete(m) { this.paraDeletar = m; },
    async deletar() {
      try {
        await api.delete(`/motoboys/${this.paraDeletar.id}`);
        await this.carregar();
        this.paraDeletar = null;
      } catch (err) {
        alert('Erro ao remover.');
      }
    },
    formatarData(d) {
      if (!d) return '—';
      return new Date(d).toLocaleDateString('pt-BR');
    }
  }
};
</script>

<style scoped>
.courier-management { padding-bottom: 2rem; }
.dash-card { background: var(--cf-white); border-radius: 16px; border: 1px solid var(--cf-border); box-shadow: var(--cf-shadow-sm); overflow: hidden; }
.courier-avatar { width: 36px; height: 36px; border-radius: 10px; background: var(--cf-green-xlight); color: var(--cf-green); display: flex; align-items: center; justify-content: center; font-size: 0.9rem; flex-shrink: 0; }
.table th { font-size: 0.68rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-muted); font-weight: 600; padding: 1rem; border-bottom: 1px solid var(--cf-border); background: var(--cf-ivory); }
.table td { padding: 1rem; font-size: 0.88rem; vertical-align: middle; }

/* ─── Modal Premium ClickFarma ─── */
.cf-modal-overlay {
  position: fixed; inset: 0; background: rgba(15, 23, 42, 0.7); 
  z-index: 2000; display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(6px); padding: 20px;
}
.cf-modal-box {
  background: white; border-radius: 24px; width: 800px; max-width: 100%;
  max-height: calc(100vh - 60px);
  box-shadow: 0 25px 50px -12px rgba(0,0,0,0.4); 
  display: flex; flex-direction: column;
  overflow: hidden; border: 1px solid rgba(255,255,255,0.1);
}
.cf-modal-header { 
  padding: 1.25rem 1.5rem; border-bottom: 1px solid #f1f5f9;
  display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0;
}
.modal-icon-wrap {
  width: 44px; height: 44px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center; font-size: 1.2rem;
}
.cf-modal-body { 
  flex: 1; overflow-y: auto; background: #fafafa;
  scrollbar-width: thin; scrollbar-color: var(--cf-green-light) transparent;
}
.cf-modal-body::-webkit-scrollbar { width: 6px; }
.cf-modal-body::-webkit-scrollbar-thumb { background: var(--cf-green-light); border-radius: 10px; }

.cf-modal-footer {
  padding: 1.25rem 1.5rem; border-top: 1px solid #f1f5f9;
  display: flex; justify-content: flex-end; gap: 1rem;
  background: #fff; flex-shrink: 0;
}

@media (max-width: 768px) {
  .cf-modal-overlay { padding: 0; }
  .cf-modal-box { border-radius: 0; height: 100vh; max-height: 100vh; }
  .cf-modal-footer { padding: 1rem; flex-direction: column; align-items: stretch; }
  .cf-modal-footer .btn { width: 100%; }
  .modal-icon-wrap { display: none; }
}

/* Form Elements */
.cf-label-premium { font-size: 0.72rem; font-weight: 600; color: var(--cf-text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.45rem; display: block; }
.cf-input-premium {
  width: 100%; padding: 0.7rem 1rem; border-radius: 10px;
  border: 1px solid var(--cf-border-mid); background: #fff;
  font-size: 0.92rem; transition: all 0.2s; outline: none;
}
.cf-input-premium:focus { border-color: var(--cf-green); box-shadow: 0 0 0 4px rgba(42,92,69,0.06); }

.btn-close-custom {
  background: var(--cf-ivory); border: none; width: 34px; height: 34px;
  border-radius: 50%; color: var(--cf-text-muted); cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.2s;
}
.btn-close-custom:hover { background: var(--cf-cream); color: var(--cf-text-dark); transform: rotate(90deg); }
</style>
