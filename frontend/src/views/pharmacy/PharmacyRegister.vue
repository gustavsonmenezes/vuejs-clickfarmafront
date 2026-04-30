<template>
  <div class="container py-5 fade-in-up">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <div class="card shadow-sm border-warning" style="border-top-width: 4px;">
          <div class="card-body p-4">
            <h3 class="text-center mb-4"><i class="fas fa-store-alt text-warning me-2"></i>Cadastro de Farmácia Parceira</h3>
            <form @submit.prevent="register">
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Nome da Farmácia</label>
                  <input type="text" class="form-control" v-model="form.nome" required placeholder="Ex: Farmácia Saúde">
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">CNPJ</label>
                  <input type="text" class="form-control" v-model="form.cnpj" required placeholder="00.000.000/0000-00">
                </div>
              </div>
              <div class="row">
                <div class="col-md-8 mb-3">
                  <label class="form-label">Endereço (Rua, Número, Bairro)</label>
                  <input type="text" class="form-control" v-model="form.endereco" required>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="form-label">Cidade</label>
                  <input type="text" class="form-control" v-model="form.cidade" required>
                </div>
              </div>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Email</label>
                  <input type="email" class="form-control" v-model="form.email" required>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Senha</label>
                  <input type="password" class="form-control" v-model="form.senha" required>
                </div>
              </div>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Telefone de Contato</label>
                  <input type="text" class="form-control" v-model="form.telefone" required>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">URL da Foto/Logotipo</label>
                  <input type="url" class="form-control" v-model="form.fotoUrl">
                </div>
              </div>
              
              <button type="submit" class="btn btn-warning w-100 fw-bold mt-3">Cadastrar Farmácia</button>
            </form>
            <div v-if="error" class="alert alert-danger mt-3 text-center small py-2">{{ error }}</div>
            <div class="text-center mt-3">
              <router-link to="/pharmacy/login">Já possui cadastro? Faça login.</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import farmaciasService from '@/services/farmaciasService';

const router = useRouter();
const isLoading = ref(false);
const error = ref('');

const form = reactive({
  nome: '',
  cnpj: '',
  endereco: '',
  cidade: '',
  email: '',
  senha: '',
  telefone: '',
  fotoUrl: ''
});

const register = async () => {
  isLoading.value = true;
  error.value = '';
  try {
    await farmaciasService.registrar(form);
    alert('Cadastro realizado com sucesso! Agora você pode fazer login.');
    router.push('/pharmacy/login');
  } catch (err) {
    console.error('Erro no cadastro de farmácia:', err);
    if (err.response?.data && typeof err.response.data === 'object') {
      const data = err.response.data;
      if (data.mensagem) {
        error.value = data.mensagem;
      } else {
        // Pega o primeiro erro de validação encontrado
        error.value = Object.values(data)[0] || 'Dados inválidos.';
      }
    } else {
      error.value = 'Erro ao realizar cadastro. Verifique sua conexão.';
    }
  } finally {
    isLoading.value = false;
  }
};
</script>
