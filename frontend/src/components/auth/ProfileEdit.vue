<template>
  <div class="profile-edit">
    <div v-if="loading" class="text-center py-4">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
    </div>

    <div v-else>
      <ul class="nav nav-tabs mb-4" id="profileTabs" role="tablist">
        <li class="nav-item" role="presentation">
          <button class="nav-link active" id="personal-tab" data-bs-toggle="tab" 
                  data-bs-target="#personal" type="button" role="tab">
            <i class="fas fa-user me-2"></i>Dados Pessoais
          </button>
        </li>
        <li class="nav-item" role="presentation">
          <button class="nav-link" id="security-tab" data-bs-toggle="tab" 
                  data-bs-target="#security" type="button" role="tab">
            <i class="fas fa-lock me-2"></i>Segurança
          </button>
        </li>
      </ul>
      
      <div class="tab-content" id="profileTabsContent">
        <!-- Dados Pessoais -->
        <div class="tab-pane fade show active" id="personal" role="tabpanel">
          <form @submit.prevent="updateProfile">
            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label">Nome completo</label>
                <input type="text" class="form-control" v-model="userData.name" required>
              </div>
              
              <div class="col-md-6 mb-3">
                <label class="form-label">E-mail</label>
                <input type="email" class="form-control" v-model="userData.email" required>
              </div>
            </div>
            
            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label">Telefone</label>
                <input type="tel" class="form-control" v-model="userData.phone" 
                       placeholder="(81) 99999-9999">
              </div>
              
              <div class="col-md-6 mb-3">
                <label class="form-label">Data de Nascimento</label>
                <input type="date" class="form-control" v-model="userData.birthdate">
              </div>
            </div>
            
            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label">CPF</label>
                <input type="text" class="form-control" v-model="userData.cpf" 
                       placeholder="000.000.000-00">
              </div>
            </div>
            
            <div class="d-flex justify-content-end gap-2 mt-4">
              <button type="button" class="btn btn-outline-secondary" @click="resetForm">
                Cancelar
              </button>
              <button type="submit" class="btn btn-primary" :disabled="updating">
                <span v-if="updating" class="spinner-border spinner-border-sm me-1" role="status"></span>
                {{ updating ? 'Salvando...' : 'Salvar Alterações' }}
              </button>
            </div>
          </form>
        </div>
        
        <!-- Segurança -->
        <div class="tab-pane fade" id="security" role="tabpanel">
          <form @submit.prevent="changePassword">
            <div class="row">
              <div class="col-md-6">
                <div class="mb-3">
                  <label class="form-label">Senha Atual</label>
                  <input type="password" class="form-control" v-model="passwordData.current" required>
                </div>
                
                <div class="mb-3">
                  <label class="form-label">Nova Senha</label>
                  <input type="password" class="form-control" v-model="passwordData.new" required>
                </div>
                
                <div class="mb-3">
                  <label class="form-label">Confirmar Nova Senha</label>
                  <input type="password" class="form-control" v-model="passwordData.confirm" required>
                </div>
                
                <div class="d-flex justify-content-end gap-2 mt-4">
                  <button type="submit" class="btn btn-primary" :disabled="changingPassword">
                    <span v-if="changingPassword" class="spinner-border spinner-border-sm me-1" role="status"></span>
                    {{ changingPassword ? 'Alterando...' : 'Alterar Senha' }}
                  </button>
                </div>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'ProfileEdit',
  data() {
    return {
      userData: {
        name: '',
        email: '',
        phone: '',
        birthdate: '',
        cpf: ''
      },
      passwordData: {
        current: '',
        new: '',
        confirm: ''
      },
      loading: true,
      updating: false,
      changingPassword: false
    }
  },
  computed: {
    ...mapGetters(['user'])
  },
  methods: {
    ...mapActions(['updateUserProfile', 'changeUserPassword']),
    
    async updateProfile() {
      this.updating = true
      try {
        await this.updateUserProfile(this.userData)
        this.$toast.success('Perfil atualizado com sucesso!')
      } catch (error) {
        this.$toast.error(error.message || 'Erro ao atualizar perfil')
      } finally {
        this.updating = false
      }
    },
    
    async changePassword() {
      if (this.passwordData.new !== this.passwordData.confirm) {
        this.$toast.error('As senhas não coincidem!')
        return
      }
      
      this.changingPassword = true
      try {
        await this.changeUserPassword(this.passwordData)
        this.$toast.success('Senha alterada com sucesso!')
        this.passwordData = { current: '', new: '', confirm: '' }
      } catch (error) {
        this.$toast.error(error.message || 'Erro ao alterar senha')
      } finally {
        this.changingPassword = false
      }
    },
    
    resetForm() {
      this.loadUserData()
    },
    
    loadUserData() {
      if (this.user) {
        this.userData = { ...this.user }
      }
      this.loading = false
    }
  },
  mounted() {
    this.loadUserData()
  }
}
</script>

<style scoped>
.nav-tabs .nav-link {
  color: #495057;
  font-weight: 500;
}

.nav-tabs .nav-link.active {
  color: #0d6efd;
  font-weight: 600;
}

.form-label {
  font-weight: 500;
  margin-bottom: 0.5rem;
}
</style>