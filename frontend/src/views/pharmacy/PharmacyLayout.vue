<template>
  <div class="cf-pharm-shell">
    <!-- Overlay mobile -->
    <div v-if="sidebarOpen && isMobile" class="cf-sidebar-overlay" @click="sidebarOpen = false"></div>

    <!-- ═══ SIDEBAR ═══ -->
    <nav class="cf-sidebar" :class="{ 'sidebar-open': sidebarOpen }">
      <div class="cf-sidebar-logo">
        <div class="logo-mark"><i class="fas fa-leaf"></i></div>
        <div>
          <div class="logo-name">ClickFarma</div>
          <div class="logo-sub">{{ currentUser?.nome || 'Farmácia' }}</div>
        </div>
        <button v-if="isMobile" @click="sidebarOpen = false" class="cf-close-btn">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div class="cf-divider"></div>

      <div class="cf-section-label">PAINEL</div>
      <ul class="cf-nav">
        <li>
          <router-link to="/pharmacy/dashboard" class="cf-nav-link"
            :class="{ active: $route.path === '/pharmacy/dashboard' }" @click="sidebarOpen = false">
            <i class="fas fa-chart-line cf-nav-icon"></i>
            <span>Visão Geral</span>
          </router-link>
        </li>
        <li>
          <router-link to="/pharmacy/products" class="cf-nav-link"
            :class="{ active: $route.path === '/pharmacy/products' }" @click="sidebarOpen = false">
            <i class="fas fa-pills cf-nav-icon"></i>
            <span>Meus Produtos</span>
          </router-link>
        </li>
        <li>
          <router-link to="/pharmacy/orders" class="cf-nav-link d-flex justify-content-between align-items-center"
            :class="{ active: $route.path === '/pharmacy/orders' }" @click="sidebarOpen = false">
            <span class="d-flex align-items-center gap-2">
              <i class="fas fa-clipboard-list cf-nav-icon"></i>
              Pedidos
            </span>
            <span v-if="pedidosPendentes > 0" class="cf-badge-alert">{{ pedidosPendentes }}</span>
          </router-link>
        </li>
        <li>
          <router-link to="/pharmacy/financial" class="cf-nav-link"
            :class="{ active: $route.path === '/pharmacy/financial' }" @click="sidebarOpen = false">
            <i class="fas fa-hand-holding-usd cf-nav-icon"></i>
            <span>Financeiro (Repasses)</span>
          </router-link>
        </li>
        <li>
          <router-link to="/pharmacy/settings" class="cf-nav-link"
            :class="{ active: $route.path === '/pharmacy/settings' }" @click="sidebarOpen = false">
            <i class="fas fa-cog cf-nav-icon"></i>
            <span>Configurações</span>
          </router-link>
        </li>
      </ul>

      <div class="cf-sidebar-footer">
        <div class="cf-user-info">
          <div class="cf-avatar">{{ currentUser?.nome?.charAt(0)?.toUpperCase() || 'F' }}</div>
          <div>
            <div class="cf-uname">{{ currentUser?.nome || 'Farmácia' }}</div>
            <div class="cf-urole">Parceiro ClickFarma</div>
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
            <span class="cf-bc-root">Farmácia</span>
            <i class="fas fa-chevron-right cf-bc-sep"></i>
            <span class="cf-bc-page">{{ pageTitle }}</span>
          </div>
        </div>
        <div class="cf-topbar-right">
          <span v-if="pedidosPendentes > 0" class="cf-topbar-alert">
            <i class="fas fa-bell me-1"></i>
            {{ pedidosPendentes }} pedido{{ pedidosPendentes > 1 ? 's' : '' }} pendente{{ pedidosPendentes > 1 ? 's' : '' }}
          </span>
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
  name: 'PharmacyLayout',
  data() {
    return {
      currentUser: null,
      pedidosPendentes: 0,
      farmaciaId: null,
      pollingInterval: null,
      sidebarOpen: false,
      isMobile: false
    };
  },
  computed: {
    pageTitle() {
      const titles = {
        '/pharmacy/dashboard': 'Visão Geral',
        '/pharmacy/products':  'Meus Produtos',
        '/pharmacy/orders':    'Pedidos',
        '/pharmacy/financial': 'Financeiro (Repasses)',
        '/pharmacy/settings':  'Configurações'
      };
      return titles[this.$route.path] || 'Painel da Farmácia';
    }
  },
  async mounted() {
    this.checkMobile();
    window.addEventListener('resize', this.checkMobile);
    const raw = localStorage.getItem('user');
    if (raw) this.currentUser = JSON.parse(raw);
    await this.carregarBadge();
    this.pollingInterval = setInterval(() => this.carregarBadge(), 30000);
  },
  beforeUnmount() {
    clearInterval(this.pollingInterval);
    window.removeEventListener('resize', this.checkMobile);
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth < 768;
      if (!this.isMobile) this.sidebarOpen = false;
    },
    async carregarBadge() {
      try {
        const api = (await import('@/services/api')).default;
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const farmaciasRes = await api.get('/farmacias');
        const farmacia = farmaciasRes.data.find(f => f.email === user.email);
        if (!farmacia) return;
        this.farmaciaId = farmacia.id;
        const res = await api.get(`/pedidos/farmacia/${farmacia.id}`);
        this.pedidosPendentes = res.data.filter(p =>
          p.status === 'AGUARDANDO_PAGAMENTO' || p.status === 'PAGO'
        ).length;
      } catch {}
    },
    logout() {
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      this.$router.push('/pharmacy/login');
    }
  }
};
</script>

<style scoped>
.cf-pharm-shell {
  display: flex; min-height: 100vh;
  background: var(--cf-ivory); font-family: var(--cf-sans);
}

