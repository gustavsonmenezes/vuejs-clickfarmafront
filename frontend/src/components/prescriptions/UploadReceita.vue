<template>
  <div class="upload-receita-page">
    <!-- Notification -->
    <div v-if="notificacao.visivel" class="notificacao" :class="notificacao.tipo">
      <i :class="notificacao.icone"></i>
      <span>{{ notificacao.mensagem }}</span>
    </div>

    <div class="upload-receita-card cf-card">
      <!-- Header -->
      <div class="receita-header">
        <div class="header-icon">
          <i class="fas fa-file-medical"></i>
        </div>
        <h2>Leitura de Receita Médica</h2>
        <p>Envie uma foto da sua receita e nossa IA identifica os medicamentos automaticamente.</p>
      </div>

      <!-- Upload Area -->
      <div v-if="!resultado && !carregando" class="upload-section">
        <div class="upload-area" :class="{ 'has-image': imagemPreview }">
          <input
            type="file"
            ref="fileInput"
            @change="selecionarArquivo"
            accept="image/jpeg,image/png,image/jpg"
            class="file-input"
          />

          <div v-if="!imagemPreview" class="upload-placeholder">
            <div class="upload-icon-box">
              <i class="fas fa-cloud-upload-alt"></i>
            </div>
            <p class="upload-title">Clique ou arraste uma imagem</p>
            <p class="upload-subtitle">JPG, PNG — máx. 5MB</p>
          </div>

          <div v-else class="preview-area">
            <img :src="imagemPreview" alt="Preview" class="preview-image" />
            <button @click.stop="limparImagem" class="btn-remove-img">
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </div>

        <!-- Process Button -->
        <button v-if="imagemBase64 && !carregando && !resultado" @click="enviarReceita" class="cf-btn cf-btn-primary btn-processar">
          <i class="fas fa-microchip"></i>
          Processar com IA
        </button>
      </div>

      <!-- Loading -->
      <div v-if="carregando" class="loading-section">
        <div class="loading-spinner">
          <div class="spinner-ring"></div>
          <i class="fas fa-microchip loading-icon"></i>
        </div>
        <h4>Analisando sua receita...</h4>
        <p>Isso pode levar alguns segundos</p>
      </div>

      <!-- Results -->
      <div v-if="resultado" class="resultados-section">
        <!-- Success with medications -->
        <div v-if="resultado.sucesso && resultado.medicamentos?.length > 0">
          <!-- Orientation Message -->
          <div v-if="resultado.mensagemOrientacao" class="orientacao-msg">
            <i class="fas fa-info-circle"></i>
            <p>{{ resultado.mensagemOrientacao }}</p>
          </div>

          <div class="resultados-header">
            <h3>
              <i class="fas fa-pills"></i>
              Medicamentos identificados
            </h3>
            <span class="cf-badge cf-badge-primary">{{ resultado.medicamentos.length }} {{ resultado.medicamentos.length === 1 ? 'item' : 'itens' }}</span>
          </div>

          <div class="medicamentos-lista">
            <div v-for="(med, index) in resultado.medicamentos" :key="index" class="medicamento-card">
              <div class="med-content">
                <div class="med-header">
                  <div class="med-name-group">
                    <div class="med-rank">{{ index + 1 }}</div>
                    <div>
                      <strong>{{ med.nome }}</strong>
                      <span v-if="med.dosagem" class="dosagem-badge">{{ med.dosagem }}</span>
                      <span v-if="med.descricaoIA && med.descricaoIA.includes('OCR com baixa qualidade')" class="badge-ocr-ruim">
                        <i class="fas fa-exclamation-triangle"></i> Revisar
                      </span>
                    </div>
                  </div>
                  <div v-if="med.preco > 0" class="med-price">
                    R$ {{ formatarPreco(med.preco) }}
                  </div>
                </div>

                <p v-if="med.descricaoProduto" class="med-desc">{{ med.descricaoProduto }}</p>

                <div class="med-details">
                  <span class="med-detail">
                    <i class="fas fa-cubes"></i>
                    {{ med.quantidade }} {{ med.quantidade === 1 ? 'unidade' : 'unidades' }}
                  </span>
                  <span v-if="med.produtoId" class="med-detail" :class="{ 'sem-estoque': med.estoque === 0 }">
                    <i class="fas fa-warehouse"></i>
                    {{ med.estoque > 0 ? med.estoque + ' em estoque' : 'Esgotado' }}
                  </span>
                  <span v-else class="med-detail sem-catalogo">
                    <i class="fas fa-link"></i>
                    Não encontrado no catálogo
                  </span>
                </div>

                <!-- Manual Edit -->
                <details v-if="med.descricaoIA && med.descricaoIA.includes('OCR com baixa qualidade')" class="edicao-manual">
                  <summary><i class="fas fa-pen"></i> Revisar nome do medicamento</summary>
                  <div class="edicao-group">
                    <input v-model="med.nomeEditado" :placeholder="med.nome" class="cf-input edicao-input" />
                    <button @click="buscarProdutoPorNome(med, index)" class="cf-btn cf-btn-primary cf-btn-sm" :disabled="carregandoBusca">
                      <i class="fas fa-search"></i> Buscar
                    </button>
                  </div>
                </details>

                <!-- Alternatives -->
                <details v-if="med.alternativasSugeridas?.length" class="alternativas-box">
                  <summary><i class="fas fa-exchange-alt"></i> Opções similares no ClickFarma</summary>
                  <div v-for="(alt, aidx) in med.alternativasSugeridas" :key="'alt-' + index + '-' + aidx" class="alternativa-item">
                    <div class="alt-info">
                      <strong>{{ alt.nome }}</strong>
                      <span v-if="alt.motivoIndicacao" class="alt-motivo">{{ alt.motivoIndicacao }}</span>
                      <span class="alt-meta">R$ {{ formatarPreco(alt.preco) }} · {{ alt.estoque }} em estoque</span>
                    </div>
                    <button @click="adicionarAlternativaAoCarrinho(alt)" class="cf-btn cf-btn-primary cf-btn-sm btn-alt" :disabled="!alt.produtoId || alt.estoque === 0">
                      <i class="fas fa-cart-plus"></i>
                    </button>
                  </div>
                </details>
              </div>

              <!-- Actions -->
              <div v-if="!somenteLeitura" class="med-actions">
                <div class="qty-control">
                  <button @click="diminuirQuantidade(index)" class="qty-btn"><i class="fas fa-minus"></i></button>
                  <input v-model.number="quantidades[index]" type="number" min="1" class="qty-input" />
                  <button @click="aumentarQuantidade(index)" class="qty-btn"><i class="fas fa-plus"></i></button>
                </div>
                <button @click="adicionarAoCarrinho(med, index)" class="cf-btn cf-btn-success btn-add" :disabled="!med.produtoId || med.estoque === 0">
                  <i class="fas fa-cart-plus"></i> Adicionar
                </button>
              </div>
            </div>
          </div>

          <!-- Bulk Actions -->
          <div v-if="!somenteLeitura" class="bulk-actions">
            <button @click="adicionarTodosAoCarrinho" class="cf-btn cf-btn-primary btn-bulk">
              <i class="fas fa-shopping-cart"></i>
              Adicionar Todos
            </button>
            <button @click="limparImagem" class="cf-btn cf-btn-secondary btn-bulk">
              <i class="fas fa-redo"></i>
              Nova Receita
            </button>
          </div>
        </div>

        <!-- No medications found -->
        <div v-else-if="resultado.sucesso" class="empty-result">
          <div class="empty-icon"><i class="fas fa-eye-slash"></i></div>
          <h4>Nenhum medicamento identificado</h4>
          <p>Tente outra imagem com melhor qualidade</p>
          <button @click="limparImagem" class="cf-btn cf-btn-primary">Tentar novamente</button>
        </div>

        <!-- Error -->
        <div v-else class="error-result">
          <div class="error-icon"><i class="fas fa-exclamation-triangle"></i></div>
          <h4>Erro ao processar</h4>
          <p>{{ resultado.erro }}</p>
          <button @click="limparImagem" class="cf-btn cf-btn-primary">Tentar novamente</button>
        </div>

        <!-- OCR Debug -->
        <details v-if="debugOcr && resultado.textoOriginal" class="ocr-debug">
          <summary>Texto lido (OCR)</summary>
          <pre>{{ resultado.textoOriginal }}</pre>
        </details>
      </div>
    </div>
  </div>
