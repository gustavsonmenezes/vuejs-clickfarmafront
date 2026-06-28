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
                <h6 class="mb-0 fw-bold">{{ farmaciaNome }}</h6>
                <small class="text-muted" v-if="farmaciaFrete > 0">Frete: R$ {{ farmaciaFrete.toFixed(2) }}</small>
                <small class="text-muted" v-else>Frete incluso no preço</small>
              </div>
            </div>
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
                @blur="buscarCep"
              >
              <small class="text-muted">Campos com * são obrigatórios.</small>
            </div>
          </div>
        </div>
        <!-- Opção de Entrega -->
        <div class="card shadow-sm border-0 p-4 mb-4">
          <h4 class="mb-4 font-weight-bold">🚚 Opção de Entrega</h4>
          <div class="row g-3">
            <div class="col-md-6">
              <div
                class="delivery-type-card"
                :class="{ 'selected': deliveryType === 'standard' }"
                @click="deliveryType = 'standard'"
              >
                <div class="form-check">
                  <input
                    type="radio"
                    id="delivery-standard"
                    value="standard"
                    v-model="deliveryType"
                    class="form-check-input"
                  >
                  <label class="form-check-label" for="delivery-standard">
                    <h6 class="mb-1">Entrega Padrão</h6>
                    <p class="mb-0 text-muted small">2-3 dias úteis · Grátis acima de R$ 300</p>
                  </label>
                </div>
              </div>
            </div>
            <div class="col-md-6">
              <div
                class="delivery-type-card"
                :class="{ 'selected': deliveryType === 'uber_direct' }"
                @click="deliveryType = 'uber_direct'"
              >
                <div class="form-check">
                  <input
                    type="radio"
                    id="delivery-uber"
                    value="uber_direct"
                    v-model="deliveryType"
                    class="form-check-input"
                  >
                  <label class="form-check-label" for="delivery-uber">
                    <h6 class="mb-1">
                      Entrega Rápida
                      <span class="badge bg-dark ms-1" style="font-size: 0.6rem;">UBER DIRECT</span>
                    </h6>
                    <p class="mb-0 text-muted small">Entregue por parceiros Uber · Rastreio em tempo real</p>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- Calcular Frete Uber Direct -->
          <div v-if="deliveryType === 'uber_direct'" class="mt-3 p-3 bg-light rounded">
            <div class="d-flex align-items-center justify-content-between">
              <div>
                <strong>Frete via Uber Direct</strong>
                <p class="mb-0 text-muted small">Saiba o valor e tempo estimado para entrega</p>
              </div>
              <button
                class="btn btn-dark"
                :disabled="!enderecoValido || uberLoading"
                @click="calcularFreteUber"
              >
                <span v-if="uberLoading" class="spinner-border spinner-border-sm me-1"></span>
                {{ uberLoading ? 'Calculando...' : 'Calcular Frete' }}
              </button>
            </div>

            <!-- Resultado da cotação -->
            <div v-if="uberQuote" class="mt-3 p-3 bg-white rounded border">
              <div class="row text-center">
                <div class="col-6 border-end">
                  <small class="text-muted d-block">Valor do Frete</small>
                  <strong class="text-primary fs-5">
                    R$ {{ uberQuote.delivery_fee.toFixed(2) }}
                  </strong>
                </div>
                <div class="col-6">
                  <small class="text-muted d-block">Previsão de Entrega</small>
                  <strong class="text-success fs-5">
                    <i class="fas fa-clock me-1"></i>{{ uberQuote.estimated_time }}
                  </strong>
                </div>
              </div>
              <div class="mt-2 text-center">
                <small class="text-muted">
                  <i class="fas fa-info-circle me-1"></i>
                  Entregador vai até a farmácia retirar o medicamento e leva até você
                </small>
              </div>
            </div>

            <div v-if="!enderecoValido && deliveryType === 'uber_direct'" class="mt-2">
              <small class="text-danger">
                Preencha o endereço de entrega acima para calcular o frete
              </small>
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
            <div class="d-flex justify-content-between mb-2">
              <span>Subtotal</span>
              <span>R$ {{ cartTotal.toFixed(2) }}</span>
            </div>
            <div v-if="deliveryType === 'uber_direct' && uberQuote" class="d-flex justify-content-between mb-2">
              <span>Frete (Uber Direct)</span>
              <span class="text-primary">R$ {{ uberQuote.delivery_fee.toFixed(2) }}</span>
            </div>
            <div v-else class="d-flex justify-content-between mb-2">
              <span>Frete</span>
              <span class="text-success">Grátis</span>
            </div>

            <!-- Cupom -->
            <div class="mb-3">
              <label class="form-label small">Cupom de desconto</label>
              <div class="input-group">
                <input v-model="cupomCodigo" class="form-control form-control-sm" placeholder="Insira o código" :disabled="!!cupomAplicado" />
                <button v-if="!cupomAplicado" class="btn btn-sm btn-outline-primary" type="button" @click="validarCupom" :disabled="!cupomCodigo || validandoCupom">
                  {{ validandoCupom ? '...' : 'Aplicar' }}
                </button>
                <button v-else class="btn btn-sm btn-outline-danger" type="button" @click="removerCupom">
                  <i class="fas fa-times"></i>
                </button>
              </div>
              <small v-if="cupomErro" class="text-danger">{{ cupomErro }}</small>
              <small v-if="cupomAplicado" class="text-success">{{ cupomAplicado }}</small>
            </div>

            <div v-if="cupomDesconto > 0" class="d-flex justify-content-between mb-2">
              <span class="text-success">Desconto cupom</span>
              <span class="text-success">− R$ {{ cupomDesconto.toFixed(2) }}</span>
            </div>
            <hr>
            <div class="d-flex justify-content-between mb-4 h5 font-weight-bold">
              <span>Total</span>
              <span class="text-primary">R$ {{ totalComFrete.toFixed(2) }}</span>
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
import { mapState, mapGetters } from 'vuex';
import { OrderService } from '@/services/orderService.js';
import { UberDirectService } from '@/services/uberDirectService';
import PaymentMethod from '@/components/checkout/PaymentMethod.vue';
import cepService from '@/services/cepService';
import cuponsService from '@/services/cuponsService';

