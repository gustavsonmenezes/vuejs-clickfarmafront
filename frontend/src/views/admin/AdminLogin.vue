<template>
  <div class="admin-login container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="card">
          <div class="card-body">
            <h2 class="card-title text-center">Login Administrativo - ClickFarma</h2>
            <form @submit.prevent="login">
              <div class="form-group mb-3">
                <label for="email">Email</label>
                <input 
                  id="email" 
                  v-model="email" 
                  type="email" 
                  class="form-control" 
                  placeholder="admin@clickfarma.com" 
                  required 
                />
              </div>
              <div class="form-group mb-3">
                <label for="password">Senha</label>
                <input 
                  id="password" 
                  v-model="password" 
                  type="password" 
                  class="form-control" 
                  placeholder="senha123" 
                  required 
                />
              </div>
              <button 
                type="submit" 
                class="btn btn-primary w-100"
                :disabled="isLoading"
              >
                <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
                {{ isLoading ? 'Entrando...' : 'Entrar no Painel' }}
              </button>
            </form>
            <p v-if="error" class="alert alert-danger mt-3 text-center">{{ error }}</p>
            <div class="mt-3 p-3 bg-light rounded">
              <small class="text-muted">
                <strong>Credenciais para teste:</strong><br>
                Emails: jdts1@discente.ifpe.edu.br ou gmb6@discente.ifpe.edu.br<br>
                Senha: admin123click
              </small>
            </div>
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

    if (role !== 'ADMIN') {
      error.value = 'Acesso restrito apenas para administradores.';
      return;
    }

    const userData = { id, nome, email: email.value, role };
    
    // Salvar no localStorage conforme padrão do projeto
    localStorage.setItem('authToken', token);
    localStorage.setItem('user', JSON.stringify(userData));
    
    // Atualizar store
    store.commit('SET_USER', userData);
    store.commit('SET_AUTH_TOKEN', token);

    router.push('/admin/dashboard');
  } catch (err) {
    console.error('Erro no login admin:', err);
    error.value = err.response?.data?.mensagem || 'Falha na autenticação. Verifique suas credenciais.';
  } finally {
    isLoading.value = false;
  }
}
</script>

<style scoped>
.card { 
  box-shadow: 0 4px 6px rgba(0,0,0,0.1); 
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
}
</style>