<template>
  <div class="saldo-page">
    <div class="saldo-card">
      <p class="saldo-label">Saldo disponível</p>
      <p class="saldo-value">R$ {{ formatMoney(saldo) }}</p>
    </div>

    <div class="resumo-card">
      <h3>Resumo</h3>
      <div class="resumo-row">
        <span>Total de entregas</span>
        <span class="resumo-value">{{ totalEntregas }}</span>
      </div>
      <div class="resumo-row">
        <span>Entregas este mês</span>
        <span class="resumo-value">{{ entregasMes }}</span>
      </div>
      <div class="resumo-row">
        <span>Ganho este mês</span>
        <span class="resumo-value">R$ {{ formatMoney(ganhoMes) }}</span>
      </div>
    </div>

    <div class="acoes-card">
      <button class="btn-saque" @click="solicitarSaque">
        💸 Solicitar saque
      </button>
      <p class="pix-info" v-if="chavePix">
        Chave PIX: <strong>{{ chavePix }}</strong>
      </p>
        <p v-else class="pix-info sem-pix">
        Nenhuma chave PIX cadastrada
      </p>
    </div>

    <div v-if="saqueMsg" class="msg-box">{{ saqueMsg }}</div>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'

export default {
  name: 'EntregadorSaldo',
  data() {
    return { entregas: [], saqueMsg: '' }
  },
  async created() {
    try {
      const entregador = entregadorService.getEntregador()
      if (entregador) this.entregas = await entregadorService.getHistorico(entregador.id)
    } catch (err) { console.error(err) }
  },
  computed: {
    totalEntregas() { return this.entregas.length },
    entregasMes() {
      const now = new Date()
      return this.entregas.filter(e => {
        const d = new Date(e.entregueEm || e.criadoEm)
        return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
      }).length
    },
    saldo() {
      return this.entregas.reduce((acc, e) => acc + Number(e.taxaEntrega || 0), 0)
    },
    ganhoMes() {
      const now = new Date()
      return this.entregas
        .filter(e => {
          const d = new Date(e.entregueEm || e.criadoEm)
          return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
        })
        .reduce((acc, e) => acc + Number(e.taxaEntrega || 0), 0)
    },
    chavePix() {
      const e = entregadorService.getEntregador()
      return e?.chavePix || ''
    }
  },
  methods: {
    formatMoney(v) { return Number(v || 0).toFixed(2).replace('.', ',') },
    solicitarSaque() {
      this.saqueMsg = 'Função de saque em breve! Entre em contato com o suporte.'
    }
  }
}
</script>

<style scoped>
.saldo-page { padding-top: 4px; }
.saldo-card {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 20px; padding: 30px; text-align: center; color: white; margin-bottom: 16px;
}
.saldo-label { font-size: 14px; opacity: 0.9; margin: 0 0 8px; }
.saldo-value { font-size: 36px; font-weight: 700; margin: 0; }
.resumo-card { background: white; border-radius: 14px; padding: 16px; margin-bottom: 12px; }
.resumo-card h3 { margin: 0 0 12px; font-size: 15px; color: #333; }
.resumo-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 13px; color: #555; }
.resumo-value { font-weight: 600; color: #333; }
.acoes-card { background: white; border-radius: 14px; padding: 16px; text-align: center; margin-bottom: 12px; }
.btn-saque {
  padding: 14px 30px; border: none; border-radius: 12px; font-size: 15px;
  font-weight: 600; cursor: pointer; background: #2ecc71; color: white;
  margin-bottom: 12px;
}
.pix-info { font-size: 13px; color: #555; margin: 0; }
.sem-pix { color: #e74c3c; }
.msg-box {
  background: #fef3cd; color: #856404; padding: 12px 16px; border-radius: 10px;
  font-size: 13px; text-align: center;
}
</style>
