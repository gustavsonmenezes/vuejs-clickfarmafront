<template>
  <div class="cart-ai-advisor" :class="{ expanded: isExpanded }">
    <div class="advisor-header" @click="toggleAdvisor">
      <div class="header-left">
        <span class="icon">💡</span>
        <div>
          <h4>Consultor Inteligente</h4>
          <span v-if="analysis" class="subtitle">Análise disponível</span>
        </div>
      </div>
      <span class="arrow">{{ isExpanded ? '▲' : '▼' }}</span>
    </div>

    <div v-show="isExpanded" class="advisor-body">
      <!-- Sem análise ainda -->
      <div v-if="!isAnalyzing && !analysis && !error" class="state">
        <p>Nossa IA analisa seu carrinho e sugere economia, interações e dicas de uso.</p>
        <button @click="analyzeCart" class="btn-analyze" :disabled="cartItems.length === 0">
          ✨ Analisar Carrinho
        </button>
      </div>

      <!-- Carregando -->
      <div v-if="isAnalyzing" class="state loading">
        <div class="spinner"></div>
        <p>Analisando seu carrinho...</p>
      </div>

      <!-- Resultado -->
      <div v-if="analysis && !isAnalyzing" class="state">
        <div class="result" v-html="renderedAnalysis"></div>
        <button @click="analyzeCart" class="btn-retry">🔄 Analisar Novamente</button>
      </div>

      <!-- Erro -->
      <div v-if="error" class="state">
        <div class="error-msg">❌ {{ error }}</div>
        <button @click="clearError" class="btn-retry">Fechar</button>
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
    cartItems: { type: Array, required: true },
    cartTotal: { type: Number, required: true }
  },
  data() {
    return {
      isExpanded: true,
      isAnalyzing: false,
      analysis: null,
      error: null
    }
  },
  computed: {
    renderedAnalysis() {
      if (!this.analysis) return ''
      try {
        return marked(this.analysis, { breaks: true, gfm: true })
      } catch (e) {
        return this.analysis
      }
    }
  },
  mounted() {
    console.log('✅ CartAIAdvisor montado! Items:', this.cartItems.length, 'Total:', this.cartTotal)
  },
  methods: {
    toggleAdvisor() { this.isExpanded = !this.isExpanded },
    async analyzeCart() {
      if (this.cartItems.length === 0) {
        this.error = 'Seu carrinho está vazio.'
        return
      }
      this.isAnalyzing = true
      this.error = null
      try {
        const cartData = {
          items: this.cartItems.map(item => ({
            id: item.id, name: item.name, description: item.description,
            price: item.price, quantity: item.quantity, category: item.category
          })),
          totalPrice: this.cartTotal
        }
        const response = await api.post('/gemini/analyze-cart', cartData)
        if (response.data && response.data.analysis) {
          this.analysis = response.data.analysis
        } else {
          throw new Error('Resposta inválida')
        }
      } catch (error) {
        let msg = 'Não consegui analisar seu carrinho. Tente novamente.'
        if (error.response?.data?.analysis) msg = error.response.data.analysis
        else if (error.response?.data?.error) msg = error.response.data.error
        else if (error.response?.status === 500) msg = 'Erro no servidor. Verifique a API Gemini.'
        else if (error.message === 'Network Error') msg = 'Erro de conexão.'
        this.error = msg
      } finally { this.isAnalyzing = false }
    },
    clearError() { this.error = null }
  }
}
</script>

<style scoped>
.cart-ai-advisor {
  border: 2px solid #2A5C45;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  margin-bottom: 1.5rem;
}

.advisor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #F0F7F3;
  cursor: pointer;
  user-select: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-left .icon {
  font-size: 1.3rem;
}

.advisor-header h4 {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #1C1C1A;
  font-family: inherit;
}

.subtitle {
  font-size: 0.8rem;
  color: #2A5C45;
  font-weight: 500;
}

.arrow {
  font-size: 0.7rem;
  color: #868680;
}

.advisor-body {
  padding: 20px;
  background: #FAF9F6;
  border-top: 1px solid rgba(28,28,26,0.1);
}

.state { text-align: center; }

.state p {
  color: #868680;
  font-size: 0.875rem;
  margin: 0 0 14px;
}

.btn-analyze {
  background: #2A5C45;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 24px;
  font-size: 0.9rem;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.btn-analyze:hover { background: #1C3D2E; }
.btn-analyze:disabled { opacity: 0.5; cursor: not-allowed; }

.spinner {
  width: 32px; height: 32px;
  border: 3px solid #E8E5DE;
  border-top-color: #B89550;
  border-radius: 50%;
  margin: 0 auto 10px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.loading p { color: #4A4A47; font-weight: 500; }

.result {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid rgba(28,28,26,0.1);
  margin-bottom: 14px;
  font-size: 0.875rem;
  line-height: 1.6;
  color: #4A4A47;
  max-height: 300px;
  overflow-y: auto;
  text-align: left;
}

.result :deep(strong) { color: #1C1C1A; }

.btn-retry {
  background: transparent;
  color: #2A5C45;
  border: 1px solid #2A5C45;
  border-radius: 8px;
  padding: 8px 20px;
  font-size: 0.85rem;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-retry:hover {
  background: #2A5C45;
  color: #fff;
}

.error-msg {
  background: #F9EDED;
  border: 1px solid rgba(139, 58, 58, 0.15);
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 12px;
  color: #8B3A3A;
  font-size: 0.875rem;
}
</style>
