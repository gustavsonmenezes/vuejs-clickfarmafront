<template>
  <div class="upload-receita-container">
    <div class="receita-card">
      <div class="receita-header">
        <h2>
          <i class="fas fa-camera-retro"></i>
          Leitura de Receita Médica
        </h2>
        <p class="descricao">
          Tire uma foto ou selecione uma imagem da sua receita médica.
          Nossa IA identifica os medicamentos automaticamente!
        </p>
      </div>

      <div class="upload-area" :class="{ 'has-image': imagemPreview }">
        <input
            type="file"
            ref="fileInput"
            @change="selecionarArquivo"
            accept="image/jpeg,image/png,image/jpg"
            class="file-input"
        />

        <div v-if="!imagemPreview" class="upload-placeholder">
          <i class="fas fa-cloud-upload-alt"></i>
          <p>Clique ou arraste uma imagem</p>
          <small>Suporta JPG, PNG (máx. 5MB)</small>
        </div>

        <div v-else class="preview-area">
          <img :src="imagemPreview" alt="Preview" class="preview-image" />
          <div class="preview-overlay">
            <button @click="limparImagem" class="btn-clear">
              <i class="fas fa-times"></i> Remover
            </button>
          </div>
        </div>
      </div>

      <div class="actions" v-if="imagemBase64 && !carregando && !resultado">
        <button @click="enviarReceita" class="btn-processar">
          <i class="fas fa-microchip"></i>
          Processar com IA
        </button>
      </div>

      <div v-if="carregando" class="loading-area">
        <div class="spinner"></div>
        <p>Analisando sua receita...</p>
        <small>Isso pode levar alguns segundos</small>
      </div>

	        <div v-if="resultado" class="resultados">
	          <div v-if="resultado.sucesso && resultado.medicamentos?.length > 0">
	          <div v-if="resultado.mensagemOrientacao" class="orientacao-clickfarma">
	            <i class="fas fa-info-circle"></i>
	            <p>{{ resultado.mensagemOrientacao }}</p>
	          </div>

          <h3>
            <i class="fas fa-pills"></i>
            Itens lidos na receita ({{ resultado.medicamentos.length }})
          </h3>

          <div class="medicamentos-lista">
	            <div v-for="(med, index) in resultado.medicamentos" :key="index" class="medicamento-card">
	              <div class="medicamento-info">
	                <div class="medicamento-header">
	                  <div class="medicamento-nome">
	                    <strong>{{ med.nome }}</strong>
	                    <span v-if="med.dosagem" class="dosagem">{{ med.dosagem }}</span>
	                    <span v-if="med.descricaoIA && med.descricaoIA.includes('OCR com baixa qualidade')"
	                          class="badge-ocr-ruim"
	                          title="A leitura automática teve baixa qualidade. Verifique o nome na receita original.">
	                      <i class="fas fa-exclamation-triangle"></i> Revisar
	                    </span>
	                  </div>
	                  <div v-if="med.preco > 0" class="medicamento-preco">
	                    <span class="preco-label">R$</span>
	                    <span class="preco-valor">{{ formatarPreco(med.preco) }}</span>
	                  </div>
	                </div>

	                <div v-if="med.descricaoProduto" class="medicamento-descricao">
	                  {{ med.descricaoProduto }}
	                </div>

	                <div class="medicamento-detalhes">
	                  <div class="detalhe">
	                    <i class="fas fa-cubes"></i>
	                    <span>Quantidade prescrita: {{ med.quantidade }}</span>
	                  </div>
	                  <div
	                      v-if="med.produtoId"
	                      class="detalhe"
	                      :class="{ 'sem-estoque': med.estoque === 0 }"
	                  >
	                    <i class="fas fa-warehouse"></i>
	                    <span v-if="med.estoque > 0">{{ med.estoque }} em estoque</span>
	                    <span v-else>Esgotado no catálogo</span>
	                  </div>
	                  <div v-else class="detalhe aviso-catalogo">
	                    <i class="fas fa-link"></i>
	                    <span>Não vinculamos automaticamente a um produto do catálogo. Veja abaixo se há alternativa no ClickFarma ou adicione pelo nome da receita.</span>
	                  </div>
	                </div>

		                <details
		                    v-if="med.descricaoIA && med.descricaoIA.includes('OCR com baixa qualidade')"
		                    class="edicao-manual-area"
		                >
	                  <summary class="edicao-manual-summary">
	                    <i class="fas fa-exclamation-triangle"></i>
	                    Revisar nome do medicamento
	                  </summary>
	                  <label>Confirmar nome do medicamento:</label>
	                  <div class="edicao-manual-input-group">
	                    <input v-model="med.nomeEditado" :placeholder="med.nome" class="input-edicao-nome" />
	                    <button @click="buscarProdutoPorNome(med, index)" class="btn-buscar-edicao" :disabled="carregandoBusca">
	                      <i class="fas fa-search"></i> Buscar no catálogo
	                    </button>
	                  </div>
	                </details>

		                <details
		                    v-if="med.alternativasSugeridas?.length"
		                    class="alternativas-box"
		                >
	                  <summary class="alternativas-titulo">
	                    <i class="fas fa-exchange-alt"></i>
	                    No ClickFarma: opções similares ou com efeito próximo (confirme com médico/farmacêutico)
	                  </summary>
	                  <div
	                      v-for="(alt, aidx) in med.alternativasSugeridas"
	                      :key="'alt-' + index + '-' + aidx"
	                      class="alternativa-linha"
	                  >
                    <div class="alternativa-info">
                      <strong>{{ alt.nome }}</strong>
                      <span v-if="alt.motivoIndicacao" class="alternativa-motivo">{{ alt.motivoIndicacao }}</span>
                      <span class="alternativa-meta">
                        R$ {{ formatarPreco(alt.preco) }}
                        · {{ alt.estoque }} em estoque
                      </span>
                    </div>
                    <button
                        type="button"
                        class="btn-alt-add"
                        :disabled="!alt.produtoId || alt.estoque === 0"
                        @click="adicionarAlternativaAoCarrinho(alt)"
                    >
	                      <i class="fas fa-cart-plus"></i> Adicionar
	                    </button>
	                  </div>
	                </details>
	              </div>

	              <div v-if="!somenteLeitura" class="medicamento-acoes">
                <div class="quantidade-selector">
                  <button @click="diminuirQuantidade(index)" class="btn-qty">
                    <i class="fas fa-minus"></i>
                  </button>
                  <input
                      v-model.number="quantidades[index]"
                      type="number"
                      min="1"
                      class="input-qty"
                  />
                  <button @click="aumentarQuantidade(index)" class="btn-qty">
                    <i class="fas fa-plus"></i>
                  </button>
                </div>
                <button
                    @click="adicionarAoCarrinho(med, index)"
                    class="btn-adicionar"
                    :disabled="!med.produtoId || med.estoque === 0"
                >
                  <i class="fas fa-cart-plus"></i> Adicionar
                </button>
              </div>
            </div>
          </div>

	          <div v-if="!somenteLeitura" class="acoes-bulk">
            <button @click="adicionarTodosAoCarrinho" class="btn-adicionar-todos">
              <i class="fas fa-shopping-cart"></i>
              Adicionar Todos ({{ resultado.medicamentos.length }})
            </button>
            <button @click="limparImagem" class="btn-nova-receita">
              <i class="fas fa-redo"></i>
              Nova Receita
            </button>
          </div>
        </div>

        <div v-else-if="resultado.sucesso" class="alerta-sem-medicamentos">
          <i class="fas fa-eye-slash"></i>
          <h4>Nenhum medicamento identificado</h4>
          <p>Tente outra imagem com melhor qualidade</p>
          <button @click="limparImagem" class="btn-tentar-novamente">
            Tentar novamente
          </button>
        </div>

	        <div v-else class="erro-area">
	          <i class="fas fa-exclamation-triangle"></i>
	          <h4>Erro ao processar</h4>
	          <p>{{ resultado.erro }}</p>
	          <button @click="limparImagem" class="btn-tentar-novamente">
	            Tentar novamente
	          </button>
	        </div>

	        <details v-if="debugOcr && resultado.textoOriginal" class="ocr-debug">
	          <summary>Texto lido (OCR)</summary>
	          <pre>{{ resultado.textoOriginal }}</pre>
	        </details>
	      </div>
    </div>

    <!-- Sistema simples de notificação -->
    <div v-if="notificacao.visivel" class="notificacao" :class="notificacao.tipo">
      <div class="notificacao-conteudo">
        <i :class="notificacao.icone"></i>
        <span>{{ notificacao.mensagem }}</span>
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
      notificacao: {
        visivel: false,
        mensagem: '',
        tipo: 'sucesso',
        icone: ''
      }
    };
  },
  created() {
    try {
      this.debugOcr = localStorage.getItem('debug_ocr') === '1';
      // Por padrão, permitimos adicionar ao carrinho quando o item estiver no catálogo (produtoId).
      // Para forçar modo somente leitura, use localStorage.receita_readonly = '1'.
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
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = '';
      }
    },

    async enviarReceita() {
      if (!this.imagemBase64) {
        this.mostrarNotificacao('Selecione uma imagem primeiro', 'erro');
        return;
      }

      this.carregando = true;

      try {
        const response = await receitaService.processarReceita(
            this.imagemBase64,
            this.nomeArquivo
        );
        this.resultado = response;

        // Inicializar quantidades com os valores da receita
        if (response.medicamentos) {
          response.medicamentos.forEach((med, index) => {
            this.quantidades[index] = med.quantidade || 1;
          });
        }
      } catch (error) {
        this.resultado = {
          sucesso: false,
          erro: error.response?.data?.erro || error.message
        };
      } finally {
        this.carregando = false;
      }
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

          if (this.$set) {
            this.$set(this.resultado.medicamentos, index, medicamento);
          }
        } else {
          this.mostrarNotificacao(`Nenhum produto encontrado para "${nomeBusca}"`, 'warning');
        }
      } catch (error) {
        console.error('Erro ao buscar produto:', error);
        this.mostrarNotificacao('Erro ao buscar produto', 'erro');
      } finally {
        this.carregandoBusca = false;
      }
    },

    aumentarQuantidade(index) {
      if (!this.quantidades[index]) {
        this.quantidades[index] = 1;
      }
      this.quantidades[index]++;
    },

    diminuirQuantidade(index) {
      if (this.quantidades[index] && this.quantidades[index] > 1) {
        this.quantidades[index]--;
      }
    },

    adicionarAlternativaAoCarrinho(alt) {
      if (!alt || !alt.produtoId) return;
      const item = {
        id: alt.produtoId,
        name: alt.nome,
        price: alt.preco || 0,
        quantity: 1,
        description: alt.motivoIndicacao || '',
        dosagem: '',
        origem: 'receita-alternativa',
        dataAdicao: new Date().toISOString()
      };
      if (this.$store && this.$store.dispatch) {
        this.$store.dispatch('addToCart', item);
        this.mostrarNotificacao(`${alt.nome} adicionado ao carrinho!`, 'sucesso');
      } else {
        const carrinho = JSON.parse(localStorage.getItem('carrinho') || '[]');
        carrinho.push(item);
        localStorage.setItem('carrinho', JSON.stringify(carrinho));
        this.mostrarNotificacao(`${alt.nome} adicionado (local)!`, 'sucesso');
      }
    },

    adicionarAoCarrinho(medicamento, index) {
      if (!medicamento || !medicamento.produtoId) {
        this.mostrarNotificacao('Este item não está vinculado ao catálogo. Use a busca para encontrar o produto.', 'warning');
        return;
      }
      const quantidade = this.quantidades[index] || medicamento.quantidade || 1;

      const item = {
        id: medicamento.produtoId,
        name: medicamento.nomeCompleto || medicamento.nome,
        price: medicamento.preco || 0,
        quantity: quantidade,
        description: medicamento.descricaoProduto || medicamento.descricaoIA || '',
        dosagem: medicamento.dosagem || '',
        origem: 'receita',
        dataAdicao: new Date().toISOString()
      };

      // Usar a ação correta do Vuex
      if (this.$store && this.$store.dispatch) {
        this.$store.dispatch('addToCart', item);
        this.mostrarNotificacao(`${item.name} adicionado ao carrinho!`, 'sucesso');
      } else {
        // Fallback para localStorage se Vuex não estiver disponível
        const carrinho = JSON.parse(localStorage.getItem('carrinho') || '[]');
        carrinho.push(item);
        localStorage.setItem('carrinho', JSON.stringify(carrinho));
        this.mostrarNotificacao(`${item.name} adicionado ao carrinho (local)!`, 'sucesso');
      }
    },

    adicionarTodosAoCarrinho() {
      if (!this.resultado?.medicamentos) return;

      this.resultado.medicamentos.forEach((med, index) => {
        if (med && med.produtoId && !(med.estoque === 0)) {
          this.adicionarAoCarrinho(med, index);
        }
      });

      this.mostrarNotificacao(`${this.resultado.medicamentos.length} medicamentos adicionados ao carrinho!`, 'sucesso');

      setTimeout(() => {
        if (this.$router) {
          this.$router.push('/cart');
        }
      }, 1500);
    },

    formatarPreco(preco) {
      return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
      }).format(preco);
    },

    mostrarNotificacao(mensagem, tipo = 'sucesso') {
      // Limpar timeout anterior se existir
      if (this.timeoutNotificacao) {
        clearTimeout(this.timeoutNotificacao);
      }

      // Configurar ícone baseado no tipo
      const icones = {
        sucesso: 'fas fa-check-circle',
        erro: 'fas fa-exclamation-circle',
        info: 'fas fa-info-circle',
        warning: 'fas fa-exclamation-triangle'
      };

      this.notificacao = {
        visivel: true,
        mensagem: mensagem,
        tipo: tipo,
        icone: icones[tipo] || icones.info
      };

      // Esconder após 3 segundos
      this.timeoutNotificacao = setTimeout(() => {
        this.notificacao.visivel = false;
      }, 3000);
    }
  },

  beforeUnmount() {
    // Limpar timeout ao destruir componente
    if (this.timeoutNotificacao) {
      clearTimeout(this.timeoutNotificacao);
    }
  }
};
</script>

