<template>
  <div class="product-management fade-in-up">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="mb-0 fw-bold"><i class="fas fa-pills me-2 text-warning"></i>Gestão Global de Produtos</h3>
      <div class="d-flex gap-2">
        <button class="btn btn-outline-secondary btn-sm" @click="fetchProducts">
          <i class="fas fa-sync-alt me-1"></i> Atualizar
        </button>
      </div>
    </div>

    <!-- Filtros -->
    <div class="row g-3 mb-4">
      <div class="col-md-5">
        <div class="input-group">
          <span class="input-group-text bg-white border-end-0"><i class="fas fa-search text-muted"></i></span>
          <input v-model="busca" type="text" class="form-control border-start-0" placeholder="Buscar por nome, princípio ou laboratório...">
        </div>
      </div>
      <div class="col-md-4">
        <select v-model="filtroFarmacia" class="form-select">
          <option value="todas">Todas as Farmácias</option>
          <option v-for="f in farmacias" :key="f.id" :value="f.id">{{ f.nome }}</option>
        </select>
      </div>
      <div class="col-md-3">
        <select v-model="filtroReceita" class="form-select">
          <option value="todos">Prescrição: Todos</option>
          <option :value="true">Apenas com Receita</option>
          <option :value="false">Sem Receita</option>
        </select>
      </div>
    </div>

    <!-- Tabela Global -->
    <div class="card shadow-sm border-0 cf-card-premium overflow-hidden">
      <div class="card-body p-0">
        <div v-if="isLoading" class="text-center p-5">
          <div class="spinner-border text-warning" role="status"></div>
          <p class="mt-2 text-muted">Carregando catálogo completo...</p>
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Produto / Origem</th>
                <th>Estoque</th>
                <th>Preço</th>
                <th>Status</th>
                <th class="text-center">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="produto in produtosFiltrados" :key="produto.id">
                <td>
                  <div class="d-flex align-items-center">
                    <img :src="produto.imageUrl || 'https://cdn-icons-png.flaticon.com/512/883/883360.png'" 
                         class="rounded border me-3" width="40" height="40" style="object-fit: cover;">
                    <div>
                      <h6 class="mb-0 fw-bold">{{ produto.nome }}</h6>
                      <small class="text-primary d-block">
                        <i class="fas fa-store me-1"></i>{{ produto.farmaciaNome || 'Desconhecida' }}
                      </small>
                    </div>
                  </div>
                </td>
                <td>
                  <span class="badge" :class="produto.estoque > 0 ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-danger-subtle text-danger border border-danger-subtle'">
                    {{ produto.estoque }} un
                  </span>
                </td>
                <td class="fw-bold">R$ {{ produto.preco.toFixed(2) }}</td>
                <td>
                  <span v-if="produto.necessitaReceita" class="badge bg-danger text-white">Receita</span>
                  <span v-else class="badge bg-light text-dark border">Livre</span>
                </td>
                <td class="text-center">
                  <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-primary" @click="abrirModalEdicao(produto)" title="Editar">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-outline-danger" @click="deletarProduto(produto.id)" title="Excluir">
                      <i class="fas fa-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="produtosFiltrados.length === 0">
                <td colspan="5" class="text-center p-5 text-muted">Nenhum produto encontrado.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal Edição (Admin também pode editar qualquer produto) -->
    <div v-if="showModal" class="cf-modal-overlay" @click.self="showModal = false">
      <div class="cf-modal-box animate__animated animate__zoomIn">
        <div class="cf-modal-header">
          <div class="d-flex align-items-center gap-3">
            <div class="modal-icon-wrap bg-dark">
              <i class="fas fa-edit text-white"></i>
            </div>
            <div>
              <h5 class="mb-0 fw-bold">Editar Produto (Admin)</h5>
              <p class="mb-0 text-muted small">Gerenciamento global de catálogo</p>
            </div>
          </div>
          <button class="btn-close-custom" @click="showModal = false"><i class="fas fa-times"></i></button>
        </div>
        
        <div class="cf-modal-body p-4">
           <form id="adminProductForm" @submit.prevent="atualizarProduto">
            <div class="row g-4">
              <div class="col-md-7">
                <div class="mb-3">
                  <label class="cf-label-premium">Nome do Produto</label>
                  <input type="text" class="cf-input-premium" v-model="form.nome" required>
                </div>
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="cf-label-premium">Princípio Ativo</label>
                    <input type="text" class="cf-input-premium" v-model="form.principioAtivo">
                  </div>
                  <div class="col-md-6">
                    <label class="cf-label-premium">Dosagem</label>
                    <input type="text" class="cf-input-premium" v-model="form.dosagem">
                  </div>
                </div>
                <div class="mt-3">
                  <label class="cf-label-premium">Laboratório</label>
                  <input type="text" class="cf-input-premium" v-model="form.laboratorio">
                </div>
              </div>
              <div class="col-md-5">
                <label class="cf-label-premium">Imagem do Produto (URL)</label>
                <div class="image-upload-preview">
                  <img :src="form.imageUrl || 'https://cdn-icons-png.flaticon.com/512/883/883360.png'" alt="Preview">
                  <input type="text" class="cf-input-premium mt-2" v-model="form.imageUrl">
                </div>
              </div>
              
              <div class="col-md-4">
                <label class="cf-label-premium">Estoque Global</label>
                <input type="number" class="cf-input-premium" v-model.number="form.estoque" required>
              </div>
              <div class="col-md-4">
                <label class="cf-label-premium">Preço Unitário (R$)</label>
                <input type="number" step="0.01" class="cf-input-premium" v-model.number="form.preco" required>
              </div>
              <div class="col-md-4">
                <label class="cf-label-premium">Exige Receita?</label>
                <select class="cf-input-premium" v-model="form.necessitaReceita">
                  <option :value="true">Sim, obrigatória</option>
                  <option :value="false">Não, venda livre</option>
                </select>
              </div>
            </div>
           </form>
        </div>

        <div class="cf-modal-footer">
          <button type="button" class="btn btn-light fw-bold px-4" @click="showModal = false">Cancelar</button>
          <button type="submit" form="adminProductForm" class="btn btn-dark fw-bold px-4" :disabled="isSaving">
            <span v-if="isSaving" class="spinner-border spinner-border-sm me-2"></span>
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
  name: 'ProductManagement',
  data() {
    return {
      produtos: [],
      farmacias: [],
      isLoading: true,
      isSaving: false,
      showModal: false,
      busca: '',
      filtroFarmacia: 'todas',
      filtroReceita: 'todos',
      form: {},
      editandoId: null
    }
  },
  computed: {
    produtosFiltrados() {
      let lista = this.produtos;
      if (this.busca) {
        const t = this.busca.toLowerCase();
        lista = lista.filter(p => p.nome?.toLowerCase().includes(t) || p.principioAtivo?.toLowerCase().includes(t));
      }
      if (this.filtroFarmacia !== 'todas') {
        lista = lista.filter(p => p.farmaciaId === parseInt(this.filtroFarmacia));
      }
      if (this.filtroReceita !== 'todos') {
        lista = lista.filter(p => p.necessitaReceita === this.filtroReceita);
      }
      return lista;
    }
  },
  async mounted() {
    await this.fetchFarmacias();
    await this.fetchProducts();
  },
  methods: {
    async fetchFarmacias() {
      try {
        const res = await api.get('/farmacias');
        this.farmacias = res.data;
      } catch (err) { console.error(err); }
    },
    async fetchProducts() {
      this.isLoading = true;
      try {
        const res = await api.get('/produtos');
        this.produtos = res.data;
      } catch (err) { console.error(err); }
      finally { this.isLoading = false; }
    },
    abrirModalEdicao(produto) {
      this.editandoId = produto.id;
      const catId = produto.categoriaId || produto.categoria?.id;
      this.form = {
        nome: produto.nome || '',
        principioAtivo: produto.principioAtivo || '',
        dosagem: produto.dosagem || '',
        laboratorio: produto.laboratorio || '',
        imageUrl: produto.imageUrl || '',
        descricaoBreve: produto.descricaoBreve || '',
        descricao: produto.descricao || '',
        preco: produto.preco || 0,
        estoque: produto.estoque || 0,
        categoriaId: catId,
        farmaciaId: produto.farmaciaId,
        necessitaReceita: produto.necessitaReceita || false
      };
      this.showModal = true;
    },
    async atualizarProduto() {
      this.isSaving = true;
      try {
        await api.put(`/produtos/${this.editandoId}`, this.form);
        this.showModal = false;
        await this.fetchProducts();
      } catch (err) {
        console.error('Erro Admin Update:', err);
        alert('Erro ao atualizar produto. Verifique os dados enviadas.');
      } finally {
        this.isSaving = false;
      }
    },
    async deletarProduto(id) {
      if (confirm('Deseja excluir este produto globalmente?')) {
        try {
          await api.delete(`/produtos/${id}`);
          await this.fetchProducts();
        } catch (err) { alert('Erro ao deletar'); }
      }
    }
  }
}
</script>

