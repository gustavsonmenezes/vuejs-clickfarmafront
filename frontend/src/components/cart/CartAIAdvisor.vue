<template>
  <div class="cart-ai-advisor">
    <div class="advisor-header">
      <div class="header-content">
        <i class="fas fa-lightbulb"></i>
        <h4>Consultor Inteligente</h4>
      </div>
      <button @click="toggleAdvisor" class="toggle-btn" :title="isExpanded ? 'Minimizar' : 'Expandir'">
        <i :class="isExpanded ? 'fas fa-chevron-up' : 'fas fa-chevron-down'"></i>
      </button>
    </div>

    <div v-if="isExpanded" class="advisor-content">
      <!-- Botão para analisar carrinho -->
      <div v-if="!isAnalyzing && !analysis" class="analyze-section">
        <p class="analyze-description">
          Deixe nosso assistente inteligente analisar seu carrinho e fornecer sugestões de economia, dicas de uso e alertas importantes.
        </p>
        <button @click="analyzeCart" class="btn-analyze" :disabled="cartItems.length === 0">
          <i class="fas fa-magic"></i> Analisar Carrinho
        </button>
      </div>

      <!-- Estado de carregamento -->
      <div v-if="isAnalyzing" class="loading-section">
        <div class="spinner">
          <div class="spinner-circle"></div>
        </div>
        <p>Analisando seu carrinho...</p>
      </div>

      <!-- Análise renderizada em Markdown -->
      <div v-if="analysis && !isAnalyzing" class="analysis-section">
        <div class="analysis-content" v-html="renderedAnalysis"></div>
        <button @click="analyzeCart" class="btn-reanalyze">
          <i class="fas fa-redo"></i> Analisar Novamente
        </button>
      </div>

      <!-- Mensagem de erro -->
      <div v-if="error" class="error-section">
        <div class="alert alert-danger">
          <i class="fas fa-exclamation-circle"></i>
          <span>{{ error }}</span>
        </div>
        <button @click="clearError" class="btn-dismiss">Descartar</button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api'
import { marked } from 'marked'

export default {
  name: 'CartAIAdvisor',
  props: {
    cartItems: {
      type: Array,
      required: true
    },
    cartTotal: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      isExpanded: false,
      isAnalyzing: false,
      analysis: null,
      error: null
    }
  },
  computed: {
    renderedAnalysis() {
      if (!this.analysis) return ''
      try {
        return marked(this.analysis, {
          breaks: true,
          gfm: true
        })
      } catch (e) {
        console.error('Erro ao renderizar Markdown:', e)
        return `<pre>${this.escapeHtml(this.analysis)}</pre>`
      }
    }
  },
  methods: {
    toggleAdvisor() {
      this.isExpanded = !this.isExpanded
    },

    async analyzeCart() {
      if (this.cartItems.length === 0) {
        this.error = 'Seu carrinho está vazio. Adicione itens antes de analisar.'
        return
      }

      this.isAnalyzing = true
      this.error = null

      try {
        // Preparar dados do carrinho para envio
        const cartData = {
          items: this.cartItems.map(item => ({
            id: item.id,
            name: item.name,
            description: item.description,
            price: item.price,
            quantity: item.quantity,
            category: item.category
          })),
          totalPrice: this.cartTotal
        }

        console.log('📤 Enviando análise de carrinho:', cartData)

        // Fazer requisição para o backend
        const response = await api.post('/gemini/analyze-cart', cartData)

        if (response.data && response.data.analysis) {
          this.analysis = response.data.analysis
          console.log('✅ Análise recebida com sucesso')
        } else {
          throw new Error('Resposta inválida do servidor')
        }
      } catch (error) {
        console.error('❌ Erro ao analisar carrinho:', error)

        let errorMessage = 'Desculpe, não consegui analisar seu carrinho. Tente novamente.'

        if (error.response) {
          if (error.response.status === 401) {
            errorMessage = 'Você precisa estar autenticado para usar o consultor.'
          } else if (error.response.status === 500) {
            errorMessage = 'Erro no servidor. Verifique se a chave da API Gemini está configurada.'
          } else if (error.response.data?.analysis) {
            errorMessage = error.response.data.analysis
          } else if (error.response.data?.error) {
            errorMessage = error.response.data.error
          }
        } else if (error.message === 'Network Error') {
          errorMessage = 'Erro de conexão. Verifique se o servidor está rodando.'
        }

        this.error = errorMessage
      } finally {
        this.isAnalyzing = false
      }
    },

    clearError() {
      this.error = null
    },

    escapeHtml(text) {
      const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
      }
      return text.replace(/[&<>"']/g, m => map[m])
    }
  }
}
</script>

<style scoped>
.cart-ai-advisor {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 2rem;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
}

.advisor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: rgba(0, 0, 0, 0.1);
  cursor: pointer;
  user-select: none;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
}

.header-content i {
  font-size: 20px;
}

.header-content h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.toggle-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.toggle-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.advisor-content {
  padding: 20px;
  background: white;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.analyze-section {
  text-align: center;
}

.analyze-description {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
  line-height: 1.5;
}

.btn-analyze,
.btn-reanalyze {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-analyze:hover:not(:disabled),
.btn-reanalyze:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-analyze:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-section {
  text-align: center;
  padding: 20px;
}

.spinner {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.spinner-circle {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-section p {
  color: #667eea;
  font-weight: 600;
  margin: 0;
}

.analysis-section {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.analysis-content {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  line-height: 1.6;
  color: #333;
  max-height: 400px;
  overflow-y: auto;
}

.analysis-content :deep(h1),
.analysis-content :deep(h2),
.analysis-content :deep(h3),
.analysis-content :deep(h4),
.analysis-content :deep(h5),
.analysis-content :deep(h6) {
  color: #667eea;
  margin-top: 15px;
  margin-bottom: 10px;
  font-weight: 600;
}

.analysis-content :deep(h1) {
  font-size: 1.5rem;
}

.analysis-content :deep(h2) {
  font-size: 1.3rem;
}

.analysis-content :deep(h3) {
  font-size: 1.1rem;
}

.analysis-content :deep(strong) {
  color: #667eea;
  font-weight: 600;
}

.analysis-content :deep(ul),
.analysis-content :deep(ol) {
  margin: 10px 0;
  padding-left: 20px;
}

.analysis-content :deep(li) {
  margin: 5px 0;
}

.analysis-content :deep(code) {
  background: #e9ecef;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  color: #d63384;
}

.analysis-content :deep(pre) {
  background: #e9ecef;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
}

.error-section {
  margin-bottom: 15px;
}

.alert {
  padding: 12px 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.alert-danger {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.alert-danger i {
  font-size: 18px;
}

.btn-dismiss {
  background: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-dismiss:hover {
  background: #c82333;
}

.btn-reanalyze {
  width: 100%;
  justify-content: center;
}

/* Responsivo */
@media (max-width: 768px) {
  .advisor-content {
    padding: 15px;
  }

  .analysis-content {
    max-height: 300px;
  }

  .header-content h4 {
    font-size: 14px;
  }
}
</style>