/* ══════════════════════════════════════
   SHELL DO PAINEL — Identidade ClickFarma
   ══════════════════════════════════════ */
.cf-pharm-shell {
  display: flex; min-height: 100vh;
  background: var(--cf-ivory); font-family: var(--cf-sans);
  overflow-x: hidden; width: 100%;
}

/* ─── SIDEBAR ─── */
.cf-sidebar {
  width: 250px; height: 100vh;
  background: var(--cf-green-dark);
  position: fixed; left: 0; top: 0; z-index: 1050;
  display: flex; flex-direction: column;
  transition: transform 0.28s cubic-bezier(0.4,0,0.2,1);
  overflow-y: auto;
  scrollbar-width: none;
}
.cf-sidebar::-webkit-scrollbar { display: none; }

.cf-sidebar-logo {
  display: flex; align-items: center; gap: 0.8rem;
  padding: 1.3rem 1.2rem; flex-shrink: 0;
}
.logo-mark {
  width: 36px; height: 36px; border-radius: 9px;
  background: var(--cf-gold); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 1rem; flex-shrink: 0;
}
.logo-name { font-family: var(--cf-serif); color: #fff; font-size: 1rem; line-height: 1.1; }
.logo-sub  { font-size: 0.6rem; color: rgba(255,255,255,0.4); text-transform: uppercase; letter-spacing: 0.1em; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 120px; }
.cf-close-btn { margin-left: auto; background: none; border: none; color: rgba(255,255,255,0.5); cursor: pointer; }

.cf-divider { height: 1px; background: rgba(255,255,255,0.08); margin: 0 1.2rem; flex-shrink: 0; }
.cf-section-label {
  font-size: 0.58rem; letter-spacing: 0.18em; text-transform: uppercase;
  color: rgba(255,255,255,0.3); padding: 1rem 1.35rem 0.35rem; font-weight: 500; flex-shrink: 0;
}

.cf-nav { list-style: none; padding: 0 0.7rem; margin: 0; flex: 1; }
.cf-nav-link {
  display: flex; align-items: center; gap: 0.7rem;
  padding: 0.62rem 0.85rem; border-radius: var(--cf-r-md);
  color: rgba(255,255,255,0.6); text-decoration: none;
  font-size: 0.875rem; font-weight: 400; margin-bottom: 2px;
  border-left: 3px solid transparent;
  transition: all 0.18s var(--cf-ease);
}
.cf-nav-link:hover { background: rgba(255,255,255,0.08); color: #fff; border-left-color: var(--cf-gold); }
.cf-nav-link.active { background: rgba(184,149,80,0.18); color: var(--cf-gold); border-left-color: var(--cf-gold); font-weight: 500; }
.cf-nav-icon { width: 15px; text-align: center; opacity: 0.7; flex-shrink: 0; }
.cf-nav-link.active .cf-nav-icon { opacity: 1; }

.cf-badge-alert {
  font-size: 0.65rem; font-weight: 700; background: #8B3A3A;
  color: #fff; border-radius: 10px; padding: 1px 7px; flex-shrink: 0;
}

.cf-sidebar-footer {
  margin-top: auto; padding: 1rem 1.2rem;
  border-top: 1px solid rgba(255,255,255,0.08);
  display: flex; align-items: center; gap: 0.7rem;
  flex-shrink: 0; background: rgba(0,0,0,0.1);
}
.cf-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--cf-gold); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.82rem; font-weight: 700; flex-shrink: 0;
}
.cf-uname { font-size: 0.78rem; font-weight: 500; color: #fff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 110px; }
.cf-urole { font-size: 0.6rem; color: rgba(255,255,255,0.35); }
.cf-logout-btn { background: none; border: none; color: rgba(255,255,255,0.35); cursor: pointer; font-size: 0.9rem; padding: 0.3rem; border-radius: 5px; margin-left: auto; flex-shrink: 0; transition: color 0.2s; }
.cf-logout-btn:hover { color: var(--cf-gold); }

.cf-sidebar-overlay { position: fixed; inset: 0; background: rgba(28,28,26,0.6); z-index: 1040; backdrop-filter: blur(4px); }

/* ─── MAIN ─── */
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
  background: none;
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-sm);
  padding: 0.35rem 0.55rem;
  cursor: pointer;
  color: var(--cf-text-muted);
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
.cf-bc-root { font-size: 0.72rem; color: var(--cf-text-muted); }
.cf-bc-sep  { font-size: 0.55rem; color: var(--cf-text-faint); }
.cf-bc-page {
  font-size: 0.82rem;
  font-weight: 500;
  color: var(--cf-text-dark);
  overflow: hidden;
  text-overflow: ellipsis;
}

.cf-topbar-alert {
  font-size: 0.68rem;
  color: #8B3A3A;
  background: #F9EDED;
  padding: 0.3rem 0.75rem;
  border-radius: 20px;
  border: 1px solid rgba(139,58,58,0.15);
  font-weight: 500;
  animation: pulse 2s ease-in-out infinite;
  white-space: nowrap;
}
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.6; } }

.cf-content {
  flex: 1;
  padding: 1.5rem;
  background: var(--cf-ivory);
  animation: fadeInUp 0.32s var(--cf-ease) both;
  min-width: 0;
  width: 100%;
}
@keyframes fadeInUp { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 991px) {
  .cf-sidebar { transform: translateX(-100%); width: 280px; }
  .cf-sidebar.sidebar-open { transform: translateX(0); }
  .cf-main { margin-left: 0; width: 100%; }
  .cf-content { padding: 1rem; }
  .cf-topbar { padding: 0 1rem; }
  .cf-topbar-right { display: none; }
}
</style>
