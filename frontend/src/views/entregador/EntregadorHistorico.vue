<template>
  <div
    class="historico-page"
    @touchstart.passive="onTouchStart"
    @touchmove.passive="onTouchMove"
    @touchend.passive="onTouchEnd"
  >
    <div v-if="pulando" class="pull-indicator">
      <svg class="pull-spinner" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10" stroke-dasharray="32" stroke-dashoffset="32" />
      </svg>
      Atualizando...
    </div>

    <h2 class="page-title">
      <IconEntregador name="history" size="20" />
      Histórico de corridas
    </h2>

    <!-- Skeleton -->
    <div v-if="carregando" class="skeleton-list">
      <div v-for="n in 4" :key="n" class="skeleton-card">
        <div class="skeleton-line w-50"></div>
        <div class="skeleton-line w-80"></div>
        <div class="skeleton-line w-40"></div>
      </div>
    </div>

    <div v-if="!carregando && corridas.length === 0" class="empty-state">
      <IconEntregador name="inbox" size="56" class="empty-icon" />
      <p>Nenhuma corrida finalizada ainda</p>
    </div>

    <div v-for="c in corridas" :key="c.id" class="corrida-card">
      <div class="card-top">
        <span class="pedido-num">#{{ c.entrega.codigoPedido }}</span>
        <span class="corrida-data">{{ formatDate(c.finalizadoEm || c.criadoEm) }}</span>
      </div>

      <div class="card-body">
        <div class="info-row">
          <span>
            <IconEntregador name="user" size="14" />
            {{ c.entrega.clienteNome }}
          </span>
          <span class="valor">R$ {{ formatMoney(c.taxaFinal) }}</span>
        </div>
        <div class="info-row" v-if="c.entrega.enderecoDestino">
          <span class="endereco">
            <IconEntregador name="map-pin" size="14" />
            {{ c.entrega.enderecoDestino }}
          </span>
        </div>
      </div>

      <div v-if="avaliacoes[c.id]" class="avaliacao-area">
        <div class="estrelas">
          <span v-for="n in 5" :key="n" :class="['star', { filled: n <= avaliacoes[c.id].nota }]">
            <IconEntregador name="star" :size="18" :customClass="n <= avaliacoes[c.id].nota ? 'star-filled' : ''" />
          </span>
          <span class="nota-text">{{ avaliacoes[c.id].nota }}/5</span>
        </div>
        <p v-if="avaliacoes[c.id].comentario" class="comentario">{{ avaliacoes[c.id].comentario }}</p>
      </div>
      <div v-else-if="c.status === 'FINALIZADA'" class="avaliar-area">
        <button class="btn-avaliar" @click="abrirAvaliacao(c)">
          <IconEntregador name="star" size="16" />
          Avaliar cliente
        </button>
      </div>
    </div>

    <AvaliacaoModal
      v-if="mostrarModal"
      :corrida="corridaSelecionada"
      @fechar="mostrarModal = false"
      @avaliado="onAvaliado"
    />
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'
import AvaliacaoModal from '@/components/entregador/AvaliacaoModal.vue'
import IconEntregador from '@/components/entregador/IconEntregador.vue'
import EntregadorMixin from '@/components/entregador/EntregadorMixin.js'

export default {
  name: 'EntregadorHistorico',
  components: { AvaliacaoModal, IconEntregador },
  mixins: [EntregadorMixin],
  data() {
    return {
      corridas: [],
      avaliacoes: {},
      carregando: true,
      mostrarModal: false,
      corridaSelecionada: null,
      pulando: false,
      pullDist: 0,
      touchStartY: 0
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

        const corridas = await entregadorService.getCorridasEntregador(entregador.id)
        this.corridas = (corridas || []).filter(c => c.status === 'FINALIZADA')

        const avaliacoes = await entregadorService.getAvaliacoesEntregador(entregador.id)
        if (avaliacoes) {
          for (const a of avaliacoes) {
            this.avaliacoes[a.corridaId] = a
          }
        }
      } catch (err) {
        this.$toast.error('Erro ao carregar histórico')
      } finally {
        this.carregando = false
      }
    },

    abrirAvaliacao(corrida) {
      this.corridaSelecionada = corrida
      this.mostrarModal = true
    },

    onAvaliado() {
      this.mostrarModal = false
      this.corridaSelecionada = null
      this.carregar()
      this.$toast.success('Avaliação enviada com sucesso!')
    },

    // formatMoney e formatDate vindos do mixin
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
.historico-page { padding-top: 4px; }
.page-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--cf-ntr-800);
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
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
.skeleton-line.w-40 { width: 40%; }
.skeleton-line.w-50 { width: 50%; }
.skeleton-line.w-80 { width: 80%; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.loading { text-align: center; padding: 40px; color: var(--cf-ntr-400); }
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--cf-ntr-400);
}
.empty-icon {
  color: var(--cf-ntr-300);
  margin-bottom: 12px;
}
.empty-state p {
  margin: 0;
  font-size: 14px;
}
.corrida-card {
  background: var(--cf-surface);
  border-radius: 14px;
  padding: 14px;
  margin-bottom: 12px;
  box-shadow: var(--cf-shadow-sm);
  transition: box-shadow var(--cf-transition);
}
.corrida-card:hover {
  box-shadow: var(--cf-shadow-md);
}
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.pedido-num {
  font-weight: 700;
  font-size: 14px;
  color: var(--cf-ntr-800);
}
.corrida-data {
  font-size: 12px;
  color: var(--cf-ntr-400);
}
.card-body { margin-bottom: 8px; }
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 3px 0;
  font-size: 13px;
  color: var(--cf-ntr-500);
  gap: 8px;
}
.info-row span { display: flex; align-items: center; gap: 4px; }
.valor {
  font-weight: 700;
  color: var(--cf-grn-600);
  font-size: 15px;
  white-space: nowrap;
}
.endereco {
  font-size: 12px;
  color: var(--cf-ntr-400);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.avaliacao-area {
  border-top: 1px solid var(--cf-ntr-100);
  padding-top: 8px;
}
.estrelas {
  display: flex;
  align-items: center;
  gap: 2px;
}
.star {
  display: inline-flex;
  color: var(--cf-ntr-200);
  transition: color var(--cf-transition);
}
.star-filled { color: var(--cf-amb-500); }
.nota-text {
  font-size: 12px;
  color: var(--cf-ntr-400);
  margin-left: 6px;
  font-weight: 500;
}
.comentario {
  font-size: 12px;
  color: var(--cf-ntr-500);
  margin: 4px 0 0;
  font-style: italic;
}
.avaliar-area {
  border-top: 1px solid var(--cf-ntr-100);
  padding-top: 8px;
}
.btn-avaliar {
  width: 100%;
  padding: 10px;
  border: 2px dashed var(--cf-ora-300);
  border-radius: var(--cf-radius-lg);
  background: transparent;
  color: var(--cf-ora-600);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: var(--cf-font);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all var(--cf-transition);
}
.btn-avaliar:hover {
  background: var(--cf-ora-50);
  border-color: var(--cf-ora-600);
  border-style: solid;
}
.btn-avaliar:focus-visible {
  outline: 2px solid var(--cf-ora-500);
  outline-offset: 2px;
}
</style>
