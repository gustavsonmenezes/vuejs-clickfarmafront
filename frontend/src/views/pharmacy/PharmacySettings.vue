<template>
  <div class="cf-settings">
    <div class="cf-mgmt-header mb-4">
      <h4 class="cf-page-title"><span class="cf-dot gold"></span>Configurações da Farmácia</h4>
    </div>

    <div v-if="isLoading" class="text-center py-5">
      <div class="cf-spinner mx-auto"></div>
      <p class="mt-3 text-muted">Carregando dados da unidade...</p>
    </div>

    <div v-else class="row g-4">
      <!-- Coluna Esquerda: Dados Básicos -->
      <div class="col-lg-8">
        <div class="cf-card-premium">
          <div class="card-header-premium">
            <i class="fas fa-store me-2"></i>Informações Gerais
          </div>
          <div class="p-4">
            <div class="row g-3">
              <div class="col-md-6">
                <label class="cf-label-premium">Nome da Farmácia</label>
                <input v-model="form.nome" type="text" class="cf-input-premium" placeholder="Nome Fantasia">
              </div>
              <div class="col-md-6">
                <label class="cf-label-premium">CNPJ (Somente Leitura)</label>
                <input v-model="form.cnpj" type="text" class="cf-input-premium bg-light" readonly>
              </div>
              <div class="col-md-6">
                <label class="cf-label-premium">Telefone de Contato</label>
                <input v-model="form.telefone" type="text" class="cf-input-premium" placeholder="(00) 00000-0000">
              </div>
              <div class="col-md-6">
                <label class="cf-label-premium">Email Comercial</label>
                <input v-model="form.email" type="email" class="cf-input-premium" placeholder="contato@farmacia.com">
              </div>
              <div class="col-12">
                <label class="cf-label-premium">Endereço Completo</label>
                <input v-model="form.endereco" type="text" class="cf-input-premium" placeholder="Rua, Número, Bairro, Cidade - UF">
              </div>
            </div>
          </div>
        </div>

        <div class="cf-card-premium mt-4">
          <div class="card-header-premium bg-gold-light text-dark">
            <i class="fas fa-wallet me-2"></i>Dados para Recebimento (PIX)
          </div>
          <div class="p-4">
            <div class="alert alert-info border-0 rounded-4 small mb-4">
              <i class="fas fa-info-circle me-2"></i>
              Estes dados serão utilizados para os repasses automáticos das suas vendas na plataforma.
            </div>
            <div class="row g-3">
              <div class="col-md-5">
                <label class="cf-label-premium">Tipo de Chave</label>
                <select v-model="form.tipoChavePix" class="cf-input-premium">
                  <option value="CNPJ">CNPJ</option>
                  <option value="CPF">CPF</option>
                  <option value="EMAIL">E-mail</option>
                  <option value="TELEFONE">Telefone</option>
                  <option value="ALEATORIA">Chave Aleatória</option>
                </select>
              </div>
              <div class="col-md-7">
                <label class="cf-label-premium">Chave PIX</label>
                <input v-model="form.chavePix" type="text" class="cf-input-premium" placeholder="Insira sua chave aqui">
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Coluna Direita: Foto e Ações -->
      <div class="col-lg-4">
        <div class="cf-card-premium text-center p-4">
          <label class="cf-label-premium text-start mb-3">Logo / Imagem da Unidade</label>
          <div class="avatar-edit-wrap mb-3">
            <img :src="form.fotoUrl || 'https://cdn-icons-png.flaticon.com/512/883/883360.png'" class="img-fluid rounded-4 shadow-sm border" style="width: 150px; height: 150px; object-fit: cover;">
          </div>
          <input v-model="form.fotoUrl" type="text" class="cf-input-premium small mb-2" placeholder="URL da Logo">
          <p class="text-muted small">Recomendado: 400x400px</p>
        </div>

        <div class="d-grid mt-4">
          <button class="cf-btn-save" @click="salvarConfig" :disabled="isSaving">
            <i v-if="isSaving" class="spinner-border spinner-border-sm me-2"></i>
            <i v-else class="fas fa-check-circle me-2"></i>
            Salvar Alterações
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'PharmacySettings',
  data() {
    return {
      isLoading: true,
      isSaving: false,
      farmaciaId: null,
      form: {
        nome: '', cnpj: '', telefone: '', email: '',
        endereco: '', fotoUrl: '', chavePix: '', tipoChavePix: 'CNPJ'
      }
    };
  },
  async mounted() {
    await this.fetchFarmacia();
  },
  methods: {
    async fetchFarmacia() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const { data } = await api.get('/farmacias');
        const far = data.find(f => f.email === user.email);
        if (far) {
          this.farmaciaId = far.id;
          this.form = { ...far };
        }
      } catch (err) {
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    async salvarConfig() {
      this.isSaving = true;
      try {
        // Limpeza de campos para o DTO do backend
        const payload = { ...this.form };
        delete payload.usuario;
        delete payload.dataCadastro;
        
        await api.put(`/farmacias/${this.farmaciaId}`, payload);
        alert('Configurações salvas com sucesso!');
      } catch (err) {
        alert('Erro ao salvar configurações.');
      } finally {
        this.isSaving = false;
      }
    }
  }
};
</script>

<style scoped>
.cf-settings { padding-bottom: 3rem; }
.cf-card-premium { background: #fff; border-radius: 20px; border: 1px solid var(--cf-border); overflow: hidden; box-shadow: var(--cf-shadow-sm); }
.card-header-premium { padding: 1.2rem 1.5rem; background: var(--cf-ivory); border-bottom: 1px solid var(--cf-border); font-weight: 700; font-size: 0.95rem; color: var(--cf-text-dark); }

.cf-input-premium { width: 100%; padding: 0.75rem 1rem; border-radius: 12px; border: 1px solid var(--cf-border-mid); background: #fff; font-size: 0.9rem; outline: none; transition: all 0.2s; }
.cf-input-premium:focus { border-color: var(--cf-green); box-shadow: 0 0 0 4px rgba(42,92,69,0.06); }
.cf-label-premium { font-size: 0.7rem; font-weight: 600; color: var(--cf-text-muted); text-transform: uppercase; margin-bottom: 0.5rem; display: block; letter-spacing: 0.05em; }

.cf-btn-save { background: var(--cf-green); color: #fff; border: none; padding: 1rem; border-radius: 16px; font-weight: 700; font-size: 1rem; cursor: pointer; transition: all 0.2s; box-shadow: 0 4px 12px rgba(42,92,69,0.2); }
.cf-btn-save:hover { background: var(--cf-green-dark); transform: translateY(-2px); box-shadow: 0 6px 16px rgba(42,92,69,0.3); }
.cf-btn-save:disabled { opacity: 0.7; transform: none; }

.bg-gold-light { background: var(--cf-gold-light); }

.cf-spinner { width: 40px; height: 40px; border: 3px solid var(--cf-border); border-top-color: var(--cf-green); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
