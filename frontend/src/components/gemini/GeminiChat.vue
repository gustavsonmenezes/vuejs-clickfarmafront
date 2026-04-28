<template>
  <div class="gemini-chat">
    <div class="chat-header">
      <i class="fa-solid fa-robot"></i>
      <h3>Assistente Virtual</h3>
      <button @click="$emit('close')" class="close-btn">✕</button>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role]"
      >
        <div class="message-avatar">
          <i :class="msg.role === 'user' ? 'fa-solid fa-user' : 'fa-solid fa-robot'"></i>
        </div>
        <div class="message-content">
          <div class="message-text" v-if="msg.content" v-html="renderMarkdown(msg.content)"></div>
          
          <div v-if="msg.image" class="message-image-preview">
            <img :src="msg.image" alt="Receita Analisada" style="max-width: 200px; border-radius: 8px; margin-top: 8px; border: 2px solid var(--cf-green);" />
          </div>

          <div v-if="msg.suggestedProducts && msg.suggestedProducts.length" class="chat-products-wrapper">
            <div v-for="prod in msg.suggestedProducts" :key="prod.id" class="chat-product-item">
              <ProductCard :product="prod" />
            </div>
          </div>

          <div class="message-time">{{ msg.time }}</div>
        </div>
      </div>

      <div v-if="loading" class="message bot">
        <div class="message-avatar">
          <i class="fa-solid fa-robot"></i>
        </div>
        <div class="message-content">
          <div class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-wrapper">
      <div class="chat-input">
        <input 
          type="file" 
          ref="receitaInput" 
          @change="tratarUploadReceita" 
          accept="image/jpeg,image/png,image/jpg" 
          style="display: none;" 
        />
        <button class="attach-btn" title="Anexar Receita" @click="openPrescriptionUpload">
          <i class="fa-solid fa-camera"></i>
        </button>
        <textarea
            v-model="userMessage"
            class="message-textarea"
            @keyup.enter.exact="sendMessage"
            @keyup.enter.shift.exact="userMessage += '\n'"
            placeholder="Digite sua mensagem..."
            rows="1"
            :disabled="loading"
        ></textarea>
        <button class="send-btn" @click="sendMessage" :disabled="loading || !userMessage.trim()">
          <i class="fa-solid fa-paper-plane"></i>
        </button>
      </div>
    </div>

    <div v-if="messages.length <= 1" class="chat-suggestions">
      <button
          v-for="suggestion in suggestions"
          :key="suggestion"
          @click="sendSuggestion(suggestion)"
          class="suggestion-btn"
      >
        {{ suggestion }}
      </button>
      <button @click="getWellnessSuggestions" class="suggestion-btn wellness-btn">
        🌿 Recomendações de Bem-Estar
      </button>
    </div>
  </div>
</template>

<script>
import api from '@/services/api'
import { marked } from 'marked'
import { mapState, mapActions } from 'vuex'
import ProductCard from '@/components/products/ProductCard.vue'
import receitaService from '@/services/receitaService'

