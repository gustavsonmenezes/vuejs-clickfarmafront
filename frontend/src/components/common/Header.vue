<template>
  <nav class="navbar navbar-expand-lg navbar-custom">
    <div class="container">
      <!-- Logo -->
      <router-link to="/" class="navbar-brand">
        <div class="brand-logo">
          <span class="logo-icon">💊</span>
          <span class="brand-text">ClickFarma</span>
        </div>
      </router-link>
      
      <!-- Mobile Toggle -->
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      
      <!-- Navbar Content -->
      <div class="collapse navbar-collapse" id="navbarNav">
        <!-- Navigation Links -->
        <ul class="navbar-nav me-auto">
          <li class="nav-item">
            <router-link to="/" class="nav-link">
              <i class="fas fa-home me-1"></i>Home
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/products" class="nav-link">
              <i class="fas fa-pills me-1"></i>Produtos
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/promotions" class="nav-link">
              <i class="fas fa-tag me-1"></i>Promoções
            </router-link>
          </li>
           <li class="nav-item">
          <router-link to="/track-order" class="nav-link">
            <i class="fas fa-truck me-1"></i>Rastrear Pedido
          </router-link>
          </li>  

          <li class="nav-item">
            <router-link to="/about" class="nav-link">
              <i class="fas fa-info-circle me-1"></i>Sobre
            </router-link>
          </li>
        </ul>
        
        <!-- Right Side Actions -->
        <div class="navbar-actions d-flex align-items-center gap-3">
          <!-- Cart -->
          <router-link to="/cart" class="cart-btn position-relative">
            <i class="fas fa-shopping-cart"></i>
            <span v-if="cartItemsCount > 0" class="cart-badge">
              {{ cartItemsCount }}
            </span>
          </router-link>
          
          <!-- Auth Section -->
          <div v-if="!isAuthenticated" class="auth-buttons d-flex gap-2">
            <router-link to="/login" class="btn btn-outline-light btn-sm">
              <i class="fas fa-sign-in-alt me-1"></i>Entrar
            </router-link>
            <router-link to="/register" class="btn btn-light btn-sm">
              <i class="fas fa-user-plus me-1"></i>Cadastrar
            </router-link>
          </div>
          
          <!-- User Menu -->
          <div v-else class="user-menu dropdown">
            <button class="user-dropdown-btn dropdown-toggle d-flex align-items-center" type="button" data-bs-toggle="dropdown">
              <div class="user-avatar">
                <i class="fas fa-user-circle"></i>
              </div>
              <span class="user-name ms-2">{{ user?.name || 'Usuário' }}</span>
            </button>
            <ul class="dropdown-menu dropdown-menu-end">
              <li>
                <router-link to="/profile" class="dropdown-item">
                  <i class="fas fa-user me-2"></i>Meu Perfil
                </router-link>
              </li>
              <li>
                <router-link to="/prescriptions" class="dropdown-item">
                  <i class="fas fa-file-medical me-2"></i>Minhas Receitas
                </router-link>
              </li>
              <li>
                <router-link to="/orders" class="dropdown-item">
                  <i class="fas fa-shopping-bag me-2"></i>Meus Pedidos
                </router-link>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <button class="dropdown-item text-danger" @click="handleLogout">
                  <i class="fas fa-sign-out-alt me-2"></i>Sair
                </button>
              </li>
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
        this.setupEventListeners();
      } catch (error) {
        console.error('❌ Erro ao inicializar header:', error);
      }
    },
    
    initializeBootstrapComponents() {
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
    document.removeEventListener('click', this.handleDocumentClick);
    console.log('🧹 Header - cleanup realizado');
  }
}
</script>

<style scoped>
.navbar-custom {
  background: linear-gradient(135deg, #198754 0%, #146c43 100%);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 0.8rem 0;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.logo-icon {
  font-size: 2rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.brand-text {
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.navbar-nav .nav-link {
  color: rgba(255, 255, 255, 0.9) !important;
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  margin: 0 0.2rem;
}

.navbar-nav .nav-link:hover,
.navbar-nav .nav-link.router-link-active {
  color: white !important;
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-1px);
}

.cart-btn {
  color: white;
  font-size: 1.3rem;
  padding: 0.5rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  text-decoration: none;
  position: relative;
}

.cart-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: scale(1.1);
}

.cart-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #dc3545;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 0.7rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.user-dropdown-btn {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 25px;
  transition: all 0.3s ease;
}

.user-dropdown-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-1px);
}

.user-avatar {
  font-size: 1.5rem;
}

.user-name {
  font-weight: 500;
}

.dropdown-menu {
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  margin-top: 0.5rem;
}

.dropdown-item {
  padding: 0.7rem 1rem;
  transition: all 0.3s ease;
  border-radius: 5px;
  margin: 0.1rem 0.5rem;
  width: auto;
}

.dropdown-item:hover {
  background: linear-gradient(135deg, #198754, #146c43);
  color: white;
  transform: translateX(5px);
}

.btn-outline-light:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

/* Responsividade */
@media (max-width: 991.98px) {
  .navbar-actions {
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .auth-buttons {
    width: 100%;
    justify-content: center;
  }
  
  .auth-buttons .btn {
    flex: 1;
    text-align: center;
  }
}
</style>