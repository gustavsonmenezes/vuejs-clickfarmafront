<template>
  <div class="cf-mgmt">
    <div class="cf-mgmt-header">
      <h4 class="cf-page-title"><span class="cf-dot green"></span>Farmácias Parceiras</h4>
      <div class="cf-mgmt-actions">
        <div class="cf-search-wrap">
          <i class="fas fa-search cf-search-icon"></i>
          <input v-model="busca" type="text" class="cf-search" placeholder="Buscar farmácia...">
        </div>
      </div>
    </div>

    <div class="cf-table-card">
      <div v-if="isLoading" class="cf-loading-row">
        <div class="cf-spinner"></div>
        <span>Carregando farmácias...</span>
      </div>
      <div v-else class="cf-table-wrap">
        <table class="cf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Farmácia</th>
              <th>CNPJ</th>
              <th>Cidade</th>
              <th>Telefone</th>
              <th>Cadastro</th>
              <th class="text-center">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="f in filtradas" :key="f.id">
              <td class="cf-td-faint">#{{ f.id }}</td>
              <td>
                <div class="cf-avatar-row">
                  <div class="cf-icon-avatar green"><i class="fas fa-store"></i></div>
                  <div>
                    <div class="cf-td-bold">{{ f.nome }}</div>
                    <div class="cf-td-muted">{{ f.email }}</div>
                  </div>
                </div>
              </td>
              <td class="cf-mono">{{ f.cnpj }}</td>
              <td><span class="cf-tag">{{ f.cidade || '—' }}</span></td>
              <td class="cf-td-muted">{{ f.telefone || '—' }}</td>
              <td class="cf-td-muted">{{ d(f.dataCadastro) }}</td>
              <td class="text-center">
                <button class="cf-icon-btn" @click="abrirEdicao(f)" title="Editar"><i class="fas fa-pencil-alt"></i></button>
                <button class="cf-icon-btn danger" @click="confirmarDelete(f)" title="Remover"><i class="fas fa-trash"></i></button>
              </td>
            </tr>
            <tr v-if="!filtradas.length"><td colspan="7" class="cf-empty">{{ busca ? 'Nenhuma farmácia encontrada.' : 'Nenhuma farmácia cadastrada.' }}</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal de Edição/Criação -->
    <div v-if="modalEdicao" class="cf-modal-overlay" @click.self="fecharModal">
      <div class="cf-modal-box animate__animated animate__fadeInDown">
        <div class="cf-modal-header">
          <div class="d-flex align-items-center gap-3">
            <div class="modal-icon-wrap bg-primary-subtle">
              <i class="fas fa-store text-primary"></i>
            </div>
            <div>
              <h5 class="mb-0 fw-bold">Editar Farmácia</h5>
              <p class="mb-0 text-muted small">ID: #{{ editandoId }}</p>
            </div>
          </div>
          <button class="btn-close-custom" @click="fecharModal"><i class="fas fa-times"></i></button>
        </div>
        
        <div class="cf-modal-body p-4">
          <div class="row g-4">
            <div class="col-md-6">
              <label class="cf-label-premium">Nome da Unidade</label>
              <input v-model="form.nome" class="cf-input-premium" placeholder="Nome Fantasia">
            </div>
            <div class="col-md-6">
              <label class="cf-label-premium">CNPJ</label>
              <input v-model="form.cnpj" class="cf-input-premium" placeholder="00.000.000/0000-00">
            </div>
            <div class="col-md-8">
              <label class="cf-label-premium">Endereço Completo</label>
              <input v-model="form.endereco" class="cf-input-premium" placeholder="Rua, Número, Bairro">
            </div>
            <div class="col-md-4">
              <label class="cf-label-premium">Cidade</label>
              <input v-model="form.cidade" class="cf-input-premium">
            </div>
            <div class="col-md-6">
              <label class="cf-label-premium">E-mail de Contato</label>
              <input v-model="form.email" type="email" class="cf-input-premium">
            </div>
            <div class="col-md-6">
              <label class="cf-label-premium">Telefone/WhatsApp</label>
              <input v-model="form.telefone" class="cf-input-premium">
            </div>
            <!-- Campos PIX -->
            <div class="col-md-4">
              <label class="cf-label-premium">Tipo de Chave PIX</label>
              <select v-model="form.tipoChavePix" class="cf-input-premium">
                <option value="CNPJ">CNPJ</option>
                <option value="EMAIL">E-mail</option>
                <option value="TELEFONE">Telefone</option>
                <option value="ALEATORIA">Chave Aleatória</option>
              </select>
            </div>
            <div class="col-md-8">
              <label class="cf-label-premium">Chave PIX</label>
              <input v-model="form.chavePix" class="cf-input-premium" placeholder="Insira a chave para repasses">
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

    <!-- Modal confirmação delete -->
    <div v-if="paraDeletar" class="cf-modal-overlay" @click.self="paraDeletar = null">
      <div class="cf-modal-box animate__animated animate__headShake" style="max-width:400px">
        <div class="cf-modal-header bg-danger-subtle py-3">
          <h5 class="mb-0 text-danger fw-bold"><i class="fas fa-exclamation-triangle me-2"></i>Confirmar Remoção</h5>
        </div>
        <div class="cf-modal-body text-center p-4">
          <p class="mb-2">Deseja remover permanentemente a farmácia:</p>
          <h5 class="fw-bold text-dark mb-3">{{ paraDeletar.nome }}</h5>
          <p class="text-muted small mb-0">Esta ação desativará o acesso da unidade ao painel.</p>
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
  name: 'PharmacyManagement',
  data() {
    return { farmacias: [], isLoading: true, busca: '', modalEdicao: false, form: {}, editandoId: null, paraDeletar: null, salvando: false };
  },
  computed: {
    filtradas() {
      if (!this.busca) return this.farmacias;
      const t = this.busca.toLowerCase();
      return this.farmacias.filter(f => f.nome?.toLowerCase().includes(t) || f.cidade?.toLowerCase().includes(t) || f.cnpj?.includes(t) || f.email?.toLowerCase().includes(t));
    }
  },
  async mounted() { await this.carregar(); },
  methods: {
    async carregar() {
      this.isLoading = true;
      try { const { data } = await api.get('/farmacias'); this.farmacias = data; }
      catch (e) { console.error(e); }
      finally { this.isLoading = false; }
    },
    abrirEdicao(f) { this.editandoId = f.id; this.form = { ...f }; this.modalEdicao = true; },
    fecharModal() { this.modalEdicao = false; this.form = {}; this.editandoId = null; },
    async salvar() {
      this.salvando = true;
      try { await api.put(`/farmacias/${this.editandoId}`, this.form); await this.carregar(); this.fecharModal(); }
      catch { alert('Erro ao salvar.'); }
      finally { this.salvando = false; }
    },
    confirmarDelete(f) { this.paraDeletar = f; },
    async deletar() {
      try { await api.delete(`/farmacias/${this.paraDeletar.id}`); await this.carregar(); this.paraDeletar = null; }
      catch { alert('Erro ao remover.'); }
    },
    d(dt) { if (!dt) return '—'; return new Date(dt).toLocaleDateString('pt-BR'); }
  }
};
</script>