export default {
  name: 'GeminiChat',
  components: {
    ProductCard
  },
  data() {
    return {
      messages: [
        {
          role: 'bot',
          content: 'Olá! Sou o assistente virtual da ClickFarma. Como posso ajudar você hoje?',
          time: this.getCurrentTime()
        }
      ],
      userMessage: '',
      loading: false,
      suggestions: [
        '💊 Qual remédio para dor de cabeça?',
        '📦 Como rastrear meu pedido?',
        '🚚 Qual prazo de entrega?',
        '💳 Formas de pagamento'
      ]
    }
  },
  computed: {
    ...mapState(['products'])
  },
  mounted() {
    this.scrollToBottom();
    // Dispatch fetchProducts se necessário
    if (!this.products || this.products.length === 0) {
      if (typeof this.$store.dispatch === 'function') {
        this.$store.dispatch('fetchProducts');
      }
    }
  },
  methods: {
    ...mapActions(['addToCart']),

    extractSuggestedProducts(text) {
      if (!text || !this.products || !this.products.length) return [];
      const lowerText = text.toLowerCase();
      // Only returns REAL products from the Database
      const matched = this.products.filter(p => {
        if (!p.name) return false;
        const pName = p.name.toLowerCase();
        if (pName.length < 4) return false;
        return lowerText.includes(pName);
      });
      // Limit to max 4 items to avoid overwhelming the chat
      return matched.slice(0, 4);
    },

    renderMarkdown(text) {
      if (!text) return '';
      // Retorna safe HTML caso venha apenas texto, e converte ### ou * etc
      return marked.parse(text);
    },
    getCurrentTime() {
      return new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },

    async getWellnessSuggestions() {
      this.loading = true;
      try {
        const userId = localStorage.getItem('userId');
        const userName = localStorage.getItem('userName');

        const response = await api.post('/gemini/wellness', {
          userId: userId,
          userName: userName
        });

        const botResponse = response.data.response;
        const suggestions = this.extractSuggestedProducts(botResponse);

        this.messages.push({
          role: 'bot',
          content: botResponse,
          suggestedProducts: suggestions,
          time: this.getCurrentTime()
        });
      } catch (error) {
        console.error('Erro ao obter recomendações de bem-estar:', error);
        this.messages.push({
          role: 'bot',
          content: 'Desculpe, não consegui gerar recomendações de bem-estar no momento.',
          time: this.getCurrentTime()
        });
      } finally {
        this.loading = false;
        this.scrollToBottom();
      }
    },

    async sendMessage() {
      if (!this.userMessage.trim() || this.loading) return

      const message = this.userMessage.trim()
      this.messages.push({
        role: 'user',
        content: message,
        time: this.getCurrentTime()
      })
      this.userMessage = ''
      this.scrollToBottom()

      this.loading = true

      try {
        // Collect history of the chat avoiding huge base64 images or products arrays
        const history = this.messages
          .filter(m => m.role === 'user' || m.role === 'bot')
          .map(m => {
            // Groq uses 'assistant' for the bot role instead of 'bot'
            const role = m.role === 'bot' ? 'assistant' : 'user';
            
            // if it's an image block that has content, just say an image was sent
            let content = m.content || "";
            if (m.image) {
              content += " [Imagem anexada]";
            }
            return { role: role, content: content };
          });

        const response = await api.post('/gemini/chat', {
          message: message, // keeping for backwards compatibility
          messages: history
        })

        const botResponse = response.data.response;
        const suggestions = this.extractSuggestedProducts(botResponse);

        this.messages.push({
          role: 'bot',
          content: botResponse,
          suggestedProducts: suggestions,
          time: this.getCurrentTime()
        })
      } catch (error) {
        console.error('Erro completo:', error)
        console.error('Detalhes da resposta:', error.response)

        let errorMessage = 'Desculpe, estou com problemas. Tente novamente mais tarde.'

        if (error.response) {
          console.error('Status:', error.response.status)
          console.error('Data:', error.response.data)

          if (error.response.status === 403) {
            errorMessage = 'Erro de permissão. Verificando configurações...'
          } else if (error.response.status === 401) {
            errorMessage = 'Você precisa estar autenticado para usar o chat.'
          } else if (error.response.status === 500) {
            errorMessage = 'Erro no servidor. Verifique se a chave da API Gemini está configurada.'
          } else if (error.response.data?.response) {
            errorMessage = error.response.data.response
          }
        } else if (error.message === 'Network Error') {
          errorMessage = 'Erro de conexão. Verifique se o backend está rodando em http://localhost:8080'
        }

        this.messages.push({
          role: 'bot',
          content: errorMessage,
          time: this.getCurrentTime()
        })
      } finally {
        this.loading = false
        this.scrollToBottom()
      }
    },

    sendSuggestion(suggestion) {
      this.userMessage = suggestion
      this.sendMessage()
    },

    openPrescriptionUpload() {
      if (this.$refs.receitaInput) {
        this.$refs.receitaInput.click();
      }
    },

    tratarUploadReceita(event) {
      const arquivo = event.target.files[0];
      if (!arquivo) return;

      const reader = new FileReader();
      reader.onload = async (e) => {
        const fullDataUrl = e.target.result;
        const imagemBase64 = fullDataUrl.split(',')[1];
        
        this.messages.push({
          role: 'user',
          content: '📸 Enviei uma receita médica. Pode analisar?',
          image: fullDataUrl,
          time: this.getCurrentTime()
        });
        
        this.loading = true;
        this.scrollToBottom();

        try {
          const response = await receitaService.processarReceita(imagemBase64, arquivo.name);
          if (response.sucesso && response.medicamentos && response.medicamentos.length > 0) {
            
            // Map the API results to our ProductCard acceptable shape safely
            const mappedProducts = response.medicamentos.map(m => ({
                id: m.produtoId || Math.floor(Math.random() * 100000).toString(),
                name: m.nomeCompleto || m.nome,
                price: m.preco || 0,
                inStock: (m.estoque !== undefined && m.estoque > 0) ? true : false,
                category: 'Medicamentos',
                description: m.descricaoProduto || ''
            }));

            this.messages.push({
              role: 'bot',
              content: 'Prontinho! Li a receita. Consegui identificar os seguintes medicamentos:',
              suggestedProducts: mappedProducts,
              time: this.getCurrentTime()
            });

          } else {
             this.messages.push({
               role: 'bot',
               content: 'Desculpe, não consegui identificar medicamentos na imagem. Tente uma foto mais nítida.',
               time: this.getCurrentTime()
             });
          }
        } catch (error) {
          this.messages.push({
            role: 'bot',
            content: '⚠️ Ocorreu um erro ao processar a receita. O serviço de leitura inteligente pode estar indisponível.',
            time: this.getCurrentTime()
          });
          console.error(error);
        } finally {
          this.loading = false;
          this.scrollToBottom();
          // Reset file input
          if (this.$refs.receitaInput) {
            this.$refs.receitaInput.value = '';
          }
        }
      };
      
      reader.readAsDataURL(arquivo);
    }
  }
}
</script>

