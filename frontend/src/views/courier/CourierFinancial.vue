<template>
  <div class="courier-financial animate__animated animate__fadeIn">
    <div class="row g-4">
      <div class="col-lg-5">
        <div class="card border-0 shadow-sm rounded-4 h-100">
          <div class="card-body p-4">
            <h5 class="fw-bold mb-3"><i class="fas fa-qrcode me-2 text-primary"></i>Configuração PIX</h5>
            <p class="text-muted small">Insira sua chave PIX para receber os pagamentos das entregas.</p>
            
            <div v-if="loading" class="text-center py-4">
              <div class="spinner-border text-primary"></div>
            </div>
            <form v-else @submit.prevent="salvarPix">
              <div class="mb-3">
                <label class="form-label small fw-bold text-uppercase">Tipo de Chave</label>
                <select v-model="pix.tipo" class="form-select border-2" required>
                  <option value="CPF">CPF</option>
                  <option value="EMAIL">E-mail</option>
                  <option value="TELEFONE">Telefone</option>
                  <option value="ALEATORIA">Chave Aleatória</option>
                </select>
              </div>
              <div class="mb-4">
                <label class="form-label small fw-bold text-uppercase">Sua Chave</label>
                <input v-model="pix.chave" type="text" class="form-control form-control-lg border-2" placeholder="Ex: 000.000.000-00" required>
              </div>
              <button class="btn btn-primary w-100 fw-bold py-3 shadow-sm" :disabled="salvando">
                <span v-if="salvando" class="spinner-border spinner-border-sm me-2"></span>
                Salvar Dados
              </button>
            </form>
          </div>
        </div>
      </div>

      <div class="col-lg-7">
        <div class="card border-0 shadow-sm rounded-4 h-100">
          <div class="card-body p-4">
            <h5 class="fw-bold mb-3">Extrato de Repasses</h5>
            <div class="table-responsive">
              <table class="table table-hover align-middle">
                <thead>
                  <tr class="text-muted small">
                    <th>Data</th>
                    <th>Referência</th>
                    <th>Valor</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="p in pagamentos" :key="p.id">
                    <td class="small">{{ formatarData(p.dataPagamento || p.dataCriacao) }}</td>
                    <td class="fw-semibold">{{ p.referenciaPeriodo }}</td>
                    <td class="text-success fw-bold">R$ {{ p.valorLiquido.toFixed(2) }}</td>
                    <td>
                      <span class="badge" :class="p.status === 'PAGO' ? 'bg-success-subtle text-success border border-success' : 'bg-warning-subtle text-warning border border-warning'">
                        {{ p.status }}
                      </span>
                    </td>
                  </tr>
                  <tr v-if="pagamentos.length === 0">
                    <td colspan="4" class="text-center py-5 text-muted">Ainda não há pagamentos registrados.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'CourierFinancial',
  data() {
    return {
      loading: true,
      salvando: false,
      motoboyId: null,
      pix: { tipo: 'CPF', chave: '' },
      pagamentos: []
    };
  },
  async mounted() {
    await this.carregar();
  },
  methods: {
    async carregar() {
      this.loading = true;
      try {
        const user = JSON.parse(localStorage.getItem('user'));
        const motoboys = await api.get('/motoboys');
        const motoboy = motoboys.data.find(m => m.usuario?.id === user.id);
        
        if (motoboy) {
          this.motoboyId = motoboy.id;
          this.pix.chave = motoboy.chavePix || '';
          this.pix.tipo = motoboy.tipoChavePix || 'CPF';
          
          const resP = await api.get(`/pagamentos/motoboy/${motoboy.id}`);
          this.pagamentos = resP.data;
        }
      } catch (err) {
        console.error(err);
      } finally {
        this.loading = false;
      }
    },
    async salvarPix() {
      this.salvando = true;
      try {
        await api.patch(`/motoboys/${this.motoboyId}/pix`, {
          chavePix: this.pix.chave,
          tipoChavePix: this.pix.tipo
        });
        alert('Dados PIX atualizados!');
      } catch (err) {
        alert('Erro ao salvar.');
      } finally {
        this.salvando = false;
      }
    },
    formatarData(d) {
      if (!d) return '—';
      return new Date(d).toLocaleDateString('pt-BR');
    }
  }
};
</script>
