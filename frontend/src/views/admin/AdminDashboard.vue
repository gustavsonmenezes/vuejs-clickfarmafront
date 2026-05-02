<template>
  <div class="admin-dashboard d-flex">
    <!-- Mobile overlay -->
    <div v-if="sidebarOpen && isMobile" class="admin-overlay" @click="closeSidebar"></div>
    
    <!-- Sidebar -->
    <nav class="admin-sidebar" :class="{ 'sidebar-open': sidebarOpen }">
      <div class="sidebar-header">
        <h5 class="mb-0">ClickFarma</h5>
        <button v-if="isMobile" @click="closeSidebar" class="btn-close-white">
          <i class="fas fa-xmark"></i>
        </button>
      </div>
      
      <div class="sidebar-nav">
        <router-link to="/admin/dashboard" class="nav-item" :class="{ active: $route.path === '/admin/dashboard' }" @click="closeSidebar">
          <i class="fas fa-grid-2"></i>
          <span>Visão Geral</span>
        </router-link>
        <router-link to="/admin/products" class="nav-item" :class="{ active: $route.path === '/admin/products' }" @click="closeSidebar">
          <i class="fas fa-pills"></i>
          <span>Produtos</span>
        </router-link>
        <router-link to="/admin/inventory" class="nav-item" :class="{ active: $route.path === '/admin/inventory' }" @click="closeSidebar">
          <i class="fas fa-warehouse"></i>
          <span>Estoque</span>
        </router-link>
        <router-link to="/admin/orders" class="nav-item" :class="{ active: $route.path === '/admin/orders' }" @click="closeSidebar">
          <i class="fas fa-bag-shopping"></i>
          <span>Pedidos</span>
        </router-link>
        <router-link to="/admin/prescriptions" class="nav-item" :class="{ active: $route.path === '/admin/prescriptions' }" @click="closeSidebar">
          <i class="fas fa-file-medical"></i>
          <span>Receitas</span>
        </router-link>
        <router-link to="/admin/users" class="nav-item" :class="{ active: $route.path === '/admin/users' }" @click="closeSidebar">
          <i class="fas fa-users"></i>
          <span>Usuários</span>
        </router-link>
      </div>
      
      <div class="sidebar-footer">
        <div class="user-info">
          <div class="avatar">{{ currentUser?.name?.charAt(0)?.toUpperCase() || 'A' }}</div>
          <div class="details">
            <div class="name">{{ currentUser?.name || 'Administrador' }}</div>
            <div class="role">Admin</div>
          </div>
        </div>
        <button @click="logout" class="btn-logout">
          <i class="fas fa-arrow-right-from-bracket"></i>
          <span>Sair</span>
        </button>
      </div>
    </nav>
    
    <!-- Main Content -->
    <main class="main-content">
      <header class="main-header">
        <button @click="toggleSidebar" class="btn-menu d-md-none">
          <i class="fas fa-bars"></i>
        </button>
        <h1 class="page-title">{{ pageTitle }}</h1>
        <div class="header-spacer"></div>
      </header>
      
      <div class="page-content">
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
        '/admin/dashboard': 'Visão Geral',
        '/admin/products': 'Produtos',
        '/admin/inventory': 'Estoque',
        '/admin/orders': 'Pedidos',
        '/admin/prescriptions': 'Validar Receitas',
        '/admin/users': 'Usuários'
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
  background-color: var(--cf-bg);
  font-family: var(--cf-font);
}

/* Sidebar */
.admin-sidebar {
  width: 260px;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1040;
  display: flex;
  flex-direction: column;
  background: var(--cf-slate-900);
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border-right: 1px solid rgba(255,255,255,0.06);
}

.sidebar-header {
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}

.sidebar-header h5 {
  color: white;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.btn-close-white {
  background: none;
  border: none;
  color: rgba(255,255,255,0.7);
  font-size: 1.1rem;
  cursor: pointer;
  padding: 4px;
}

.btn-close-white:hover {
  color: white;
}

/* Navigation */
.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  color: var(--cf-slate-400);
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.15s ease;
}

.nav-item:hover {
  background: rgba(255,255,255,0.06);
  color: var(--cf-slate-200);
}

.nav-item.active {
  background: rgba(59, 130, 246, 0.12);
  color: var(--cf-primary-500);
}

.nav-item i {
  width: 20px;
  text-align: center;
  font-size: 0.9rem;
}

/* Footer */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255,255,255,0.08);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  margin-bottom: 8px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--cf-primary-600);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.details .name {
  color: white;
  font-size: 0.8125rem;
  font-weight: 500;
}

.details .role {
  color: var(--cf-slate-500);
  font-size: 0.75rem;
}

.btn-logout {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255,255,255,0.06);
  border: none;
  border-radius: 8px;
  color: var(--cf-slate-400);
  font-size: 0.8125rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-logout:hover {
  background: rgba(255,255,255,0.1);
  color: white;
}

/* Main Content */
.main-content {
  margin-left: 260px;
  min-height: 100vh;
  transition: margin-left 0.25s ease;
}

.main-header {
  position: sticky;
  top: 0;
  z-index: 1030;
  height: 64px;
  padding: 0 32px;
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--cf-border);
}

.btn-menu {
  background: none;
  border: none;
  color: var(--cf-slate-600);
  font-size: 1.2rem;
  cursor: pointer;
  padding: 8px;
  margin-right: 12px;
}

.page-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--cf-slate-900);
  margin: 0;
  letter-spacing: -0.01em;
}

.header-spacer {
  flex: 1;
}

.page-content {
  padding: 24px 32px;
  max-width: 1400px;
}

/* Mobile Overlay */
.admin-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 1039;
}

/* Responsive */
@media (max-width: 767.98px) {
  .admin-sidebar { transform: translateX(-100%); }
  .admin-sidebar.sidebar-open { transform: translateX(0); }
  .main-content { margin-left: 0; }
  .page-content { padding: 16px; }
  .main-header { padding: 0 16px; }
}
</style>
