<template>
  <div id="app">
    <Header />
    <main class="main-content">
      <router-view />
    </main>
    <!-- Botão flutuante -->
    <div v-show="!isChatOpen" class="gemini-floating-btn" @click="toggleChat">
      <i class="fa-solid fa-robot"></i>
    </div>
    <!-- Chat -->
    <div v-show="isChatOpen" class="gemini-modal">
      <GeminiChat @close="toggleChat" />
    </div>
    
    <!-- Modal de Visualização de Produto -->
    <ProductQuickView 
      :isOpen="isQuickViewOpen" 
      :product="quickViewProduct"
      @close="closeQuickView"
    />

    <Footer />
  </div>
</template>

<script>
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'
import GeminiChat from '@/components/gemini/GeminiChat.vue'
import ProductQuickView from '@/components/products/ProductQuickView.vue'
import { mapState, mapActions } from 'vuex'

export default {
  name: 'App',
  components: {
    Header,
    Footer,
    GeminiChat,
    ProductQuickView
  },
  data() {
    return {
      isChatOpen: false
    }
  },
  computed: {
    ...mapState(['isQuickViewOpen', 'quickViewProduct'])
  },
  methods: {
    ...mapActions(['closeQuickView']),
    toggleChat() {
      this.isChatOpen = !this.isChatOpen
    }
  }
}
</script>

<style>
/* Reset global e background base do ClickFarma */
body {
  background-color: var(--cf-ivory) !important;
}

.main-content {
  min-height: calc(100vh - 160px);
  background: var(--cf-ivory);
}

.gemini-floating-btn {
  position: fixed;
  bottom: 25px;
  right: 25px;
  width: 58px;
  height: 58px;
  background: var(--cf-green);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: var(--cf-shadow-md);
  transition: all 300ms var(--cf-ease);
  z-index: 9999;
  color: white;
  font-size: 1.4rem;
  border: 4px solid var(--cf-white);
}

.gemini-floating-btn:hover {
  transform: scale(1.08) translateY(-3px);
  background: var(--cf-green-dark);
  box-shadow: var(--cf-shadow-lg);
}

.gemini-modal {
  position: fixed;
  bottom: 25px;
  right: 25px;
  width: 380px;
  height: min(600px, 80vh);
  z-index: 10000;
  border-radius: var(--cf-r-xl);
  box-shadow: var(--cf-shadow-lg);
  overflow: hidden;
  animation: slideInUp 400ms var(--cf-ease);
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
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