<style scoped>
.product-management { padding-bottom: 2rem; }
.cf-card-premium { border-radius: 16px; border: 1px solid rgba(0,0,0,0.05); background: white; box-shadow: var(--cf-shadow-sm); }

/* ─── Modal Premium ClickFarma ─── */
.cf-modal-overlay {
  position: fixed; inset: 0; background: rgba(15, 23, 42, 0.7); 
  z-index: 2000; display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(6px); padding: 20px;
}
.cf-modal-box {
  background: white; border-radius: 24px; width: 900px; max-width: 100%;
  max-height: calc(100vh - 60px);
  box-shadow: 0 25px 50px -12px rgba(0,0,0,0.4); 
  display: flex; flex-direction: column;
  overflow: hidden; border: 1px solid rgba(255,255,255,0.1);
}
.cf-modal-header { 
  padding: 1.25rem 1.5rem; border-bottom: 1px solid #f1f5f9;
  display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0;
}
.modal-icon-wrap {
  width: 44px; height: 44px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center; font-size: 1.2rem;
}
.cf-modal-body { 
  flex: 1; overflow-y: auto; background: #fafafa;
  scrollbar-width: thin; scrollbar-color: var(--cf-green-light) transparent;
}
.cf-modal-body::-webkit-scrollbar { width: 6px; }
.cf-modal-body::-webkit-scrollbar-thumb { background: var(--cf-green-light); border-radius: 10px; }

