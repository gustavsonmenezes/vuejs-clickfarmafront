<template>
  <div class="cf-mgmt">
    <div class="cf-mgmt-header">
      <h4 class="cf-page-title"><span class="cf-dot green"></span>Gerenciar Usuários</h4>
      <div class="cf-mgmt-actions">
        <select v-model="filtroRole" class="cf-select">
          <option value="">Todos os perfis</option>
          <option value="CUSTOMER">Clientes</option>
          <option value="PHARMACY">Farmácias</option>
          <option value="COURIER">Entregadores</option>
          <option value="ADMIN">Admins</option>
        </select>
        <div class="cf-search-wrap">
          <i class="fas fa-search cf-search-icon"></i>
          <input v-model="busca" type="text" class="cf-search" placeholder="Buscar usuário...">
        </div>
      </div>
    </div>

    <div class="cf-table-card">
      <div v-if="isLoading" class="cf-loading-row">
        <div class="cf-spinner"></div><span>Carregando usuários...</span>
      </div>
      <div v-else class="cf-table-wrap">
        <table class="cf-table">
          <thead>
            <tr><th>ID</th><th>Usuário</th><th>Email</th><th>Perfil</th><th>Cidade</th><th>Cadastro</th></tr>
          </thead>
          <tbody>
            <tr v-for="u in filtrados" :key="u.id">
              <td class="cf-td-faint">#{{ u.id }}</td>
              <td>
                <div class="cf-avatar-row">
                  <div class="cf-user-avatar" :style="`background:${corRole(u.role)}15;color:${corRole(u.role)}`">
                    {{ u.nome?.charAt(0)?.toUpperCase() || '?' }}
                  </div>
                  <span class="cf-td-bold">{{ u.nome }}</span>
                </div>
              </td>
              <td class="cf-td-muted">{{ u.email }}</td>
              <td><span class="cf-role-badge" :style="`background:${corRole(u.role)}12;color:${corRole(u.role)}`">{{ u.role || '—' }}</span></td>
              <td class="cf-td-muted">{{ u.cidade || '—' }}</td>
              <td class="cf-td-muted">{{ d(u.dataCadastro) }}</td>
            </tr>
            <tr v-if="!filtrados.length"><td colspan="6" class="cf-empty">Nenhum usuário encontrado.</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
export default {
  name: 'UserManagement',
  data() { return { usuarios: [], isLoading: true, busca: '', filtroRole: '' }; },
  computed: {
    filtrados() {
      return this.usuarios.filter(u => {
        const mr = !this.filtroRole || u.role === this.filtroRole;
        const mb = !this.busca || u.nome?.toLowerCase().includes(this.busca.toLowerCase()) || u.email?.toLowerCase().includes(this.busca.toLowerCase());
        return mr && mb;
      });
    }
  },
  async mounted() {
    this.isLoading = true;
    try { const { data } = await api.get('/usuarios'); this.usuarios = data; }
    catch (e) { console.error(e); }
    finally { this.isLoading = false; }
  },
  methods: {
    corRole(r) { return { ADMIN:'#2A5C45', PHARMACY:'#3D7A5E', COURIER:'#B89550', CUSTOMER:'#868680' }[r] || '#B0AFA9'; },
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
.cf-mgmt-actions { display: flex; gap: 0.75rem; flex-wrap: wrap; align-items: center; }

.cf-select {
  padding: 0.55rem 1rem; border: 1px solid var(--cf-border-mid); border-radius: 12px;
  font-family: var(--cf-sans); font-size: 0.88rem; color: var(--cf-text-dark); background: var(--cf-white);
  outline: none; cursor: pointer; transition: all 0.2s;
}
.cf-select:focus { border-color: var(--cf-green); box-shadow: 0 0 0 4px rgba(42,92,69,0.06); }

.cf-search-wrap { position: relative; }
.cf-search-icon { position: absolute; left: 0.85rem; top: 50%; transform: translateY(-50%); color: var(--cf-text-faint); font-size: 0.85rem; }
.cf-search { padding: 0.55rem 1rem 0.55rem 2.4rem; border: 1px solid var(--cf-border-mid); border-radius: 12px; font-family: var(--cf-sans); font-size: 0.88rem; color: var(--cf-text-dark); background: var(--cf-white); outline: none; transition: all 0.2s; width: 240px; }
.cf-search:focus { border-color: var(--cf-green); box-shadow: 0 0 0 4px rgba(42,92,69,0.06); }

@media (max-width: 768px) {
  .cf-mgmt-header { flex-direction: column; align-items: stretch; }
  .cf-mgmt-actions { flex-direction: column; align-items: stretch; }
  .cf-search, .cf-select { width: 100%; }
}

.cf-table-card { background: var(--cf-white); border: 1px solid var(--cf-border); border-radius: 16px; box-shadow: var(--cf-shadow-sm); overflow: hidden; }
.cf-table-wrap { overflow-x: auto; scrollbar-width: thin; }
.cf-table { width: 100%; border-collapse: collapse; min-width: 900px; }
.cf-table th { font-size: 0.68rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-muted); font-weight: 600; padding: 1rem; border-bottom: 1px solid var(--cf-border); background: var(--cf-ivory); white-space: nowrap; }
.cf-table td { padding: 1rem; border-bottom: 1px solid var(--cf-border); font-size: 0.88rem; vertical-align: middle; }
.cf-table tbody tr:last-child td { border-bottom: none; }
.cf-table tbody tr:hover td { background: rgba(42,92,69,0.02); }

.cf-avatar-row { display: flex; align-items: center; gap: 0.8rem; }
.cf-user-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 0.85rem; font-weight: 700; flex-shrink: 0; }
.cf-td-bold { font-weight: 600; color: var(--cf-text-dark); }
.cf-td-muted { color: var(--cf-text-muted); font-size: 0.8rem; }
.cf-td-faint { color: var(--cf-text-faint); font-size: 0.8rem; }
.cf-role-badge { font-size: 0.65rem; font-weight: 600; letter-spacing: 0.08em; padding: 0.25rem 0.8rem; border-radius: 20px; text-transform: uppercase; }
.cf-empty { text-align: center; color: var(--cf-text-faint); padding: 4rem 2rem; font-size: 0.9rem; }
.cf-loading-row { display: flex; align-items: center; justify-content: center; gap: 1rem; padding: 5rem 2rem; color: var(--cf-text-muted); font-size: 0.9rem; }
.cf-spinner { width: 28px; height: 28px; border: 3px solid var(--cf-border); border-top-color: var(--cf-green); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>