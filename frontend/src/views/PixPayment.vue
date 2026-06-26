<template>
  <div class="container mt-5 mb-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="card shadow-sm border-0 p-4 text-center">
          <div v-if="pago">
            <div class="my-4">
              <div class="rounded-circle bg-success d-inline-flex align-items-center justify-content-center" style="width: 80px; height: 80px;">
                <i class="fas fa-check text-white fa-3x"></i>
              </div>
            </div>
            <h3 class="text-success font-weight-bold">Pagamento Confirmado!</h3>
            <p class="text-muted">Seu pedido #{{ codigoPedido }} foi pago com sucesso.</p>
            <button class="btn btn-primary btn-lg mt-3" @click="irParaSucesso">
              Ver detalhes do pedido
            </button>
          </div>

          <div v-else>
            <div class="mb-4">
              <i class="fas fa-qrcode text-success fa-4x mb-2"></i>
              <h4 class="font-weight-bold">Pague com PIX</h4>
              <p class="text-muted">Escaneie o QR Code ou copie o código abaixo</p>
            </div>

            <div class="bg-light p-4 rounded mb-4 d-inline-block">
              <img v-if="qrCodeBase64" :src="'data:image/png;base64,' + qrCodeBase64"
                   alt="QR Code PIX" class="img-fluid" style="max-width: 250px;">
              <div v-else class="spinner-border text-success" role="status"></div>
            </div>

            <div class="mb-4">
              <label class="text-muted small">Código PIX (copia e cola):</label>
              <div class="input-group">
                <input type="text" class="form-control bg-light" :value="copiaECola" readonly ref="pixInput">
                <button class="btn btn-outline-success" @click="copiarCodigo">
                  <i class="fas fa-copy"></i> Copiar
                </button>
              </div>
            </div>

            <div class="mb-4">
              <div class="d-flex justify-content-center align-items-center text-muted">
                <i class="fas fa-clock me-2"></i>
                <span v-if="tempoRestante > 0">
                  Expira em <strong>{{ formatarTempo(tempoRestante) }}</strong>
                </span>
                <span v-else class="text-danger font-weight-bold">Expirado</span>
              </div>
              <div class="progress mt-2" style="height: 6px;">
                <div class="progress-bar bg-success"
                     :style="{ width: progressoPercent + '%' }"
                     :class="{ 'bg-danger': tempoRestante < 120 }">
                </div>
              </div>
            </div>

            <div class="alert alert-info d-flex align-items-center">
              <i class="fas fa-sync-alt fa-spin me-2"></i>
              <span>Verificando pagamento automaticamente...</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { paymentService } from '@/services/payment';

export default {
  name: 'PixPayment',
  data() {
    return {
      pedidoId: null,
      codigoPedido: '',
      qrCodeBase64: '',
      copiaECola: '',
      expiracao: '',
      pago: false,
      tempoRestante: 1800,
      totalSegundos: 1800,
      pollingInterval: null,
      timerInterval: null
    };
  },
  computed: {
    progressoPercent() {
      return Math.max(0, (this.tempoRestante / this.totalSegundos) * 100);
    }
  },
  mounted() {
    this.pedidoId = this.$route.params.pedidoId;
    this.codigoPedido = this.$route.params.codigoPedido || '';
    this.qrCodeBase64 = this.$route.params.qrCodeBase64 || '';
    this.copiaECola = this.$route.params.copiaECola || '';
    this.expiracao = this.$route.params.expiracao || '';

    if (!this.qrCodeBase64 && !this.copiaECola) {
      const stored = localStorage.getItem('ultimoPixData');
      if (stored) {
        try {
          const data = JSON.parse(stored);
          this.pedidoId = data.pedidoId || this.pedidoId;
          this.codigoPedido = data.codigoPedido || this.codigoPedido;
          this.qrCodeBase64 = data.qrCodeBase64 || this.qrCodeBase64;
          this.copiaECola = data.copiaECola || this.copiaECola;
          this.expiracao = data.expiracao || this.expiracao;
        } catch (e) {}
      }
    }

    if (this.expiracao) {
      const diff = new Date(this.expiracao) - new Date();
      if (diff > 0) {
        this.totalSegundos = Math.floor(diff / 1000);
        this.tempoRestante = this.totalSegundos;
      }
    }

    this.iniciarTimer();
    this.iniciarPolling();
  },
  beforeUnmount() {
    this.pararIntervalos();
  },
  methods: {
    copiarCodigo() {
      const input = this.$refs.pixInput;
      if (input) {
        input.select();
        navigator.clipboard?.writeText(input.value).catch(() => {});
      }
    },
    formatarTempo(segundos) {
      const min = Math.floor(segundos / 60);
      const seg = segundos % 60;
      return String(min).padStart(2, '0') + ':' + String(seg).padStart(2, '0');
    },
    iniciarTimer() {
      this.timerInterval = setInterval(() => {
        if (this.tempoRestante > 0) {
          this.tempoRestante--;
        } else {
          this.pararIntervalos();
        }
      }, 1000);
    },
    iniciarPolling() {
      this.pollingInterval = setInterval(async () => {
        if (this.pago || !this.pedidoId) return;
        try {
          const res = await paymentService.checkPaymentStatus(this.pedidoId);
          if (res.data && res.data.status === 'PAGO') {
            this.pago = true;
            this.pararIntervalos();
            localStorage.removeItem('ultimoPixData');
            try {
              if (this.pedidoId) localStorage.setItem('ultimoPedidoId', String(this.pedidoId));
              if (this.codigoPedido) localStorage.setItem('ultimoCodigoPedido', String(this.codigoPedido));
            } catch (e) {}
          }
        } catch (e) {
          console.error('Erro ao verificar pagamento:', e);
        }
      }, 5000);
    },
    pararIntervalos() {
      if (this.pollingInterval) clearInterval(this.pollingInterval);
      if (this.timerInterval) clearInterval(this.timerInterval);
      this.pollingInterval = null;
      this.timerInterval = null;
    },
    irParaSucesso() {
      this.$router.push('/sucesso-pagamento');
    }
  }
};
</script>