</template>

<script>
import receitaService from '@/services/receitaService';

export default {
  name: 'UploadReceita',
  data() {
    return {
      imagemBase64: null,
      imagemPreview: null,
      nomeArquivo: null,
      carregando: false,
      carregandoBusca: false,
      resultado: null,
      quantidades: {},
      debugOcr: false,
      somenteLeitura: true,
      notificacao: { visivel: false, mensagem: '', tipo: 'sucesso', icone: '' }
    };
  },
  created() {
    try {
      this.debugOcr = localStorage.getItem('debug_ocr') === '1';
      this.somenteLeitura = localStorage.getItem('receita_readonly') === '1';
    } catch (e) {
      this.debugOcr = false;
      this.somenteLeitura = false;
    }
  },
  methods: {
    selecionarArquivo(event) {
      const arquivo = event.target.files[0];
      if (!arquivo) return;
      if (arquivo.size > 5 * 1024 * 1024) {
        this.mostrarNotificacao('A imagem deve ter no máximo 5MB', 'erro');
        return;
      }
      this.nomeArquivo = arquivo.name;
      this.resultado = null;
      this.quantidades = {};
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagemPreview = e.target.result;
        this.imagemBase64 = e.target.result.split(',')[1];
      };
      reader.readAsDataURL(arquivo);
    },
    limparImagem() {
      this.imagemBase64 = null;
      this.imagemPreview = null;
      this.nomeArquivo = null;
      this.resultado = null;
      this.quantidades = {};
      if (this.$refs.fileInput) this.$refs.fileInput.value = '';
    },
    async enviarReceita() {
      if (!this.imagemBase64) {
        this.mostrarNotificacao('Selecione uma imagem primeiro', 'erro');
        return;
      }
      this.carregando = true;
      try {
        const response = await receitaService.processarReceita(this.imagemBase64, this.nomeArquivo);
        this.resultado = response;
        if (response.medicamentos) {
          response.medicamentos.forEach((med, index) => {
            this.quantidades[index] = med.quantidade || 1;
          });
        }
      } catch (error) {
        this.resultado = { sucesso: false, erro: error.response?.data?.erro || error.message };
      } finally { this.carregando = false; }
    },
    async buscarProdutoPorNome(medicamento, index) {
      if (!medicamento.nomeEditado && !medicamento.nome) return;
      const nomeBusca = medicamento.nomeEditado || medicamento.nome;
      this.carregandoBusca = true;
      try {
        const produtos = await receitaService.buscarProdutos(nomeBusca);
        if (produtos && produtos.length > 0) {
          const produto = produtos[0];
          medicamento.produtoId = produto.id;
          medicamento.nomeCompleto = produto.nome;
          medicamento.preco = produto.preco;
          medicamento.estoque = produto.estoque;
          medicamento.situacaoCatalogo = produto.estoque > 0 ? 'DISPONIVEL' : 'SEM_ESTOQUE';
          medicamento.descricaoProduto = produto.descricao;
          this.mostrarNotificacao(`Produto encontrado: ${produto.nome}`, 'sucesso');
          if (this.$set) this.$set(this.resultado.medicamentos, index, medicamento);
        } else {
          this.mostrarNotificacao(`Nenhum produto encontrado para "${nomeBusca}"`, 'warning');
        }
      } catch (error) {
        console.error('Erro ao buscar produto:', error);
        this.mostrarNotificacao('Erro ao buscar produto', 'erro');
      } finally { this.carregandoBusca = false; }
    },
    aumentarQuantidade(index) {
      if (!this.quantidades[index]) this.quantidades[index] = 1;
      this.quantidades[index]++;
    },
    diminuirQuantidade(index) {
      if (this.quantidades[index] && this.quantidades[index] > 1) this.quantidades[index]--;
    },
    adicionarAlternativaAoCarrinho(alt) {
      if (!alt || !alt.produtoId) return;
      const item = {
        id: alt.produtoId, name: alt.nome, price: alt.preco || 0, quantity: 1,
        description: alt.motivoIndicacao || '', dosagem: '', origem: 'receita-alternativa',
        dataAdicao: new Date().toISOString()
      };
      if (this.$store && this.$store.dispatch) {
        this.$store.dispatch('addToCart', item);
        this.mostrarNotificacao(`${alt.nome} adicionado ao carrinho!`, 'sucesso');
      } else {
        const carrinho = JSON.parse(localStorage.getItem('carrinho') || '[]');
        carrinho.push(item);
        localStorage.setItem('carrinho', JSON.stringify(carrinho));
        this.mostrarNotificacao(`${alt.nome} adicionado!`, 'sucesso');
      }
    },
    adicionarAoCarrinho(medicamento, index) {
      if (!medicamento || !medicamento.produtoId) {
        this.mostrarNotificacao('Item não vinculado ao catálogo.', 'warning');
        return;
      }
      const quantidade = this.quantidades[index] || medicamento.quantidade || 1;
      const item = {
        id: medicamento.produtoId, name: medicamento.nomeCompleto || medicamento.nome,
        price: medicamento.preco || 0, quantity: quantidade,
        description: medicamento.descricaoProduto || medicamento.descricaoIA || '',
        dosagem: medicamento.dosagem || '', origem: 'receita',
        dataAdicao: new Date().toISOString()
      };
      if (this.$store && this.$store.dispatch) {
        this.$store.dispatch('addToCart', item);
        this.mostrarNotificacao(`${item.name} adicionado ao carrinho!`, 'sucesso');
      } else {
        const carrinho = JSON.parse(localStorage.getItem('carrinho') || '[]');
        carrinho.push(item);
        localStorage.setItem('carrinho', JSON.stringify(carrinho));
        this.mostrarNotificacao(`${item.name} adicionado!`, 'sucesso');
      }
    },
    adicionarTodosAoCarrinho() {
      if (!this.resultado?.medicamentos) return;
      this.resultado.medicamentos.forEach((med, index) => {
        if (med && med.produtoId && !(med.estoque === 0)) this.adicionarAoCarrinho(med, index);
      });
      this.mostrarNotificacao('Medicamentos adicionados ao carrinho!', 'sucesso');
      setTimeout(() => { if (this.$router) this.$router.push('/cart'); }, 1500);
    },
    formatarPreco(preco) {
      return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(preco);
    },
    mostrarNotificacao(mensagem, tipo = 'sucesso') {
      if (this.timeoutNotificacao) clearTimeout(this.timeoutNotificacao);
      const icones = { sucesso: 'fas fa-check-circle', erro: 'fas fa-exclamation-circle', info: 'fas fa-info-circle', warning: 'fas fa-exclamation-triangle' };
      this.notificacao = { visivel: true, mensagem, tipo, icone: icones[tipo] || icones.info };
      this.timeoutNotificacao = setTimeout(() => { this.notificacao.visivel = false; }, 3000);
    }
  },
  beforeUnmount() {
    if (this.timeoutNotificacao) clearTimeout(this.timeoutNotificacao);
  }
};
</script>

