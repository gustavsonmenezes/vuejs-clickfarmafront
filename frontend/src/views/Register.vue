<template>
  <div class="auth-page-container py-5">
    <div class="row justify-content-center w-100 m-0">
      <div class="col-md-7 col-lg-6">
        <div class="card fade-in-up">
          <div class="card-header py-4">
            <h3 class="text-center mb-0 fw-bold">Criar sua Conta</h3>
          </div>
          <div class="card-body p-4 p-md-5">
            
            <!-- Mensagem de Boas-Vindas -->
            <div v-if="showWelcome" class="alert alert-success animate__animated animate__fadeIn">
              <h5 class="fw-bold">🎉 Bem-vindo(a) à família ClickFarma!</h5>
              <p class="mb-0">Que bom ter você conosco! Agora você pode explorar farmácias perto de você e fazer suas compras com toda a praticidade.</p>
            </div>

            <!-- Sugestão quando email já existe -->
            <div v-if="showRecoveryOption" class="alert alert-info animate__animated animate__fadeIn">
              <p class="mb-2">Parece que você já é da família! Que tal fazer login?</p>
              <div class="d-flex gap-2">
                <router-link to="/login" class="btn btn-sm btn-primary">
                  Fazer Login
                </router-link>
                <router-link to="/forgot-password" class="btn btn-sm btn-outline-primary">
                  Recuperar Senha
                </router-link>
              </div>
            </div>

            <form @submit.prevent="handleRegister" ref="registerForm">
              <div class="mb-4">
                <label class="form-label">Nome completo:</label>
                <input 
                  ref="nameInput"
                  type="text" 
                  class="form-control form-control-lg" 
                  v-model="userData.name"
                  required
                  placeholder="Seu nome completo"
                  @input="clearFieldError('name')"
                  :class="{ 'is-invalid': fieldErrors.name }"
                >
                <div v-if="fieldErrors.name" class="invalid-feedback">
                  {{ fieldErrors.name }}
                </div>
              </div>
              
              <div class="mb-4">
                <label class="form-label">Email:</label>
                <input 
                  ref="emailInput"
                  type="email" 
                  class="form-control form-control-lg" 
                  v-model="userData.email"
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
                  v-model="userData.password"
                  required
                  placeholder="Mínimo 6 caracteres"
                  @input="validatePasswordStrength()"
                  :class="{ 
                    'is-invalid': fieldErrors.password,
                    'is-valid': passwordStrength.score > 2 
                  }"
                >
                <div v-if="fieldErrors.password" class="invalid-feedback">
                  {{ fieldErrors.password }}
                </div>
                <div v-if="userData.password" class="password-strength mt-2">
                  <div class="d-flex justify-content-between align-items-center mb-1">
                    <small class="text-muted">Força da senha:</small>
                    <small :class="passwordStrength.class" class="fw-bold">{{ passwordStrength.text }}</small>
                  </div>
                  <div class="progress" style="height: 4px;">
                    <div 
                      class="progress-bar" 
                      :class="passwordStrength.class.replace('text-', 'bg-')"
                      :style="{ width: passwordStrength.percentage + '%' }"
                    ></div>
                  </div>
                </div>
              </div>
              
              <div class="mb-5">
                <label class="form-label">Confirmar senha:</label>
                <input 
                  ref="confirmPasswordInput"
                  type="password" 
                  class="form-control form-control-lg" 
                  v-model="userData.confirmPassword"
                  required
                  placeholder="Digite novamente sua senha"
                  @input="clearFieldError('confirmPassword')"
                  :class="{ 'is-invalid': fieldErrors.confirmPassword }"
                >
                <div v-if="fieldErrors.confirmPassword" class="invalid-feedback">
                  {{ fieldErrors.confirmPassword }}
                </div>
              </div>
              
              <button 
                ref="submitButton"
                type="submit" 
                class="btn btn-primary btn-lg w-100 py-3"
                :disabled="loading"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                {{ loading ? 'Criando conta...' : 'Criar Minha Conta' }}
              </button>
            </form>
            
            <div class="text-center mt-4">
              <p class="text-muted">Já tem conta? <router-link to="/login" class="text-primary fw-bold">Faça login aqui</router-link></p>
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
  name: 'Register',
  data() {
    return {
      userData: {
        name: '',
        email: '',
        password: '',
        confirmPassword: ''
      },
      loading: false,
      fieldErrors: {},
      showWelcome: false,
      showRecoveryOption: false,
      passwordStrength: {
        score: 0,
        text: 'Muito fraca',
        class: 'text-danger',
        percentage: 0
      }
    }
  },
  mounted() {
    this.focusNameField();
  },
  methods: {
    ...mapActions(['register']),
    
    focusNameField() {
      if (this.$refs.nameInput) {
        this.$refs.nameInput.focus();
      }
    },
    
    validatePasswordStrength() {
      const password = this.userData.password;
      let score = 0;
      
      if (password.length >= 8) score++;
      if (/[A-Z]/.test(password)) score++;
      if (/[0-9]/.test(password)) score++;
      if (/[^A-Za-z0-9]/.test(password)) score++;
      
      this.passwordStrength.score = score;
      
      switch (score) {
        case 0:
        case 1:
          this.passwordStrength.text = 'Muito fraca';
          this.passwordStrength.class = 'text-danger';
          this.passwordStrength.percentage = 25;
          break;
        case 2:
          this.passwordStrength.text = 'Fraca';
          this.passwordStrength.class = 'text-warning';
          this.passwordStrength.percentage = 50;
          break;
        case 3:
          this.passwordStrength.text = 'Boa';
          this.passwordStrength.class = 'text-info';
          this.passwordStrength.percentage = 75;
          break;
        case 4:
          this.passwordStrength.text = 'Forte';
          this.passwordStrength.class = 'text-success';
          this.passwordStrength.percentage = 100;
          break;
      }
    },
    
    validateForm() {
      this.fieldErrors = {};
      this.showRecoveryOption = false;
      let isValid = true;
      
      if (!this.userData.name.trim()) {
        this.fieldErrors.name = 'Nome é obrigatório';
        this.focusOnError('nameInput');
        isValid = false;
      }
      
      if (!this.userData.email) {
        this.fieldErrors.email = 'Email é obrigatório';
        if (isValid) this.focusOnError('emailInput');
        isValid = false;
      } else if (!this.userData.email.includes('@')) {
        this.fieldErrors.email = 'Email inválido';
        if (isValid) this.focusOnError('emailInput');
        isValid = false;
      }
      
      if (!this.userData.password) {
        this.fieldErrors.password = 'Senha é obrigatória';
        if (isValid) this.focusOnError('passwordInput');
        isValid = false;
      } else if (this.userData.password.length < 6) {
        this.fieldErrors.password = 'Senha deve ter pelo menos 6 caracteres';
        if (isValid) this.focusOnError('passwordInput');
        isValid = false;
      }
      
      if (!this.userData.confirmPassword) {
        this.fieldErrors.confirmPassword = 'Confirme sua senha';
        if (isValid) this.focusOnError('confirmPasswordInput');
        isValid = false;
      } else if (this.userData.password !== this.userData.confirmPassword) {
        this.fieldErrors.confirmPassword = 'As senhas não coincidem';
        if (isValid) this.focusOnError('confirmPasswordInput');
        isValid = false;
      }
      
      return isValid;
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
    
    async handleRegister() {
      if (!this.validateForm()) {
        this.shakeForm();
        return;
      }

      this.loading = true;
      this.showWelcome = false;

      try {
        const userDataToSend = {
          nome: this.userData.name,
          email: this.userData.email,
          senha: this.userData.password
        };

        await this.register(userDataToSend);
        this.showWelcome = true;

        setTimeout(() => {
          this.$router.push('/');
        }, 3000);

      } catch (error) {
        console.error('❌ Erro no registro:', error);

        if (error.message?.includes('email') || error.message?.includes('já existe') || error.message?.includes('already')) {
          this.fieldErrors.email = 'Parece que você já é da família!';
          this.showRecoveryOption = true;
          this.focusOnError('emailInput');
        } else if (error.message?.includes('senha') || error.message?.includes('password')) {
          this.fieldErrors.password = error.message;
          this.focusOnError('passwordInput');
        } else {
          alert(error.message || 'Erro ao criar conta. Tente novamente.');
        }

        this.shakeForm();
      } finally {
        this.loading = false;
      }
    },
    
    shakeForm() {
      if (this.$refs.registerForm) {
        this.$refs.registerForm.classList.add('shake');
        setTimeout(() => {
          this.$refs.registerForm.classList.remove('shake');
        }, 500);
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
  max-width: 580px;
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