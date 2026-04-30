<template>
  <div class="cf-courier-shell">
    <!-- Overlay mobile -->
    <div v-if="sidebarOpen && isMobile" class="cf-sidebar-overlay" @click="sidebarOpen = false"></div>

    <!-- ═══ SIDEBAR ═══ -->
    <nav class="cf-sidebar" :class="{ 'sidebar-open': sidebarOpen }">
      <div class="cf-sidebar-logo">
        <div class="logo-mark"><i class="fas fa-motorcycle"></i></div>
        <div>
          <div class="logo-name">ClickFarma</div>
          <div class="logo-sub">Entregador</div>
        </div>
        <button v-if="isMobile" @click="sidebarOpen = false" class="cf-close-btn">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div class="cf-divider"></div>

      <div class="cf-section-label">PAINEL MOTOBOY</div>
      <ul class="cf-nav">
        <li>
          <router-link to="/courier/dashboard" class="cf-nav-link"
            :class="{ active: $route.path === '/courier/dashboard' }" @click="sidebarOpen = false">
            <i class="fas fa-home cf-nav-icon"></i>
            <span>Início</span>
          </router-link>
        </li>
        <li>
          <router-link to="/courier/deliveries" class="cf-nav-link"
            :class="{ active: $route.path === '/courier/deliveries' }" @click="sidebarOpen = false">
            <i class="fas fa-route cf-nav-icon"></i>
            <span>Minhas Entregas</span>
          </router-link>
        </li>
        <li>
          <router-link to="/courier/financial" class="cf-nav-link"
            :class="{ active: $route.path === '/courier/financial' }" @click="sidebarOpen = false">
            <i class="fas fa-wallet cf-nav-icon"></i>
            <span>Financeiro (PIX)</span>
          </router-link>
        </li>
      </ul>

      <div class="cf-sidebar-footer">
        <div class="cf-user-info">
          <div class="cf-avatar">{{ currentUser?.nome?.charAt(0)?.toUpperCase() || 'M' }}</div>
          <div>
            <div class="cf-uname">{{ currentUser?.nome || 'Entregador' }}</div>
            <div class="cf-urole">Parceiro Logístico</div>
          </div>
        </div>
        <button @click="logout" class="cf-logout-btn" title="Sair">
          <i class="fas fa-sign-out-alt"></i>
        </button>
      </div>
    </nav>

    <!-- ═══ MAIN ═══ -->
    <div class="cf-main">
      <header class="cf-topbar">
        <div class="cf-topbar-left">
          <button @click="sidebarOpen = !sidebarOpen" class="cf-menu-btn d-md-none">
            <i class="fas fa-bars"></i>
          </button>
          <div class="cf-breadcrumb">
            <span class="cf-bc-root">Entregador</span>
            <i class="fas fa-chevron-right cf-bc-sep"></i>
            <span class="cf-bc-page">{{ pageTitle }}</span>
          </div>
        </div>
        <div class="cf-topbar-right">
           <div class="cf-status-badge">
             <span class="dot"></span> Online
           </div>
        </div>
      </header>

      <div class="cf-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CourierLayout',
  data() {
    return {
      currentUser: null,
      sidebarOpen: false,
      isMobile: false
    };
  },
  computed: {
    pageTitle() {
      const titles = {
        '/courier/dashboard': 'Dashboard',
        '/courier/deliveries': 'Entregas',
        '/courier/financial': 'Financeiro'
      };
      return titles[this.$route.path] || 'Painel';
    }
  },
  mounted() {
    this.checkMobile();
    window.addEventListener('resize', this.checkMobile);
    const raw = localStorage.getItem('user');
    if (raw) this.currentUser = JSON.parse(raw);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.checkMobile);
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth < 768;
      if (!this.isMobile) this.sidebarOpen = false;
    },
    logout() {
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      this.$router.push('/courier/login');
    }
  }
};
</script>

<style scoped>
.cf-courier-shell { 
  display: flex; min-height: 100vh; 
  background: var(--cf-ivory); font-family: var(--cf-sans); 
  overflow-x: hidden; width: 100%;
}

