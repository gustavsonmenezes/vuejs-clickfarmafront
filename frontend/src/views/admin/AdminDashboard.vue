<template>
  <div class="admin-layout">
    <!-- Top Bar -->
    <header class="admin-topbar">
      <div class="topbar-left">
        <button class="btn-toggle-sidebar" @click="sidebarOpen = !sidebarOpen">
          <i class="fas fa-bars"></i>
        </button>
        <router-link to="/admin/dashboard" class="topbar-brand">
          <i class="fas fa-cross"></i>
          <span class="brand-text">Click<span class="brand-em">Farma</span></span>
          <span class="brand-badge">Admin</span>
        </router-link>
      </div>
      <div class="topbar-right">
        <div class="admin-user">
          <div class="user-avatar">{{ currentUser?.name?.charAt(0) || 'A' }}</div>
          <div class="user-info">
            <span class="user-name">{{ currentUser?.name || 'Admin' }}</span>
            <span class="user-role">Administrador</span>
          </div>
        </div>
        <button class="btn-logout" @click="logout" title="Sair">
          <i class="fas fa-right-from-bracket"></i>
        </button>
      </div>
    </header>

    <!-- Sidebar -->
    <aside class="admin-sidebar" :class="{ 'sidebar-open': sidebarOpen }">
      <div class="sidebar-nav">
        <router-link to="/admin/dashboard" class="nav-item" active-class="active" @click="sidebarOpen = false">
          <i class="fas fa-chart-pie"></i>
          <span>Visão Geral</span>
        </router-link>
        <router-link to="/admin/products" class="nav-item" active-class="active" @click="sidebarOpen = false">
          <i class="fas fa-capsules"></i>
          <span>Produtos</span>
        </router-link>
        <router-link to="/admin/inventory" class="nav-item" active-class="active" @click="sidebarOpen = false">
          <i class="fas fa-boxes-stacked"></i>
          <span>Estoque</span>
        </router-link>
        <router-link to="/admin/orders" class="nav-item" active-class="active" @click="sidebarOpen = false">
          <i class="fas fa-truck"></i>
          <span>Pedidos</span>
        </router-link>
        <router-link to="/admin/prescriptions" class="nav-item" active-class="active" @click="sidebarOpen = false">
          <i class="fas fa-file-prescription"></i>
          <span>Receitas</span>
        </router-link>
        <router-link to="/admin/users" class="nav-item" active-class="active" @click="sidebarOpen = false">
          <i class="fas fa-user-group"></i>
          <span>Usuários</span>
        </router-link>
        <router-link to="/admin/entregadores" class="nav-item" active-class="active" @click="sidebarOpen = false">
          <i class="fas fa-motorcycle"></i>
          <span>Entregadores</span>
        </router-link>
        <router-link to="/admin/farmacias" class="nav-item" active-class="active" @click="sidebarOpen = false">
          <i class="fas fa-store-alt"></i>
          <span>Farmácias</span>
        </router-link>
        <router-link to="/admin/cupons" class="nav-item" active-class="active" @click="sidebarOpen = false">
          <i class="fas fa-tags"></i>
          <span>Cupons</span>
        </router-link>
        <router-link to="/admin/corridas" class="nav-item" active-class="active" @click="sidebarOpen = false">
          <i class="fas fa-route"></i>
          <span>Corridas</span>
        </router-link>
      </div>
      <div class="sidebar-footer">
        <button class="btn-logout-sidebar" @click="logout">
          <i class="fas fa-right-from-bracket"></i>
          <span>Sair</span>
        </button>
      </div>
    </aside>

    <!-- Overlay for mobile -->
    <div v-if="sidebarOpen" class="sidebar-overlay" @click="sidebarOpen = false"></div>

    <!-- Content -->
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>

