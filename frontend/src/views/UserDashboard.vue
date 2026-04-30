<template>
  <div class="dashboard-page">
    <div class="container py-lg-5 py-4">
      
      <!-- Welcome Header -->
      <div class="header-wrap mb-5 fade-in-up">
        <span class="section-eyebrow">Acesso Restrito</span>
        <h1 class="section-title">Bem-vindo, <em>{{ userName }}</em></h1>
        <p class="section-desc">Gerencie seus pedidos, prescrições e dados de saúde em um só lugar.</p>
      </div>

      <div class="row g-4">
        <!-- Main Stats -->
        <div class="col-lg-12 fade-in-up">
          <div class="cf-stats-row">
            <div class="stat-card">
              <div class="stat-icon"><i class="fa-solid fa-box-open"></i></div>
              <div class="stat-data">
                <span class="stat-val">{{ ordersCount }}</span>
                <span class="stat-label">Total de Pedidos</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon icon-gold"><i class="fa-solid fa-clock-rotate-left"></i></div>
              <div class="stat-data">
                <span class="stat-val">{{ pendingOrders }}</span>
                <span class="stat-label">Em Andamento</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon icon-green"><i class="fa-solid fa-file-prescription"></i></div>
              <div class="stat-data">
                <span class="stat-val">3</span>
                <span class="stat-label">Receitas Ativas</span>
              </div>
            </div>
            <div class="stat-card">
               <div class="stat-icon icon-premium"><i class="fa-solid fa-shield-heart"></i></div>
               <div class="stat-data">
                 <span class="stat-val">Premium</span>
                 <span class="stat-label">Plano Health+</span>
               </div>
            </div>
          </div>
        </div>

        <!-- Recent Activity & Tracking -->
        <div class="col-lg-8 fade-in-up" style="animation-delay: 0.1s">
          <!-- Active Tracking -->
          <div class="cf-dashboard-card mb-4 overflow-hidden">
            <div class="card-header-cf">
              <i class="fa-solid fa-truck-fast"></i>
              <span>Rastreamento em Tempo Real</span>
            </div>
            <div v-if="activeTracking" class="p-4 bg-sage-tint">
               <div class="row align-items-center">
                 <div class="col-md-7">
                    <h5 class="tracking-order-id mb-1">Pedido #{{ activeTracking.orderId }}</h5>
                    <p class="small text-muted mb-3">{{ activeTracking.currentLocation }}</p>
                    <div class="tracking-progress-mini mb-2">
                       <div class="progress-bar-cf" style="width: 65%"></div>
                    </div>
                    <span class="cf-badge badge-warning">A caminho da entrega</span>
                 </div>
                 <div class="col-md-5 text-md-end mt-3 mt-md-0">
                    <button @click="viewFullTracking(activeTracking.orderId)" class="btn btn-primary btn-sm">
                       Ver Mapa Completo
                       <i class="fa-solid fa-map-location-dot ms-2"></i>
                    </button>
                 </div>
               </div>
            </div>
            <div v-else class="p-5 text-center">
               <i class="fa-solid fa-box-archive fa-2x mb-3 text-faint"></i>
               <p class="text-muted mb-0">Nenhum pedido em trânsito no momento.</p>
            </div>
          </div>

          <!-- Recent Orders -->
          <div class="cf-dashboard-card">
            <div class="card-header-cf">
              <i class="fa-solid fa-receipt"></i>
              <span>Últimos Pedidos</span>
              <router-link to="/orders" class="ms-auto small-link">Ver todos</router-link>
            </div>
            <div class="p-0">
               <div class="table-responsive">
                 <table class="table cf-table mb-0">
                   <thead>
                     <tr>
                        <th>ID</th>
                        <th>Data</th>
                        <th>Status</th>
                        <th class="text-end">Total</th>
                        <th></th>
                     </tr>
                   </thead>
                   <tbody>
                      <tr v-for="order in recentOrders" :key="order.id">
                        <td class="order-id">#{{ order.id }}</td>
                        <td class="order-date">{{ formatDate(order.dataPedido) }}</td>
                        <td>
                          <span :class="order.status" class="cf-badge-dot">
                            {{ getStatusText(order.status) }}
                          </span>
                        </td>
                        <td class="text-end fw-bold text-dark">R$ {{ (order.valorTotal || 0).toFixed(2).replace('.', ',') }}</td>
                        <td class="text-end">
                          <router-link :to="`/orders/${order.id}`" class="icon-action">
                            <i class="fa-solid fa-chevron-right"></i>
                          </router-link>
                        </td>
                      </tr>
                    </tbody>
                 </table>
               </div>
            </div>
          </div>
        </div>

        <!-- Sidebar Actions -->
        <div class="col-lg-4 fade-in-up" style="animation-delay: 0.2s">
          <div class="cf-dashboard-card mb-4">
             <div class="p-4">
                <h5 class="section-title-sm mb-4">Acesso Rápido</h5>
                <div class="quick-nav">
                   <router-link to="/products" class="q-link">
                      <i class="fa-solid fa-store"></i>
                      Ir para a Loja
                   </router-link>
                   <router-link to="/profile" class="q-link">
                      <i class="fa-solid fa-user-pen"></i>
                      Editar Perfil
                   </router-link>
                   <router-link to="/prescriptions" class="q-link">
                      <i class="fa-solid fa-file-medical"></i>
                      Receitas Médicas
                   </router-link>
                   <router-link to="/support" class="q-link">
                      <i class="fa-solid fa-headset"></i>
                      Suporte ClickFarma
                   </router-link>
                </div>
             </div>
          </div>

          <div class="health-tip-card p-4">
             <div class="d-flex gap-3 align-items-center mb-3">
                <i class="fa-solid fa-star-of-life text-gold"></i>
                <h6 class="mb-0 fw-bold">Dica de Saúde</h6>
             </div>
             <p class="small mb-0 text-muted">Lembre-se de manter seus exames em dia. Clientes ClickFarma Premium têm 15% de desconto em laboratórios parceiros.</p>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import pedidoService from '@/services/pedidoService';

