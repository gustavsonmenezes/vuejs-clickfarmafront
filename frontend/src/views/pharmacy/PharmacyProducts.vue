<template>
  <div class="pharmacy-products fade-in-up">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="mb-0 fw-bold"><i class="fas fa-boxes me-2 text-warning"></i>Meus Produtos</h3>
      <button class="btn btn-warning fw-bold px-4" @click="abrirModalCadastro">
        <i class="fas fa-plus me-1"></i> Cadastrar Produto
      </button>
    </div>

    <!-- Filtros Rápidos -->
    <div class="row g-3 mb-4">
      <div class="col-md-8">
        <div class="input-group">
          <span class="input-group-text bg-white border-end-0"><i class="fas fa-search text-muted"></i></span>
          <input v-model="busca" type="text" class="form-control border-start-0" placeholder="Buscar por nome ou princípio ativo...">
        </div>
      </div>
      <div class="col-md-4">
        <select v-model="filtroEstoque" class="form-select">
          <option value="todos">Todos os estoques</option>
          <option value="baixo">Estoque Baixo (< 10)</option>
          <option value="zerado">Sem Estoque (0)</option>
        </select>
      </div>
    </div>

    <!-- Tabela de Produtos -->
    <div class="card shadow-sm border-0 cf-card-premium">
      <div class="card-body p-0">
        <div v-if="isLoading" class="text-center p-5">
          <div class="spinner-border text-warning" role="status"></div>
          <p class="mt-2 text-muted">Carregando seus produtos...</p>
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Produto</th>
                <th>Estoque</th>
                <th>Preço Base</th>
                <th>Final (App)</th>
                <th class="text-center">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="produto in produtosFiltrados" :key="produto.id">
                <td>
                  <div class="d-flex align-items-center">
                    <img :src="produto.imageUrl || 'https://cdn-icons-png.flaticon.com/512/883/883360.png'" 
                         class="rounded border me-3" width="48" height="48" style="object-fit: cover;">
                    <div>
                      <h6 class="mb-0 fw-bold">{{ produto.nome }}</h6>
                      <small class="text-muted d-block">{{ produto.principioAtivo }} - {{ produto.dosagem }}</small>
                      <span v-if="produto.necessitaReceita" class="badge bg-danger-subtle text-danger border border-danger-subtle small-badge">
                        <i class="fas fa-file-prescription me-1"></i>Receita Obrigatória
                      </span>
                    </div>
                  </div>
                </td>
                <td>
                  <span class="badge" :class="getEstoqueClass(produto.estoque)">
                    {{ produto.estoque }} un
                  </span>
                </td>
                <td>R$ {{ (produto.preco || 0).toFixed(2) }}</td>
                <td class="text-success fw-bold">R$ {{ calcularPrecoFinal(produto.preco || 0).toFixed(2) }}</td>
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
                <td colspan="5" class="text-center p-5 text-muted">
                  <i class="fas fa-box-open fa-3x mb-3 opacity-25"></i>
                  <p>Nenhum produto encontrado.</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal de Cadastro/Edição -->
    <div v-if="showModal" class="cf-modal-overlay" @click.self="showModal = false">
      <div class="cf-modal-box animate__animated animate__zoomIn">
        <div class="cf-modal-header">
          <div class="d-flex align-items-center gap-3">
            <div class="modal-icon-wrap" :class="{ 'bg-warning-subtle': !editandoId, 'bg-primary-subtle': editandoId }">
              <i class="fas" :class="editandoId ? 'fa-edit text-primary' : 'fa-box-open text-warning'"></i>
            </div>
            <div>
              <h5 class="mb-0 fw-bold">{{ editandoId ? 'Editar Produto' : 'Cadastrar Novo Produto' }}</h5>
              <p class="mb-0 text-muted small">Preencha os dados técnicos do medicamento</p>
            </div>
          </div>
          <button class="btn-close-custom" @click="showModal = false"><i class="fas fa-times"></i></button>
        </div>
        
        <div class="cf-modal-body">
          <form id="productForm" @submit.prevent="salvarProduto" class="p-4">
            <div class="row g-4">
              <!-- Dados Básicos -->
              <div class="col-md-7">
                <div class="mb-3">
                  <label class="cf-label-premium">Nome do Produto <span class="text-danger">*</span></label>
                  <input type="text" class="cf-input-premium" v-model="form.nome" placeholder="Ex: Paracetamol 750mg" required>
                </div>
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="cf-label-premium">Princípio Ativo</label>
                    <input type="text" class="cf-input-premium" v-model="form.principioAtivo" placeholder="Ex: Paracetamol">
                  </div>
                  <div class="col-md-6">
                    <label class="cf-label-premium">Dosagem</label>
                    <input type="text" class="cf-input-premium" v-model="form.dosagem" placeholder="Ex: 750mg">
                  </div>
                </div>
                <div class="mt-3">
                  <label class="cf-label-premium">Laboratório</label>
                  <input type="text" class="cf-input-premium" v-model="form.laboratorio" placeholder="Ex: Medley, EMS, Eurofarma...">
                </div>
              </div>
              
              <!-- Imagem -->
              <div class="col-md-5">
                <label class="cf-label-premium">Imagem do Produto (URL)</label>
                <div class="image-upload-preview">
                  <img :src="form.imageUrl || 'https://cdn-icons-png.flaticon.com/512/883/883360.png'" alt="Preview">
                  <input type="text" class="cf-input-premium mt-2" v-model="form.imageUrl" placeholder="Cole a URL da imagem aqui">
                </div>
              </div>

              <!-- Descrições -->
              <div class="col-12">
                <div class="mb-3">
                  <label class="cf-label-premium">Chamada Comercial (Vitrini)</label>
                  <input type="text" class="cf-input-premium" v-model="form.descricaoBreve" placeholder="Ex: Alívio rápido para dor e febre. Caixa com 20 comprimidos.">
                </div>
                <div>
                  <label class="cf-label-premium">Informações Detalhadas</label>
                  <textarea class="cf-input-premium" v-model="form.descricao" rows="3" placeholder="Composição, indicações, contraindicações..."></textarea>
                </div>
              </div>

              <!-- Estoque, Preço e Categoria -->
              <div class="col-md-4">
                <label class="cf-label-premium">Estoque Inicial <span class="text-danger">*</span></label>
                <input type="number" class="cf-input-premium" v-model.number="form.estoque" required min="0">
              </div>
              <div class="col-md-4">
                <label class="cf-label-premium">Preço Farmácia (R$) <span class="text-danger">*</span></label>
                <input type="number" step="0.01" class="cf-input-premium" v-model.number="form.preco" required min="0.01">
              </div>
              <div class="col-md-4">
                <label class="cf-label-premium">Categoria <span class="text-danger">*</span></label>
                <select class="cf-input-premium" v-model="form.categoriaId" required>
                  <option v-for="cat in categorias" :key="cat.id" :value="cat.id">{{ cat.nome }}</option>
                </select>
              </div>

              <!-- Switch Receita -->
              <div class="col-12">
                <div class="cf-switch-box" :class="{ 'active': form.necessitaReceita }">
                  <div class="form-check form-switch m-0">
                    <input class="form-check-input" type="checkbox" v-model="form.necessitaReceita" id="checkReceita">
                    <label class="form-check-label fw-bold ms-2" for="checkReceita">
                      <i class="fas fa-file-prescription me-1"></i> Este medicamento exige prescrição médica (Receita)
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </form>
        </div>

        <div class="cf-modal-footer">
          <div class="price-summary">
            <span class="label">Valor Final no App:</span>
            <span class="value text-success">R$ {{ calcularPrecoFinal(form.preco || 0).toFixed(2) }}</span>
            <span class="tax-info">(Taxa de Serviço 0.5% inclusa)</span>
          </div>
          <div class="d-flex gap-2">
            <button type="button" class="btn btn-light fw-bold px-4" @click="showModal = false">Cancelar</button>
            <button type="submit" form="productForm" class="btn btn-warning fw-bold px-4 shadow-sm" :disabled="isSaving">
              <span v-if="isSaving" class="spinner-border spinner-border-sm me-2"></span>
              {{ editandoId ? 'Atualizar Produto' : 'Salvar Produto' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';
import farmaciasService from '@/services/farmaciasService';

export default {
  name: 'PharmacyProducts',
  data() {
    return {
      showModal: false,
      isLoading: true,
      isSaving: false,
      editandoId: null,
      farmaciaId: null,
      busca: '',
      filtroEstoque: 'todos',
      categorias: [],
      form: {
        nome: '',
        principioAtivo: '',
        dosagem: '',
        laboratorio: '',
        imageUrl: '',
        descricaoBreve: '',
        descricao: '',
        preco: 0,
        estoque: 0,
        categoriaId: null,
        necessitaReceita: false
      },
      produtos: []
    }
  },
  computed: {
    produtosFiltrados() {
      let lista = this.produtos;
      
      if (this.busca) {
        const t = this.busca.toLowerCase();
        lista = lista.filter(p => 
          p.nome?.toLowerCase().includes(t) || 
          p.principioAtivo?.toLowerCase().includes(t) ||
          p.laboratorio?.toLowerCase().includes(t)
        );
      }

      if (this.filtroEstoque === 'baixo') {
        lista = lista.filter(p => p.estoque > 0 && p.estoque < 10);
      } else if (this.filtroEstoque === 'zerado') {
        lista = lista.filter(p => p.estoque <= 0);
      }

      return lista;
    }
  },
  async mounted() {
    await this.carregarCategorias();
    await this.carregarFarmaciaEProdutos();
  },
  methods: {
    async carregarCategorias() {
      try {
        const res = await api.get('/categorias');
        this.categorias = res.data;
      } catch (err) { console.error(err); }
    },
    async carregarFarmaciaEProdutos() {
      this.isLoading = true;
      try {
        const user = JSON.parse(localStorage.getItem('user'));
        const farmacias = await farmaciasService.listarTodas();
        const farmacia = farmacias.data.find(f => f.email === user.email);
        
        if (farmacia) {
          this.farmaciaId = farmacia.id;
          const response = await api.get(`/produtos/buscar?farmaciaId=${this.farmaciaId}`);
          this.produtos = response.data.filter(p => p.farmaciaId === this.farmaciaId);
        }
      } catch (error) {
        console.error('Erro ao carregar produtos:', error);
      } finally {
        this.isLoading = false;
      }
    },
    calcularPrecoFinal(precoBase) {
      return precoBase + (precoBase * 0.005);
    },
    getEstoqueClass(qtd) {
      if (qtd <= 0) return 'bg-danger';
      if (qtd < 10) return 'bg-warning text-dark';
      return 'bg-success';
    },
    abrirModalCadastro() {
      this.editandoId = null;
      this.form = {
        nome: '', principioAtivo: '', dosagem: '', laboratorio: '', imageUrl: '',
        descricaoBreve: '', descricao: '', preco: 0, estoque: 0, 
        categoriaId: this.categorias[0]?.id || null, necessitaReceita: false
      };
      this.showModal = true;
    },
    abrirModalEdicao(produto) {
      this.editandoId = produto.id;
      // Garante que o categoriaId seja mapeado corretamente e limpa campos virtuais
      const catId = produto.categoriaId || produto.categoria?.id || (this.categorias.find(c => c.nome === produto.categoriaNome)?.id);
      
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
        necessitaReceita: produto.necessitaReceita || false
      };
      this.isEdit = true;
      this.showModal = true;
    },
    async salvarProduto() {
      if (!this.farmaciaId) return;
      
      this.isSaving = true;
      try {
        const payload = { 
          ...this.form, 
          farmaciaId: this.farmaciaId 
        };
        
        if (this.editandoId) {
          await api.put(`/produtos/${this.editandoId}`, payload);
        } else {
          await api.post('/produtos', payload);
        }
        
        this.showModal = false;
        await this.carregarFarmaciaEProdutos();
      } catch (error) {
        console.error('Erro ao salvar:', error);
        alert('Erro ao salvar produto. Verifique se todos os campos obrigatórios estão preenchidos.');
      } finally {
        this.isSaving = false;
      }
    },
    async deletarProduto(id) {
      if (confirm('Deseja realmente excluir este produto?')) {
        try {
          await api.delete(`/produtos/${id}`);
          await this.carregarFarmaciaEProdutos();
        } catch (error) {
          alert('Erro ao deletar produto');
        }
      }
    }
  }
}
</script>

<style scoped>
.pharmacy-products {
  padding-bottom: 2rem;
}
.cf-card-premium { border-radius: 16px; overflow: hidden; border: 1px solid rgba(0,0,0,0.05); background: white; }
.small-badge { font-size: 0.65rem; padding: 2px 6px; margin-top: 4px; display: inline-block; }

/* ─── Modal Premium ─── */
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
  position: relative;
}
.cf-modal-header { 
  padding: 1.25rem 1.5rem; border-bottom: 1px solid #f1f5f9;
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; flex-shrink: 0;
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
.cf-modal-body::-webkit-scrollbar-track { background: transparent; }
.cf-modal-body::-webkit-scrollbar-thumb { background: var(--cf-green-light); border-radius: 10px; }

.cf-modal-footer {
  padding: 1.25rem 1.5rem; border-top: 1px solid #f1f5f9;
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; flex-shrink: 0;
  flex-wrap: wrap; gap: 1rem;
}

/* ─── Form Elements ─── */
.cf-label-premium { 
  font-size: 0.72rem; font-weight: 600; color: var(--cf-text-muted); 
  text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.45rem; display: block; 
}

@media (max-width: 768px) {
  .cf-modal-overlay { padding: 0; }
  .cf-modal-box { border-radius: 0; height: 100vh; max-height: 100vh; width: 100%; }
  .cf-modal-header { padding: 1rem; }
  .cf-modal-footer { padding: 1rem; flex-direction: column; align-items: stretch; }
  .cf-modal-footer .price-summary { text-align: center; margin-bottom: 0.5rem; border-bottom: 1px solid #f1f5f9; padding-bottom: 1rem; }
  .cf-modal-footer .d-flex { flex-direction: column; }
  .cf-modal-footer .btn { width: 100%; }
  .modal-icon-wrap { display: none; }
}

.cf-input-premium {
  width: 100%; padding: 0.7rem 1rem; border-radius: 10px;
  border: 1px solid var(--cf-border-mid); background: #fff;
  font-size: 0.92rem; transition: all 0.2s;
}
.cf-input-premium:focus { border-color: var(--cf-green); outline: none; box-shadow: 0 0 0 3px rgba(42,92,69,0.08); }

.image-upload-preview {
  background: #fff; border: 2px dashed var(--cf-border); border-radius: 16px;
  padding: 1.5rem; text-align: center; height: 100%; display: flex; flex-direction: column; justify-content: center;
}
.image-upload-preview img { max-width: 100%; max-height: 120px; border-radius: 8px; object-fit: contain; margin-bottom: 1rem; }

.cf-switch-box {
  padding: 1.25rem; border-radius: 16px; border: 1px solid var(--cf-border-mid);
  background: #fff; transition: all 0.2s; display: flex; align-items: center;
}
.cf-switch-box.active { border-color: var(--cf-danger); background: #FFF5F5; color: var(--cf-danger); }
.cf-switch-box .form-check-input:checked { background-color: var(--cf-danger) !important; border-color: var(--cf-danger) !important; }

.price-summary { display: flex; flex-direction: column; min-width: 180px; }
.price-summary .label { font-size: 0.65rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--cf-text-muted); }
.price-summary .value { font-size: 1.6rem; font-weight: 800; line-height: 1; color: var(--cf-green); margin: 2px 0; }
.price-summary .tax-info { font-size: 0.65rem; color: var(--cf-text-faint); }

.btn-close-custom {
  background: var(--cf-ivory); border: none; width: 34px; height: 34px;
  border-radius: 50%; color: var(--cf-text-muted); cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.2s;
}
.btn-close-custom:hover { background: var(--cf-cream); color: var(--cf-text-dark); transform: rotate(90deg); }

.table th { font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.08em; color: var(--cf-text-muted); font-weight: 600; }
</style>
