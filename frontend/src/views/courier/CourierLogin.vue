<template>
  <div class="cf-login-page">
    <div class="cf-login-card animate__animated animate__fadeInUp">
      <div class="text-center mb-4">
        <div class="cf-logo-icon bg-primary"><i class="fas fa-motorcycle"></i></div>
        <h2 class="cf-login-title mt-3">Portal do Entregador</h2>
        <p class="text-muted">Acesse suas entregas e ganhos</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label class="cf-label">E-mail</label>
          <input v-model="email" type="email" class="cf-input" placeholder="seu@email.com" required>
        </div>
        <div class="mb-4">
          <label class="cf-label">Senha</label>
          <input v-model="password" type="password" class="cf-input" placeholder="••••••••" required>
        </div>

        <button type="submit" class="cf-btn-primary w-100 py-3" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          Entrar no Painel
        </button>
      </form>
      
      <div class="mt-4 text-center">
        <router-link to="/login" class="text-decoration-none small text-muted">Voltar para o site</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'CourierLogin',
  data() {
    return { email: '', password: '', loading: false };
  },
  methods: {
    async handleLogin() {
      this.loading = true;
      try {
        const res = await api.post('/auth/login', { email: this.email, senha: this.password });
        localStorage.setItem('authToken', res.data.token);
        localStorage.setItem('user', JSON.stringify(res.data));
        this.$router.push('/courier/dashboard');
      } catch (err) {
        alert('Credenciais inválidas para entregador.');
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.cf-login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #f8fafc; padding: 20px; }
.cf-login-card { background: #fff; width: 100%; max-width: 400px; padding: 2.5rem; border-radius: 24px; box-shadow: 0 20px 50px rgba(0,0,0,0.05); }
.cf-logo-icon { width: 60px; height: 60px; border-radius: 18px; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 1.8rem; margin: 0 auto; }
.cf-login-title { font-family: var(--cf-serif); font-weight: 800; font-size: 1.5rem; color: #1e293b; }
.cf-label { font-size: 0.75rem; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem; display: block; }
.cf-input { width: 100%; padding: 0.8rem 1rem; border-radius: 12px; border: 2px solid #f1f5f9; background: #f8fafc; transition: all 0.2s; }
.cf-input:focus { border-color: #3b82f6; outline: none; background: #fff; }
.cf-btn-primary { background: #3b82f6; color: #fff; border: none; border-radius: 12px; font-weight: 700; transition: all 0.2s; }
.cf-btn-primary:hover { background: #2563eb; transform: translateY(-2px); box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.3); }
</style>