export default {
  name: 'UserDashboard',
  data() {
    return {
      recentOrders: [],
      activeTracking: null,
      loading: false
    }
  },
  computed: {
    userName() {
      const user = this.$store.state.user || JSON.parse(localStorage.getItem('user') || '{}');
      return user ? (user.nome || user.name) : 'Visitante';
    },
    ordersCount() { return this.recentOrders.length; },
    pendingOrders() {
      return this.recentOrders.filter(order => 
        ['AGUARDANDO_PAGAMENTO', 'PAGO', 'EM_PREPARACAO', 'EM_TRANSITO'].includes(order.status)
      ).length;
    }
  },
  async mounted() {
    await this.loadDashboardData();
  },
  methods: {
    async loadDashboardData() {
      this.loading = true;
      try {
        const user = this.$store.state.user || JSON.parse(localStorage.getItem('user') || '{}');
        if (user && user.id) {
          const res = await pedidoService.listarPorUsuario(user.id);
          this.recentOrders = res.data.slice(0, 5); // Apenas os 5 mais recentes
          this.loadActiveTracking();
        }
      } catch (error) {
        console.error('Erro ao carregar dashboard:', error);
      } finally {
        this.loading = false;
      }
    },
    loadActiveTracking() {
      // Busca o pedido mais recente que está em trânsito
      const active = this.recentOrders.find(o => o.status === 'EM_TRANSITO');
      if (active) {
        this.activeTracking = {
          orderId: active.id,
          status: 'EM_TRANSITO',
          currentLocation: 'Pedido saiu para entrega',
          estimatedDelivery: active.dataAtualizacao
        };
      }
    },
    viewFullTracking(orderId) { this.$router.push(`/tracking/${orderId}`); },
    getStatusText(status) {
      const map = { 
        'AGUARDANDO_PAGAMENTO': 'Aguardando Pagto', 
        'PAGO': 'Pago', 
        'EM_PREPARACAO': 'Em Preparação', 
        'EM_TRANSITO': 'Em Trânsito', 
        'ENTREGUE': 'Entregue', 
        'CANCELADO': 'Cancelado' 
      };
      return map[status] || status;
    },
    formatDate(dateString) { 
      if (!dateString) return '—';
      return new Date(dateString).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: '2-digit' }); 
    }
  }
}
</script>

