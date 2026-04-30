<template>
  <div class="cf-admin-shell">
    <!-- Overlay mobile -->
    <div v-if="sidebarOpen && isMobile" class="cf-sidebar-overlay" @click="sidebarOpen = false"></div>

    <!-- ═══ SIDEBAR ═══ -->
    <nav class="cf-sidebar" :class="{ 'sidebar-open': sidebarOpen }">
      <!-- Logo -->
      <div class="cf-sidebar-logo">
        <div class="logo-mark">
          <i class="fas fa-leaf"></i>
        </div>
        <div>
          <div class="logo-name">ClickFarma</div>
          <div class="logo-role">Painel Admin</div>
        </div>
        <button v-if="isMobile" @click="sidebarOpen = false" class="cf-sidebar-close">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div class="cf-sidebar-divider"></div>

      <!-- Navegação -->
      <div class="cf-sidebar-section-label">VISÃO GERAL</div>
      <ul class="cf-nav">
        <li>
          <router-link to="/admin/dashboard" class="cf-nav-link" :class="{ active: $route.path === '/admin/dashboard' }" @click="sidebarOpen = false">
            <i class="fas fa-chart-pie cf-nav-icon"></i>
            <span>Dashboard</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/orders" class="cf-nav-link" :class="{ active: $route.path === '/admin/orders' }" @click="sidebarOpen = false">
            <i class="fas fa-shopping-bag cf-nav-icon"></i>
            <span>Pedidos</span>
          </router-link>
        </li>
      </ul>

      <div class="cf-sidebar-section-label">GESTÃO</div>
      <ul class="cf-nav">
        <li>
          <router-link to="/admin/users" class="cf-nav-link" :class="{ active: $route.path === '/admin/users' }" @click="sidebarOpen = false">
            <i class="fas fa-users cf-nav-icon"></i>
            <span>Clientes</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/pharmacies" class="cf-nav-link" :class="{ active: $route.path === '/admin/pharmacies' }" @click="sidebarOpen = false">
            <i class="fas fa-store cf-nav-icon"></i>
            <span>Farmácias</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/couriers" class="cf-nav-link" :class="{ active: $route.path === '/admin/couriers' }" @click="sidebarOpen = false">
            <i class="fas fa-motorcycle cf-nav-icon"></i>
            <span>Entregadores</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/products" class="cf-nav-link" :class="{ active: $route.path === '/admin/products' }" @click="sidebarOpen = false">
            <i class="fas fa-pills cf-nav-icon"></i>
            <span>Produtos</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/categories" class="cf-nav-link" :class="{ active: $route.path === '/admin/categories' }" @click="sidebarOpen = false">
            <i class="fas fa-tags cf-nav-icon"></i>
            <span>Categorias</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/prescriptions" class="cf-nav-link" :class="{ active: $route.path === '/admin/prescriptions' }" @click="sidebarOpen = false">
            <i class="fas fa-file-medical cf-nav-icon"></i>
            <span>Receitas</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/payments" class="cf-nav-link" :class="{ active: $route.path === '/admin/payments' }" @click="sidebarOpen = false">
            <i class="fas fa-hand-holding-usd cf-nav-icon"></i>
            <span>Financeiro (Repasses)</span>
          </router-link>
        </li>
      </ul>

      <!-- Footer da sidebar -->
      <div class="cf-sidebar-footer">
        <div class="cf-user-info">
          <div class="cf-user-avatar">
            {{ currentUser?.nome?.charAt(0)?.toUpperCase() || 'A' }}
          </div>
          <div class="cf-user-details">
            <div class="cf-user-name">{{ currentUser?.nome || 'Administrador' }}</div>
            <div class="cf-user-role">Admin ClickFarma</div>
          </div>
        </div>
        <button @click="logout" class="cf-logout-btn">
          <i class="fas fa-sign-out-alt"></i>
        </button>
      </div>
    </nav>

    <!-- ═══ CONTEÚDO PRINCIPAL ═══ -->
    <div class="cf-main">
      <!-- Topbar -->
      <header class="cf-topbar">
        <div class="cf-topbar-left">
          <button @click="sidebarOpen = !sidebarOpen" class="cf-menu-btn d-md-none">
            <i class="fas fa-bars"></i>
          </button>
          <div class="cf-breadcrumb">
            <span class="cf-breadcrumb-root">Admin</span>
            <i class="fas fa-chevron-right cf-breadcrumb-sep"></i>
            <span class="cf-breadcrumb-current">{{ pageTitle }}</span>
          </div>
        </div>
        <div class="cf-topbar-right">
          <div class="cf-topbar-badge">
            <i class="fas fa-leaf me-1"></i>
            ClickFarma Admin
          </div>
        </div>
      </header>

      <!-- Área de conteúdo -->
      <div class="cf-content">
        <router-view />
      </div>
    </div>
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
    };
  },
  computed: {
    pageTitle() {
      const titles = {
        '/admin/dashboard': 'Dashboard',
        '/admin/products': 'Produtos',
        '/admin/inventory': 'Estoque',
        '/admin/orders': 'Pedidos',
        '/admin/prescriptions': 'Receitas',
        '/admin/users': 'Clientes',
        '/admin/pharmacies': 'Farmácias',
        '/admin/couriers': 'Entregadores',
        '/admin/payments': 'Financeiro (Repasses)',
        '/admin/categories': 'Categorias'
      };
      return titles[this.$route.path] || 'Painel';
    }
  },
  mounted() {
    this.checkMobile();
    this.loadUser();
    window.addEventListener('resize', this.checkMobile);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.checkMobile);
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth < 768;
      if (!this.isMobile) this.sidebarOpen = false;
    },
    loadUser() {
      try {
        const raw = localStorage.getItem('user');
        if (raw) this.currentUser = JSON.parse(raw);
      } catch (e) {}
    },
    logout() {
      if (confirm('Deseja sair do painel?')) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
        this.$router.push('/admin/login');
      }
    }
  }
};
</script>

