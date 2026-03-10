
<template>
  <div class="user-management">
    <div class="page-header">
      <h2>Gerenciar Usuários</h2>
    </div>
    
    <div class="users-table">
      <table class="table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Email</th>
            <th>Tipo</th>
            <th>Data de Cadastro</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.name }}</td>
            <td>{{ user.email }}</td>
            <td>
              <select v-model="user.role" @change="updateUserRole(user)" class="form-select-sm">
                <option value="customer">Cliente</option>
                <option value="admin">Administrador</option>
              </select>
            </td>
            <td>{{ formatDate(user.createdAt) }}</td>
            <td>
              <span :class="`status-${user.status}`">
                {{ user.status === 'active' ? 'Ativo' : 'Inativo' }}
              </span>
            </td>
            <td>
              <button @click="toggleUserStatus(user)" class="btn btn-sm" 
                :class="user.status === 'active' ? 'btn-warning' : 'btn-success'">
                {{ user.status === 'active' ? 'Desativar' : 'Ativar' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  name: 'UserManagement',
  setup() {
    const users = ref([])
    
    // Mock data
    const mockUsers = [
      {
        id: 1,
        name: 'João Silva',
        email: 'joao@email.com',
        role: 'customer',
        status: 'active',
        createdAt: '2024-01-01'
      },
      {
        id: 2,
        name: 'Maria Santos',
        email: 'maria@email.com',
        role: 'admin',
        status: 'active',
        createdAt: '2024-01-05'
      }
    ]
    
    onMounted(() => {
      users.value = mockUsers
    })
    
    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleDateString('pt-BR')
    }
    
    const updateUserRole = (user) => {
      if (confirm(`Deseja alterar o tipo de usuário para ${user.role === 'admin' ? 'Administrador' : 'Cliente'}?`)) {
        console.log('Atualizando role do usuário:', user.id, user.role)
        // Implementar chamada API
      }
    }
    
    const toggleUserStatus = (user) => {
      const newStatus = user.status === 'active' ? 'inactive' : 'active'
      const action = newStatus === 'active' ? 'ativar' : 'desativar'
      
      if (confirm(`Deseja ${action} o usuário ${user.name}?`)) {
        user.status = newStatus
        console.log('Atualizando status do usuário:', user.id, newStatus)
        // Implementar chamada API
      }
    }
    
    return {
      users,
      formatDate,
      updateUserRole,
      toggleUserStatus
    }
  }
}
</script>

<style scoped>
.status-active {
  color: #27ae60;
  font-weight: bold;
}

.status-inactive {
  color: #7f8c8d;
}

.btn-warning {
  background: #f39c12;
  color: white;
}

.btn-success {
  background: #27ae60;
  color: white;
}

.form-select-sm {
  padding: 5px;
  border-radius: 4px;
  border: 1px solid #ddd;
}
</style>