<template>
  <div class="gemini-chat">
    <div class="chat-header">
      <div class="header-left">
        <div class="ai-avatar">
          <i class="fas fa-robot"></i>
        </div>
        <div class="header-info">
          <h3>Assistente ClickFarma</h3>
          <span class="status-badge"><span class="status-dot"></span> Online</span>
        </div>
      </div>
      <button @click="$emit('close')" class="close-btn" title="Fechar chat">
        <i class="fas fa-times"></i>
      </button>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role]"
      >
        <div class="message-avatar">
          <i :class="msg.role === 'user' ? 'fas fa-user' : 'fas fa-robot'"></i>
        </div>
        <div class="message-content">
          <div class="message-bubble">
            <div class="message-text">{{ msg.content }}</div>
          </div>
          
          <div v-if="msg.suggestedProduct" class="chat-product-card">
            <div class="prod-image">
              <i class="fas fa-pills"></i>
            </div>
            <div class="prod-details">
              <span class="prod-name">{{ msg.suggestedProduct.nome }}</span>
              <span class="prod-price">R$ {{ msg.suggestedProduct.preco.toFixed(2).replace('.', ',') }}</span>
            </div>
            <button @click="addToCart(msg.suggestedProduct)" class="mini-add-btn">
              <i class="fas fa-cart-plus"></i>
            </button>
          </div>

          <div class="message-actions">
            <span class="message-time">{{ msg.time }}</span>
            <button
                v-if="msg.role === 'bot'"
                @click="speakMessage(msg.content, index)"
                class="speak-btn"
                :class="{ speaking: isSpeakingIndex === index }"
                title="Ouvir resposta"
            >
              <i :class="isSpeakingIndex === index ? 'fas fa-volume-up' : 'fas fa-volume-off'"></i>
            </button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="message bot">
        <div class="message-avatar">
          <i class="fas fa-robot"></i>
        </div>
        <div class="message-content">
          <div class="message-bubble bot-bubble">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-suggestions">
      <button
          v-for="suggestion in suggestions"
          :key="suggestion"
          @click="sendSuggestion(suggestion)"
          class="suggestion-btn"
      >
        {{ suggestion }}
      </button>
    </div>

    <div class="chat-input">
      <textarea
          v-model="userMessage"
          @keyup.enter.exact="sendMessage"
          @keyup.enter.shift.exact="userMessage += '\n'"
          placeholder="Digite sua mensagem..."
          rows="1"
          :disabled="loading"
      ></textarea>
      <button
          @click="toggleVoiceInput"
          :class="['mic-btn', { listening: isListening }]"
          :disabled="loading"
          title="Clique para falar"
      >
        <i :class="isListening ? 'fas fa-stop' : 'fas fa-microphone'"></i>
      </button>
      <button @click="sendMessage" :disabled="loading || !userMessage.trim()" class="send-btn">
        <i class="fas fa-paper-plane"></i>
      </button>
    </div>
  </div>
</template>

<script>
import api from '@/services/api'
import { mapState, mapActions } from 'vuex'

export default {
  name: 'GeminiChat',
  data() {
    return {
      messages: [
        {
          role: 'bot',
          content: 'Olá! Sou o assistente Gemini da ClickFarma. Como posso ajudar você hoje?',
          time: this.getCurrentTime()
        }
      ],
      userMessage: '',
      loading: false,
      isListening: false,
      isSpeakingIndex: null,
      speechRecognition: null,
      suggestions: [
        '💊 Qual remédio para dor de cabeça?',
        '📦 Como rastrear meu pedido?',
        '🚚 Qual prazo de entrega?',
        '💳 Formas de pagamento'
      ]
    }
  },
  mounted() {
    this.scrollToBottom()
    this.initSpeechRecognition()
  },
  computed: {
    ...mapState(['products']),
  },
  methods: {
    ...mapActions(['addToCart']),
    getCurrentTime() {
      return new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    },

    initSpeechRecognition() {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
      if (SpeechRecognition) {
        this.speechRecognition = new SpeechRecognition()
        this.speechRecognition.lang = 'pt-BR'
        this.speechRecognition.continuous = false
        this.speechRecognition.interimResults = false

        this.speechRecognition.onresult = (event) => {
          const transcript = event.results[0][0].transcript
          this.userMessage = transcript.charAt(0).toUpperCase() + transcript.slice(1)
          this.isListening = false
          setTimeout(() => this.sendMessage(), 500)
        }

        this.speechRecognition.onerror = (event) => {
          console.error('Erro no reconhecimento de voz:', event.error)
          this.isListening = false
          if (event.error === 'not-allowed') {
            alert('Permissão de microfone negada. Habilite nas configurações do navegador.')
          }
        }

        this.speechRecognition.onend = () => {
          this.isListening = false
        }
      } else {
        console.warn('Speech Recognition não suportado neste navegador.')
      }
    },

    toggleVoiceInput() {
      if (!this.speechRecognition) {
        alert('Seu navegador não suporta reconhecimento de voz. Use o Chrome ou Edge.')
        return
      }
      if (this.isListening) {
        this.speechRecognition.stop()
        this.isListening = false
      } else {
        this.speechRecognition.start()
        this.isListening = true
      }
    },

    speakMessage(text, index) {
      if (this.isSpeakingIndex === index) {
        this.stopSpeaking()
        return
      }
      
      window.speechSynthesis.cancel()
      
      const utterance = new SpeechSynthesisUtterance(text)
      utterance.lang = 'pt-BR'
      utterance.rate = 1.1 // Um pouco mais rápido
      
      utterance.onstart = () => {
        this.isSpeakingIndex = index
      }
      
      utterance.onend = () => {
        this.isSpeakingIndex = null
      }
      
      utterance.onerror = () => {
        this.isSpeakingIndex = null
      }
      
      window.speechSynthesis.speak(utterance)
    },

    stopSpeaking() {
      window.speechSynthesis.cancel()
      this.isSpeakingIndex = null
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

        this.messages.push({
          role: 'bot',
          content: response.data.response,
          time: this.getCurrentTime()
        });
        this.speakMessage(response.data.response, this.messages.length - 1);
      } catch (error) {
        console.error('Erro ao obter recomendações de bem-estar:', error)
        this.messages.push({
          role: 'bot',
          content: 'Desculpe, não consegui gerar recomendações de bem-estar no momento.',
          time: this.getCurrentTime()
        })
      } finally {
        this.loading = false;
        this.scrollToBottom();
      }
    },

    async sendMessage() {
      if (!this.userMessage.trim() || this.loading) return
      
      this.stopSpeaking() // Para de falar se o usuário enviar nova mensagem

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
        const response = await api.post('/gemini/chat', {
          message: message
        })

        let responseContent = response.data.response
        let suggestedProd = null
        
        const regex = /\|CARRINHO:([^|]+)\|/i
        const match = responseContent.match(regex)
        
        if (match && this.products) {
            const prodName = match[1].trim()
            suggestedProd = this.products.find(p => p.nome && p.nome.toLowerCase().includes(prodName.toLowerCase()))
            responseContent = responseContent.replace(regex, '').trim()
        }

        this.messages.push({
          role: 'bot',
          content: responseContent,
          time: this.getCurrentTime(),
          suggestedProduct: suggestedProd
        })
        this.speakMessage(responseContent, this.messages.length - 1)
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
    }
  }
}
</script>

