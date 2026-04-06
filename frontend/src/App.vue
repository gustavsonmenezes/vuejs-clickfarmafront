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
import GeminiChat from '@/components/GeminiChat.vue'

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
}

/* Botão flutuante */
.gemini-floating-btn {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #4285f4, #34a853);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: 0.3s;
  z-index: 999;
}

.gemini-floating-btn:hover {
  transform: scale(1.1);
}

/* Modal do chat */
.gemini-modal {
  position: fixed;
  bottom: 150px;
  right: 20px;
  width: 380px;
  height: 600px;
  z-index: 1000;
}

/* Responsivo */
@media (max-width: 768px) {
  .gemini-modal {
    width: 100%;
    height: 100%;
    bottom: 0;
    right: 0;
  }
}
</style>