<template>
  <div class="gemini-chat">
    <div class="chat-header">
      <i class="fas fa-brain"></i>
      <h3>Assistente Gemini</h3>
      <button @click="$emit('close')" class="close-btn">✕</button>
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
          <div class="message-text">{{ msg.content }}</div>
          <div class="message-time">{{ msg.time }}</div>
        </div>
      </div>

      <div v-if="loading" class="message bot">
        <div class="message-avatar">
          <i class="fas fa-robot"></i>
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

    <div class="chat-input">
      <textarea
        v-model="userMessage"
        @keyup.enter.exact="sendMessage"
        @keyup.enter.shift.exact="userMessage += '\n'"
        placeholder="Digite sua mensagem..."
        rows="1"
        :disabled="loading"
      ></textarea>
      <button @click="sendMessage" :disabled="loading || !userMessage.trim()">
        <i class="fas fa-paper-plane"></i>
      </button>
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
  </div>
</template>

<script>
import api from '@/services/api'

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
  },
  methods: {
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
        const response = await api.post('/gemini/chat', { message })
        this.messages.push({
          role: 'bot',
          content: response.data.response,
          time: this.getCurrentTime()
        })
      } catch (error) {
        console.error('Erro no chat:', error)
        this.messages.push({
          role: 'bot',
          content: 'Desculpe, estou com problemas. Tente novamente mais tarde.',
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
  height: 100%;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 20px;
  background: linear-gradient(135deg, #4285f4, #34a853);
  color: white;
}

.chat-header i {
  font-size: 24px;
}

.chat-header h3 {
  flex: 1;
  margin: 0;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e9ecef;
  color: #495057;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #0d6efd;
  color: white;
}

.message-content {
  max-width: 70%;
  flex: 1;
}

.message.user .message-content {
  text-align: right;
}

.message-text {
  background: white;
  padding: 10px 15px;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  color: #212529;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.message.user .message-text {
  background: #0d6efd;
  color: white;
}

.message-time {
  font-size: 11px;
  color: #6c757d;
  margin-top: 5px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 10px 15px;
  background: white;
  border-radius: 12px;
  width: fit-content;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6c757d;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-input {
  display: flex;
  gap: 10px;
  padding: 15px 20px;
  background: white;
  border-top: 1px solid #dee2e6;
}

.chat-input textarea {
  flex: 1;
  padding: 10px;
  border: 1px solid #dee2e6;
  border-radius: 20px;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  outline: none;
  max-height: 100px;
}

.chat-input textarea:focus {
  border-color: #4285f4;
}

.chat-input button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #4285f4;
  border: none;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.chat-input button:hover:not(:disabled) {
  background: #3367d6;
  transform: scale(1.05);
}

.chat-input button:disabled {
  background: #adb5bd;
  cursor: not-allowed;
}

.chat-suggestions {
  display: flex;
  gap: 8px;
  padding: 10px 20px 15px;
  background: white;
  border-top: 1px solid #dee2e6;
  flex-wrap: wrap;
}

.suggestion-btn {
  padding: 6px 12px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 20px;
  font-size: 12px;
  color: #495057;
  cursor: pointer;
  transition: all 0.3s;
}

.suggestion-btn:hover {
  background: #e9ecef;
  border-color: #4285f4;
  color: #4285f4;
}
</style>