<template>
  <div class="container mt-4">
    <h2>👤 Meu Perfil</h2>
    
    <!-- Botão para mostrar/ocultar seções -->
    <div class="d-flex gap-2 mb-4">
      <button 
        @click="activeSection = 'personal'"
        class="btn"
        :class="activeSection === 'personal' ? 'btn-primary' : 'btn-outline-primary'"
      >
        📋 Informações Pessoais
      </button>
      <button 
        @click="activeSection = 'addresses'"
        class="btn"
        :class="activeSection === 'addresses' ? 'btn-primary' : 'btn-outline-primary'"
      >
        📍 Endereços
      </button>
      <button 
        @click="activeSection = 'security'"
        class="btn"
        :class="activeSection === 'security' ? 'btn-primary' : 'btn-outline-primary'"
      >
        🔒 Segurança
      </button>
    </div>
    
    <div class="row">
      <div class="col-md-8">
        <!-- Seção de Informações Pessoais -->
        <div v-show="activeSection === 'personal'" class="card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mb-0">📋 Informações Pessoais</h5>
            <button 
              @click="toggleEditMode('personal')"
              class="btn btn-sm"
              :class="editMode.personal ? 'btn-secondary' : 'btn-outline-secondary'"
            >
              {{ editMode.personal ? 'Cancelar' : '✏️ Editar' }}
            </button>
          </div>
          <div class="card-body">
            <form @submit.prevent="updateProfile">
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Nome completo</label>
                  <input 
                    v-model="profile.name" 
                    type="text" 
                    class="form-control" 
                    :disabled="!editMode.personal"
                    required
                  >
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Email</label>
                  <input 
                    v-model="profile.email" 
                    type="email" 
                    class="form-control" 
                    :disabled="!editMode.personal"
                    required
                  >
                </div>
              </div>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Telefone</label>
                  <input 
                    v-model="profile.phone" 
                    type="tel" 
                    class="form-control"
                    :disabled="!editMode.personal"
                  >
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Data de Nascimento</label>
                  <input 
                    v-model="profile.birthDate" 
                    type="date" 
                    class="form-control"
                    :disabled="!editMode.personal"
                  >
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label">CPF</label>
                <input 
                  v-model="profile.cpf" 
                  type="text" 
                  class="form-control"
                  :disabled="!editMode.personal"
                >
              </div>
              
              <!-- Botões de ação (mostrar apenas no modo edição) -->
              <div v-show="editMode.personal" class="d-flex gap-2">
                <button type="submit" class="btn btn-success">
                  💾 Salvar Alterações
                </button>
                <button 
                  type="button" 
                  @click="toggleEditMode('personal')" 
                  class="btn btn-secondary"
                >
                  ❌ Cancelar
                </button>
              </div>
            </form>
            
            <!-- Visualização dos dados (modo leitura) -->
            <div v-show="!editMode.personal" class="profile-info-view">
              <div class="row">
                <div class="col-6"><strong>Nome:</strong></div>
                <div class="col-6">{{ profile.name || 'Não informado' }}</div>
              </div>
              <div class="row mt-2">
                <div class="col-6"><strong>Email:</strong></div>
                <div class="col-6">{{ profile.email || 'Não informado' }}</div>
              </div>
              <div class="row mt-2">
                <div class="col-6"><strong>Telefone:</strong></div>
                <div class="col-6">{{ profile.phone || 'Não informado' }}</div>
              </div>
              <div class="row mt-2">
                <div class="col-6"><strong>Data Nasc.:</strong></div>
                <div class="col-6">{{ formatDate(profile.birthDate) || 'Não informado' }}</div>
              </div>
              <div class="row mt-2">
                <div class="col-6"><strong>CPF:</strong></div>
                <div class="col-6">{{ profile.cpf || 'Não informado' }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Seção de Endereços -->
        <div v-show="activeSection === 'addresses'" class="card">
          <div class="card-header">
            <h5 class="mb-0">📍 Meus Endereços</h5>
          </div>
          <div class="card-body">
            <div v-if="addresses.length === 0" class="text-center py-4">
              <p class="text-muted">Nenhum endereço cadastrado.</p>
              <button class="btn btn-primary" @click="showAddAddressForm = true">
                ➕ Adicionar Endereço
              </button>
            </div>
            
            <div v-else>
              <!-- Lista de endereços -->
              <div v-for="address in addresses" :key="address.id" class="address-item border-bottom pb-3 mb-3">
                <div class="d-flex justify-content-between align-items-start">
                  <div>
                    <h6>{{ address.nickname }}</h6>
                    <p class="mb-1">{{ address.street }}, {{ address.number }}</p>
                    <p class="mb-1">{{ address.neighborhood }} - {{ address.city }}/{{ address.state }}</p>
                    <p class="mb-1">CEP: {{ address.zipCode }}</p>
                    <span v-if="address.isDefault" class="badge bg-primary">Principal</span>
                  </div>
                  <div class="d-flex gap-1">
                    <button class="btn btn-sm btn-outline-primary">Editar</button>
                    <button class="btn btn-sm btn-outline-danger">Excluir</button>
                  </div>
                </div>
              </div>
              
              <button 
                v-show="!showAddAddressForm"
                class="btn btn-outline-primary"
                @click="showAddAddressForm = true"
              >
                ➕ Adicionar Novo Endereço
              </button>
            </div>

            <!-- Formulário de novo endereço -->
            <div v-show="showAddAddressForm" class="mt-4 p-3 border rounded">
              <h6>➕ Novo Endereço</h6>
              <form @submit.prevent="addAddress">
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Apelido (ex: Casa, Trabalho)</label>
                    <input v-model="newAddress.nickname" type="text" class="form-control" required>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label class="form-label">CEP</label>
                    <input v-model="newAddress.zipCode" type="text" class="form-control" required>
                  </div>
                </div>
                <div class="row">
                  <div class="col-md-8 mb-3">
                    <label class="form-label">Rua</label>
                    <input v-model="newAddress.street" type="text" class="form-control" required>
                  </div>
                  <div class="col-md-4 mb-3">
                    <label class="form-label">Número</label>
                    <input v-model="newAddress.number" type="text" class="form-control" required>
                  </div>
                </div>
                <div class="d-flex gap-2">
                  <button type="submit" class="btn btn-success">Salvar Endereço</button>
                  <button type="button" class="btn btn-secondary" @click="cancelAddAddress">Cancelar</button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <!-- Seção de Segurança -->
        <div v-show="activeSection === 'security'" class="card">
          <div class="card-header">
            <h5 class="mb-0">🔒 Configurações de Segurança</h5>
          </div>
          <div class="card-body">
            <div class="mb-4">
              <h6>Alterar Senha</h6>
              <form @submit.prevent="changePassword">
                <div class="mb-3">
                  <label class="form-label">Senha Atual</label>
                  <input v-model="passwordForm.currentPassword" type="password" class="form-control" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Nova Senha</label>
                  <input v-model="passwordForm.newPassword" type="password" class="form-control" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Confirmar Nova Senha</label>
                  <input v-model="passwordForm.confirmPassword" type="password" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-primary">Alterar Senha</button>
              </form>
            </div>
            
            <div>
              <h6>Sessões Ativas</h6>
              <p class="text-muted small">Você está logado neste dispositivo.</p>
              <button class="btn btn-outline-danger btn-sm">Encerrar Outras Sessões</button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-md-4">
        <!-- Sidebar de Ações Rápidas -->
        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">⚡ Ações Rápidas</h5>
          </div>
          <div class="card-body">
            <button 
              @click="activeSection = 'personal'"
              class="btn btn-outline-primary w-100 mb-2 text-start"
            >
              📋 Editar Perfil
            </button>
            <button 
              @click="activeSection = 'addresses'"
              class="btn btn-outline-primary w-100 mb-2 text-start"
            >
              📍 Gerenciar Endereços
            </button>
            <button 
              @click="activeSection = 'security'"
              class="btn btn-outline-primary w-100 mb-2 text-start"
            >
              🔒 Alterar Senha
            </button>
            <hr>
            <router-link to="/prescriptions" class="btn btn-outline-secondary w-100 mb-2 text-start">
              📄 Minhas Receitas
            </router-link>
            <router-link to="/orders" class="btn btn-outline-secondary w-100 text-start">
              📦 Meus Pedidos
            </router-link>
          </div>
        </div>

        <!-- Resumo do Perfil -->
        <div v-show="activeSection === 'personal'" class="card mt-3">
          <div class="card-header">
            <h6 class="mb-0">📊 Resumo</h6>
          </div>
          <div class="card-body">
            <div class="small">
              <p><strong>Membro desde:</strong> {{ memberSince }}</p>
              <p><strong>Total de pedidos:</strong> {{ totalOrders }}</p>
              <p><strong>Endereços cadastrados:</strong> {{ addresses.length }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'Profile',
  data() {
    return {
      activeSection: 'personal', // Controle de seção ativa com v-show
      editMode: {
        personal: false
      },
      showAddAddressForm: false,
      profile: {
        name: '',
        email: '',
        phone: '',
        birthDate: '',
        cpf: ''
      },
      addresses: [
        {
          id: 1,
          nickname: 'Casa',
          street: 'Rua das Flores',
          number: '123',
          neighborhood: 'Centro',
          city: 'São Paulo',
          state: 'SP',
          zipCode: '01234-567',
          isDefault: true
        }
      ],
      newAddress: {
        nickname: '',
        street: '',
        number: '',
        neighborhood: '',
        city: '',
        state: '',
        zipCode: ''
      },
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      memberSince: 'Jan 2024',
      totalOrders: 5
    }
  },
  computed: {
    ...mapState(['user'])
  },
  mounted() {
    if (this.user) {
      this.profile.name = this.user.name || '';
      this.profile.email = this.user.email || '';
    }
  },
  methods: {
    toggleEditMode(section) {
      this.editMode[section] = !this.editMode[section];
    },
    
    updateProfile() {
      // Lógica para atualizar o perfil
      alert('Perfil atualizado com sucesso!');
      this.editMode.personal = false;
    },
    
    addAddress() {
      const address = {
        id: Date.now(),
        ...this.newAddress,
        isDefault: this.addresses.length === 0
      };
      this.addresses.push(address);
      this.cancelAddAddress();
      alert('Endereço adicionado com sucesso!');
    },
    
    cancelAddAddress() {
      this.showAddAddressForm = false;
      this.newAddress = {
        nickname: '',
        street: '',
        number: '',
        neighborhood: '',
        city: '',
        state: '',
        zipCode: ''
      };
    },
    
    changePassword() {
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        alert('As senhas não coincidem!');
        return;
      }
      alert('Senha alterada com sucesso!');
      this.passwordForm = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      };
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('pt-BR');
    }
  }
}
</script>

<style scoped>
.address-item {
  transition: background-color 0.2s;
}

.address-item:hover {
  background-color: #f8f9fa;
  padding: 10px;
  margin: -10px;
  border-radius: 5px;
}

.profile-info-view {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 5px;
}

.btn {
  transition: all 0.2s ease-in-out;
}

/* Transições suaves para v-show */
[v-show] {
  transition: opacity 0.3s ease-in-out;
}

.card {
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-2px);
}
</style>