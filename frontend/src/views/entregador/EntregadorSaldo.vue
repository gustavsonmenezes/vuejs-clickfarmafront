<template>
  <div class="saldo-page">
    <div class="saldo-card">
      <span class="saldo-label">
        <IconEntregador name="wallet" size="16" />
        Saldo disponível
      </span>
      <span class="saldo-valor">R$ {{ formatMoney(saldo) }}</span>
      <span class="saldo-entregas">{{ totalEntregas }} entregas realizadas</span>
    </div>

    <div class="acoes-card">
      <button class="btn-saque" @click="solicitarSaque" :disabled="saldo <= 0">
        <IconEntregador name="dollar" size="16" />
        {{ salvando ? 'Solicitando...' : 'Solicitar saque' }}
      </button>
      <div v-if="chavePix" class="pix-info">
        <span class="pix-label">
          <IconEntregador name="credit-card" size="14" />
          Chave PIX
        </span>
        <span class="pix-value">{{ chavePix }}</span>
      </div>
    </div>

    <div v-if="saqueMsg" :class="['msg', saqueMsg.tipo]">
      <IconEntregador :name="saqueMsg.tipo === 'sucesso' ? 'check' : 'x'" size="16" />
      {{ saqueMsg.texto }}
    </div>

    <div class="resumo-card">
      <h3>
        <IconEntregador name="trending-up" size="16" />
        Resumo financeiro
      </h3>
      <div class="resumo-row">
        <span>Total de entregas</span>
        <span class="resumo-valor">{{ totalEntregas }}</span>
      </div>
      <div class="resumo-row">
        <span>Entregas este mês</span>
        <span class="resumo-valor">{{ entregasMes }}</span>
      </div>
      <div class="resumo-row">
        <span>Ganho este mês</span>
        <span class="resumo-valor ganho">R$ {{ formatMoney(ganhoMes) }}</span>
      </div>
      <div class="resumo-row">
        <span>Ganho total</span>
        <span class="resumo-valor ganho">R$ {{ formatMoney(ganhoTotal) }}</span>
      </div>
    </div>

    <div v-if="corridas.length" class="extrato-card">
      <h3>
        <IconEntregador name="clock" size="16" />
        Últimas corridas
      </h3>
      <div v-for="c in corridas.slice(0, 20)" :key="c.id" class="extrato-item">
        <div class="extrato-left">
          <span class="extrato-pedido">#{{ c.entrega.codigoPedido }}</span>
          <span class="extrato-data">{{ formatDate(c.finalizadoEm || c.criadoEm) }}</span>
        </div>
        <span v-if="c.status === 'FINALIZADA'" class="extrato-valor positivo">
          + R$ {{ formatMoney(c.taxaFinal) }}
        </span>
        <span v-else class="extrato-valor cancelado">R$ {{ formatMoney(c.taxaFinal) }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'
import IconEntregador from '@/components/entregador/IconEntregador.vue'
import EntregadorMixin from '@/components/entregador/EntregadorMixin.js'

export default {
  name: 'EntregadorSaldo',
  components: { IconEntregador },
  mixins: [EntregadorMixin],
  data() {
    return {
      corridas: [],
      saqueMsg: null,
      chavePix: '',
      salvando: false
    }
  },
  computed: {
    totalEntregas() {
      return this.corridas.filter(c => c.status === 'FINALIZADA').length
    },
    entregasMes() {
      const now = new Date()
      const mes = now.getMonth()
      const ano = now.getFullYear()
      return this.corridas.filter(c => {
        if (c.status !== 'FINALIZADA') return false
        const d = new Date(c.finalizadoEm || c.criadoEm)
        return d.getMonth() === mes && d.getFullYear() === ano
      }).length
    },
    saldo() {
      return this.corridas
        .filter(c => c.status === 'FINALIZADA')
        .reduce((acc, c) => acc + Number(c.taxaFinal || 0), 0)
    },
    ganhoMes() {
      const now = new Date()
      const mes = now.getMonth()
      const ano = now.getFullYear()
      return this.corridas
        .filter(c => {
          if (c.status !== 'FINALIZADA') return false
          const d = new Date(c.finalizadoEm || c.criadoEm)
          return d.getMonth() === mes && d.getFullYear() === ano
        })
        .reduce((acc, c) => acc + Number(c.taxaFinal || 0), 0)
    },
    ganhoTotal() {
      return this.saldo
    }
  },
  async created() {
    await this.carregar()
  },
  methods: {
    async carregar() {
      try {
        const entregador = entregadorService.getEntregador()
        if (!entregador) return

        this.chavePix = entregador.chavePix || '-'
        const data = await entregadorService.getCorridasEntregador(entregador.id)
        this.corridas = data || []
      } catch (err) {
        this.$toast.error('Erro ao carregar saldo')
      }
    },
    async solicitarSaque() {
      if (this.saldo <= 0) return
      if (!this.chavePix || this.chavePix === '-') {
        this.saqueMsg = { tipo: 'erro', texto: 'Configure sua chave PIX no perfil antes de solicitar saque' }
        return
      }
      this.salvando = true
      try {
        const entregador = entregadorService.getEntregador()
        await entregadorService.solicitarSaque(entregador.id, this.saldo)
        this.saqueMsg = {
          tipo: 'sucesso',
          texto: `Saque de R$ ${this.formatMoney(this.saldo)} solicitado! O valor será transferido para sua chave PIX em até 2 dias úteis.`
        }
        this.$toast.success('Saque solicitado com sucesso!')
      } catch (err) {
        this.saqueMsg = {
          tipo: 'erro',
          texto: err.response?.data || 'Erro ao solicitar saque. Tente novamente.'
        }
        this.$toast.error(err.response?.data || 'Erro ao solicitar saque')
      } finally {
        this.salvando = false
      }
    },
    // formatMoney e formatDate vindos do mixin
  }
}
</script>

<style scoped>
.saldo-page { padding-top: 4px; }
.saldo-card {
  background: linear-gradient(135deg, var(--cf-ora-600), var(--cf-ora-700));
  border-radius: var(--cf-radius-xl);
  padding: 24px;
  text-align: center;
  color: white;
  margin-bottom: 16px;
}
.saldo-label {
  font-size: 13px;
  opacity: 0.8;
  display: block;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.saldo-valor {
  font-size: 36px;
  font-weight: 800;
  display: block;
  margin: 8px 0;
  letter-spacing: -0.02em;
}
.saldo-entregas { font-size: 12px; opacity: 0.7; }
.acoes-card { margin-bottom: 16px; }
.btn-saque {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: var(--cf-radius-lg);
  background: var(--cf-grn-600);
  color: white;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  font-family: var(--cf-font);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background var(--cf-transition), transform var(--cf-transition);
}
.btn-saque:hover:not(:disabled) {
  background: var(--cf-grn-700);
  transform: translateY(-1px);
}
.btn-saque:disabled {
  background: var(--cf-ntr-300);
  cursor: not-allowed;
}
.btn-saque:focus-visible {
  outline: 2px solid var(--cf-grn-500);
  outline-offset: 2px;
}
.pix-info {
  background: var(--cf-surface);
  border-radius: var(--cf-radius-lg);
  padding: 10px 14px;
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  align-items: center;
}
.pix-label {
  font-size: 12px;
  color: var(--cf-ntr-400);
  display: flex;
  align-items: center;
  gap: 4px;
}
.pix-value {
  font-size: 12px;
  color: var(--cf-ntr-700);
  font-weight: 600;
}
.msg {
  padding: 10px 14px;
  border-radius: var(--cf-radius-lg);
  font-size: 13px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.msg.sucesso { background: var(--cf-grn-50); color: var(--cf-grn-700); }
.msg.erro { background: var(--cf-red-50); color: var(--cf-red-600); }
.resumo-card, .extrato-card {
  background: var(--cf-surface);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 12px;
}
h3 {
  margin: 0 0 12px;
  font-size: 15px;
  color: var(--cf-ntr-800);
  display: flex;
  align-items: center;
  gap: 6px;
}
.resumo-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 13px;
  color: var(--cf-ntr-500);
}
.resumo-valor { font-weight: 600; }
.ganho { color: var(--cf-grn-600); font-weight: 700; }
.extrato-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--cf-ntr-100);
}
.extrato-item:last-child { border-bottom: none; }
.extrato-left { display: flex; flex-direction: column; }
.extrato-pedido { font-size: 13px; font-weight: 600; color: var(--cf-ntr-700); }
.extrato-data { font-size: 11px; color: var(--cf-ntr-400); }
.extrato-valor { font-size: 14px; font-weight: 700; }
.positivo { color: var(--cf-grn-600); }
.cancelado { color: var(--cf-ntr-300); }
</style>
