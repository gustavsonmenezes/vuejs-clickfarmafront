  <template>
  <div class="admin-dashboard d-flex">
    <!-- Overlay para mobile -->
    <div 
      v-if="sidebarOpen && isMobile" 
      class="sidebar-overlay" 
      @click="closeSidebar"
    ></div>
    
    <!-- Sidebar -->
    <nav 
      class="admin-sidebar bg-dark text-white p-3" 
      :class="{ 'sidebar-open': sidebarOpen }"
    >
      <div class="sidebar-header d-flex justify-content-between align-items-center mb-4">
        <h4 class="text-center mb-0">Painel Admin</h4>
        <button 
          v-if="isMobile" 
          @click="closeSidebar" 
          class="btn btn-outline-light btn-sm d-md-none"
        >
          ✕
        </button>
      </div>
      
      <ul class="nav flex-column">
        <li class="nav-item">
          <router-link 
            to="/admin/dashboard" 
            class="nav-link text-white"
            :class="{ active: $route.path === '/admin/dashboard' }"
            @click="closeSidebar"
          >
            <i class="fas fa-tachometer-alt me-2"></i>
            Início
          </router-link>
        </li>
        <li class="nav-item">
          <router-link 
            to="/admin/products" 
            class="nav-link text-white"
            :class="{ active: $route.path === '/admin/products' }"
            @click="closeSidebar"
          >
            <i class="fas fa-pills me-2"></i>
            Gerenciar Produtos
          </router-link>
        </li>
        <li class="nav-item">
          <router-link 
            to="/admin/inventory" 
            class="nav-link text-white"
            :class="{ active: $route.path === '/admin/inventory' }"
            @click="closeSidebar"
          >
            <i class="fas fa-boxes me-2"></i>
            Gerenciar Estoque
          </router-link>
        </li>
        <li class="nav-item">
          <router-link 
            to="/admin/orders" 
            class="nav-link text-white"
            :class="{ active: $route.path === '/admin/orders' }"
            @click="closeSidebar"
          >
            <i class="fas fa-shopping-cart me-2"></i>
            Visualizar Pedidos
          </router-link>
        </li>
        <li class="nav-item">
          <router-link 
            to="/admin/prescriptions" 
            class="nav-link text-white"
            :class="{ active: $route.path === '/admin/prescriptions' }"
            @click="closeSidebar"
          >
            <i class="fas fa-file-medical me-2"></i>
            Validar Receitas
          </router-link>
        </li>
        <li class="nav-item">
          <router-link 
            to="/admin/users" 
            class="nav-link text-white"
            :class="{ active: $route.path === '/admin/users' }"
            @click="closeSidebar"
          >
            <i class="fas fa-users me-2"></i>
            Gerenciar Usuários
          </router-link>
        </li>
      </ul>
      
      <div class="sidebar-footer mt-auto">
        <hr class="text-white-50">
        <div class="user-info mb-3">
          <small class="text-white-50">Logado como:</small>
          <div class="text-white">{{ currentUser?.name || 'Administrador' }}</div>
        </div>
        <button @click="logout" class="btn btn-outline-light w-100">
          <i class="fas fa-sign-out-alt me-2"></i>
          Sair
        </button>
      </div>
    </nav>
    
    <!-- Conteúdo Principal -->
    <main class="main-content flex-grow-1">
      <!-- Header do conteúdo principal -->
      <header class="main-header bg-white shadow-sm p-3 d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center">
          <button 
            @click="toggleSidebar" 
            class="btn btn-outline-secondary me-3 d-md-none"
          >
            <i class="fas fa-bars"></i>
          </button>
          <h1 class="h4 mb-0">{{ pageTitle }}</h1>
        </div>
        
        <div class="header-actions d-flex align-items-center">
          <div class="dropdown">
            <button 
              class="btn btn-outline-secondary dropdown-toggle" 
              type="button" 
              id="userDropdown" 
              data-bs-toggle="dropdown"
            >
              <i class="fas fa-user-circle me-1"></i>
              {{ currentUser?.name || 'Admin' }}
            </button>
            <ul class="dropdown-menu dropdown-menu-end">
              <li><a class="dropdown-item" href="#"><i class="fas fa-user me-2"></i>Perfil</a></li>
              <li><a class="dropdown-item" href="#"><i class="fas fa-cog me-2"></i>Configurações</a></li>
              <li><hr class="dropdown-divider"></li>
              <li><a class="dropdown-item" href="#" @click="logout"><i class="fas fa-sign-out-alt me-2"></i>Sair</a></li>
            </ul>
          </div>
        </div>
      </header>
      
      <!-- Conteúdo da página -->
      <div class="page-content p-4">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script>