<style scoped>
.upload-receita-page {
  max-width: 760px;
  margin: 0 auto;
  padding: 24px 0;
  position: relative;
}

.upload-receita-card { overflow: hidden; }

/* Header */
.receita-header {
  text-align: center;
  padding: 40px 32px 32px;
  border-bottom: 1px solid var(--cf-border);
}

.header-icon {
  width: 56px; height: 56px; border-radius: 14px;
  background: var(--cf-primary-100); color: var(--cf-primary-600);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.5rem; margin: 0 auto 16px;
}

.receita-header h2 {
  font-size: 1.375rem; font-weight: 700; color: var(--cf-slate-900);
  margin: 0 0 8px; letter-spacing: -0.02em;
}

.receita-header p {
  font-size: 0.875rem; color: var(--cf-slate-500); margin: 0;
  max-width: 400px; margin: 0 auto;
}

/* Upload Section */
.upload-section { padding: 32px; }

.upload-area {
  border: 2px dashed var(--cf-border);
  border-radius: var(--cf-radius-lg);
  cursor: pointer;
  position: relative;
  min-height: 200px;
  transition: border-color 0.2s ease, background 0.2s ease;
  overflow: hidden;
}

.upload-area:hover {
  border-color: var(--cf-primary-500);
  background: var(--cf-primary-50);
}

