<template>
  <div class="admin-login">
    <div class="login-bg"></div>
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <i class="fas fa-cross"></i>
        </div>
        <h2 class="mb-1">Painel Administrativo</h2>
        <p class="mb-0 text-muted">ClickFarma</p>
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

        <button type="submit" class="login-btn" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
          {{ isLoading ? 'Entrando...' : 'Entrar no Painel' }}
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
        this.error = 'Credenciais inválidas.';
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
  background: var(--cf-slate-900);
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 50%, rgba(21, 128, 61, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 50%, rgba(14, 165, 233, 0.1) 0%, transparent 50%);
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  animation: slideUp 400ms ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-header {
  text-align: center;
  padding: 36px 32px 24px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--cf-primary-700), var(--cf-primary-500));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  margin: 0 auto 16px;
  box-shadow: 0 8px 24px rgba(21, 128, 61, 0.25);
}

.login-header h2 {
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--cf-slate-900);
  letter-spacing: -0.02em;
}

.login-form { padding: 0 32px 24px; }

.form-label {
  display: block;
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--cf-slate-700);
  margin-bottom: 6px;
}

.login-btn {
  width: 100%;
  padding: 12px 24px;
  background: var(--cf-primary-700);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.9375rem;
  font-weight: 600;
  font-family: var(--cf-font);
  cursor: pointer;
  transition: all 0.15s;
  box-shadow: 0 1px 3px rgba(21, 128, 61, 0.2);
}

.login-btn:hover:not(:disabled) {
  background: var(--cf-primary-800);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(21, 128, 61, 0.3);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  margin: 0 32px 16px;
  background: var(--cf-danger-light);
  border: 1px solid rgba(220, 38, 38, 0.15);
  border-radius: 8px;
  color: var(--cf-danger);
  font-size: 0.8125rem;
}

.login-footer {
  padding: 16px 32px;
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
  transition: color 0.15s;
}

.back-link:hover { color: var(--cf-primary-700); }

.spinner-border-sm { width: 1rem; height: 1rem; }
</style>