<style scoped>
/* ══════════════════════════════════════
   SHELL DO PAINEL — Identidade ClickFarma
   ══════════════════════════════════════ */
.cf-admin-shell {
  display: flex;
  min-height: 100vh;
  background: var(--cf-ivory);
  font-family: var(--cf-sans);
  overflow-x: hidden; /* Prevenir scroll horizontal global */
  width: 100%;
}

/* ─── SIDEBAR ─── */
.cf-sidebar {
  width: 260px;
  height: 100vh;
  background: var(--cf-green-dark);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0; top: 0;
  z-index: 1050;
  transition: transform 0.3s cubic-bezier(0.4,0,0.2,1);
  overflow-y: auto;
  scrollbar-width: none; /* Firefox */
}
.cf-sidebar::-webkit-scrollbar { display: none; } /* Chrome/Safari */

.cf-sidebar-logo {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 1.4rem 1.25rem;
  flex-shrink: 0;
}
.logo-mark {
  width: 38px; height: 38px;
  background: var(--cf-gold);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: white; font-size: 1.1rem;
  flex-shrink: 0;
}
.logo-name {
  font-family: var(--cf-serif);
  font-size: 1.05rem;
  font-weight: 600;
  color: #fff;
  letter-spacing: -0.01em;
  line-height: 1.1;
}
.logo-role {
  font-size: 0.62rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: rgba(255,255,255,0.45);
  margin-top: 2px;
}
.cf-sidebar-close {
  margin-left: auto;
  background: none;
  border: none;
  color: rgba(255,255,255,0.5);
  cursor: pointer;
  font-size: 1rem;
}

.cf-sidebar-divider {
  height: 1px;
  background: rgba(255,255,255,0.08);
  margin: 0 1.25rem;
  flex-shrink: 0;
}

.cf-sidebar-section-label {
  font-size: 0.6rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(255,255,255,0.35);
  padding: 1.1rem 1.4rem 0.4rem;
  font-weight: 500;
  flex-shrink: 0;
}

.cf-nav {
  list-style: none;
  padding: 0 0.75rem;
  margin: 0;
  flex: 1;
}

