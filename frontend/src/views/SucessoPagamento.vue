<template>
  <div class="success-page">
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-lg-6 col-md-8">
          <!-- Confetti Background -->
          <div class="confetti-container" aria-hidden="true">
            <div class="confetti confetti-1"></div>
            <div class="confetti confetti-2"></div>
            <div class="confetti confetti-3"></div>
            <div class="confetti confetti-4"></div>
            <div class="confetti confetti-5"></div>
            <div class="confetti confetti-6"></div>
            <div class="confetti confetti-7"></div>
            <div class="confetti confetti-8"></div>
          </div>

          <!-- Success Card -->
          <div class="success-card">
            <div class="card-body p-4 p-md-5">
              <!-- Animated Checkmark -->
              <div class="success-icon-wrapper mb-4">
                <div class="success-icon-circle">
                  <svg class="success-checkmark" viewBox="0 0 52 52" aria-hidden="true">
                    <circle class="success-checkmark-circle" cx="26" cy="26" r="25" fill="none"/>
                    <path class="success-checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
                  </svg>
                </div>
              </div>

              <!-- Header -->
              <div class="text-center mb-4">
                <h1 class="success-title mb-2">Pedido Confirmado!</h1>
                <p class="success-subtitle mb-0">Seu pagamento foi processado com sucesso</p>
              </div>

              <!-- Order ID -->
              <div v-if="pedidoId || codigoPedido" class="order-id-badge text-center mb-4">
                <span class="badge-label">Número do pedido</span>
                <div class="badge-content" @click="copyOrderId" role="button" tabindex="0" aria-label="Copiar número do pedido">
                  <span class="order-number">#{{ codigoPedido || pedidoId }}</span>
                  <svg class="copy-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                  </svg>
                </div>
                <small v-if="copySuccess" class="copy-feedback text-success">Copiado!</small>
              </div>

              <!-- Order Details -->
              <div class="order-details-card mb-4">
                <div class="detail-row">
                  <div class="detail-item">
                    <span class="detail-label">Status</span>
                    <span class="detail-value status-confirmed">
                      <svg class="status-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 6L9 17l-5-5"/>
                      </svg>
                      Confirmado
                    </span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">Data</span>
                    <span class="detail-value">{{ currentDate }}</span>
                  </div>
                </div>
                <div class="detail-row detail-row-divider">
                  <div class="detail-item detail-item-full">
                    <span class="detail-label">Próxima atualização</span>
                    <span class="detail-value detail-value-muted">Você receberá um e-mail quando seu pedido sair para entrega</span>
                  </div>
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="action-buttons">
                <router-link
                  v-if="codigoPedido"
                  :to="`/tracking-backend/${encodeURIComponent(codigoPedido)}`"
                  class="btn btn-track w-100 mb-3"
                >
                  <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <polyline points="12 6 12 12 16 14"/>
                  </svg>
                  Rastrear Pedido
                </router-link>

                <router-link to="/orders" class="btn btn-orders w-100 mb-3">
                  <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                    <polyline points="10 9 9 9 8 9"/>
                  </svg>
                  Ver Meus Pedidos
                </router-link>

                <router-link to="/products" class="btn btn-shopping w-100 mb-3">
                  <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="9" cy="21" r="1"/>
                    <circle cx="20" cy="21" r="1"/>
                    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
                  </svg>
                  Continuar Comprando
                </router-link>

                <a
                  v-if="pedidoId || codigoPedido"
                  :href="whatsappShareLink"
                  target="_blank"
                  rel="noopener"
                  class="btn btn-whatsapp w-100"
                >
                  <svg class="btn-icon btn-icon-whatsapp" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z"/>
                  </svg>
                  Compartilhar via WhatsApp
                </a>
              </div>
            </div>
          </div>

          <!-- Support Card -->
          <div class="support-card mt-4">
            <div class="card-body p-4">
              <div class="d-flex align-items-center">
                <div class="support-icon me-3">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                </div>
                <div>
                  <h6 class="support-title mb-1">Precisa de ajuda?</h6>
                  <p class="support-text mb-0">Entre em contato conosco a qualquer momento</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      pedidoId: localStorage.getItem('ultimoPedidoId') || '',
      codigoPedido: localStorage.getItem('ultimoCodigoPedido') || '',
      copySuccess: false
    }
  },
  computed: {
    whatsappShareLink() {
      const id = this.codigoPedido || this.pedidoId;
      if (!id) return '#';
      const text = `Pedido ClickFarma #${id} - Pagamento confirmado! Acompanhe seu pedido. `;
      return `https://wa.me/?text=${encodeURIComponent(text)}`;
    },
    currentDate() {
      return new Date().toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  },
  mounted() {
    localStorage.removeItem('cart');
    localStorage.removeItem('ultimoPedidoId');
    localStorage.removeItem('ultimoCodigoPedido');
    this.$store.dispatch('clearCart');
  },
  methods: {
    copyOrderId() {
      const orderId = this.codigoPedido || this.pedidoId;
      if (!orderId) return;
      navigator.clipboard.writeText(orderId).then(() => {
        this.copySuccess = true;
        setTimeout(() => {
          this.copySuccess = false;
        }, 2000);
      }).catch(() => {});
    }
  }
}
</script>

