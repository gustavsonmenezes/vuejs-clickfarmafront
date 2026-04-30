<template>
  <div class="category-management fade-in-up">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="mb-0 fw-bold"><i class="fas fa-tags me-2 text-primary"></i>Gestão de Categorias</h3>
      <button class="btn btn-primary" @click="abrirModalCriacao">
        <i class="fas fa-plus me-2"></i>Nova Categoria
      </button>
    </div>

    <!-- Tabela de Categorias -->
    <div class="card shadow-sm border-0 cf-card-premium overflow-hidden">
      <div class="card-body p-0">
        <div v-if="isLoading" class="text-center p-5">
          <div class="spinner-border text-primary" role="status"></div>
          <p class="mt-2 text-muted">Carregando categorias...</p>
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>ID</th>
                <th>Nome da Categoria</th>
                <th>Descrição</th>
                <th class="text-center">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="cat in categorias" :key="cat.id">
                <td>#{{ cat.id }}</td>
                <td>
                  <div class="fw-bold">{{ cat.nome }}</div>
                </td>
                <td>{{ cat.descricao || 'Sem descrição' }}</td>
                <td class="text-center">
                  <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-primary" @click="abrirModalEdicao(cat)" title="Editar">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-outline-danger" @click="deletarCategoria(cat.id)" title="Excluir">
                      <i class="fas fa-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="categorias.length === 0">
                <td colspan="4" class="text-center p-5 text-muted">Nenhuma categoria cadastrada.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal Criar/Editar -->
    <div v-if="showModal" class="cf-modal-overlay" @click.self="showModal = false">
      <div class="cf-modal-box animate__animated animate__zoomIn" style="width: 500px;">
        <div class="cf-modal-header">
          <div class="d-flex align-items-center gap-3">
            <div class="modal-icon-wrap bg-primary">
              <i class="fas fa-tag text-white"></i>
            </div>
            <div>
              <h5 class="mb-0 fw-bold">{{ editandoId ? 'Editar Categoria' : 'Nova Categoria' }}</h5>
            </div>
          </div>
          <button class="btn-close-custom" @click="showModal = false"><i class="fas fa-times"></i></button>
        </div>
        
        <div class="cf-modal-body p-4">
           <form id="categoryForm" @submit.prevent="salvarCategoria">
            <div class="mb-3">
              <label class="cf-label-premium">Nome da Categoria</label>
              <input type="text" class="cf-input-premium" v-model="form.nome" required placeholder="Ex: Higiene, Vitaminas...">
            </div>
            <div class="mb-3">
              <label class="cf-label-premium">Descrição (Opcional)</label>
              <textarea class="cf-input-premium" v-model="form.descricao" rows="3"></textarea>
            </div>
           </form>
        </div>

        <div class="cf-modal-footer">
          <button type="button" class="btn btn-light fw-bold px-4" @click="showModal = false">Cancelar</button>
          <button type="submit" form="categoryForm" class="btn btn-primary fw-bold px-4" :disabled="isSaving">
            <span v-if="isSaving" class="spinner-border spinner-border-sm me-2"></span>
            {{ editandoId ? 'Atualizar' : 'Criar' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'CategoryManagement',
  data() {
    return {
      categorias: [],
      isLoading: true,
      isSaving: false,
      showModal: false,
      editandoId: null,
      form: {
        nome: '',
        descricao: ''
      }
    }
  },
  async mounted() {
    await this.fetchCategorias();
  },
  methods: {
    async fetchCategorias() {
      this.isLoading = true;
      try {
        const res = await api.get('/categorias');
        this.categorias = res.data;
      } catch (err) {
        console.error('Erro ao buscar categorias:', err);
      } finally {
        this.isLoading = false;
      }
    },
    abrirModalCriacao() {
      this.editandoId = null;
      this.form = { nome: '', descricao: '' };
      this.showModal = true;
    },
    abrirModalEdicao(cat) {
      this.editandoId = cat.id;
      this.form = { nome: cat.nome, descricao: cat.descricao };
      this.showModal = true;
    },
    async salvarCategoria() {
      this.isSaving = true;
      try {
        if (this.editandoId) {
          await api.put(`/categorias/${this.editandoId}`, this.form);
        } else {
          await api.post('/categorias', this.form);
        }
        this.showModal = false;
        await this.fetchCategorias();
        // Opcional: Atualizar o store do Vuex
        this.$store.dispatch('fetchCategories');
      } catch (err) {
        console.error('Erro ao salvar categoria:', err);
        alert('Erro ao salvar categoria.');
      } finally {
        this.isSaving = false;
      }
    },
    async deletarCategoria(id) {
      if (confirm('Deseja realmente excluir esta categoria? Isso pode afetar produtos vinculados.')) {
        try {
          await api.delete(`/categorias/${id}`);
          await this.fetchCategorias();
          this.$store.dispatch('fetchCategories');
        } catch (err) {
          alert('Erro ao deletar categoria. Verifique se existem produtos vinculados a ela.');
        }
      }
    }
  }
}
</script>

<style scoped>
.category-management { padding-bottom: 2rem; }
.cf-card-premium { border-radius: 16px; border: 1px solid rgba(0,0,0,0.05); background: white; box-shadow: var(--cf-shadow-sm); }

/* Modal Styles consistent with other admin views */
.cf-modal-overlay {
  position: fixed; inset: 0; background: rgba(15, 23, 42, 0.7); 
  z-index: 2000; display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(6px); padding: 20px;
}
.cf-modal-box {
  background: white; border-radius: 24px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.4); 
  display: flex; flex-direction: column; overflow: hidden;
}
.cf-modal-header { 
  padding: 1.25rem 1.5rem; border-bottom: 1px solid #f1f5f9;
  display: flex; justify-content: space-between; align-items: center;
}
.modal-icon-wrap {
  width: 44px; height: 44px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center; font-size: 1.2rem;
}
.cf-modal-footer {
  padding: 1.25rem 1.5rem; border-top: 1px solid #f1f5f9;
  display: flex; justify-content: flex-end; gap: 1rem;
}
.cf-label-premium { font-size: 0.72rem; font-weight: 600; color: var(--cf-text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.45rem; display: block; }
.cf-input-premium {
  width: 100%; padding: 0.7rem 1rem; border-radius: 10px;
  border: 1px solid var(--cf-border-mid); background: #fff;
  font-size: 0.92rem; outline: none;
}
.cf-input-premium:focus { border-color: var(--cf-green); }
.btn-close-custom { background: none; border: none; cursor: pointer; }
.table th { font-size: 0.68rem; text-transform: uppercase; color: var(--cf-text-muted); }
</style>
