<template>
  <div
    class="corridas-page"
    @touchstart.passive="onTouchStart"
    @touchmove.passive="onTouchMove"
    @touchend.passive="onTouchEnd"
  >
    <div v-if="erro" class="error-banner">{{ erro }}</div>

    <div v-if="pulando" class="pull-indicator">
      <svg class="pull-spinner" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10" stroke-dasharray="32" stroke-dashoffset="32" />
      </svg>
      Atualizando...
    </div>
    <div v-else-if="pullDist > 10" class="pull-indicator" :style="{ opacity: pullDist / 80 }">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :style="{ transform: `rotate(${pullDist * 2}deg)` }">
        <polyline points="7 13 12 18 17 13" />
        <polyline points="12 18 12 6" />
      </svg>
      {{ pullDist >= 60 ? 'Solte para atualizar' : 'Puxe para atualizar' }}
    </div>

    <div class="tab-bar">
      <button :class="['tab-btn', { active: tab === 'disponiveis' }]" @click="tab = 'disponiveis'">
        <IconEntregador name="inbox" :size="16" />
        Disponíveis
        <span v-if="disponiveis.length" class="tab-badge">{{ disponiveis.length }}</span>
      </button>
      <button :class="['tab-btn', { active: tab === 'ativas' }]" @click="tab = 'ativas'">
        <IconEntregador name="truck" :size="16" />
        Em andamento
        <span v-if="ativas.length" class="tab-badge active-badge">{{ ativas.length }}</span>
      </button>
    </div>

    <!-- Skeleton loading -->
    <div v-if="carregando && disponiveis.length === 0 && ativas.length === 0" class="skeleton-list">
      <div v-for="n in 3" :key="n" class="skeleton-card">
        <div class="skeleton-line w-40"></div>
        <div class="skeleton-line w-80"></div>
        <div class="skeleton-line w-60"></div>
        <div class="skeleton-line w-30"></div>
      </div>
    </div>

    <!-- Disponiveis -->
    <div v-if="tab === 'disponiveis' && !carregando">
      <div v-if="disponiveis.length === 0" class="empty-state">
        <IconEntregador name="inbox" size="56" class="empty-icon" />
        <p class="empty-title">Nenhuma corrida disponível</p>
        <p class="empty-sub">As novas corridas aparecerão aqui automaticamente</p>
      </div>

      <div v-for="c in disponiveis" :key="c.id" class="corrida-card">
        <div class="card-header">
          <span class="pedido-num">#{{ c.entrega.codigoPedido }}</span>
          <span v-if="c.multiplicadorSurge > 1" class="surge-badge">
            <IconEntregador name="trending-up" size="12" />
            {{ ((c.multiplicadorSurge - 1) * 100).toFixed(0) }}% a mais
          </span>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="info-label">
              <IconEntregador name="map-pin" size="14" />
              Destino
            </span>
            <span class="info-value">{{ c.entrega.enderecoDestino }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">
              <IconEntregador name="user" size="14" />
              Cliente
            </span>
            <span class="info-value">{{ c.entrega.clienteNome }}</span>
          </div>
          <div class="info-row" v-if="c.distanciaKm">
            <span class="info-label">
              <IconEntregador name="map" size="14" />
              Distância
            </span>
            <span class="info-value">{{ Number(c.distanciaKm).toFixed(1) }} km</span>
          </div>
        </div>

        <div class="card-footer">
          <div class="valor-area">
            <span v-if="c.multiplicadorSurge > 1" class="taxa-original">
              R$ {{ formatMoney(c.taxaBase) }}
            </span>
            <span class="taxa-final">R$ {{ formatMoney(c.taxaFinal) }}</span>
          </div>
          <div class="acoes">
            <button class="btn-aceitar" @click="aceitar(c)">
              <IconEntregador name="check" size="16" />
              Aceitar
            </button>
            <button class="btn-recusar" @click="recusar(c)">
              <IconEntregador name="x" size="16" />
              Recusar
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Ativas -->
    <div v-if="tab === 'ativas' && !carregando">
      <div v-if="ativas.length === 0" class="empty-state">
        <IconEntregador name="truck" size="56" class="empty-icon" />
        <p class="empty-title">Nenhuma corrida ativa</p>
        <p class="empty-sub">Aceite uma corrida para começar</p>
      </div>

      <div
        v-for="c in ativas"
        :key="c.id"
        class="corrida-card ativa"
        @click="$router.push(`/entregador/corridas/${c.id}`)"
      >
        <div class="card-header">
          <span class="pedido-num">#{{ c.entrega.codigoPedido }}</span>
          <span :class="['status-badge', c.status.toLowerCase()]">{{ statusLabel(c.status) }}</span>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="info-label">
              <IconEntregador name="map-pin" size="14" />
              Destino
            </span>
            <span class="info-value">{{ c.entrega.enderecoDestino }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">
              <IconEntregador name="user" size="14" />
              Cliente
            </span>
            <span class="info-value">{{ c.entrega.clienteNome }}</span>
          </div>
          <div class="info-row" v-if="c.aceitoEm">
            <span class="info-label">
              <IconEntregador name="clock" size="14" />
              Aceito
            </span>
            <span class="info-value">{{ timeAgo(c.aceitoEm) }}</span>
          </div>
        </div>

        <div class="card-footer">
          <div class="valor-area">
            <span class="taxa-final">R$ {{ formatMoney(c.taxaFinal) }}</span>
          </div>
          <span class="ver-detalhes">
            Ver detalhes
            <IconEntregador name="arrow-left" size="14" style="transform: rotate(180deg)" />
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'
import IconEntregador from '@/components/entregador/IconEntregador.vue'
import EntregadorMixin from '@/components/entregador/EntregadorMixin.js'

export default {
  name: 'EntregadorCorridas',
  components: { IconEntregador },
  mixins: [EntregadorMixin],
  data() {
    return {
      tab: 'disponiveis',
      disponiveis: [],
      ativas: [],
      carregando: true,
      erro: '',
      pollTimer: null,
      pulando: false,
      pullDist: 0,
      touchStartY: 0
    }
  },
  async created() {
    await this.carregar()
    this.pollTimer = setInterval(() => this.carregar(), 10000)
  },
  beforeUnmount() {
    if (this.pollTimer) clearInterval(this.pollTimer)
  },
  methods: {
    async carregar() {
      try {
        this.erro = ''
        const entregador = entregadorService.getEntregador()
        if (!entregador) return

        const [disponiveis, corridas] = await Promise.all([
          entregadorService.getCorridasDisponiveis(entregador.latitude, entregador.longitude),
          entregadorService.getCorridasEntregador(entregador.id)
        ])
        this.disponiveis = disponiveis || []
        this.ativas = (corridas || []).filter(c =>
          c.status === 'ACEITA' || c.status === 'EM_ANDAMENTO'
        )
      } catch (err) {
        if (this.disponiveis.length === 0 && this.ativas.length === 0) {
          this.erro = 'Erro ao carregar corridas'
        } else {
          this.$toast.error('Erro ao atualizar corridas')
        }
      } finally {
        this.carregando = false
      }
    },

    async aceitar(corrida) {
      const entregador = entregadorService.getEntregador()
      if (!entregador) return
      try {
        await entregadorService.aceitarCorrida(corrida.id, entregador.id)
        await this.carregar()
        this.$toast.success('Corrida aceita com sucesso!')
      } catch (err) {
        this.$toast.error(err.response?.data || 'Erro ao aceitar corrida')
      }
    },

    async recusar(corrida) {
      try {
        await entregadorService.cancelarCorrida(corrida.id)
        await this.carregar()
        this.$toast.success('Corrida recusada')
      } catch (err) {
        this.$toast.error(err.response?.data || 'Erro ao recusar corrida')
      }
    },

    statusLabel(status) {
      const labels = { DISPONIVEL: 'Disponível', ACEITA: 'Aceita', EM_ANDAMENTO: 'Em andamento', FINALIZADA: 'Finalizada', CANCELADA: 'Cancelada' }
      return labels[status] || status
    },

    onTouchStart(e) {
      this.touchStartY = e.touches[0].clientY
    },
    onTouchMove(e) {
      if (this.carregando || this.pulando) return
      const scrollTop = this.$el?.scrollTop || 0
      if (scrollTop > 0) return
      const dy = e.touches[0].clientY - this.touchStartY
      if (dy > 0) {
        this.pullDist = Math.min(dy * 0.4, 80)
      }
    },
    onTouchEnd() {
      if (this.pullDist >= 60) {
        this.pulando = true
        this.pullDist = 0
        this.carregar().finally(() => {
          setTimeout(() => { this.pulando = false }, 500)
        })
      } else {
        this.pullDist = 0
      }
    }
  }
}
</script>

<style scoped>
.corridas-page { padding-top: 4px; }
.error-banner {
  background: var(--cf-red-50);
  color: var(--cf-red-600);
  padding: 10px 14px;
  border-radius: var(--cf-radius-lg);
  font-size: 13px;
  margin-bottom: 12px;
  border: 1px solid var(--cf-red-500);
}

/* ─── Skeleton ─── */
.skeleton-list { display: flex; flex-direction: column; gap: 12px; }
.skeleton-card {
  background: var(--cf-surface);
  border-radius: 14px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  box-shadow: var(--cf-shadow-sm);
}
.skeleton-line {
  height: 12px;
  background: linear-gradient(90deg, var(--cf-ntr-100) 25%, var(--cf-ntr-200) 50%, var(--cf-ntr-100) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 6px;
}
.skeleton-line.w-30 { width: 30%; }
.skeleton-line.w-40 { width: 40%; }
.skeleton-line.w-60 { width: 60%; }
.skeleton-line.w-80 { width: 80%; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ─── Tabs ─── */
.tab-bar { display: flex; gap: 8px; margin-bottom: 16px; }
.tab-btn {
  flex: 1;
  padding: 10px;
  border: 2px solid transparent;
  border-radius: var(--cf-radius-lg);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  background: var(--cf-ntr-100);
  color: var(--cf-ntr-500);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all var(--cf-transition);
  font-family: var(--cf-font);
}
.tab-btn:hover {
  background: var(--cf-ora-50);
  color: var(--cf-ora-600);
  border-color: var(--cf-ora-200);
}
.tab-btn.active {
  background: var(--cf-ora-600);
  color: white;
  border-color: var(--cf-ora-600);
}
.tab-btn.active:hover {
  background: var(--cf-ora-700);
  border-color: var(--cf-ora-700);
}
.tab-btn:focus-visible {
  outline: 2px solid var(--cf-ora-500);
  outline-offset: 2px;
}
.tab-badge {
  background: rgba(0,0,0,0.15);
  color: white;
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 10px;
  font-weight: 700;
}
.tab-btn.active .tab-badge { background: rgba(255,255,255,0.25); }
.active-badge { background: var(--cf-red-600); }

/* ─── Empty state ─── */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}
.empty-icon {
  color: var(--cf-ntr-300);
  margin-bottom: 12px;
}
.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--cf-ntr-700);
  margin: 0 0 4px;
}
.empty-sub {
  font-size: 13px;
  color: var(--cf-ntr-400);
  margin: 0;
}

/* ─── Cards ─── */
.corrida-card {
  background: var(--cf-surface);
  border-radius: 14px;
  padding: 14px;
  margin-bottom: 12px;
  box-shadow: var(--cf-shadow-sm);
  transition: box-shadow var(--cf-transition), transform var(--cf-transition);
}
.corrida-card:hover {
  box-shadow: var(--cf-shadow-md);
}
.corrida-card.ativa {
  border-left: 4px solid var(--cf-ora-500);
  cursor: pointer;
}
.corrida-card.ativa:hover {
  transform: translateY(-1px);
  box-shadow: var(--cf-shadow-lg);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.pedido-num {
  font-weight: 700;
  font-size: 15px;
  color: var(--cf-ntr-800);
}
.surge-badge {
  background: var(--cf-amb-50);
  color: var(--cf-amb-600);
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: var(--cf-radius-sm);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.status-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 8px;
}
.status-badge.aceita { background: var(--cf-grn-50); color: var(--cf-grn-700); }
.status-badge.em_andamento { background: var(--cf-blu-100); color: var(--cf-blu-700); }
.card-body { margin-bottom: 10px; }
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  gap: 8px;
}
.info-label {
  font-size: 12px;
  color: var(--cf-ntr-400);
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.info-value {
  font-size: 12px;
  color: var(--cf-ntr-700);
  font-weight: 500;
  text-align: right;
  max-width: 60%;
}
.card-footer {
  border-top: 1px solid var(--cf-ntr-100);
  padding-top: 10px;
}
.valor-area {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.taxa-original {
  font-size: 12px;
  color: var(--cf-ntr-300);
  text-decoration: line-through;
}
.taxa-final {
  font-size: 22px;
  font-weight: 800;
  color: var(--cf-grn-600);
}
.acoes { display: flex; gap: 8px; }
.btn-aceitar {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: var(--cf-radius-lg);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  background: var(--cf-grn-600);
  color: white;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: background var(--cf-transition), transform var(--cf-transition);
  font-family: var(--cf-font);
}
.btn-aceitar:hover {
  background: var(--cf-grn-700);
  transform: scale(1.02);
}
.btn-aceitar:focus-visible {
  outline: 2px solid var(--cf-grn-500);
  outline-offset: 2px;
}
.btn-recusar {
  padding: 10px 16px;
  border: 2px solid var(--cf-red-500);
  border-radius: var(--cf-radius-lg);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  background: white;
  color: var(--cf-red-600);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all var(--cf-transition);
  font-family: var(--cf-font);
}
.btn-recusar:hover {
  background: var(--cf-red-50);
  border-color: var(--cf-red-600);
}
.btn-recusar:focus-visible {
  outline: 2px solid var(--cf-red-500);
  outline-offset: 2px;
}
.ver-detalhes {
  font-size: 12px;
  color: var(--cf-ora-500);
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: gap var(--cf-transition);
}
.ver-detalhes:hover {
  gap: 8px;
}
</style>
