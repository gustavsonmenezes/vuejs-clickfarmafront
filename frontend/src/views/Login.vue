<template>
  <div class="auth-page-container py-5">
    <div class="row justify-content-center w-100 m-0">
      <div class="col-md-6 col-lg-5">
        <div class="card fade-in-up">
          <div class="card-header py-4">
            <h3 class="text-center mb-0 fw-bold">Entrar na ClickFarma</h3>
          </div>
          <div class="card-body p-4 p-md-5">
            <form @submit.prevent="handleLogin" ref="loginForm">
              <div class="mb-4">
                <label class="form-label">Email:</label>
                <input 
                  ref="emailInput"
                  type="email" 
                  class="form-control form-control-lg" 
                  v-model="credentials.email"
                  required
                  placeholder="seu@email.com"
                  @input="clearFieldError('email')"
                  :class="{ 'is-invalid': fieldErrors.email }"
                >
                <div v-if="fieldErrors.email" class="invalid-feedback">
                  {{ fieldErrors.email }}
                </div>
              </div>
              
              <div class="mb-4">
                <label class="form-label">Senha:</label>
                <input 
                  ref="passwordInput"
                  type="password" 
                  class="form-control form-control-lg" 
                  v-model="credentials.password"
                  required
                  placeholder="Sua senha"
                  @input="clearFieldError('password')"
                  :class="{ 'is-invalid': fieldErrors.password }"
                >
                <div v-if="fieldErrors.password" class="invalid-feedback">
                  {{ fieldErrors.password }}
                </div>
              </div>
              
              <button 
                ref="submitButton"
                type="submit" 
                class="btn btn-primary btn-lg w-100 py-3"
                :disabled="loading"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                {{ loading ? 'Entrando...' : 'Entrar na Minha Conta' }}
              </button>
            </form>
            
            <div class="text-center mt-4 pt-2">
              <p class="mb-2 text-muted">Ainda não tem conta? 
                <router-link to="/register" class="text-primary fw-bold">Cadastre-se aqui</router-link>
              </p>
              <p class="mb-0">
                <router-link to="/forgot-password" class="text-muted small">
                  <i class="fas fa-question-circle me-1"></i>
                  Não lembro minha senha
                </router-link>
              </p>
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
  name: 'Login',
  data() {
    return {
      credentials: {
        email: '',
        password: ''
      },
      loading: false,
      fieldErrors: {}
    }
  },
  mounted() {
    this.focusEmailField();
  },
  methods: {
    ...mapActions(['login']),
    
    focusEmailField() {
      if (this.$refs.emailInput) {
        this.$refs.emailInput.focus();
      }
    },
    
    validateForm() {
      this.fieldErrors = {};
      
      if (!this.credentials.email) {
        this.fieldErrors.email = 'Email é obrigatório';
        this.focusOnError('emailInput');
        return false;
      }
      
      if (!this.credentials.email.includes('@')) {
        this.fieldErrors.email = 'Email inválido';
        this.focusOnError('emailInput');
        return false;
      }
      
      if (!this.credentials.password) {
        this.fieldErrors.password = 'Senha é obrigatória';
        this.focusOnError('passwordInput');
        return false;
      }
      
      return true;
    },
    
    focusOnError(fieldRef) {
      if (this.$refs[fieldRef]) {
        this.$refs[fieldRef].focus();
        this.$refs[fieldRef].scrollIntoView({ 
          behavior: 'smooth', 
          block: 'center' 
        });
      }
    },
    
    clearFieldError(fieldName) {
      if (this.fieldErrors[fieldName]) {
        this.fieldErrors[fieldName] = null;
      }
    },
    
    shakeForm() {
      if (this.$refs.loginForm) {
        this.$refs.loginForm.classList.add('shake');
        setTimeout(() => {
          this.$refs.loginForm.classList.remove('shake');
        }, 500);
      }
    },
    
    async handleLogin() {
      if (!this.validateForm()) {
        this.shakeForm();
        return;
      }
      
      this.loading = true;
      
      try {
        const user = await this.login(this.credentials);
        
        // Saudação personalizada
        const userName = user.name?.split(' ')[0] || '';
        alert(`Olá, ${userName}! Que bom te ver de volta! 🎉`);
        
        this.$router.push('/');
        
      } catch (error) {
        console.error('Erro no login:', error);
        
        // Tratamento específico de erros
        if (error.message?.includes('email') || error.message?.includes('não encontrado')) {
          this.fieldErrors.email = 'Email não encontrado. Verifique ou crie uma conta.';
          this.focusOnError('emailInput');
        } else if (error.message?.includes('senha') || error.message?.includes('password') || error.message?.includes('incorreta')) {
          this.fieldErrors.password = 'Senha incorreta. Tente novamente ou recupere sua senha.';
          this.focusOnError('passwordInput');
        } else {
          alert(error.message || 'Erro ao fazer login. Tente novamente.');
        }
        
        this.shakeForm();
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.auth-page-container {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
}

.card {
  width: 100%;
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-xl);
  box-shadow: var(--cf-shadow-lg);
  overflow: hidden;
  background: white;
}

@media (max-width: 576px) {
  .card-header h3 { font-size: 1.4rem; }
  .card-body { padding: 1.5rem; }
  .auth-page-container { min-height: 70vh; padding: 1rem; }
}

.shake {
  animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}
</style>