.cf-sidebar {
  width: 250px; height: 100vh; background: #1e293b;
  position: fixed; left: 0; top: 0; z-index: 1050;
  display: flex; flex-direction: column; transition: transform 0.28s cubic-bezier(0.4,0,0.2,1);
  overflow-y: auto; scrollbar-width: none;
}
.cf-sidebar::-webkit-scrollbar { display: none; }

.cf-sidebar-logo { display: flex; align-items: center; gap: 0.8rem; padding: 1.3rem 1.2rem; flex-shrink: 0; }
.logo-mark { width: 36px; height: 36px; border-radius: 9px; background: #3b82f6; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 1rem; flex-shrink: 0; }
.logo-name { font-family: var(--cf-serif); color: #fff; font-size: 1rem; line-height: 1.1; }
.logo-sub { font-size: 0.6rem; color: rgba(255,255,255,0.4); text-transform: uppercase; letter-spacing: 0.1em; }

.cf-divider { height: 1px; background: rgba(255,255,255,0.08); margin: 0 1.2rem; flex-shrink: 0; }
.cf-section-label { font-size: 0.58rem; letter-spacing: 0.18em; text-transform: uppercase; color: rgba(255,255,255,0.3); padding: 1.2rem 1.35rem 0.35rem; flex-shrink: 0; }

.cf-nav { list-style: none; padding: 0 0.7rem; margin: 0; flex: 1; }
.cf-nav-link {
  display: flex; align-items: center; gap: 0.7rem; padding: 0.7rem 0.85rem; border-radius: 10px;
  color: rgba(255,255,255,0.6); text-decoration: none; font-size: 0.875rem; margin-bottom: 2px;
  transition: all 0.18s;
}
.cf-nav-link:hover { background: rgba(255,255,255,0.08); color: #fff; }
.cf-nav-link.active { background: rgba(59, 130, 246, 0.2); color: #60a5fa; font-weight: 500; }
.cf-nav-icon { width: 15px; text-align: center; opacity: 0.7; flex-shrink: 0; }

.cf-sidebar-footer { 
  margin-top: auto; padding: 1rem 1.2rem; border-top: 1px solid rgba(255,255,255,0.08); 
  display: flex; align-items: center; gap: 0.7rem; flex-shrink: 0; background: rgba(0,0,0,0.1);
}
.cf-avatar { width: 32px; height: 32px; border-radius: 50%; background: #3b82f6; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.8rem; flex-shrink: 0; }
.cf-uname { font-size: 0.78rem; font-weight: 500; color: #fff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 110px; }
.cf-urole { font-size: 0.6rem; color: rgba(255,255,255,0.35); }
.cf-logout-btn { background: none; border: none; color: rgba(255,255,255,0.35); cursor: pointer; margin-left: auto; flex-shrink: 0; }

.cf-main {
  margin-left: 250px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-width: 0;
  width: calc(100% - 250px);
  overflow-x: hidden;
}
.cf-topbar {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  position: sticky;
  top: 0;
  z-index: 100;
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
  background: none;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 0.3rem 0.5rem;
  cursor: pointer;
  color: #64748b;
  flex-shrink: 0;
}
.cf-breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  min-width: 0;
  white-space: nowrap;
}
.cf-bc-root { color: #64748b; font-size: 0.72rem; }
.cf-bc-page {
  font-weight: 600;
  color: #1e293b;
  font-size: 0.82rem;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cf-status-badge {
  background: #f0fdf4;
  color: #166534;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.65rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}
.cf-status-badge .dot { width: 6px; height: 6px; background: #22c55e; border-radius: 50%; }

.cf-content { flex: 1; padding: 1.5rem; min-width: 0; width: 100%; }
.cf-sidebar-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); z-index: 1040; backdrop-filter: blur(4px); }

@media (max-width: 991px) {
  .cf-sidebar { transform: translateX(-100%); width: 280px; }
  .cf-sidebar.sidebar-open { transform: translateX(0); }
  .cf-main { margin-left: 0; width: 100%; }
  .cf-content { padding: 1rem; }
  .cf-topbar { padding: 0 1rem; }
  .cf-topbar-right { display: none; }
}
</style>