<style scoped>
.gemini-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--cf-white);
  border-radius: var(--cf-r-xl);
  overflow: hidden;
  box-shadow: var(--cf-shadow-lg);
  border: 1px solid var(--cf-border);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 1.1rem 1.4rem;
  background: var(--cf-white);
  border-bottom: 2px solid var(--cf-green-xlight);
  color: var(--cf-green);
}

.chat-header i { font-size: 1.2rem; opacity: 0.9; }
.chat-header h3 {
  flex: 1;
  margin: 0;
  font-family: var(--cf-sans);
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--cf-text-dark);
}

.close-btn {
  background: var(--cf-ivory);
  border: 1px solid var(--cf-border-mid);
  color: var(--cf-text-muted);
  width: 28px; height: 28px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all 200ms var(--cf-ease);
}
.close-btn:hover { background: rgba(255,255,255,0.2); transform: rotate(90deg); }

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  background: var(--cf-ivory);
  display: flex;
  flex-direction: column;
}

.message {
  display: flex;
  gap: 10px;
  margin-bottom: 1.2rem;
  max-width: 85%;
  animation: messageUp 0.4s var(--cf-ease) both;
}

@keyframes messageUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user { align-self: flex-end; flex-direction: row-reverse; }
.message.bot { align-self: flex-start; }

.message-avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  font-size: 0.9rem;
}
.message.bot .message-avatar { background: var(--cf-green-light); color: var(--cf-green); border: 1px solid var(--cf-green-mid); }
.message.user .message-avatar { background: var(--cf-ivory); color: var(--cf-text-mid); border: 1px solid var(--cf-border-mid); }

.message-text {
  padding: 0.8rem 1rem;
  border-radius: var(--cf-r-md);
  font-size: 0.9rem;
  line-height: 1.5;
  box-shadow: var(--cf-shadow-xs);
  word-wrap: break-word;
}

:deep(.message-text p) {
  margin-bottom: 0.5rem;
}
:deep(.message-text p:last-child) {
  margin-bottom: 0;
}
:deep(.message-text ul), :deep(.message-text ol) {
  margin-bottom: 0.5rem;
  padding-left: 1.5rem;
}
:deep(.message-text ul li), :deep(.message-text ol li) {
  margin-bottom: 0.2rem;
}
:deep(.message-text h1), :deep(.message-text h2), :deep(.message-text h3) {
  font-size: 1.05rem;
  font-weight: 600;
  margin-top: 0.5rem;
  margin-bottom: 0.5rem;
  color: inherit;
}

