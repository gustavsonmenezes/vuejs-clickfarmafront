<template>
  <div class="pharmacy-financial fade-in-up">
    <div class="row g-4">
      <!-- Configuração de PIX -->
      <div class="col-lg-5">
        <div class="card cf-card-premium h-100">
          <div class="card-header bg-white border-0 pt-4">
            <h5 class="mb-0 fw-bold"><i class="fas fa-qrcode me-2 text-warning"></i>Minha Chave PIX</h5>
            <p class="text-muted small mt-1">Configure onde deseja receber seus repasses semanais.</p>
          </div>
          <div class="card-body">
            <div v-if="loadingPix" class="text-center py-4">
              <div class="spinner-border text-warning"></div>
            </div>
            <form v-else @submit.prevent="salvarPix">
              <div class="mb-3">
                <label class="form-label fw-bold">Tipo de Chave</label>
                <select v-model="pix.tipo" class="form-select" required>
                  <option value="CNPJ">CNPJ</option>
                  <option value="CPF">CPF</option>
                  <option value="EMAIL">E-mail</option>
                  <option value="TELEFONE">Telefone</option>
                  <option value="ALEATORIA">Chave Aleatória</option>
                </select>
              </div>
              <div class="mb-4">
                <label class="form-label fw-bold">Chave PIX</label>
                <input v-model="pix.chave" type="text" class="form-control form-control-lg" 
                       placeholder="Insira sua chave aqui..." required>
                <div class="form-text">Certifique-se de que a chave está correta para evitar atrasos.</div>
              </div>
              <button type="submit" class="btn btn-warning w-100 fw-bold py-3" :disabled="salvandoPix">
                {{ salvandoPix ? 'Salvando...' : 'Atualizar Dados de Recebimento' }}
              </button>
            </form>

            <div class="alert alert-info mt-4 mb-0">
              <i class="fas fa-info-circle me-2"></i>
              <small>Os repasses são gerados automaticamente pelo sistema e pagos manualmente pelo administrador ClickFarma.</small>
            </div>
          </div>
        </div>
      </div>

      <!-- Histórico de Repasses -->
      <div class="col-lg-7">
        <div class="card cf-card-premium h-100">
          <div class="card-header bg-white border-0 pt-4">
            <h5 class="mb-0 fw-bold"><i class="fas fa-history me-2 text-primary"></i>Histórico de Recebimentos</h5>
          </div>
          <div class="card-body p-0">
            <div v-if="loadingHistorico" class="text-center py-5">
              <div class="spinner-border text-primary"></div>
            </div>
            <div v-else class="table-responsive">
              <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                  <tr>
                    <th>Período</th>
                    <th>Valor Líquido</th>
                    <th>Status</th>
                    <th>Data</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="pg in pagamentos" :key="pg.id">
                    <td><span class="fw-bold">{{ formatarPeriodo(pg.referenciaPeriodo) }}</span></td>
                    <td class="text-success fw-bold">R$ {{ pg.valorLiquido.toFixed(2) }}</td>
                    <td>
                      <span class="badge" :class="pg.status === 'PAGO' ? 'bg-success' : 'bg-warning text-dark'">
                        {{ pg.status }}
                      </span>
                    </td>
                    <td class="small text-muted">{{ formatarData(pg.dataPagamento || pg.dataCriacao) }}</td>
                  </tr>
                  <tr v-if="pagamentos.length === 0">
                    <td colspan="4" class="text-center py-5 text-muted">
                      Nenhum repasse registrado ainda.
                    </td>
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
import farmaciasService from '@/services/farmaciasService';

export default {
  name: 'PharmacyFinancial',
  data() {
    return {
      pix: { tipo: 'CNPJ', chave: '' },
      pagamentos: [],
      farmaciaId: null,
      loadingPix: true,
      salvandoPix: false,
      loadingHistorico: true
    }
  },
  async mounted() {
    await this.carregarDados();
  },
  methods: {
    async carregarDados() {
      try {
        const user = JSON.parse(localStorage.getItem('user'));
        const farmacias = await farmaciasService.listarTodas();
        const farmacia = farmacias.data.find(f => f.email === user.email);
        
        if (farmacia) {
          this.farmaciaId = farmacia.id;
          this.pix.chave = farmacia.chavePix || '';
          this.pix.tipo = farmacia.tipoChavePix || 'CNPJ';
          this.loadingPix = false;

          const resP = await api.get(`/pagamentos/farmacia/${this.farmaciaId}`);
          this.pagamentos = resP.data;
        }
      } catch (err) {
        console.error(err);
      } finally {
        this.loadingPix = false;
        this.loadingHistorico = false;
      }
    },
    async salvarPix() {
      this.salvandoPix = true;
      try {
        await api.patch(`/farmacias/${this.farmaciaId}/pix`, {
          chavePix: this.pix.chave,
          tipoChavePix: this.pix.tipo
        });
        alert('Dados PIX atualizados com sucesso!');
      } catch (err) {
        alert('Erro ao atualizar PIX');
      } finally {
        this.salvandoPix = false;
      }
    },
    formatarPeriodo(p) {
      if (!p) return '—';
      const [ano, mes] = p.split('-');
      const meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
      return `${meses[parseInt(mes)-1]} / ${ano}`;
    },
    formatarData(d) {
      if (!d) return '—';
      return new Date(d).toLocaleDateString('pt-BR');
    }
  }
}
</script>

<style scoped>
.cf-card-premium { border-radius: 16px; border: 1px solid rgba(0,0,0,0.05); }
.table th { font-size: 0.75rem; text-transform: uppercase; color: #999; }
</style>