export default {
  name: 'AdminDashboard',
  data() {
    return {
      sidebarOpen: false,
      isMobile: false,
      currentUser: null
    }
  },
  computed: {
    pageTitle() {
      const routeTitles = {
        '/admin/dashboard': 'Dashboard',
        '/admin/products': 'Gerenciar Produtos',
        '/admin/inventory': 'Gerenciar Estoque',
        '/admin/orders': 'Visualizar Pedidos',
        '/admin/prescriptions': 'Validar Receitas',
        '/admin/users': 'Gerenciar Usuários'
      }
      return routeTitles[this.$route.path] || 'Painel Administrativo'
    }
  },
  mounted() {
    this.checkMobile()
    this.loadCurrentUser()
    window.addEventListener('resize', this.checkMobile)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.checkMobile)
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth < 768
      if (!this.isMobile) {
        this.sidebarOpen = false
      }
    },
    toggleSidebar() {
      this.sidebarOpen = !this.sidebarOpen
    },
    closeSidebar() {
      this.sidebarOpen = false
    },
    loadCurrentUser() {
      try {
        const userData = localStorage.getItem('user')
        if (userData) {
          this.currentUser = JSON.parse(userData)
        }
      } catch (err) {
        console.error('Erro ao carregar dados do usuário:', err)
      }
    },
    logout() {
      if (confirm('Tem certeza que deseja sair?')) {
        localStorage.removeItem('authToken')
        localStorage.removeItem('user')
        this.$router.push('/admin/login')
      }
    }
  }
}
</script>

<style scoped>
.admin-dashboard { 
  min-height: 100vh; 
}

.admin-sidebar {
  width: 280px;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1040;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease-in-out;
  background: linear-gradient(180deg, #2c5aa0 0%, #1e3a8a 100%) !important;
}

.main-content {
  margin-left: 280px;
  min-height: 100vh;
  transition: margin-left 0.3s ease-in-out;
}

.main-header {
  position: sticky;
  top: 0;
  z-index: 1030;
  border-bottom: 1px solid #dee2e6;
}

.page-content {
  background-color: #f8f9fa;
  min-height: calc(100vh - 80px);
}

.nav-link { 
  font-size: 1rem; 
  padding: 12px 16px; 
  border-radius: 8px;
  margin-bottom: 4px;
  transition: all 0.2s ease-in-out;
  border-left: 3px solid transparent;
}

.nav-link:hover { 
  background-color: rgba(255,255,255,0.15) !important; 
  transform: translateX(4px);
  border-left-color: #ffffff;
}

.nav-link.active {
  background-color: rgba(255,255,255,0.2) !important;
  font-weight: 600;
  border-left-color: #ffffff;
}

.sidebar-footer {
  margin-top: auto;
}

.user-info {
  font-size: 0.9rem;
}

/* Botões com cor azul */
.btn-outline-primary {
  border-color: #2c5aa0;
  color: #2c5aa0;
}

.btn-outline-primary:hover {
  background-color: #2c5aa0;
  border-color: #2c5aa0;
}

/* Responsividade Mobile */
@media (max-width: 767.98px) {
  .admin-sidebar {
    transform: translateX(-100%);
  }
  
  .admin-sidebar.sidebar-open {
    transform: translateX(0);
  }
  
  .main-content {
    margin-left: 0;
  }
  
  .sidebar-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 1039;
  }
}

/* Animações */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-content {
  animation: slideIn 0.3s ease-out;
}

/* Melhorias visuais */
.btn {
  transition: all 0.2s ease-in-out;
}

.btn:hover {
  transform: translateY(-1px);
}

.dropdown-menu {
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dropdown-item {
  transition: background-color 0.2s ease-in-out;
}

.dropdown-item:hover {
  background-color: #f8f9fa;
}

/* Ícones com cor mais suave */
.nav-link i {
  color: rgba(255,255,255,0.8);
}

.nav-link.active i {
  color: #ffffff;
}

/* Header com cor azul sutil */
.main-header {
  background: linear-gradient(135deg, #ffffff 0%, #f0f8ff 100%) !important;
}
</style>

