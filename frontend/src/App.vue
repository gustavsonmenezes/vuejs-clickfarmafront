<template>
  <div id="app">
    <Header />
    <main class="main-content">
      <router-view />
    </main>
    <!-- Botão flutuante -->
    <div class="gemini-floating-btn" @click="toggleChat">
      <i class="fas fa-brain"></i>
    </div>
    <!-- Chat -->
    <div v-if="isChatOpen" class="gemini-modal">
      <GeminiChat @close="toggleChat" />
    </div>
    <Footer />
  </div>
</template>

<script>
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import GeminiChat from '@/components/gemini/GeminiChat.vue'

export default {
  name: 'App',
  components: {
    Header,
    Footer,
    GeminiChat
  },
  data() {
    return {
      isChatOpen: false
    }
  },
  methods: {
    toggleChat() {
      this.isChatOpen = !this.isChatOpen
    }
  }
}
</script>

<style>
.main-content {
  min-height: calc(100vh - 160px);
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
}

.gemini-floating-btn {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #0056A0, #00B4D8);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(0, 86, 160, 0.3);
  transition: all 250ms ease-in-out;
  z-index: 999;
  color: white;
  font-size: 1.5rem;
}

.gemini-floating-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 12px 32px rgba(0, 86, 160, 0.4);
}

.gemini-modal {
  position: fixed;
  bottom: 150px;
  right: 20px;
  width: 420px;
  height: 650px;
  z-index: 1000;
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: slideInUp 300ms ease-in-out;
  background: transparent;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .gemini-modal {
    width: 100%;
    height: 100%;
    bottom: 0;
    right: 0;
    border-radius: 0;
  }
}
</style>