<style scoped>
.upload-receita-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
}

.receita-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.receita-header {
  background: var(--cf-green);
  color: white;
  padding: 30px;
  text-align: center;
}

.receita-header h2 {
  margin: 0 0 10px;
  font-size: 24px;
}

.descricao {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.upload-area {
  margin: 30px;
  border: 2px dashed #ddd;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  min-height: 250px;
}

.upload-area:hover {
  border-color: var(--cf-green);
  background: var(--cf-green-xlight);
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.upload-placeholder {
  text-align: center;
  padding: 60px 20px;
}

.upload-placeholder i {
  font-size: 64px;
  color: #999;
  margin-bottom: 20px;
}

.preview-area {
  position: relative;
  text-align: center;
  padding: 20px;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
}

.preview-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
}

.btn-clear {
  background: var(--cf-danger);
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 12px;
}

.btn-clear:hover {
  background: #c82333;
}

.actions {
  text-align: center;
  margin: 20px 30px 30px;
}

.btn-processar {
  background: var(--cf-gold);
  color: white;
  border: none;
  padding: 14px 30px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  width: 100%;
  font-weight: 600;
}

.btn-processar:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(220, 169, 64, 0.4);
}

.loading-area {
  text-align: center;
  padding: 40px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--cf-ivory);
  border-top: 4px solid var(--cf-green);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.resultados {
  margin: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.resultados h3 {
  color: #333;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.ocr-debug {
  margin-top: 16px;
  padding: 12px;
  border-radius: 10px;
  background: #f1f3f5;
  border: 1px solid #e9ecef;
}

.ocr-debug summary {
  cursor: pointer;
  font-weight: 600;
  color: #495057;
}

.ocr-debug pre {
  margin: 10px 0 0;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 260px;
  overflow: auto;
  font-size: 12px;
  color: #343a40;
}

.medicamentos-lista {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.medicamento-card {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-left: 4px solid var(--cf-green);
  gap: 15px;
}

.medicamento-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.medicamento-info {
  flex: 1;
}

.medicamento-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.medicamento-nome {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.medicamento-nome strong {
  font-size: 16px;
  color: #333;
}

.dosagem {
  display: inline-block;
  background: #e9ecef;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  color: #666;
}

.badge-ocr-ruim {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #ffc107;
  color: #856404;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
}

.medicamento-preco {
  display: flex;
  align-items: baseline;
  gap: 4px;
  font-weight: 600;
}

.preco-label {
  font-size: 12px;
  color: var(--cf-green);
}

.preco-valor {
  font-size: 18px;
  color: var(--cf-green);
}

.medicamento-descricao {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.4;
}

.medicamento-detalhes {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #666;
  flex-wrap: wrap;
}

.detalhe {
  display: flex;
  align-items: center;
  gap: 5px;
}

.detalhe.sem-estoque {
  color: #dc3545;
  font-weight: 600;
}

.detalhe.aviso-catalogo {
  color: #856404;
  background: #fff8e1;
  padding: 6px 8px;
  border-radius: 6px;
  font-size: 11px;
  line-height: 1.35;
}

.edicao-manual-area {
  margin-top: 12px;
  padding: 12px;
  background: #fff8e1;
  border-radius: 8px;
  border-left: 3px solid #ffc107;
}

.edicao-manual-summary {
  cursor: pointer;
  font-size: 12px;
  font-weight: 700;
  color: #856404;
  display: flex;
  align-items: center;
  gap: 8px;
  list-style: none;
}

.edicao-manual-summary::-webkit-details-marker {
  display: none;
}

.edicao-manual-area label {
  font-size: 12px;
  color: #856404;
  margin-bottom: 6px;
  display: block;
  font-weight: 500;
  margin-top: 10px;
}

.edicao-manual-input-group {
  display: flex;
  gap: 8px;
}

.input-edicao-nome {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ffc107;
  border-radius: 6px;
  font-size: 14px;
}

.input-edicao-nome:focus {
  outline: none;
  border-color: #e0a800;
  box-shadow: 0 0 0 2px rgba(255, 193, 7, 0.2);
}

.btn-buscar-edicao {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: background 0.2s;
}

.btn-buscar-edicao:hover:not(:disabled) {
  background: #218838;
}

.btn-buscar-edicao:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.orientacao-clickfarma {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  background: linear-gradient(135deg, #e8f4fc 0%, #f0e8fc 100%);
  border: 1px solid #c5d9f0;
  border-radius: 12px;
  padding: 16px 18px;
  margin-bottom: 20px;
  font-size: 14px;
  line-height: 1.55;
  color: #2c3e50;
}

.orientacao-clickfarma i {
  color: #667eea;
  font-size: 20px;
  margin-top: 2px;
  flex-shrink: 0;
}

.orientacao-clickfarma p {
  margin: 0;
}

.alternativas-box {
  margin-top: 12px;
  padding: 12px;
  background: #f8f9ff;
  border-radius: 10px;
  border: 1px dashed #a8b4f5;
}

.alternativas-titulo {
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  list-style: none;
}

.alternativas-titulo::-webkit-details-marker {
  display: none;
}

.alternativa-linha {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
  border-top: 1px solid #e2e8f0;
}

.alternativa-linha:first-of-type {
  border-top: none;
  padding-top: 0;
}

.alternativa-info {
  flex: 1;
  min-width: 0;
}

.alternativa-info strong {
  display: block;
  color: #2d3748;
  margin-bottom: 4px;
}

.alternativa-motivo {
  display: block;
  font-size: 12px;
  color: #5a6778;
  margin-bottom: 4px;
  line-height: 1.35;
}

.alternativa-meta {
  font-size: 12px;
  color: #718096;
}

.btn-alt-add {
  flex-shrink: 0;
  padding: 8px 14px;
  font-size: 13px;
  border: none;
  border-radius: 8px;
  background: #667eea;
  color: white;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-alt-add:hover:not(:disabled) {
  background: #5a6fd6;
}

.btn-alt-add:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.medicamento-acoes {
  display: flex;
  gap: 10px;
  align-items: center;
}

.quantidade-selector {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
}

.btn-qty {
  background: none;
  border: none;
  padding: 6px 8px;
  cursor: pointer;
  color: var(--cf-gold);
  font-size: 14px;
}

.btn-qty:hover {
  background: #f0f0f0;
}

.input-qty {
  width: 40px;
  border: none;
  text-align: center;
  font-weight: 600;
  padding: 4px 0;
}

.input-qty:focus {
  outline: none;
}

.btn-adicionar {
  background: var(--cf-green);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
  font-size: 13px;
  font-weight: 600;
}

.btn-adicionar:hover:not(:disabled) {
  background: var(--cf-green-dark);
}

.btn-adicionar:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 0.6;
}

.acoes-bulk {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #dee2e6;
}

.btn-adicionar-todos {
  flex: 1;
  background: var(--cf-green);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-adicionar-todos:hover {
  background: var(--cf-green-dark);
}

.btn-nova-receita {
  flex: 1;
  background: var(--cf-ivory);
  color: var(--cf-text-dark);
  border: 1px solid var(--cf-border);
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-nova-receita:hover {
  background: var(--cf-border-mid);
}

.alerta-sem-medicamentos,
.erro-area {
  text-align: center;
  padding: 40px;
}

.alerta-sem-medicamentos i {
  font-size: 48px;
  color: #ffc107;
  margin-bottom: 15px;
  display: block;
}

.erro-area i {
  font-size: 48px;
  color: var(--cf-danger);
  margin-bottom: 15px;
  display: block;
}

.btn-tentar-novamente {
  background: var(--cf-gold);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 15px;
}

.btn-tentar-novamente:hover {
  opacity: 0.9;
}

/* Estilos da notificação */
.notificacao {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  animation: slideIn 0.3s ease-out;
}

.notificacao-conteudo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  font-size: 14px;
  font-weight: 500;
}

.notificacao.sucesso .notificacao-conteudo {
  background: #d4edda;
  color: #155724;
  border-left: 4px solid #28a745;
}

.notificacao.erro .notificacao-conteudo {
  background: #f8d7da;
  color: #721c24;
  border-left: 4px solid #dc3545;
}

.notificacao.info .notificacao-conteudo {
  background: #d1ecf1;
  color: #0c5460;
  border-left: 4px solid #17a2b8;
}

.notificacao.warning .notificacao-conteudo {
  background: #fff3cd;
  color: #856404;
  border-left: 4px solid #ffc107;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .medicamento-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .medicamento-header {
    flex-direction: column;
    width: 100%;
  }

  .medicamento-preco {
    margin-top: 8px;
  }

  .medicamento-acoes {
    width: 100%;
    flex-direction: column;
  }

  .btn-adicionar {
    width: 100%;
  }

  .quantidade-selector {
    width: 100%;
    justify-content: center;
  }

  .acoes-bulk {
    flex-direction: column;
  }

  .btn-adicionar-todos,
  .btn-nova-receita {
    width: 100%;
  }

  .notificacao {
    left: 20px;
    right: 20px;
    top: 10px;
  }

  .edicao-manual-input-group {
    flex-direction: column;
  }

  .btn-buscar-edicao {
    width: 100%;
  }
}
</style>
