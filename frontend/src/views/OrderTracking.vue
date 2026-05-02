<template>
  <div class="track-page">
    <!-- Hero de busca -->
    <section class="track-hero">
      <div class="track-hero-inner">
        <h1 class="track-title">Rastrear Pedido</h1>
        <p class="track-subtitle">Acompanhe a entrega em tempo real</p>
        <div class="search-box">
          <input
            v-model="searchOrderId"
            type="text"
            class="search-input"
            placeholder="Digite o código do pedido (ex: CF-XXXX)"
            @keyup.enter="loadOrder"
          />
          <button
            class="search-btn"
            @click="loadOrder"
            :disabled="loading || !searchOrderId.trim()"
          >
            <svg v-if="loading" class="spin" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 11-6.2-8.6"/><path d="M21 3v6h-6"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7"/><path d="m16.5 16.5 4 4"/></svg>
            <span>{{ loading ? 'Buscando...' : 'Rastrear' }}</span>
          </button>
        </div>
      </div>
    </section>

    <!-- Nenhum pedido — state -->
    <div v-if="!currentOrder && !searchPerformed" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--cf-green)" stroke-width="1.2">
        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
        <line x1="3" y1="6" x2="21" y2="6"/>
        <path d="M16 10a4 4 0 0 1-8 0"/>
      </svg>
      <h3>Digite o código do seu pedido</h3>
      <p>O código foi enviado por e-mail após a compra.</p>
      <router-link to="/products" class="shop-link">Ver produtos</router-link>
    </div>

    <!-- Pedido não encontrado -->
    <div v-if="searchPerformed && !currentOrder && !loading" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--cf-danger)" stroke-width="1.2">
        <circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/>
      </svg>
      <h3>Pedido não encontrado</h3>
      <p>Verifique o código e tente novamente.</p>
      <button class="cf-btn cf-btn-primary" @click="resetSearch">Tentar novamente</button>
    </div>

    <!-- Resultado do rastreamento -->
    <div v-if="currentOrder" class="track-result">
      <div class="result-grid">
        <!-- Timeline principal -->
        <div class="result-main">
          <div class="result-card">
            <div class="card-head">
              <div>
                <h2 class="card-order-id">{{ orderCode }}</h2>
                <span class="card-date">{{ orderDate }}</span>
              </div>
              <span class="card-status" :class="statusBadgeClass">{{ orderStatusLabel }}</span>
            </div>

            <OrderTimeline :order="currentOrder" :auto-refresh="true" />
          </div>
        </div>

        <!-- Sidebar -->
        <aside class="result-side">
          <div class="side-card" v-if="deliveryAddress">
            <h4 class="side-title">Entrega</h4>
            <p class="side-address">{{ deliveryAddress }}</p>
          </div>

          <button class="cf-btn cf-btn-outline w-100" @click="refreshTracking" :disabled="loading">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 11-6.2-8.6"/><path d="M21 3v6h-6"/></svg>
            Atualizar status
          </button>

          <button class="cf-btn cf-btn-outline w-100" @click="resetSearch">
            Buscar outro pedido
          </button>
        </aside>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'
import { OrderService } from '@/services/orderService'
import OrderTimeline from '@/components/orders/OrderTimeline.vue'

export default {
  name: 'OrderTracking',
  components: { OrderTimeline },
  props: {
    orderId: { type: String, default: null }
  },
  data() {
    return {
      searchOrderId: '',
      currentOrder: null,
      loading: false,
      searchPerformed: false
    }
  },
  computed: {
    orderCode() {
      return '#' + (this.currentOrder?.codigoPedido || this.currentOrder?.id || '')
    },
    orderDate() {
      if (!this.currentOrder?.date) return ''
      try {
        return new Date(this.currentOrder.date).toLocaleDateString('pt-BR', {
          day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
        })
      } catch { return '' }
    },
    orderStatus() {
      return this.currentOrder?.status || ''
    },
    orderStatusLabel() {
      const m = {
        'confirmed': 'Confirmado', 'processing': 'Processando',
        'shipped': 'Enviado', 'out_for_delivery': 'Saiu para Entrega',
        'delivered': 'Entregue', 'cancelled': 'Cancelado',
        'AGUARDANDO_PAGAMENTO': 'Aguardando Pagamento', 'PAGO': 'Pago',
        'PREPARANDO': 'Preparando', 'ENVIADO': 'Enviado',
        'ENTREGUE': 'Entregue', 'CANCELADO': 'Cancelado'
      }
      return m[this.orderStatus] || this.orderStatus
    },
    statusBadgeClass() {
      const cls = {
        'confirmed': 'badge-green', 'processing': 'badge-blue',
        'shipped': 'badge-gold', 'out_for_delivery': 'badge-gold',
        'delivered': 'badge-green', 'cancelled': 'badge-red',
        'AGUARDANDO_PAGAMENTO': 'badge-gold', 'PAGO': 'badge-green',
        'PREPARANDO': 'badge-blue', 'ENVIADO': 'badge-gold',
        'ENTREGUE': 'badge-green', 'CANCELADO': 'badge-red'
      }
      return cls[this.orderStatus] || 'badge-gray'
    },
    deliveryAddress() {
      return this.currentOrder?.enderecoEntrega || this.currentOrder?.deliveryInfo || ''
    }
  },
  methods: {
    ...mapActions(['fetchOrderTracking']),
    async loadOrder() {
      if (!this.searchOrderId.trim()) return
      this.loading = true
      this.searchPerformed = true
      const query = this.searchOrderId.trim()
      try {
        const data = await OrderService.trackOrder(query)
        if (data) {
          this.currentOrder = this.buildOrderFromTracking(data)
          this.loading = false
          return
        }
      } catch { /* fallback */ }
      try {
        const saved = JSON.parse(localStorage.getItem('userOrders') || '[]')
        const found = saved.find(o =>
          (o.id && o.id.toLowerCase() === query.toLowerCase()) ||
          (o.codigoPedido && o.codigoPedido.toLowerCase() === query.toLowerCase())
        )
        this.currentOrder = found || null
      } catch { this.currentOrder = null }
      finally { this.loading = false }
    },
    buildOrderFromTracking(t) {
      return {
        id: t.codigoPedido || t.pedidoId, codigoPedido: t.codigoPedido,
        status: t.status || 'processing', date: t.dataEnvio || new Date().toISOString(),
        items: t.itens || [], total: t.valorTotal || 0, valorTotal: t.valorTotal,
        enderecoEntrega: t.enderecoEntrega || '',
        metodoPagamento: t.metodoPagamento || 'pix'
      }
    },
    async refreshTracking() {
      if (this.currentOrder?.id) {
        try { await this.fetchOrderTracking(this.currentOrder.id) } catch {}
      }
    },
    resetSearch() {
      this.searchOrderId = ''
      this.currentOrder = null
      this.searchPerformed = false
    }
  },
  async mounted() {
    if (this.orderId) {
      this.searchOrderId = this.orderId
      await this.loadOrder()
    }
  }
}
</script>