.cf-nav-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.65rem 0.85rem;
  border-radius: var(--cf-r-md);
  color: rgba(255,255,255,0.65);
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 400;
  transition: all 0.18s var(--cf-ease);
  margin-bottom: 2px;
  border-left: 3px solid transparent;
}
.cf-nav-link:hover {
  background: rgba(255,255,255,0.08);
  color: #fff;
  border-left-color: var(--cf-gold);
}
.cf-nav-link.active {
  background: rgba(184,149,80,0.18);
  color: var(--cf-gold);
  border-left-color: var(--cf-gold);
  font-weight: 500;
}
.cf-nav-icon {
  width: 16px;
  text-align: center;
  opacity: 0.75;
  flex-shrink: 0;
}
.cf-nav-link.active .cf-nav-icon { opacity: 1; }

/* Footer da sidebar */
.cf-sidebar-footer {
  margin-top: auto;
  padding: 1rem 1.25rem;
  border-top: 1px solid rgba(255,255,255,0.08);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
  background: rgba(0,0,0,0.1);
}
.cf-user-avatar {
  width: 34px; height: 34px;
  border-radius: 50%;
  background: var(--cf-gold);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.85rem; font-weight: 700;
  flex-shrink: 0;
}
.cf-user-details { flex: 1; min-width: 0; }
.cf-user-name {
  font-size: 0.8rem; font-weight: 500; color: #fff;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.cf-user-role { font-size: 0.65rem; color: rgba(255,255,255,0.4); }
.cf-logout-btn {
  background: none; border: none;
  color: rgba(255,255,255,0.4);
  cursor: pointer; font-size: 0.95rem;
  padding: 0.3rem; border-radius: 6px;
  transition: color 0.2s;
  flex-shrink: 0;
}
.cf-logout-btn:hover { color: var(--cf-gold); }

/* Overlay mobile */
.cf-sidebar-overlay {
  position: fixed; inset: 0;
  background: rgba(28,28,26,0.6);
  z-index: 1040;
  backdrop-filter: blur(4px);
}

/* ─── CONTEÚDO PRINCIPAL ─── */
.cf-main {
  margin-left: 260px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-width: 0; /* Essencial para conter o flex overflow */
  width: calc(100% - 260px);
  overflow-x: hidden;
}

/* Topbar */
.cf-topbar {
  height: 64px;
  background: var(--cf-white);
  border-bottom: 1px solid var(--cf-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--cf-shadow-xs);
  flex-shrink: 0;
}
.cf-topbar-left { 
  display: flex; 
  align-items: center; 
  gap: 0.75rem; 
  min-width: 0;
  flex: 1;
}
.cf-menu-btn {
  background: none; border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-sm);
  padding: 0.35rem 0.55rem;
  cursor: pointer; color: var(--cf-text-muted);
  transition: all 0.18s;
  flex-shrink: 0;
}
.cf-menu-btn:hover { border-color: var(--cf-green); color: var(--cf-green); }

.cf-breadcrumb { 
  display: flex; 
  align-items: center; 
  gap: 0.4rem; 
  min-width: 0;
  white-space: nowrap;
}
.cf-breadcrumb-root { font-size: 0.72rem; color: var(--cf-text-muted); }
.cf-breadcrumb-sep { font-size: 0.55rem; color: var(--cf-border-mid); }
.cf-breadcrumb-current { 
  font-size: 0.82rem; 
  font-weight: 500; 
  color: var(--cf-text-dark);
  overflow: hidden;
  text-overflow: ellipsis;
}

.cf-topbar-badge {
  font-size: 0.68rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--cf-green);
  background: var(--cf-green-xlight);
  padding: 0.3rem 0.7rem;
  border-radius: 20px;
  border: 1px solid rgba(42,92,69,0.15);
  font-weight: 500;
  white-space: nowrap;
}

/* Área de conteúdo */
.cf-content {
  flex: 1;
  padding: 1.5rem;
  background: var(--cf-ivory);
  animation: fadeInUp 0.35s var(--cf-ease) both;
  min-width: 0;
  width: 100%;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Mobile */
@media (max-width: 991px) {
  .cf-sidebar { 
    transform: translateX(-100%); 
    width: 280px;
  }
  .cf-sidebar.sidebar-open { 
    transform: translateX(0); 
  }
  .cf-main { 
    margin-left: 0; 
    width: 100%;
  }
  .cf-topbar {
    padding: 0 1rem;
  }
  .cf-content {
    padding: 1rem;
  }
  .cf-topbar-right {
    display: none; 
  }
}
</style>
