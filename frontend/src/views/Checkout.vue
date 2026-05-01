<template>
  <div class="container mt-5 mb-5">
    <div class="row">
      <div class="col-md-8">
        <!-- Info Farmácia -->
        <div class="card border-0 bg-light mb-4">
          <div class="card-body d-flex align-items-center justify-content-between p-3">
            <div class="d-flex align-items-center">
              <div class="bg-white rounded-circle p-2 me-3 shadow-sm">
                <i class="fas fa-store text-primary fa-lg"></i>
              </div>
              <div>
                <h6 class="mb-0 fw-bold">ClickFarma - Matriz</h6>
                <small class="text-muted">Rua da Hora, 123 · Aberto até 22h</small>
              </div>
            </div>
            <a :href="googleMapsLink" target="_blank" class="btn btn-sm btn-outline-primary">
              <i class="fas fa-map-marker-alt me-1"></i>Ver no mapa
            </a>
          </div>
        </div>

        <!-- Endereço -->
        <div class="card shadow-sm border-0 p-4 mb-4">
          <h4 class="mb-4 font-weight-bold">📍 Entrega</h4>
          <div class="row g-3">
            <div class="col-12">
              <label class="form-label">Rua *</label>
              <input
                type="text"
                v-model.trim="enderecoForm.rua"
                class="form-control bg-light"
                placeholder="Ex: Rua das Flores"
              >
            </div>
            <div class="col-md-4">
              <label class="form-label">Número *</label>
              <input
                type="text"
                v-model.trim="enderecoForm.numero"
                class="form-control bg-light"
                placeholder="Ex: 123"
              >
            </div>
            <div class="col-md-8">
              <label class="form-label">Complemento</label>
              <input
                type="text"
                v-model.trim="enderecoForm.complemento"
                class="form-control bg-light"
                placeholder="Apto, bloco, referência..."
              >
            </div>
            <div class="col-md-6">
              <label class="form-label">Bairro *</label>
              <input
                type="text"
                v-model.trim="enderecoForm.bairro"
                class="form-control bg-light"
                placeholder="Ex: Centro"
              >
            </div>
            <div class="col-md-6">
              <label class="form-label">Cidade *</label>
              <input
                type="text"
                v-model.trim="enderecoForm.cidade"
                class="form-control bg-light"
                placeholder="Ex: Recife"
              >
            </div>
            <div class="col-md-4">
              <label class="form-label">UF *</label>
              <input
                type="text"
                v-model.trim="enderecoForm.uf"
                class="form-control bg-light text-uppercase"
                maxlength="2"
                placeholder="Ex: PE"
              >
            </div>
            <div class="col-md-8">
              <label class="form-label">CEP *</label>
              <input
                type="text"
                v-model.trim="enderecoForm.cep"
                class="form-control bg-light"
                placeholder="Ex: 50000-000"
              >
              <small class="text-muted">Campos com * são obrigatórios.</small>
            </div>
          </div>
        </div>
        <!-- Pagamento (Componente Filho) -->
        <div class="card shadow-sm border-0 p-4">
          <PaymentMethod @update-method="setMetodo" />
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm border-0 p-4 sticky-top" style="top: 20px;">
          <h4 class="mb-4 font-weight-bold">Resumo</h4>
          <div class="d-flex justify-content-between mb-4 h5 font-weight-bold">
            <span>Total</span>
            <span class="text-primary">R$ {{ cartTotal.toFixed(2) }}</span>
          </div>
          <button
            @click="finalizar"
            :disabled="loading || !enderecoValido"
            class="btn btn-primary btn-lg w-100 py-3 font-weight-bold shadow-sm"
          >
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ loading ? 'Processando...' : 'FINALIZAR E PAGAR' }}
          </button>
          <small v-if="!enderecoValido" class="text-danger d-block mt-2">
            Preencha os dados de entrega para continuar.
          </small>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import { OrderService } from '@/services/orderService.js';
import PaymentMethod from '@/components/checkout/PaymentMethod.vue';

export default {
  props: ['cart'],
  components: { PaymentMethod },
  data() {
    return {
      loading: false,
      enderecoForm: {
        rua: '',
        numero: '',
        complemento: '',
        bairro: '',
        cidade: '',
        uf: '',
        cep: ''
      },
      metodo: 'MERCADO_PAGO',
      localCart: []
    };
  },
  computed: {
    ...mapState(['user']),
    googleMapsLink() {
      return 'https://www.google.com/maps/search/?api=0&query=ClickFarma+Recife+PE';
    },
    enderecoEntrega() {
      const f = this.enderecoForm;
      const parts = [
        [f.rua, f.numero].filter(Boolean).join(', ').trim(),
        f.complemento,
        f.bairro,
        [f.cidade, (f.uf || '').toUpperCase()].filter(Boolean).join(' - ').trim(),
        f.cep ? `CEP ${f.cep}` : ''
      ].filter(Boolean);
      return parts.join(' · ');
    },
    enderecoValido() {
      const f = this.enderecoForm;
      return Boolean(
        f.rua && f.numero && f.bairro && f.cidade && f.uf && f.cep
      );
    },
    cartTotal() {
      return this.localCart.reduce((total, item) => total + (item.price * item.quantity), 0);
    }
  },
  created() {
    if (this.cart) {
      this.localCart = JSON.parse(this.cart);
    }
    // Preenche com o endereco do usuario se existir (best-effort).
    try {
      const endereco = (this.user && this.user.endereco) ? String(this.user.endereco) : '';
      if (endereco) {
        // Nao tenta "parsear" string livre; apenas coloca como rua quando vier pronto.
        this.enderecoForm.rua = endereco;
      }
    } catch (e) {}
  },
  methods: {
    setMetodo(m) {
      this.metodo = m;
    },

    async finalizar() {
      if (!this.user) {
        alert('Você precisa estar logado para finalizar o pedido.');
        this.$router.push('/login');
        return;
      }
      if (!this.enderecoValido) {
        alert('Preencha os dados de entrega (campos obrigatorios).');
        return;
      }
      this.loading = true;
      try {
        const pedidoRequest = {
          usuarioId: this.user.id,
          itens: this.localCart.map(item => ({
            produtoId: item.id,
            quantidade: item.quantity || 1
          })),
          metodoPagamento: this.metodo,
          enderecoEntrega: this.enderecoEntrega,
          observacoes: '',
          subtotal: this.cartTotal,
          valorFrete: 0.0,
          totalFinal: this.cartTotal
        };

        const res = await OrderService.createOrder(pedidoRequest);

        // Salva o identificador do pedido para a tela de sucesso (Mercado Pago redireciona de volta).
        try {
          if (res && res.id) localStorage.setItem('ultimoPedidoId', String(res.id));
          if (res && res.codigoPedido) localStorage.setItem('ultimoCodigoPedido', String(res.codigoPedido));
        } catch (err) {
          // best-effort; nao deve impedir o checkout
        }

        if (res.linkPagamento) {
          window.location.href = res.linkPagamento;
        } else {
          // Fluxo simulado: segue dentro do sistema.
          if (this.$router) {
            this.$router.push('/sucesso-pagamento');
          } else {
            window.location.href = '/sucesso-pagamento';
          }
        }
      } catch (e) {
        console.error("Erro ao finalizar o pedido:", e);
        const errorMessage = e.response?.data?.message || "Erro ao conectar com o servidor Java.";
        alert(errorMessage);
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>