.file-input {
  position: absolute; inset: 0; opacity: 0; cursor: pointer; z-index: 1;
}

.upload-placeholder {
  text-align: center; padding: 48px 20px;
}

.upload-icon-box {
  width: 64px; height: 64px; border-radius: 16px;
  background: var(--cf-slate-100); color: var(--cf-slate-400);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.5rem; margin: 0 auto 16px;
}

.upload-title { font-size: 0.9375rem; font-weight: 600; color: var(--cf-slate-700); margin: 0 0 4px; }
.upload-subtitle { font-size: 0.8125rem; color: var(--cf-slate-400); margin: 0; }

/* Preview */
.preview-area {
  position: relative; padding: 16px; text-align: center;
}

.preview-image {
  max-width: 100%; max-height: 280px; border-radius: var(--cf-radius-md);
  box-shadow: var(--cf-shadow-md);
}

.btn-remove-img {
  position: absolute; top: 24px; right: 24px;
  width: 36px; height: 36px; border-radius: 10px;
  background: rgba(220, 38, 38, 0.9); color: white;
  border: none; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.15s ease;
  z-index: 2;
}

.btn-remove-img:hover { transform: scale(1.1); }

/* Process Button */
.btn-processar {
  margin-top: 20px; width: 100%; padding: 12px; font-size: 0.9375rem;
}

