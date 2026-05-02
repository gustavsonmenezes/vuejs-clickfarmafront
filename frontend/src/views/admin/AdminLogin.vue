<template>
  <div class="admin-login">
    <div class="login-card cf-card">
      <div class="login-header">
        <div class="logo-icon">
          <i class="fas fa-shield-halved"></i>
        </div>
        <h2 class="mb-1">Painel Admin</h2>
        <p class="mb-0 cf-text-muted">ClickFarma</p>
      </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group mb-3">
          <label for="email" class="form-label">Email</label>
          <input id="email" v-model="email" type="email" class="cf-input" placeholder="seu@email.com" required />
        </div>
        
        <div class="form-group mb-4">
          <label for="password" class="form-label">Senha</label>
          <input id="password" v-model="password" type="password" class="cf-input" placeholder="••••••••" required />
        </div>
        
        <button type="submit" class="cf-btn cf-btn-primary w-100" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
          {{ isLoading ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>
      
      <p v-if="error" class="error-message">
        <i class="fas fa-circle-exclamation"></i>
        {{ error }}
      </p>
      
      <div class="login-footer">
        <router-link to="/" class="back-link">
          <i class="fas fa-arrow-left"></i>
          Voltar à loja
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex';

export default {
  name: 'AdminLogin',
  data() {
    return {
      email: '',
      password: '',
      error: '',
      isLoading: false
    }
  },
  methods: {
    ...mapActions(['login']),
    async handleLogin() {
      this.isLoading = true;
      this.error = '';
      
      try {
        const user = await this.login({ email: this.email, senha: this.password });

        if (user && user.role && user.role.toUpperCase() === 'ADMIN') {
          this.$router.push('/admin');
        } else {
          this.error = 'Acesso negado: apenas administradores.';
        }
      } catch (e) {
        this.error = e.message || 'Credenciais inválidas.';
      } finally {
        this.isLoading = false;
      }
    }
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--cf-bg);
  padding: 24px;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  padding: 32px 24px 24px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: var(--cf-primary-600);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  margin: 0 auto 16px;
}

.login-header h2 {
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--cf-slate-900);
  letter-spacing: -0.02em;
}

.login-form { padding: 0 24px 24px; }

.form-label {
  display: block;
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--cf-slate-700);
  margin-bottom: 6px;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  margin: 0 24px 16px;
  background: var(--cf-danger-light);
  border: 1px solid rgba(220, 38, 38, 0.2);
  border-radius: var(--cf-radius-md);
  color: var(--cf-danger);
  font-size: 0.875rem;
}

.login-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--cf-border);
  text-align: center;
}

.back-link {
  color: var(--cf-slate-500);
  text-decoration: none;
  font-size: 0.8125rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: color 0.15s ease;
}

.back-link:hover { color: var(--cf-primary-600); }

.spinner-border-sm { width: 1rem; height: 1rem; }
</style>