<style scoped>
.success-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #F0FDF4 0%, #FFFFFF 100%);
  position: relative;
}

/* Confetti Animation */
.confetti-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 300px;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.confetti {
  position: absolute;
  width: 10px;
  height: 10px;
  border-radius: 2px;
  opacity: 0;
  animation: confetti-fall 3s ease-out forwards;
}

.confetti-1 { background: #22C55E; left: 10%; animation-delay: 0.2s; }
.confetti-2 { background: #15803D; left: 20%; animation-delay: 0.4s; }
.confetti-3 { background: #0369A1; left: 35%; animation-delay: 0.1s; }
.confetti-4 { background: #22C55E; left: 50%; animation-delay: 0.5s; }
.confetti-5 { background: #15803D; left: 65%; animation-delay: 0.3s; }
.confetti-6 { background: #0369A1; left: 75%; animation-delay: 0.6s; }
.confetti-7 { background: #22C55E; left: 85%; animation-delay: 0.2s; }
.confetti-8 { background: #15803D; left: 95%; animation-delay: 0.4s; }

@keyframes confetti-fall {
  0% {
    transform: translateY(-20px) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(300px) rotate(720deg);
    opacity: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .confetti {
    animation: none;
    opacity: 0;
  }
}

/* Success Card */
.success-card {
  position: relative;
  z-index: 1;
  background: #FFFFFF;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(21, 128, 61, 0.08), 0 1px 3px rgba(0, 0, 0, 0.04);
  border: 1px solid #BBF7D0;
  animation: card-entrance 0.5s ease-out;
}

@keyframes card-entrance {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Success Icon */
.success-icon-wrapper {
  display: flex;
  justify-content: center;
}

.success-icon-circle {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22C55E 0%, #15803D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(34, 197, 94, 0.3);
  animation: icon-pop 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) 0.2s both;
}

@keyframes icon-pop {
  from {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.success-checkmark {
  width: 52px;
  height: 52px;
}

.success-checkmark-circle {
  stroke: #FFFFFF;
  stroke-dasharray: 166;
  stroke-dashoffset: 166;
  stroke-width: 2;
  animation: stroke-circle 0.6s cubic-bezier(0.65, 0, 0.45, 1) 0.8s forwards;
}

.success-checkmark-check {
  stroke: #FFFFFF;
  stroke-dasharray: 48;
  stroke-dashoffset: 48;
  stroke-width: 3;
  stroke-linecap: round;
  stroke-linejoin: round;
  animation: stroke-check 0.3s cubic-bezier(0.65, 0, 0.45, 1) 1.2s forwards;
}

@keyframes stroke-circle {
  to {
    stroke-dashoffset: 0;
  }
}

@keyframes stroke-check {
  to {
    stroke-dashoffset: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .success-icon-circle,
  .success-checkmark-circle,
  .success-checkmark-check {
    animation: none;
    opacity: 1;
  }
  .success-checkmark-circle {
    stroke-dashoffset: 0;
  }
  .success-checkmark-check {
    stroke-dashoffset: 0;
  }
}

/* Typography */
.success-title {
  font-size: 28px;
  font-weight: 700;
  color: #14532D;
  letter-spacing: -0.5px;
}

.success-subtitle {
  font-size: 16px;
  color: #4B5563;
  line-height: 1.5;
}

/* Order ID Badge */
.order-id-badge {
  padding: 16px;
  background: #F0FDF4;
  border-radius: 12px;
  border: 1px dashed #BBF7D0;
}

.badge-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #6B7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.badge-content {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #FFFFFF;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: all;
}

.badge-content:hover {
  background: #F9FAFB;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.badge-content:focus-visible {
  outline: 2px solid #15803D;
  outline-offset: 2px;
}

.order-number {
  font-size: 20px;
  font-weight: 700;
  color: #15803D;
  font-family: monospace;
}

.copy-icon {
  width: 18px;
  height: 18px;
  color: #6B7280;
  transition: color 0.2s ease;
}

.badge-content:hover .copy-icon {
  color: #15803D;
}

.copy-feedback {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  font-weight: 500;
  animation: fade-in 0.2s ease;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Order Details */
.order-details-card {
  background: #F9FAFB;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #E5E7EB;
}

.detail-row {
  display: flex;
  gap: 16px;
}

.detail-row-divider {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #E5E7EB;
}

.detail-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item-full {
  flex: 1;
}

.detail-label {
  font-size: 12px;
  font-weight: 500;
  color: #6B7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.detail-value-muted {
  font-weight: 400;
  color: #6B7280;
  font-size: 14px;
}

.status-confirmed {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #15803D;
}

.status-icon {
  width: 16px;
  height: 16px;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  flex-direction: column;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 20px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  border: none;
  transition: all 0.2s ease;
  cursor: pointer;
  text-decoration: none;
}

.btn:focus-visible {
  outline: 3px solid #15803D;
  outline-offset: 2px;
}

.btn-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.btn-track {
  background: #15803D;
  color: #FFFFFF;
  box-shadow: 0 4px 12px rgba(21, 128, 61, 0.25);
}

.btn-track:hover {
  background: #166534;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(21, 128, 61, 0.3);
  color: #FFFFFF;
  text-decoration: none;
}

.btn-orders {
  background: #FFFFFF;
  color: #15803D;
  border: 2px solid #BBF7D0;
}

.btn-orders:hover {
  background: #F0FDF4;
  border-color: #15803D;
  transform: translateY(-2px);
  color: #15803D;
  text-decoration: none;
}

.btn-shopping {
  background: #F9FAFB;
  color: #374151;
  border: 2px solid #E5E7EB;
}

.btn-shopping:hover {
  background: #F3F4F6;
  border-color: #D1D5DB;
  color: #111827;
  transform: translateY(-2px);
  text-decoration: none;
}

.btn-whatsapp {
  background: #25D366;
  color: #FFFFFF;
}

.btn-whatsapp:hover {
  background: #128C7E;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 211, 102, 0.3);
  color: #FFFFFF;
  text-decoration: none;
}

.btn-icon-whatsapp {
  width: 22px;
  height: 22px;
}

/* Support Card */
.support-card {
  background: #FFFFFF;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  animation: card-entrance 0.5s ease-out 0.3s both;
}

.support-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: #F0FDF4;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.support-icon svg {
  width: 24px;
  height: 24px;
  color: #15803D;
}

.support-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.support-text {
  font-size: 14px;
  color: #6B7280;
  line-height: 1.4;
}

/* Responsive */
@media (max-width: 767px) {
  .success-title {
    font-size: 24px;
  }
  
  .success-icon-circle {
    width: 72px;
    height: 72px;
  }
  
  .success-checkmark {
    width: 44px;
    height: 44px;
  }
  
  .detail-row {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
