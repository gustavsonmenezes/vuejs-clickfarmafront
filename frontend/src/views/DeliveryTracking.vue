<template>
  <div class="cf-tracking">

    <!-- Header -->
    <div class="cf-tracking-header">
      <div class="cf-container">
        <button class="cf-tracking-back" @click="$router.back()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5m7-7-7 7 7 7"/>
          </svg>
          Voltar
        </button>
        <div class="cf-tracking-id">
          <h1>Acompanhar Pedido</h1>
          <span class="cf-order-code">#{{ orderCode }}</span>
        </div>
      </div>
    </div>

    <div class="cf-container">
      <!-- Busca de pedido -->
      <div v-if="!currentOrder" class="cf-tracking-search">
        <div class="cf-tracking-search-card">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--cf-teal)" stroke-width="1.3">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
          <h2>Acompanhe sua entrega</h2>
          <p>Digite o código do pedido para ver o status em tempo real</p>
          <div class="cf-tracking-input-group">
            <input
              v-model="searchCode"
              type="text"
              class="cf-input"
              placeholder="Ex: CF-123456"
              @keyup.enter="loadOrder"
            />
            <button class="cf-btn cf-btn-primary" @click="loadOrder" :disabled="!searchCode.trim()">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="7"/><path d="m16.5 16.5 4 4"/>
              </svg>
              Rastrear
            </button>
          </div>
          <div class="cf-tracking-examples">
            <span v-for="ex in examples" :key="ex" @click="searchCode = ex; loadOrder()">{{ ex }}</span>
          </div>
        </div>
      </div>

      <!-- Tracking ativo -->
      <div v-else class="cf-tracking-active">

        <!-- Mapa (placeholder visual) -->
        <div class="cf-tracking-map">
          <div class="cf-map-placeholder">
            <div class="cf-map-marker-group">
              <div class="cf-map-marker marker-farmacia">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="#0D9488" stroke="white" stroke-width="1.5">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3" fill="white" stroke="none"/>
                </svg>
                <span class="cf-map-label">Farmácia</span>
              </div>

              <div class="cf-map-route">
                <svg width="100%" height="4" viewBox="0 0 200 4">
                  <path d="M0 2 Q50 0 100 2 Q150 4 200 2" stroke="#CCFBF1" stroke-width="3" fill="none"/>
                  <circle cx="150" cy="2" r="5" fill="#0D9488" opacity="0.3">
                    <animate attributeName="cx" values="30;170;30" dur="4s" repeatCount="indefinite"/>
                  </circle>
                </svg>
              </div>

              <div class="cf-map-marker marker-entregador">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="#0284C7" stroke="white" stroke-width="1.5">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3" fill="white" stroke="none"/>
                </svg>
                <span class="cf-map-label">Entregador</span>
              </div>

              <div class="cf-map-marker marker-destino">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="#D97706" stroke="white" stroke-width="1.5">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3" fill="white" stroke="none"/>
                </svg>
                <span class="cf-map-label">Destino</span>
              </div>
            </div>

            <!-- Grid lines para simular mapa -->
            <div class="cf-map-grid">
              <div class="cf-map-grid-inner"></div>
            </div>

            <!-- Botão mapa real -->
            <button class="cf-map-toggle" @click="openMap">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"/>
                <line x1="8" y1="2" x2="8" y2="18"/>
                <line x1="16" y1="6" x2="16" y2="22"/>
              </svg>
              Ver mapa completo
            </button>
          </div>
        </div>

        <!-- Timeline de Status -->
        <div class="cf-tracking-timeline">
          <div class="cf-tl-header">
            <div>
              <span class="cf-tl-status-badge" :class="statusBadgeClass">
                <span class="cf-tl-bullet"></span>
                {{ statusLabel }}
              </span>
              <p class="cf-tl-estimate" v-if="estimatedTime">
                Previsão: <strong>{{ estimatedTime }}</strong>
              </p>
            </div>
            <button class="cf-btn cf-btn-ghost cf-btn-sm" @click="refreshTracking">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9h-5m5 0v-5"/>
              </svg>
              Atualizar
            </button>
          </div>

          <div class="cf-tl-steps">
            <div
              v-for="(step, i) in timelineSteps"
              :key="step.id"
              class="cf-tl-step"
              :class="{ completed: step.completed, current: step.current }"
            >
              <div class="cf-tl-dot">
                <svg v-if="step.completed" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                <div v-else-if="step.current" class="cf-tl-pulse"></div>
                <div v-else class="cf-tl-empty"></div>
              </div>
              <div class="cf-tl-content">
                <strong class="cf-tl-title">{{ step.title }}</strong>
                <p class="cf-tl-desc">{{ step.description }}</p>
                <span v-if="step.time" class="cf-tl-time">{{ step.time }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Info do entregador -->
        <div v-if="entregadorInfo" class="cf-tracking-deliveryman">
          <div class="cf-dm-avatar">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--cf-teal)" stroke-width="1.5">
              <circle cx="12" cy="8" r="4"/><path d="M4 21v-2a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v2"/>
            </svg>
          </div>
          <div class="cf-dm-info">
            <strong>{{ entregadorInfo.nome }}</strong>
            <span>{{ entregadorInfo.veiculo }}</span>
          </div>
          <a :href="`tel:${entregadorInfo.telefone}`" class="cf-btn cf-btn-outline cf-btn-sm">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
            </svg>
            Ligar
          </a>
        </div>

        <!-- Detalhes do pedido -->
        <div class="cf-tracking-details">
          <h3>Detalhes do Pedido</h3>
          <div class="cf-td-grid">
            <div class="cf-td-item">
              <span class="cf-td-label">Endereço de entrega</span>
              <span class="cf-td-value">{{ deliveryAddress }}</span>
            </div>
            <div class="cf-td-item">
              <span class="cf-td-label">Forma de pagamento</span>
              <span class="cf-td-value">{{ paymentMethod }}</span>
            </div>
            <div class="cf-td-item">
              <span class="cf-td-label">Valor total</span>
              <span class="cf-td-value cf-td-price">R$ {{ formatPrice(orderTotal) }}</span>
            </div>
          </div>
          <div v-if="currentOrder.items" class="cf-td-items">
            <span class="cf-td-label">Itens</span>
            <div v-for="item in currentOrder.items" :key="item.id || item.nome" class="cf-td-item-row">
              <span>{{ item.quantity || 1 }}x {{ item.nome }}</span>
              <span>R$ {{ formatPrice(item.preco) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DeliveryTracking',
  props: {
    orderId: { type: String, default: null }
  },
  data() {
    return {
      searchCode: '',
      currentOrder: null,
      loading: false,
      examples: ['CF-123456', 'CF-789012', 'CF-345678'],
      estimatedTime: '20-40 min',
      orderCode: '',
      orderTotal: 0,
      deliveryAddress: 'Rua dos Exemplos, 123 - São Paulo, SP',
      paymentMethod: 'PIX',
      entregadorInfo: null,
      timelineSteps: [],
      currentStatus: 0,
      statusLabel: 'Preparando',
    }
  },
  computed: {
    statusBadgeClass() {
      const map = {
        0: 'badge-preparing',
        1: 'badge-checking',
        2: 'badge-delivering',
        3: 'badge-done',
      }
      return map[this.currentStatus] || 'badge-preparing'
    }
  },
  mounted() {
    if (this.orderId) {
      this.searchCode = this.orderId
      this.loadOrder()
    }
  },
  methods: {
    formatPrice(v) { return (v || 0).toFixed(2).replace('.', ',') },
    loadOrder() {
      if (!this.searchCode.trim()) return
      this.currentOrder = {
        id: this.searchCode,
        codigoPedido: this.searchCode,
        status: 1,
        items: [
          { id: 1, nome: 'Dipirona Sódica 500mg', preco: 8.90, quantity: 2 },
          { id: 2, nome: 'Amoxicilina 500mg', preco: 19.50, quantity: 1 },
        ],
      }
      this.orderCode = this.searchCode
      this.orderTotal = 37.30
      this.buildTimeline(1)
    },
    buildTimeline(step) {
      this.currentStatus = step
      const steps = [
        { id: 'confirmed', title: 'Pedido Confirmado', description: 'Seu pedido foi recebido com sucesso', time: '10:32' },
        { id: 'separating', title: 'Separando Medicamentos', description: 'Farmacêutico está separando os itens do pedido', time: '10:45' },
        { id: 'checking', title: 'Farmacêutico Checando', description: 'Profissional está verificando os medicamentos', time: '' },
        { id: 'delivering', title: 'Entregador a Caminho', description: 'Saiu para entrega no endereço informado', time: '' },
        { id: 'delivered', title: 'Entregue', description: 'Pedido entregue com sucesso', time: '' },
      ]

      const mapLabel = { 0: 'Confirmado', 1: 'Preparando', 2: 'Em checagem', 3: 'Saiu para entrega', 4: 'Entregue' }
      this.statusLabel = mapLabel[step] || 'Preparando'
      if (step >= 3) {
        this.entregadorInfo = { nome: 'Carlos Silva', veiculo: 'Moto Honda CG 160', telefone: '(11) 99999-8888' }
      }

      if (step >= 2) {
        this.estimatedTime = '15-25 min'
      }

      this.timelineSteps = steps.map((s, i) => ({
        ...s,
        completed: i < step,
        current: i === step,
      }))
    },
    refreshTracking() {
      // Simula avanço do status
      if (this.currentStatus < 4) {
        this.buildTimeline(this.currentStatus + 1)
      }
    },
    openMap() {
      // Abre Google Maps / Waze
      const url = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(this.deliveryAddress)}`
      window.open(url, '_blank')
    }
  }
}
</script>

<style scoped>
.cf-tracking {
  min-height: 80vh;
  padding-bottom: 3rem;
}

/* Header */
.cf-tracking-header {
  padding: 1.25rem 0;
  border-bottom: 1px solid var(--cf-gray-200);
  margin-bottom: 1.5rem;
}
.cf-tracking-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: none;
  color: var(--cf-gray-500);
  font-size: 0.8rem;
  cursor: pointer;
  padding: 0;
  margin-bottom: 8px;
  font-family: var(--cf-body);
}
.cf-tracking-back:hover { color: var(--cf-gray-900); }
.cf-tracking-id h1 {
  font-family: var(--cf-heading);
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}
.cf-order-code {
  font-size: 0.85rem;
  color: var(--cf-gray-500);
  font-weight: 500;
}

/* Search */
.cf-tracking-search {
  display: flex;
  justify-content: center;
  padding: 3rem 0;
}
.cf-tracking-search-card {
  text-align: center;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.cf-tracking-search-card h2 {
  font-family: var(--cf-heading);
  font-size: 1.3rem;
  font-weight: 600;
}
.cf-tracking-search-card p {
  font-size: 0.9rem;
  color: var(--cf-gray-500);
}
.cf-tracking-input-group {
  display: flex;
  gap: 8px;
  width: 100%;
}
.cf-tracking-input-group .cf-input { flex: 1; }
.cf-tracking-examples {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}
.cf-tracking-examples span {
  font-size: 0.75rem;
  color: var(--cf-gray-400);
  padding: 0.2rem 0.6rem;
  border: 1px dashed var(--cf-gray-200);
  border-radius: var(--cf-r-sm);
  cursor: pointer;
  transition: all var(--cf-fast);
}
.cf-tracking-examples span:hover {
  border-color: var(--cf-teal);
  color: var(--cf-teal);
}

/* ============================
   MAPA
   ============================ */
.cf-tracking-map {
  border-radius: var(--cf-r-xl);
  overflow: hidden;
  border: 1px solid var(--cf-gray-200);
  margin-bottom: 1.5rem;
}
.cf-map-placeholder {
  position: relative;
  height: 280px;
  background: linear-gradient(135deg, var(--cf-teal-xlight), var(--cf-blue-xlight));
  overflow: hidden;
}
.cf-map-grid {
  position: absolute;
  inset: 0;
  opacity: 0.15;
}
.cf-map-grid-inner {
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(var(--cf-teal) 1px, transparent 1px),
    linear-gradient(90deg, var(--cf-teal) 1px, transparent 1px);
  background-size: 40px 40px;
}
.cf-map-marker-group {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 2rem 3rem;
}
.cf-map-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  z-index: 2;
}
.cf-map-label {
  font-size: 0.65rem;
  font-weight: 600;
  color: var(--cf-gray-600);
  background: rgba(255,255,255,0.9);
  padding: 0.15rem 0.5rem;
  border-radius: 3px;
}
.marker-entregador {
  animation: cf-bounce 2s infinite;
}
@keyframes cf-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
.cf-map-route {
  position: absolute;
  left: 15%;
  right: 15%;
  top: 55%;
}
.cf-map-toggle {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0.5rem 1rem;
  background: rgba(255,255,255,0.95);
  border: 1px solid var(--cf-gray-200);
  border-radius: var(--cf-r-md);
  font-size: 0.78rem;
  font-weight: 500;
  color: var(--cf-gray-700);
  cursor: pointer;
  transition: all var(--cf-fast);
  font-family: var(--cf-body);
  z-index: 3;
}
.cf-map-toggle:hover {
  background: white;
  box-shadow: var(--cf-shadow-sm);
}

/* ============================
   TIMELINE
   ============================ */
.cf-tracking-timeline {
  background: var(--cf-white);
  border: 1px solid var(--cf-gray-200);
  border-radius: var(--cf-r-lg);
  padding: 1.25rem;
  margin-bottom: 1rem;
}
.cf-tl-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}
.cf-tl-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0.35rem 0.85rem;
  border-radius: var(--cf-r-full);
  font-size: 0.8rem;
  font-weight: 600;
}
.cf-tl-bullet {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}
.badge-preparing {
  background: var(--cf-blue-xlight);
  color: var(--cf-blue-dark);
}
.badge-checking {
  background: #FFFBEB;
  color: #B45309;
}
.badge-delivering {
  background: var(--cf-teal-xlight);
  color: var(--cf-teal-dark);
}
.badge-done {
  background: #F0FDF4;
  color: #166534;
}
.cf-tl-estimate {
  font-size: 0.85rem;
  color: var(--cf-gray-500);
  margin-top: 4px;
}

/* Steps */
.cf-tl-steps {
  display: flex;
  flex-direction: column;
  gap: 0;
  position: relative;
}
.cf-tl-steps::before {
  content: '';
  position: absolute;
  left: 15px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--cf-gray-200);
}
.cf-tl-step {
  display: flex;
  gap: 16px;
  padding: 0.75rem 0;
  position: relative;
}
.cf-tl-step.completed::before {
  content: '';
  position: absolute;
  left: 15px;
  top: 0;
  height: 50%;
  width: 2px;
  background: var(--cf-teal);
  transform: translateX(-50%);
  z-index: 1;
}
.cf-tl-dot {
  position: relative;
  z-index: 2;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 2px solid var(--cf-gray-200);
  background: var(--cf-white);
  transition: all var(--cf-base);
}
.cf-tl-step.completed .cf-tl-dot {
  background: var(--cf-teal);
  border-color: var(--cf-teal);
  color: white;
}
.cf-tl-step.current .cf-tl-dot {
  border-color: var(--cf-teal);
  background: var(--cf-teal-xlight);
}
.cf-tl-pulse {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--cf-teal);
  animation: cf-pulse 1.5s infinite;
}
@keyframes cf-pulse {
  0% { box-shadow: 0 0 0 0 rgba(13,148,136,0.4); }
  100% { box-shadow: 0 0 0 12px rgba(13,148,136,0); }
}
.cf-tl-empty {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--cf-gray-300);
}
.cf-tl-content {
  flex: 1;
  padding-top: 4px;
}
.cf-tl-title {
  font-size: 0.9rem;
  color: var(--cf-gray-900);
  display: block;
}
.cf-tl-desc {
  font-size: 0.8rem;
  color: var(--cf-gray-500);
  margin: 2px 0 0;
}
.cf-tl-time {
  font-size: 0.7rem;
  color: var(--cf-gray-400);
  margin-top: 2px;
  display: block;
}
.cf-tl-step.pending .cf-tl-title { color: var(--cf-gray-400); }

/* ============================
   ENTREGADOR
   ============================ */
.cf-tracking-deliveryman {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 1rem 1.25rem;
  border: 1px solid var(--cf-gray-200);
  border-radius: var(--cf-r-lg);
  background: var(--cf-white);
  margin-bottom: 1rem;
}
.cf-dm-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--cf-teal-xlight);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.cf-dm-info {
  flex: 1;
}
.cf-dm-info strong {
  font-size: 0.9rem;
  color: var(--cf-gray-900);
  display: block;
}
.cf-dm-info span {
  font-size: 0.78rem;
  color: var(--cf-gray-500);
}

/* ============================
   DETALHES
   ============================ */
.cf-tracking-details {
  background: var(--cf-white);
  border: 1px solid var(--cf-gray-200);
  border-radius: var(--cf-r-lg);
  padding: 1.25rem;
}
.cf-tracking-details h3 {
  font-family: var(--cf-heading);
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 1rem;
}
.cf-td-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 1rem;
}
.cf-td-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.cf-td-label {
  font-size: 0.72rem;
  font-weight: 600;
  color: var(--cf-gray-500);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.cf-td-value {
  font-size: 0.88rem;
  color: var(--cf-gray-900);
}
.cf-td-price {
  font-family: var(--cf-heading);
  font-weight: 700;
  font-size: 1.1rem;
}
.cf-td-items {
  border-top: 1px solid var(--cf-gray-100);
  padding-top: 0.75rem;
}
.cf-td-items .cf-td-label { margin-bottom: 8px; display: block; }
.cf-td-item-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.82rem;
  color: var(--cf-gray-600);
  padding: 4px 0;
}

/* ============================
   RESPONSIVO
   ============================ */
@media (max-width: 768px) {
  .cf-map-marker-group { padding: 1.5rem; }
  .cf-td-grid { grid-template-columns: 1fr; }
}
</style>