/* Loading */
.loading-section {
  text-align: center; padding: 60px 32px;
}

.loading-spinner {
  position: relative; width: 72px; height: 72px; margin: 0 auto 20px;
}

.spinner-ring {
  position: absolute; inset: 0;
  border: 3px solid var(--cf-slate-100);
  border-top-color: var(--cf-primary-500);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.loading-icon {
  position: absolute; top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  color: var(--cf-primary-500); font-size: 1.25rem;
}

.loading-section h4 { font-size: 1rem; font-weight: 600; color: var(--cf-slate-800); margin: 0 0 4px; }
.loading-section p { font-size: 0.8125rem; color: var(--cf-slate-500); margin: 0; }

@keyframes spin { to { transform: rotate(360deg); } }

/* Results */
.resultados-section { padding: 0 32px 32px; }

.orientacao-msg {
  display: flex; gap: 12px; align-items: flex-start;
  background: var(--cf-primary-50); border: 1px solid var(--cf-primary-100);
  border-radius: var(--cf-radius-md); padding: 14px 16px;
  margin-bottom: 24px; font-size: 0.875rem; color: var(--cf-slate-700);
}

.orientacao-msg i { color: var(--cf-primary-500); font-size: 1rem; margin-top: 1px; flex-shrink: 0; }
.orientacao-msg p { margin: 0; }

.resultados-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}