<style scoped>
.dashboard-page { background: var(--cf-white); min-height: 90vh; }

.section-desc { color: var(--cf-text-muted); font-size: 1.1rem; font-weight: 300; max-width: 600px; }

/* STATS ROW */
.cf-stats-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.5rem; margin-bottom: 2rem; }
.stat-card { background: var(--cf-white); border: 1px solid var(--cf-border); border-radius: var(--cf-r-xl); padding: 1.75rem; display: flex; align-items: center; gap: 1.5rem; transition: all 250ms; }
.stat-card:hover { border-color: var(--cf-gold); transform: translateY(-3px); box-shadow: var(--cf-shadow-sm); }
.stat-icon { width: 50px; height: 50px; background: var(--cf-sage-tint); color: var(--cf-green); border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; }
.stat-icon.icon-gold { background: var(--cf-gold-light); color: var(--cf-gold); }
.stat-icon.icon-premium { background: var(--cf-cream); color: var(--cf-gold); }
.stat-val { font-family: var(--cf-sans); font-size: 1.8rem; font-weight: 600; color: var(--cf-text-dark); line-height: 1; display: block; }
.stat-label { font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.08em; color: var(--cf-text-faint); font-weight: 600; }

/* DASHBOARD CARDS */
.cf-dashboard-card { background: var(--cf-white); border: 1px solid var(--cf-border); border-radius: var(--cf-r-xl); }
.card-header-cf { padding: 1.25rem 1.5rem; border-bottom: 1px solid var(--cf-border); display: flex; align-items: center; gap: 12px; font-weight: 600; color: var(--cf-text-dark); }
.card-header-cf i { color: var(--cf-green-mid); }
.small-link { color: var(--cf-gold); text-decoration: none; font-weight: 500; font-size: 0.85rem; }

.bg-sage-tint { background: var(--cf-sage-tint); }
.tracking-order-id { font-family: var(--cf-sans); font-size: 1.25rem; font-weight: 600; color: var(--cf-green); }
.tracking-progress-mini { height: 6px; background: rgba(0,0,0,0.05); border-radius: 100px; overflow: hidden; }
.progress-bar-cf { height: 100%; background: var(--cf-green); border-radius: 100px; }

/* TABLE */
.cf-table th { background: var(--cf-ivory); font-size: 0.65rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-text-faint); padding: 12px 1.5rem; border: none; }
.cf-table td { padding: 1rem 1.5rem; vertical-align: middle; border-bottom: 1px solid var(--cf-ivory); }
.order-id { font-weight: 600; color: var(--cf-text-dark); font-size: 0.9rem; }
.order-date { font-size: 0.85rem; color: var(--cf-text-muted); }
.icon-action { color: var(--cf-text-faint); transition: color 200ms; }
.icon-action:hover { color: var(--cf-green); }

/* BADGE DOT */
.cf-badge-dot { display: inline-flex; align-items: center; gap: 8px; font-size: 0.8rem; font-weight: 500; color: var(--cf-text-mid); }
.cf-badge-dot::before { content: ""; width: 8px; height: 8px; border-radius: 50%; }
.delivered::before { background: var(--cf-green); }
.out_for_delivery::before { background: var(--cf-gold); }
.processing::before { background: var(--cf-green-mid); }

/* QUICK NAV */
.q-link { display: flex; align-items: center; gap: 12px; padding: 1rem; background: var(--cf-ivory); border-radius: var(--cf-r-md); margin-bottom: 0.75rem; text-decoration: none; color: var(--cf-text-dark); font-weight: 500; font-size: 0.9rem; transition: all 200ms; border: 1px solid transparent; }
.q-link i { width: 20px; color: var(--cf-green-mid); font-size: 1.1rem; }
.q-link:hover { background: white; border-color: var(--cf-border); transform: translateX(5px); color: var(--cf-green); }

.health-tip-card { background: var(--cf-green-dark); color: white; border-radius: var(--cf-r-xl); }
.text-gold { color: var(--cf-gold) !important; }

.section-title-sm { font-family: var(--cf-sans); font-size: 1.4rem; font-weight: 600; color: var(--cf-text-dark); }
</style>