<script>
export default {
  name: 'AdminDashboard',
  data() {
    return {
      sidebarOpen: false,
      currentUser: null
    }
  },
  mounted() {
    this.loadCurrentUser()
  },
  methods: {
    loadCurrentUser() {
      try {
        const userData = localStorage.getItem('user')
        if (userData) this.currentUser = JSON.parse(userData)
      } catch (e) { console.error(e) }
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
.admin-layout {
  --cf-bg: #f0fdf4;
  --cf-border: #e2e8f0;
  --cf-font: 'Fira Sans', system-ui, -apple-system, sans-serif;
  min-height: 100vh;
  background: var(--cf-bg);
  display: flex;
  flex-direction: column;
}

/* ═══ TOPBAR ═══ */
.admin-topbar {
  position: sticky;
  top: 0;
  z-index: 1050;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px 0 16px;
  height: 60px;
  background: var(--cf-slate-900);
  gap: 16px;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-toggle-sidebar {
  background: rgba(255,255,255,0.06);
  border: none;
  color: rgba(255,255,255,0.6);
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 1rem;
}

.btn-toggle-sidebar:hover {
  background: rgba(255,255,255,0.12);
  color: white;
}

.topbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  text-decoration: none;
}

.topbar-brand i {
  font-size: 1.2rem;
  color: var(--cf-primary-500);
}

.brand-text {
  font-weight: 700;
  font-size: 1.05rem;
  letter-spacing: -0.01em;
}

.brand-em {
  font-weight: 300;
  color: var(--cf-primary-500);
}

.brand-badge {
  font-size: 0.6rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  padding: 2px 8px;
  background: rgba(255,255,255,0.1);
  border-radius: 4px;
  color: rgba(255,255,255,0.6);
  margin-left: 4px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--cf-primary-700);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.8125rem;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: white;
  line-height: 1.2;
}

.user-role {
  font-size: 0.6875rem;
  color: rgba(255,255,255,0.4);
}

.btn-logout {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: rgba(255,255,255,0.06);
  border: none;
  color: rgba(255,255,255,0.5);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
  font-size: 0.9rem;
}

.btn-logout:hover {
  background: rgba(239,68,68,0.15);
  color: #ef4444;
}

/* ═══ SIDEBAR ═══ */
.admin-sidebar {
  position: fixed;
  top: 60px;
  left: 0;
  bottom: 0;
  width: 240px;
  background: white;
  border-right: 1px solid var(--cf-border);
  display: flex;
  flex-direction: column;
  z-index: 1040;
  transition: transform 0.2s ease;
}

.sidebar-nav {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 8px;
  color: var(--cf-slate-600);
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.15s;
}

.nav-item:hover {
  background: var(--cf-primary-50);
  color: var(--cf-primary-700);
}

.nav-item.active {
  background: var(--cf-primary-100);
  color: var(--cf-primary-700);
  font-weight: 600;
}

.nav-item i {
  width: 20px;
  text-align: center;
  font-size: 0.9rem;
  color: var(--cf-slate-400);
}

.nav-item.active i {
  color: var(--cf-primary-600);
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--cf-border);
}

.btn-logout-sidebar {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  background: none;
  border: 1px solid var(--cf-border);
  border-radius: 8px;
  color: var(--cf-slate-500);
  font-size: 0.8125rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  font-family: var(--cf-font);
}

.btn-logout-sidebar:hover {
  background: var(--cf-danger-light);
  border-color: var(--cf-danger);
  color: var(--cf-danger);
}

/* Overlay mobile */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  top: 60px;
  background: rgba(15, 23, 42, 0.4);
  z-index: 1039;
}

/* ═══ CONTENT ═══ */
.admin-content {
  margin-left: 240px;
  padding: 24px;
  max-width: 1400px;
  min-height: calc(100vh - 60px);
}

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1023.98px) {
  .admin-sidebar {
    transform: translateX(-100%);
  }
  .admin-sidebar.sidebar-open {
    transform: translateX(0);
  }
  .admin-content {
    margin-left: 0;
  }
}

@media (max-width: 768px) {
  .admin-topbar { padding: 0 12px; }
  .brand-text, .brand-badge { display: none; }
  .admin-content { padding: 16px; }
  .user-info { display: none; }
}
</style>
