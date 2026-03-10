<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="card">
          <div class="card-header">
            <h3 class="text-center">🔐 Redefinir Senha</h3>
          </div>
          <div class="card-body">
            <p class="text-muted text-center mb-4">
              Informe seu email para receber instruções de redefinição de senha
            </p>
            
            <form @submit.prevent="handleSubmit">
              <div class="mb-3">
                <label class="form-label">Email:</label>
                <input 
                  type="email" 
                  class="form-control" 
                  v-model="email"
                  required
                  placeholder="seu@email.com"
                >
              </div>
              
              <button 
                type="submit" 
                class="btn btn-primary w-100"
                :disabled="loading"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
                {{ loading ? 'Enviando...' : 'Enviar Instruções' }}
              </button>
            </form>
            
            <div class="text-center mt-3">
              <p>Lembrou sua senha? <router-link to="/login">Fazer login</router-link></p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'ForgotPassword',
  data() {
    return {
      email: '',
      loading: false
    }
  },
  methods: {
    ...mapActions(['requestPasswordReset']),
    
    async handleSubmit() {
      this.loading = true
      try {
        await this.requestPasswordReset(this.email)
        this.$toast.success('Email de redefinição enviado com sucesso!')
        this.email = ''
      } catch (error) {
        this.$toast.error(error.message || 'Erro ao solicitar redefinição de senha')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.card {
  border: none;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.card-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  border-radius: 10px 10px 0 0 !important;
}
</style>