.cf-modal-footer {
  padding: 1.25rem 1.5rem; border-top: 1px solid #f1f5f9;
  display: flex; justify-content: flex-end; gap: 1rem;
  background: #fff; flex-shrink: 0;
}

@media (max-width: 768px) {
  .cf-modal-overlay { padding: 0; }
  .cf-modal-box { border-radius: 0; height: 100vh; max-height: 100vh; }
  .cf-modal-footer { padding: 1rem; flex-direction: column; align-items: stretch; }
  .cf-modal-footer .btn { width: 100%; }
  .modal-icon-wrap { display: none; }
}

/* Form Elements */
.cf-label-premium { font-size: 0.72rem; font-weight: 600; color: var(--cf-text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.45rem; display: block; }
.cf-input-premium {
  width: 100%; padding: 0.7rem 1rem; border-radius: 10px;
  border: 1px solid var(--cf-border-mid); background: #fff;
  font-size: 0.92rem; transition: all 0.2s; outline: none;
}
.cf-input-premium:focus { border-color: var(--cf-green); box-shadow: 0 0 0 4px rgba(42,92,69,0.06); }

.btn-close-custom {
  background: var(--cf-ivory); border: none; width: 34px; height: 34px;
  border-radius: 50%; color: var(--cf-text-muted); cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.2s;
}
.btn-close-custom:hover { background: var(--cf-cream); color: var(--cf-text-dark); transform: rotate(90deg); }

.image-upload-preview {
  background: #fff; border: 2px dashed var(--cf-border-mid); border-radius: 16px;
  padding: 1.5rem; text-align: center;
}
.image-upload-preview img { max-width: 100%; max-height: 120px; border-radius: 8px; object-fit: contain; }

.table th { font-size: 0.68rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-muted); font-weight: 600; }
</style>