.resultados-header h3 {
  font-size: 1rem; font-weight: 600; color: var(--cf-slate-900);
  margin: 0; display: flex; align-items: center; gap: 8px;
}

/* Medication Cards */
.medicamentos-lista { display: flex; flex-direction: column; gap: 12px; }

.medicamento-card {
  background: white;
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-radius-lg);
  padding: 20px;
  transition: box-shadow 0.15s ease;
  border-left: 3px solid var(--cf-success);
}

.medicamento-card:hover { box-shadow: var(--cf-shadow-md); }

.med-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 8px; flex-wrap: wrap; gap: 8px;
}

.med-name-group {
  display: flex; align-items: center; gap: 10px;
}

.med-rank {
  width: 28px; height: 28px; border-radius: 8px;
  background: var(--cf-slate-100); color: var(--cf-slate-600);
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 0.75rem; flex-shrink: 0;
}

.med-name-group strong { font-size: 0.9375rem; color: var(--cf-slate-900); }

.dosagem-badge {
  display: inline-block; background: var(--cf-slate-100);
  padding: 2px 8px; border-radius: 9999px;
  font-size: 0.6875rem; color: var(--cf-slate-500);
  margin-left: 6px; font-weight: 500;
}

.badge-ocr-ruim {
  display: inline-flex; align-items: center; gap: 4px;
  background: var(--cf-warning-light); color: var(--cf-warning);
  font-size: 0.6875rem; font-weight: 600;
  padding: 2px 8px; border-radius: 9999px; margin-left: 6px;
}

.med-price { font-size: 1.0625rem; font-weight: 700; color: var(--cf-success); }

.med-desc { font-size: 0.8125rem; color: var(--cf-slate-500); margin: 0 0 10px; line-height: 1.5; }

.med-details {
  display: flex; gap: 16px; flex-wrap: wrap; font-size: 0.75rem;
}

.med-detail {
  display: inline-flex; align-items: center; gap: 5px;
  color: var(--cf-slate-500);
}

.med-detail i { font-size: 0.6875rem; color: var(--cf-slate-400); }
.med-detail.sem-estoque { color: var(--cf-danger); font-weight: 600; }
.med-detail.sem-catalogo {
  color: var(--cf-warning); background: var(--cf-warning-light);
  padding: 3px 8px; border-radius: 6px;
}

/* Actions */
.med-actions {
  margin-top: 16px; padding-top: 14px;
  border-top: 1px solid var(--cf-slate-100);
  display: flex; gap: 10px; align-items: center;
}

.qty-control {
  display: flex; align-items: center;
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-radius-md);
  background: white;
}

.qty-btn {
  background: none; border: none; padding: 6px 10px;
  cursor: pointer; color: var(--cf-primary-500);
  font-size: 0.8125rem;
}

.qty-btn:hover { background: var(--cf-slate-50); }

.qty-input {
  width: 40px; border: none; text-align: center;
  font-weight: 600; font-size: 0.8125rem; padding: 4px 0;
  color: var(--cf-slate-800);
}

.qty-input:focus { outline: none; }

.btn-add { white-space: nowrap; }

