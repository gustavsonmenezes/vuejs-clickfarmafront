<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
      <router-link to="/" class="navbar-brand fw-bold">
        💊 ClickFarma
      </router-link>
      
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto">
          <li class="nav-item">
            <router-link to="/" class="nav-link">Home</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/products" class="nav-link">Produtos</router-link>
          </li>
        </ul>
        
        <div class="d-flex gap-2">
          <router-link to="/cart" class="btn btn-outline-light position-relative">
            🛒 Carrinho
            <span v-if="cartItemsCount > 0" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
              {{ cartItemsCount }}
            </span>
          </router-link>
          
          <div v-if="!isAuthenticated" class="d-flex gap-2">
            <router-link to="/login" class="btn btn-outline-light">
              Entrar
            </router-link>
            <router-link to="/register" class="btn btn-light">
              Cadastrar
            </router-link>
          </div>
          
          <div v-else class="dropdown">
            <button class="btn btn-outline-light dropdown-toggle" type="button" data-bs-toggle="dropdown">
              👤 {{ user?.name || 'Usuário' }}
            </button>
            <ul class="dropdown-menu">
              <li><router-link to="/profile" class="dropdown-item">Meu Perfil</router-link></li>
              <li><router-link to="/prescriptions" class="dropdown-item">Minhas Receitas</router-link></li>
              <li><a class="dropdown-item" href="#">Meus Pedidos</a></li>
              <li><hr class="dropdown-divider"></li>
              <li><button class="dropdown-item" @click="handleLogout">Sair</button></li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'Header',
  data() {
    return {
      isNavbarInitialized: false
    }
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'cartItemsCount', 'user'])
  },
  mounted() {
    console.log('🔧 Componente Header montado - inicializando...');
    this.initializeHeader();
  },
  methods: {
    ...mapActions(['logout', 'checkAuthStatus']),
    
    async initializeHeader() {
      try {
        console.log('🔐 Verificando status de autenticação...');
        await this.checkAuthStatus();
        
        
        this.initializeBootstrapComponents();
        
        // Configurar event listeners
        this.setupEventListeners();
        
      } catch (error) {
        console.error(' Erro ao inicializar header:', error);
      }
    },
    
    initializeBootstrapComponents() {
      // Inicializar dropdowns e outros componentes Bootstrap
      if (typeof bootstrap !== 'undefined') {
        const dropdownElementList = [].slice.call(document.querySelectorAll('.dropdown-toggle'));
        const dropdownList = dropdownElementList.map(function (dropdownToggleEl) {
          return new bootstrap.Dropdown(dropdownToggleEl);
        });
        
        this.isNavbarInitialized = true;
        console.log('✅ Componentes Bootstrap inicializados');
      }
    },
    
    setupEventListeners() {
      // Event listener para fechar dropdown ao clicar fora
      document.addEventListener('click', this.handleDocumentClick);
    },
    
    handleDocumentClick(event) {
      const dropdowns = document.querySelectorAll('.dropdown');
      dropdowns.forEach(dropdown => {
        if (!dropdown.contains(event.target)) {
          const dropdownMenu = dropdown.querySelector('.dropdown-menu');
          if (dropdownMenu && dropdownMenu.classList.contains('show')) {
            const dropdownInstance = bootstrap.Dropdown.getInstance(dropdown.querySelector('.dropdown-toggle'));
            dropdownInstance.hide();
          }
        }
      });
    },
    
    async handleLogout() {
      try {
        await this.logout();
        this.$router.push('/');
        console.log('👋 Logout realizado com sucesso');
      } catch (error) {
        console.error('❌ Erro ao fazer logout:', error);
      }
    }
  },
  
  beforeUnmount() {
    // Cleanup event listeners
    document.removeEventListener('click', this.handleDocumentClick);
    console.log('🧹 Header - cleanup realizado');
  }
}
</script>

<style scoped>
.navbar-brand {
  font-size: 1.5rem;
}
</style>