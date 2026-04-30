<template>
  <div class="courier-dashboard animate__animated animate__fadeIn">
    <div class="row g-4 mb-4">
      <div class="col-md-4">
        <div class="stats-card">
          <div class="icon-wrap bg-primary-subtle text-primary"><i class="fas fa-route"></i></div>
          <div class="details">
            <span class="label">Entregas Realizadas</span>
            <div class="value">{{ stats.totalEntregas }}</div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="stats-card">
          <div class="icon-wrap bg-success-subtle text-success"><i class="fas fa-hand-holding-usd"></i></div>
          <div class="details">
            <span class="label">Total a Receber</span>
            <div class="value text-success">R$ {{ stats.totalAReceber.toFixed(2) }}</div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="stats-card">
          <div class="icon-wrap bg-warning-subtle text-warning"><i class="fas fa-star"></i></div>
          <div class="details">
            <span class="label">Avaliação Média</span>
            <div class="value">4.9 <small class="fs-6 text-muted">/ 5</small></div>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm rounded-4 overflow-hidden mb-4">
      <div class="card-header bg-white py-3 border-0">
        <h5 class="mb-0 fw-bold">Entregas Disponíveis</h5>
      </div>
      <div class="card-body p-0">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary"></div>
        </div>
        <div v-else-if="entregasDisponiveis.length === 0" class="text-center py-5 text-muted">
          <i class="fas fa-box-open fa-3x mb-3 opacity-25"></i>
          <p>Nenhuma entrega disponível na sua região no momento.</p>
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="bg-light">
              <tr>
                <th>Farmácia</th>
                <th>Destino</th>
                <th>Valor Frete</th>
                <th class="text-center">Ação</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in entregasDisponiveis" :key="order.id">
                <td>
                  <div class="fw-bold">{{ order.farmaciaNome }}</div>
                  <div class="small text-muted">{{ order.farmaciaEndereco }}</div>
                </td>
                <td>
                  <div class="small fw-semibold">{{ order.enderecoEntrega }}</div>
                </td>
                <td class="fw-bold text-success">R$ 15.00</td>
                <td class="text-center">
                  <button class="btn btn-primary btn-sm px-3 rounded-pill fw-bold" @click="aceitarEntrega(order.id)">
                    Aceitar
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/services/api';

export default {
  name: 'CourierDashboard',
  data() {
    return {
      loading: true,
      stats: { totalEntregas: 0, totalAReceber: 0 },
      entregasDisponiveis: []
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
          // Em um sistema real, buscaríamos pedidos com status 'PAGO' que não têm motoboy atribuído
          // Aqui vamos simular alguns dados para visualização
          const res = await api.get('/pedidos');
          this.entregasDisponiveis = res.data.filter(p => p.status === 'PAGO').slice(0, 3);
          
          this.stats.totalEntregas = 12; // Mock
          this.stats.totalAReceber = 180.00; // Mock
        }
      } catch (err) {
        console.error(err);
      } finally {
        this.loading = false;
      }
    },
    aceitarEntrega(id) {
      alert('Entrega aceita! Dirija-se à farmácia.');
      // Lógica real de atualizar status do pedido...
    }
  }
};
</script>

<style scoped>
.stats-card { background: #fff; padding: 1.5rem; border-radius: 20px; display: flex; align-items: center; gap: 1.25rem; box-shadow: 0 4px 20px rgba(0,0,0,0.04); }
.icon-wrap { width: 54px; height: 54px; border-radius: 16px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; }
.label { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: #64748b; font-weight: 600; }
.value { font-size: 1.5rem; font-weight: 800; color: #1e293b; line-height: 1.2; }
</style>
