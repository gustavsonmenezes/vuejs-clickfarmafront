<template>
<<<<<<< main
  <div class="checkout-page">
    <div class="container py-lg-5 py-4">
      
      <div class="mb-5 fade-in-up">
        <span class="section-eyebrow">Finalização</span>
        <h1 class="section-title">Finalizar <em>Pedido</em></h1>
=======
  <div class="container mt-5 mb-5">
    <div class="row">
      <div class="col-md-8">
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
>>>>>>> main
      </div>

      <div class="row g-4">
        <!-- Esquerda: Formulários -->
        <div class="col-lg-8 fade-in-up">
          
          <!-- Seção de Entrega -->
          <div class="cf-checkout-card mb-4">
            <div class="card-header-cf">
               <i class="fa-solid fa-truck-fast"></i>
               <span>Opções de Entrega</span>
            </div>
            <div class="p-4">
              <label class="form-label">Endereço de Entrega</label>
              <div class="input-group-cf">
                <i class="fa-solid fa-location-dot"></i>
                <input type="text" v-model="endereco" class="form-control ps-5" placeholder="Digite seu endereço completo">
              </div>
              <p class="small text-muted mt-2">
                <i class="fa-solid fa-circle-info me-1"></i>
                Entregamos em todo o Brasil. O prazo varia conforme sua região.
              </p>
            </div>
          </div>

          <!-- Seção de Pagamento -->
          <div class="cf-checkout-card">
            <div class="card-header-cf">
               <i class="fa-solid fa-credit-card"></i>
               <span>Método de Pagamento</span>
            </div>
            <div class="p-4">
              <PaymentMethod @update-method="setMetodo" />
            </div>
          </div>

        </div>

        <!-- Direita: Resumo Fixo -->
        <div class="col-lg-4 fade-in-up" style="animation-delay: 0.1s">
          <div class="sticky-top" style="top: 100px;">
            <div class="cf-summary-card">
              <h4 class="summary-title mb-4">Resumo do Pedido</h4>
              
              <div class="summary-items mb-4">
                <div v-for="item in localCart" :key="item.id" class="summary-item mb-2">
                  <span class="item-name">{{ item.name }} <small>(x{{ item.quantity }})</small></span>
                  <span class="item-price">R$ {{ (item.price * item.quantity).toFixed(2).replace('.', ',') }}</span>
                </div>
              </div>

              <div class="cf-divider mb-4"></div>

              <div class="total-box d-flex justify-content-between align-items-end mb-4">
                <div class="total-label-wrap">
                  <span class="total-eyebrow">Total a pagar</span>
                  <h4 class="total-label mb-0">Total Final</h4>
                </div>
                <h4 class="total-amount mb-0">R$ {{ cartTotal.toFixed(2).replace('.', ',') }}</h4>
              </div>

              <button @click="finalizar" :disabled="loading" class="btn btn-primary btn-lg w-100 py-3">
                <i v-if="loading" class="fa-solid fa-spinner fa-spin me-2"></i>
                <i v-else class="fa-solid fa-lock me-2"></i>
                {{ loading ? 'Processando...' : 'Confirmar Pedido' }}
              </button>

              <div class="secure-info mt-4">
                <div class="d-flex align-items-center justify-content-center gap-2 text-muted small">
                  <i class="fa-solid fa-shield-check text-green"></i>
                  Ambiente Seguro de Pagamento
                </div>
              </div>
            </div>
          </div>
<<<<<<< main
=======
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
>>>>>>> main
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
      try {
        this.localCart = JSON.parse(this.cart);
      } catch(e) {
        this.localCart = [];
      }
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
    setMetodo(m) { this.metodo = m; },
    async finalizar() {
      if (!this.enderecoValido) {
        alert('Preencha os dados de entrega (campos obrigatorios).');
        return;
      }
      this.loading = true;
      try {
        const pedidoRequest = {
          usuarioId: this.user ? this.user.id : 1,
          itens: this.localCart.map(item => ({ produtoId: item.id, quantidade: item.quantity || 1 })),
          metodoPagamento: this.metodo,
          enderecoEntrega: this.enderecoEntrega,
          observacoes: '',
          subtotal: this.cartTotal,
          valorFrete: 0.0,
          totalFinal: this.cartTotal
        };

        const res = await OrderService.createOrder(pedidoRequest);
<<<<<<< main
        if (res.linkPagamento) {
          window.location.href = res.linkPagamento;
        } else {
          alert("Erro: Link de pagamento não gerado.");
=======

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
>>>>>>> main
        }
      } catch (e) {
        console.error(e);
        alert("Erro ao finalizar pedido.");
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>
<<<<<<< main

<style scoped>
.checkout-page { background: var(--cf-white); min-height: 90vh; }

.cf-checkout-card {
  background: var(--cf-white);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-xl);
  overflow: hidden;
}
.card-header-cf {
  background: var(--cf-green-xlight);
  padding: 1.25rem 2rem;
  border-bottom: 1px solid var(--cf-border);
  color: var(--cf-green);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.input-group-cf { position: relative; }
.input-group-cf i { position: absolute; left: 1.25rem; top: 50%; transform: translateY(-50%); color: var(--cf-text-faint); }

/* SUMMARY CARD */
.cf-summary-card {
  background: var(--cf-ivory);
  border: 1px solid var(--cf-border);
  border-radius: var(--cf-r-xl);
  padding: 2.5rem 2rem;
}
.summary-title { font-family: var(--cf-sans); font-size: 1.6rem; font-weight: 600; color: var(--cf-text-dark); }
.summary-item { display: flex; justify-content: space-between; gap: 1rem; }
.item-name { font-size: 0.85rem; color: var(--cf-text-mid); }
.item-price { font-size: 0.85rem; font-weight: 500; font-family: var(--cf-sans); color: var(--cf-text-dark); }

.total-eyebrow { font-size: 0.6rem; text-transform: uppercase; letter-spacing: 0.1em; color: var(--cf-gold); font-weight: 600; display: block; }
.total-label { font-family: var(--cf-sans); font-size: 1.25rem; font-weight: 600; color: var(--cf-text-dark); }
.total-amount { font-family: var(--cf-sans); font-size: 2rem; font-weight: 600; color: var(--cf-green); }

.text-green { color: var(--cf-green); }
</style>
=======
>>>>>>> main
