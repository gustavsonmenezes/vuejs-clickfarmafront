<template>
  <div class="user-management">
    <!-- Header -->
    <div class="section-header mb-4">
      <div class="search-box">
        <i class="fas fa-search"></i>
        <input v-model="search" class="cf-input ps-4" placeholder="Buscar por nome ou email..." />
      </div>
    </div>

    <!-- Users Table -->
    <div class="cf-card">
      <div class="cf-card-body p-0">
        <table class="cf-table" v-if="filteredUsers.length">
          <thead>
            <tr>
              <th class="ps-4">Usuário</th>
              <th>Email</th>
              <th class="text-center">Role</th>
              <th class="text-center">Cadastro</th>
              <th class="text-center">Pedidos</th>
              <th class="text-end pe-4">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filteredUsers" :key="user.id">
              <td class="ps-4">
                <div class="d-flex align-items-center gap-3">
                  <div class="user-avatar">{{ user.nome.charAt(0).toUpperCase() }}</div>
                  <div class="fw-semibold text-dark">{{ user.nome }}</div>
                </div>
              </td>
              <td class="cf-text-muted">{{ user.email }}</td>
              <td class="text-center">
                <select v-model="user.role" @change="updateUserRole(user)" class="cf-select role-select">
                  <option value="USER">Cliente</option>
                  <option value="ADMIN">Administrador</option>
                </select>
              </td>
              <td class="text-center cf-text-muted">{{ formatDate(user.dataCadastro) }}</td>
              <td class="text-center">
                <span class="cf-badge cf-badge-info">{{ user.quantidadePedidos || 0 }}</span>
              </td>
              <td class="text-end pe-4">
                <button @click="showDeleteModal(user)" class="action-btn danger" title="Excluir">
                  <i class="fas fa-trash"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="cf-empty-state">
          <i class="fas fa-users"></i>
          <p>Nenhum usuário encontrado.</p>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteConfirm" class="modal-backdrop" @click.self="hideDeleteModal">
      <div class="modal-panel modal-sm">
        <div class="modal-header">
          <h5 class="mb-0 fw-semibold">Excluir Usuário</h5>
          <button @click="hideDeleteModal" class="btn-close-modal"><i class="fas fa-xmark"></i></button>
        </div>
        <div class="modal-body">
          <p class="mb-1">Tem certeza que deseja excluir <strong>{{ userToDelete?.nome }}</strong>?</p>
          <p class="cf-text-muted mb-0 small">Esta ação não pode ser desfeita.</p>
        </div>
        <div class="modal-footer">
          <button class="cf-btn cf-btn-secondary" @click="hideDeleteModal">Cancelar</button>
          <button class="cf-btn cf-btn-danger" @click="confirmDelete">Excluir</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { adminService } from '@/services/adminService';

export default {
  name: 'UserManagement',
  data() {
    return {
      users: [],
      search: '',
      showDeleteConfirm: false,
      userToDelete: null
    }
  },
  computed: {
    filteredUsers() {
      if (!this.search) return this.users;
      const q = this.search.toLowerCase();
      return this.users.filter(u => 
        u.nome.toLowerCase().includes(q) || u.email.toLowerCase().includes(q)
      );
    }
  },
  mounted() { this.fetchUsers(); },
  methods: {
    async fetchUsers() {
      try {
        const res = await adminService.getUsers();
        this.users = res.data || [];
      } catch (e) {
        console.error('Erro ao buscar usuários:', e);
      }
    },
    async updateUserRole(user) {
      const roleLabel = user.role === 'ADMIN' ? 'Administrador' : 'Cliente';
      if (!confirm(`Alterar tipo de ${user.nome} para ${roleLabel}?`)) {
        await this.fetchUsers();
        return;
      }
      try {
        await adminService.updateUserRole(user.id, user.role);
      } catch (e) {
        alert('Erro: ' + (e.response?.data?.message || e.message));
        await this.fetchUsers();
      }
    },
    showDeleteModal(user) {
      this.userToDelete = user;
      this.showDeleteConfirm = true;
    },
    hideDeleteModal() {
      this.userToDelete = null;
      this.showDeleteConfirm = false;
    },
    async confirmDelete() {
      if (!this.userToDelete) return;
      try {
        await adminService.deleteUser(this.userToDelete.id);
        this.hideDeleteModal();
        await this.fetchUsers();
      } catch (e) {
        alert('Erro: ' + (e.response?.data?.message || e.message));
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return '-';
      return new Date(dateStr).toLocaleDateString('pt-BR');
    }
  }
}
</script>

<style scoped>
.user-management { max-width: 1200px; }

.section-header { display: flex; align-items: center; }

.search-box {
  position: relative;
  max-width: 320px;
}

.search-box i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--cf-slate-400);
  font-size: 0.875rem;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--cf-primary-100);
  color: var(--cf-primary-600);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.8125rem;
}

.role-select {
  width: 140px;
  padding: 4px 8px;
  font-size: 0.8125rem;
}

/* Action buttons */
.action-btn {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--cf-slate-500);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.action-btn:hover { background: var(--cf-slate-100); color: var(--cf-slate-700); }
.action-btn.danger:hover { background: var(--cf-danger-light); color: var(--cf-danger); }

/* Modal */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(4px);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.15s ease;
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.modal-panel {
  background: white;
  border-radius: var(--cf-radius-lg);
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
  animation: scaleIn 0.2s ease;
}

.modal-sm { max-width: 400px; }

@keyframes scaleIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--cf-border);
}

.btn-close-modal { background: none; border: none; color: var(--cf-slate-400); cursor: pointer; font-size: 1.1rem; padding: 4px; }
.btn-close-modal:hover { color: var(--cf-slate-700); }

.modal-body { padding: 24px; }
.modal-footer { padding: 16px 24px; border-top: 1px solid var(--cf-border); display: flex; justify-content: flex-end; gap: 8px; }
</style>