.cf-btn-success {
  background: var(--cf-success); color: white;
}
.cf-btn-success:hover { background: #047857; }
.cf-btn-success:disabled { background: var(--cf-slate-300); cursor: not-allowed; }

/* Edit */
.edicao-manual {
  margin-top: 12px; padding: 12px;
  background: var(--cf-warning-light);
  border-radius: var(--cf-radius-md);
  border-left: 3px solid var(--cf-warning);
}

.edicao-manual summary {
  cursor: pointer; font-size: 0.8125rem; font-weight: 600;
  color: var(--cf-warning); display: flex; align-items: center; gap: 6px;
  list-style: none;
}

.edicao-group { display: flex; gap: 8px; margin-top: 10px; }
.edicao-input { flex: 1; }

/* Alternatives */
.alternativas-box {
  margin-top: 12px; padding: 12px;
  background: var(--cf-primary-50);
  border-radius: var(--cf-radius-md);
  border: 1px dashed var(--cf-primary-200);
}

.alternativas-box summary {
  cursor: pointer; font-size: 0.8125rem; font-weight: 600;
  color: var(--cf-slate-600); display: flex; align-items: center; gap: 6px;
  list-style: none;
}

.alternativa-item {
  display: flex; justify-content: space-between; align-items: center;
  gap: 12px; padding: 10px 0;
  border-top: 1px solid var(--cf-border);
}

.alternativa-item:first-of-type { border-top: none; padding-top: 0; }

.alt-info { flex: 1; min-width: 0; }
.alt-info strong { display: block; font-size: 0.8125rem; color: var(--cf-slate-800); margin-bottom: 2px; }
.alt-motivo { display: block; font-size: 0.75rem; color: var(--cf-slate-500); margin-bottom: 2px; }
.alt-meta { font-size: 0.75rem; color: var(--cf-slate-400); }
.btn-alt { flex-shrink: 0; }

/* Bulk Actions */
.bulk-actions {
  display: flex; gap: 10px; margin-top: 24px; padding-top: 20px;
  border-top: 1px solid var(--cf-border);
}

.btn-bulk { flex: 1; }

/* Empty/Error States */
.empty-result, .error-result { text-align: center; padding: 48px 20px; }
.empty-icon i, .error-icon i { font-size: 2.5rem; margin-bottom: 12px; opacity: 0.3; }
.empty-icon i { color: var(--cf-warning); }
.error-icon i { color: var(--cf-danger); }
.empty-result h4, .error-result h4 { font-size: 1rem; font-weight: 600; color: var(--cf-slate-800); margin: 0 0 4px; }
.empty-result p, .error-result p { font-size: 0.8125rem; color: var(--cf-slate-500); margin: 0 0 20px; }

/* OCR Debug */
.ocr-debug {
  margin-top: 20px; padding: 12px;
  background: var(--cf-slate-50); border-radius: var(--cf-radius-md);
  border: 1px solid var(--cf-border);
}

.ocr-debug summary { cursor: pointer; font-weight: 600; color: var(--cf-slate-600); font-size: 0.8125rem; }
.ocr-debug pre {
  margin-top: 8px; white-space: pre-wrap; word-break: break-word;
  max-height: 200px; overflow: auto; font-size: 0.75rem; color: var(--cf-slate-600);
}

/* Notification */
.notificacao {
  position: fixed; top: 20px; right: 20px; z-index: 9999;
  display: flex; align-items: center; gap: 10px;
  padding: 12px 20px; border-radius: var(--cf-radius-md);
  box-shadow: var(--cf-shadow-lg); font-size: 0.8125rem; font-weight: 500;
  animation: slideIn 0.25s ease-out;
}

.notificacao.sucesso { background: var(--cf-success-light); color: var(--cf-success); }
.notificacao.erro { background: var(--cf-danger-light); color: var(--cf-danger); }
.notificacao.info { background: var(--cf-info-light); color: var(--cf-info); }
.notificacao.warning { background: var(--cf-warning-light); color: var(--cf-warning); }

@keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }

/* Responsive */
@media (max-width: 768px) {
  .upload-receita-page { padding: 16px; }
  .receita-header { padding: 28px 20px 24px; }
  .upload-section, .resultados-section { padding: 20px; }
  .medicamento-card { padding: 16px; }
  .med-actions { flex-direction: column; }
  .qty-control, .btn-add { width: 100%; }
  .bulk-actions { flex-direction: column; }
  .alternativa-item { flex-direction: column; align-items: flex-start; }
}
</style>