<style scoped>
.track-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 20px 60px;
  font-family: var(--cf-sans);
}

/* ====== Hero ====== */
.track-hero {
  text-align: center;
  padding: 48px 0 36px;
}

.track-title {
  font-family: var(--cf-serif);
  font-size: 1.8rem;
  font-weight: 500;
  color: var(--cf-text-dark);
  margin: 0 0 6px;
}

.track-subtitle {
  color: var(--cf-text-muted);
  font-size: 0.9rem;
  margin: 0 0 24px;
}

.search-box {
  display: flex;
  gap: 8px;
  max-width: 480px;
  margin: 0 auto;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-md);
  font-size: 0.9rem;
  font-family: var(--cf-sans);
  color: var(--cf-text-dark);
  background: var(--cf-white);
  transition: border-color 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: var(--cf-green);
  box-shadow: 0 0 0 3px rgba(42,92,69,0.08);
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 24px;
  background: var(--cf-green);
  color: #fff;
  border: none;
  border-radius: var(--cf-r-md);
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.search-btn:hover { background: var(--cf-green-dark); }
.search-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.spin { animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ====== Empty states ====== */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-state svg { margin-bottom: 16px; }

.empty-state h3 {
  font-family: var(--cf-serif);
  font-size: 1.2rem;
  font-weight: 500;
  color: var(--cf-text-dark);
  margin: 0 0 6px;
}

.empty-state p {
  color: var(--cf-text-muted);
  font-size: 0.875rem;
  margin: 0 0 20px;
}

.shop-link {
  color: var(--cf-green);
  font-weight: 500;
  font-size: 0.875rem;
  text-decoration: underline;
}

/* ====== Result ====== */
.track-result { margin-top: 16px; }

.result-grid {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
  align-items: start;
}

.result-card {
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg);
  overflow: hidden;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--cf-ivory);
  border-bottom: 1px solid var(--cf-border);
}

.card-order-id {
  font-family: var(--cf-sans);
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--cf-text-dark);
  margin: 0;
}

.card-date {
  font-size: 0.75rem;
  color: var(--cf-text-muted);
}

.card-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.badge-green { background: var(--cf-green-light); color: var(--cf-green); }
.badge-blue { background: #E8F2EC; color: var(--cf-green-mid); }
.badge-gold { background: var(--cf-gold-light); color: var(--cf-gold); }
.badge-red { background: #F9EDED; color: var(--cf-danger); }
.badge-gray { background: #F0F0EE; color: var(--cf-text-muted); }

/* ====== Sidebar ====== */
.side-card {
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-lg);
  padding: 16px 20px;
  margin-bottom: 10px;
}

.side-title {
  font-family: var(--cf-sans);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--cf-text-dark);
  margin: 0 0 10px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.side-address {
  font-size: 0.85rem;
  color: var(--cf-text-mid);
  line-height: 1.6;
  margin: 0;
}

.cf-btn-outline {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  background: transparent;
  color: var(--cf-green);
  border: 1px solid var(--cf-green);
  border-radius: var(--cf-r-md);
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 8px;
}

.cf-btn-outline:hover {
  background: var(--cf-green);
  color: #fff;
}

.cf-btn-outline:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.w-100 { width: 100%; }

/* ====== Responsive ====== */
@media (max-width: 768px) {
  .result-grid {
    grid-template-columns: 1fr;
  }
  .track-hero { padding: 32px 0 24px; }
  .search-box { flex-direction: column; }
  .search-btn { justify-content: center; }
}
</style>
