<template>
  <div class="container py-5 fade-in-up">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="card p-4 shadow-sm border-warning" style="border-top-width: 4px;">
          <h2 class="text-center mb-4"><i class="fas fa-store text-warning me-2"></i>Login Farmácia</h2>
          <form @submit.prevent="login">
            <div class="mb-3">
              <label>Email</label>
              <input type="email" class="form-control" v-model="email" required>
            </div>
            <div class="mb-3">
              <label>Senha</label>
              <input type="password" class="form-control" v-model="password" required>
            </div>
            <button type="submit" class="btn btn-warning w-100 fw-bold" :disabled="isLoading">
              <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
              {{ isLoading ? 'Entrando...' : 'Entrar no Painel' }}
            </button>
          </form>
          <div v-if="error" class="alert alert-danger mt-3 text-center small py-2">{{ error }}</div>
          <div class="text-center mt-3">
            <router-link to="/pharmacy/register">Não tem cadastro? Registre sua farmácia aqui.</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import api from '@/services/api';

const email = ref('');
const password = ref('');
const error = ref('');
const isLoading = ref(false);
const router = useRouter();
const store = useStore();

const login = async () => {
  isLoading.value = true;
  error.value = '';

  try {
    const response = await api.post('/auth/login', {
      email: email.value,
      senha: password.value
    });

    const { token, role, nome, id } = response.data;

    if (role !== 'PHARMACY') {
      error.value = 'Esta conta não está cadastrada como farmácia.';
      return;
    }

    const userData = { id, nome, email: email.value, role };
    
    localStorage.setItem('authToken', token);
    localStorage.setItem('user', JSON.stringify(userData));
    
    store.commit('SET_USER', userData);
    store.commit('SET_AUTH_TOKEN', token);

    router.push('/pharmacy/dashboard');
  } catch (err) {
    console.error('Erro no login farmácia:', err);
    if (err.response?.data && typeof err.response.data === 'object') {
      error.value = err.response.data.mensagem || Object.values(err.response.data)[0] || 'Falha na autenticação.';
    } else {
      error.value = 'Falha na autenticação. Verifique sua conexão.';
    }
  } finally {
    isLoading.value = false;
  }
};
</script>