<style scoped>
.gemini-chat {
  display: flex;
  flex-direction: column;
  height: 600px;
  width: 420px;
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: linear-gradient(135deg, #198754 0%, #146c43 100%);
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.header-info h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  opacity: 0.9;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4ade80;
  animation: pulse-dot 2s infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.close-btn {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 10px;
  max-width: 85%;
  animation: fadeIn 0.3s ease;
}

.message.user {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.message.bot {
  align-self: flex-start;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e2e8f0;
  color: #64748b;
  font-size: 14px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #198754;
  color: white;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.message.user .message-bubble {
  background: linear-gradient(135deg, #198754, #157347);
  border-bottom-right-radius: 4px;
}

.message.user .message-text {
  color: white;
}

.message.bot .message-bubble {
  background: white;
  border-bottom-left-radius: 4px;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
  color: #1e293b;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.chat-product-card {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
  padding: 12px;
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border-radius: 12px;
  border: 1px solid #bbf7d0;
  transition: all 0.2s;
}

.chat-product-card:hover {
  border-color: #4ade80;
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.15);
}

.prod-image {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #198754;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.prod-details {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.prod-name {
  font-weight: 600;
  font-size: 13px;
  color: #166534;
}

.prod-price {
  font-size: 12px;
  color: #15803d;
  font-weight: 500;
}

.mini-add-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #198754;
  border: none;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.mini-add-btn:hover {
  background: #146c43;
  transform: scale(1.1);
}

.message-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 4px;
}

.message-time {
  font-size: 11px;
  color: #94a3b8;
}

.speak-btn {
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 12px;
  cursor: pointer;
  padding: 4px;
  transition: color 0.2s;
}

.speak-btn:hover {
  color: #198754;
}

.speak-btn.speaking {
  color: #198754;
  animation: pulse-speak 1.5s infinite;
}

@keyframes pulse-speak {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}

.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #94a3b8;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-8px); opacity: 1; }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.chat-suggestions {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #e2e8f0;
  overflow-x: auto;
  flex-wrap: nowrap;
}

.suggestion-btn {
  padding: 8px 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  font-size: 12px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.suggestion-btn:hover {
  background: #198754;
  border-color: #198754;
  color: white;
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 14px 16px;
  background: white;
  border-top: 1px solid #e2e8f0;
  align-items: flex-end;
}

.chat-input textarea {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  outline: none;
  max-height: 80px;
  transition: border-color 0.2s;
}

.chat-input textarea:focus {
  border-color: #198754;
}

.mic-btn {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  color: white;
  cursor: pointer;
  transition: all 0.25s;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.mic-btn i {
  font-size: 18px;
}

.mic-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
}

.mic-btn.listening {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  box-shadow: 0 4px 16px rgba(239, 68, 68, 0.4);
  animation: pulse-mic 1.5s infinite;
}

@keyframes pulse-mic {
  0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4); }
  70% { box-shadow: 0 0 0 8px rgba(239, 68, 68, 0); }
  100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
}

.mic-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #198754, #146c43);
  border: none;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(25, 135, 84, 0.3);
}

.send-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}

@media (max-width: 480px) {
  .gemini-chat {
    width: 100vw;
    height: 100vh;
    border-radius: 0;
  }
}
</style>