.message.bot .message-text { 
  background: var(--cf-white); 
  border: 1px solid var(--cf-border); 
}
:deep(.message.bot .message-text), :deep(.message.bot .message-text *) {
  color: var(--cf-text-dark); 
}

.message.user .message-text { 
  background: var(--cf-green); 
}
:deep(.message.user .message-text), :deep(.message.user .message-text *) {
  color: #ffffff !important; 
}


.message-time { font-size: 0.65rem; color: var(--cf-text-faint); margin-top: 4px; padding: 0 4px; }

/* CHAT PRODUCT EMBED */
.chat-products-wrapper {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: nowrap;
  overflow-x: auto;
  padding-bottom: 5px;
}
.chat-product-item {
  min-width: 160px;
  max-width: 200px;
  flex-shrink: 0;
  /* Transform origin allows scale if we wanted, but simple width constraint is better */
}
/* Forçar mini-cartão por estar dentro do chat */
:deep(.chat-product-item .cf-product-card) {
  border-radius: 8px;
}
:deep(.chat-product-item .cf-card-body) {
  padding: 8px;
  gap: 4px;
}
:deep(.chat-product-item .cf-category-label),
:deep(.chat-product-item .cf-wishlist),
:deep(.chat-product-item .cf-product-desc) {
  display: none !important;
}
:deep(.chat-product-item .cf-product-name) {
  font-size: 0.8rem;
  margin: 0 0 2px 0;
  line-height: 1.2;
  white-space: normal;
}
:deep(.chat-product-item .cf-product-icon) {
  font-size: 2.2rem;
}
:deep(.chat-product-item .cf-price) {
  font-size: 0.9rem;
}
:deep(.chat-product-item .cf-installment) {
  font-size: 0.6rem;
}
:deep(.chat-product-item .cf-card-foot) {
  padding-top: 4px;
  margin-top: 2px;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}
:deep(.chat-product-item .cf-add-btn) {
  width: 100%;
  padding: 4px 6px;
  font-size: 0.65rem;
  justify-content: center;
}

/* INDICATOR */
.typing-indicator {
  display: flex; gap: 4px; padding: 10px;
}
.typing-indicator span {
  width: 6px; height: 6px; background: var(--cf-green-mid);
  border-radius: 50%; animation: typing 1.4s infinite;
}
@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* INPUT */
.chat-input-wrapper {
  background: var(--cf-white);
  border-top: 1px solid var(--cf-border);
}

.chat-input {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 1rem 1.4rem;
}

.message-textarea {
  flex: 1;
  border: 1px solid var(--cf-border-mid);
  border-radius: var(--cf-r-md);
  padding: 0.6rem 1rem;
  font-size: 0.88rem;
  outline: none;
  transition: border 200ms;
  resize: none;
}
.message-textarea:focus { border-color: var(--cf-green); }

.attach-btn {
  background: none;
  border: none;
  color: var(--cf-text-faint);
  font-size: 1.1rem;
  cursor: pointer;
  transition: color 200ms;
  padding: 5px;
}
.attach-btn:hover { color: var(--cf-green); }

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--cf-green);
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 200ms;
  flex-shrink: 0;
}
.send-btn:hover:not(:disabled) { background: var(--cf-green-dark); transform: scale(1.05); }
.send-btn:disabled { background: var(--cf-cream); color: var(--cf-text-faint); cursor: not-allowed; }

/* SUGGESTIONS */
.chat-suggestions {
  display: flex; flex-wrap: wrap; gap: 6px;
  padding: 0.8rem 1.4rem 1.2rem; background: var(--cf-white);
}
.suggestion-btn {
  font-size: 0.72rem; padding: 5px 12px;
  border-radius: 100px; border: 1px solid var(--cf-border-mid);
  background: var(--cf-ivory); color: var(--cf-text-muted);
  cursor: pointer; transition: all 200ms;
}
.suggestion-btn:hover { border-color: var(--cf-green); color: var(--cf-green); background: var(--cf-green-xlight); }

.wellness-btn {
  background: var(--cf-green-xlight); color: var(--cf-green);
  border-color: var(--cf-green-light); font-weight: 500;
}
</style>