<style scoped>
.cf-mgmt { padding-bottom: 2rem; }
.cf-mgmt-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
.cf-page-title { margin: 0; font-size: 1.1rem; font-weight: 700; color: var(--cf-text-dark); display: flex; align-items: center; gap: 0.6rem; }
.cf-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.cf-dot.green { background: var(--cf-green); }

.cf-search-wrap { position: relative; }
.cf-search-icon { position: absolute; left: 0.85rem; top: 50%; transform: translateY(-50%); color: var(--cf-text-faint); font-size: 0.85rem; }
.cf-search {
  padding: 0.6rem 1rem 0.6rem 2.4rem; border: 1px solid var(--cf-border-mid); border-radius: 12px;
  font-family: var(--cf-sans); font-size: 0.88rem; color: var(--cf-text-dark); background: var(--cf-white);
  transition: all 0.2s; width: 280px; outline: none;
}
.cf-search:focus { border-color: var(--cf-green); box-shadow: 0 0 0 4px rgba(42,92,69,0.06); }

.cf-table-card { background: var(--cf-white); border: 1px solid var(--cf-border); border-radius: 16px; box-shadow: var(--cf-shadow-sm); overflow: hidden; }
.cf-table-wrap { overflow-x: auto; scrollbar-width: thin; }
.cf-table { width: 100%; border-collapse: collapse; min-width: 800px; }
.cf-table th { font-size: 0.68rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-muted); font-weight: 600; padding: 1rem; border-bottom: 1px solid var(--cf-border); background: var(--cf-ivory); white-space: nowrap; }
.cf-table td { padding: 1rem; border-bottom: 1px solid var(--cf-border); font-size: 0.88rem; vertical-align: middle; }
.cf-table tbody tr:last-child td { border-bottom: none; }
.cf-table tbody tr:hover td { background: rgba(42,92,69,0.02); }

.cf-avatar-row { display: flex; align-items: center; gap: 0.8rem; }
.cf-icon-avatar { width: 38px; height: 38px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1rem; flex-shrink: 0; }
.cf-icon-avatar.green { background: var(--cf-green-xlight); color: var(--cf-green); }
.cf-td-bold { font-weight: 600; color: var(--cf-text-dark); }
.cf-td-muted { color: var(--cf-text-muted); font-size: 0.8rem; }
.cf-td-faint { color: var(--cf-text-faint); font-size: 0.8rem; }
.cf-mono { font-family: 'DM Mono', monospace; font-size: 0.8rem; color: var(--cf-text-mid); }
.cf-tag { font-size: 0.72rem; padding: 0.25rem 0.8rem; border-radius: 20px; background: var(--cf-cream); color: var(--cf-text-mid); font-weight: 500; }

.cf-icon-btn { background: var(--cf-white); border: 1px solid var(--cf-border-mid); border-radius: 8px; width: 34px; height: 34px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer; color: var(--cf-text-muted); font-size: 0.85rem; margin: 0 3px; transition: all 0.2s; }
.cf-icon-btn:hover { border-color: var(--cf-green); color: var(--cf-green); background: var(--cf-green-xlight); }
.cf-icon-btn.danger:hover { border-color: var(--cf-danger); color: var(--cf-danger); background: #FFF5F5; }

.cf-empty { text-align: center; color: var(--cf-text-faint); padding: 4rem 2rem; font-size: 0.9rem; }
.cf-loading-row { display: flex; align-items: center; justify-content: center; gap: 1rem; padding: 5rem 2rem; color: var(--cf-text-muted); font-size: 0.9rem; }
.cf-spinner { width: 28px; height: 28px; border: 3px solid var(--cf-border); border-top-color: var(--cf-green); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

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
  .cf-search { width: 100%; }
  .cf-mgmt-header { flex-direction: column; align-items: stretch; }
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