export default {
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
      deliveryType: 'standard',
      uberQuote: null,
      uberLoading: false,
      uberDeliveryId: null,
      cupomCodigo: '',
      cupomAplicado: null,
      cupomDesconto: 0,
      cupomErro: '',
      validandoCupom: false,
      cupomId: null
    };
  },
  computed: {
    ...mapState(['user', 'cart']),
    ...mapState(['selectedFarmacia']),
    farmaciaNome() {
      return this.selectedFarmacia?.nome || 'ClickFarma - Matriz';
    },
    farmaciaFrete() {
      return this.selectedFarmacia?.valorFrete || 0;
    },
    googleMapsLink() {
      return 'https://www.google.com/maps/search/?api=0&query=ClickFarma+Recife+PE';
    },
    ...mapGetters(['cartItemsCount', 'cartTotal']),
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
    enderecoCompleto() {
      const f = this.enderecoForm;
      return `${f.rua}, ${f.numero}${f.complemento ? ` - ${f.complemento}` : ''}, ${f.bairro}, ${f.cidade} - ${f.uf}, CEP ${f.cep}`;
    },
    valorFrete() {
      if (this.deliveryType === 'uber_direct' && this.uberQuote) {
        return this.uberQuote.delivery_fee || 0;
      }
      return 0;
    },
    totalComFrete() {
      return Math.max(0, this.cartTotal + this.valorFrete - this.cupomDesconto);
    }
  },
  created() {
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
    async buscarCep() {
      const cep = this.enderecoForm.cep.replace(/\D/g, '');
      if (cep.length !== 8) return;
      try {
        const res = await cepService.buscar(cep);
        const data = res.data;
        if (data.erro) return;
        if (data.logradouro) this.enderecoForm.rua = data.logradouro;
        if (data.bairro) this.enderecoForm.bairro = data.bairro;
        if (data.cidade) this.enderecoForm.cidade = data.cidade;
        if (data.estado) this.enderecoForm.uf = data.estado;
      } catch (e) {
        console.error('Erro ao buscar CEP:', e);
      }
    },
    setMetodo(m) {
      this.metodo = m;
    },

    async calcularFreteUber() {
      if (!this.enderecoValido) return;
      this.uberLoading = true;
      this.uberQuote = null;
      try {
        const quote = await UberDirectService.getQuote(this.enderecoCompleto);
        this.uberQuote = quote;
      } catch (e) {
        console.error('Erro ao calcular frete Uber Direct:', e);
        alert('Não foi possível calcular o frete no momento. Tente novamente.');
      } finally {
        this.uberLoading = false;
      }
    },

    async criarEntregaUber(pedidoId, codigoPedido) {
      try {
        const manifestItems = this.cart.map(item => ({
          name: item.name || item.nome,
          quantity: item.quantity || 1,
          size: 'small'
        }));

        const delivery = await UberDirectService.createDelivery(
          this.uberQuote.quote_id,
          {
            name: this.user?.name || this.user?.nome || 'Cliente',
            address: this.enderecoCompleto,
            phone: this.user?.telefone || this.user?.phone || ''
          },
          manifestItems,
          String(pedidoId)
        );

        this.uberDeliveryId = delivery.id;

        console.log('Entrega Uber Direct criada com sucesso:', delivery.id);
        return delivery;
      } catch (e) {
        console.error('Erro ao criar entrega Uber Direct:', e);
        throw e;
      }
    },

    async validarCupom() {
      if (!this.cupomCodigo) return;
      this.validandoCupom = true;
      this.cupomErro = '';
      try {
        const res = await cuponsService.validar(this.cupomCodigo, this.cartTotal);
        const data = res.data;
        if (data.valido) {
          this.cupomAplicado = data.mensagem;
          this.cupomDesconto = parseFloat(data.desconto);
          this.cupomId = data.cupomId;
        } else {
          this.cupomErro = data.mensagem;
        }
      } catch (e) {
        this.cupomErro = 'Erro ao validar cupom.';
      } finally { this.validandoCupom = false; }
    },
    removerCupom() {
      this.cupomCodigo = '';
      this.cupomAplicado = null;
      this.cupomDesconto = 0;
      this.cupomErro = '';
      this.cupomId = null;
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
          itens: this.cart.map(item => ({
            produtoId: item.id,
            quantidade: item.quantity || 1
          })),
          metodoPagamento: this.metodo,
          enderecoEntrega: this.enderecoEntrega,
          observacoes: '',
          subtotal: this.cartTotal,
          valorFrete: this.farmaciaFrete || this.valorFrete,
          totalFinal: this.totalComFrete,
          farmaciaId: this.selectedFarmacia?.id || null,
          cupomId: this.cupomId || null,
          tipoEntrega: this.deliveryType,
          uberQuoteId: this.uberQuote?.quote_id || null
        };

        const res = await OrderService.createOrder(pedidoRequest);

        if (this.deliveryType === 'uber_direct' && this.uberQuote && res?.id) {
          try {
            const delivery = await this.criarEntregaUber(res.id, res.codigoPedido);
            if (delivery) {
              localStorage.setItem('ultimoUberDeliveryId', delivery.id);
              if (delivery.tracking_url) {
                localStorage.setItem('ultimoUberTrackingUrl', delivery.tracking_url);
              }
            }
          } catch (err) {
            console.error('Erro ao criar entrega Uber Direct:', err);
          }
        }

        try {
          if (res && res.id) localStorage.setItem('ultimoPedidoId', String(res.id));
          if (res && res.codigoPedido) localStorage.setItem('ultimoCodigoPedido', String(res.codigoPedido));
        } catch (err) {
          // best-effort; nao deve impedir o checkout
        }

        if (res.pixQrCodeBase64) {
          sessionStorage.setItem('ultimoPixData', JSON.stringify({
            pedidoId: res.id,
            codigoPedido: res.codigoPedido,
            qrCodeBase64: res.pixQrCodeBase64,
            copiaECola: res.pixCopiaECola,
            expiracao: res.pixExpiracao
          }));
          this.$router.push({
            name: 'PixPayment',
            params: { pedidoId: String(res.id) }
          });
        } else if (res.linkPagamento) {
          window.location.href = res.linkPagamento;
        } else {
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

<style scoped>
.delivery-type-card {
  border: 2px solid #e9ecef;
  border-radius: 10px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.delivery-type-card:hover {
  border-color: #0d6efd;
  background: #f8f9fa;
}

.delivery-type-card.selected {
  border-color: #0d6efd;
  background: #f0f5ff;
}

.delivery-type-card .form-check-input:checked {
  background-color: #0d6efd;
  border-color: #0d6efd;
}
</style>
