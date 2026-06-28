<template>
  <div class="entregador-app">
    <header class="top-bar">
      <div class="top-bar-content">
        <div class="top-left">
          <IconEntregador name="motorcycle" size="20" />
          <span class="top-title">ClickFarma Entregas</span>
        </div>
        <div class="top-right">
          <span
            class="online-indicator"
            :class="{ pulse: gpsAtivo, offline: !gpsAtivo && gpsWatchId != null }"
            :title="gpsAtivo ? 'GPS ativo' : 'GPS inativo'"
          ></span>
          <IconEntregador
            v-if="gpsErro"
            name="info"
            size="14"
            class="gps-erro-icon"
            :title="gpsErro"
          />
          <span class="entregador-nome">{{ entregador?.nome || 'Entregador' }}</span>
          <button @click="sair" class="btn-sair" title="Sair">
            <IconEntregador name="log-out" size="16" />
          </button>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>

    <nav class="bottom-nav">
      <router-link to="/entregador/corridas" class="nav-item" active-class="active">
        <IconEntregador name="clipboard" size="22" />
        <span class="nav-label">Corridas</span>
        <span v-if="pendentesCount > 0" class="badge">{{ pendentesCount }}</span>
      </router-link>
      <router-link to="/entregador/historico" class="nav-item" active-class="active">
        <IconEntregador name="history" size="22" />
        <span class="nav-label">Histórico</span>
      </router-link>
      <router-link to="/entregador/saldo" class="nav-item" active-class="active">
        <IconEntregador name="wallet" size="22" />
        <span class="nav-label">Saldo</span>
      </router-link>
      <router-link to="/entregador/perfil" class="nav-item" active-class="active">
        <IconEntregador name="user" size="22" />
        <span class="nav-label">Perfil</span>
      </router-link>
    </nav>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'
import IconEntregador from '@/components/entregador/IconEntregador.vue'

export default {
  name: 'EntregadorLayout',
  components: { IconEntregador },
  data() {
    return {
      entregador: null,
      pendentesCount: 0,
      pollTimer: null,
      gpsWatchId: null,
      gpsAtivo: false,
      gpsErro: ''
    }
  },
  created() {
    this.entregador = entregadorService.getEntregador()
    if (!entregadorService.isLoggedIn() || !this.entregador) {
      this.$router.push('/entregador/login')
      return
    }
    this.carregarPendentes()
    this.pollTimer = setInterval(this.carregarPendentes, 15000)
    this.iniciarGPS()
    this.conectarWebSocket()
  },
  beforeUnmount() {
    if (this.pollTimer) clearInterval(this.pollTimer)
    this.pararGPS()
    this.desconectarWebSocket()
  },
  methods: {
    async carregarPendentes() {
      try {
        const data = await entregadorService.getPendentes()
        this.pendentesCount = data.length
      } catch { this.pendentesCount = 0 }
    },
    sair() {
      entregadorService.logout()
      this.$router.push('/entregador/login')
    },
    iniciarGPS() {
      if (!navigator.geolocation) {
        this.gpsErro = 'Geolocalização não suportada'
        return
      }
      const opcoes = { enableHighAccuracy: true, timeout: 15000, maximumAge: 30000 }
      const onSuccess = (pos) => {
        this.enviarGPS(pos.coords.latitude, pos.coords.longitude)
        this.gpsErro = ''
      }
      const onError = (err) => {
        this.gpsAtivo = false
        const msgs = {
          1: 'Permissão de localização negada',
          2: 'Sinal GPS indisponível',
          3: 'Tempo de busca excedido'
        }
        this.gpsErro = msgs[err.code] || 'Erro de GPS'
      }
      this.gpsWatchId = navigator.geolocation.watchPosition(onSuccess, onError, opcoes)
    },
    pararGPS() {
      if (this.gpsWatchId != null) {
        navigator.geolocation.clearWatch(this.gpsWatchId)
        this.gpsWatchId = null
      }
    },
    async enviarGPS(lat, lng) {
      if (!this.entregador) return
      try {
        await entregadorService.atualizarLocalizacao(this.entregador.id, lat, lng)
        this.gpsAtivo = true
      } catch { this.gpsAtivo = false }
    },
    conectarWebSocket() {
      try {
        const SockJS = require('sockjs-client')
        const { Client } = require('@stomp/stompjs')
        this.stompClient = new Client({
          webSocketFactory: () => new SockJS('/ws-corridas'),
          onConnect: () => {
            this.stompClient.subscribe('/topicos/corridas/nova', () => {
              this.carregarPendentes()
            })
          }
        })
        this.stompClient.activate()
      } catch (e) {
        console.warn('WebSocket indisponível:', e.message)
      }
    },
    desconectarWebSocket() {
      if (this.stompClient) {
        try { this.stompClient.deactivate() } catch {}
      }
    }
  }
}
</script>

<style scoped>
.entregador-app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--cf-bg);
}
.top-bar {
  background: var(--cf-ora-600);
  color: white;
  padding: 12px 16px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--cf-topbar-height);
  box-sizing: border-box;
}
.top-bar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 800px;
  margin: 0 auto;
  height: 100%;
}
.top-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.top-title {
  font-weight: 700;
  font-size: 16px;
  letter-spacing: -0.01em;
}
.top-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.online-indicator {
  width: 8px;
  height: 8px;
  background: var(--cf-grn-500);
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}
.online-indicator.pulse {
  animation: pulse 2s infinite;
}
.online-indicator.offline {
  background: var(--cf-red-500);
}
.gps-erro-icon {
  color: var(--cf-amb-500);
  flex-shrink: 0;
  cursor: help;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
.entregador-nome {
  font-size: 13px;
  font-weight: 500;
  opacity: 0.9;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.btn-sair {
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.15);
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--cf-transition), transform var(--cf-transition);
}
.btn-sair:hover {
  background: rgba(255,255,255,0.35);
  transform: scale(1.05);
}
.btn-sair:focus-visible {
  outline: 2px solid white;
  outline-offset: 2px;
}
.main-content {
  flex: 1;
  padding: calc(var(--cf-topbar-height) + 16px) 16px calc(var(--cf-bottombar-height) + 16px);
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--cf-surface);
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
  padding-bottom: 8px;
  border-top: 1px solid var(--cf-ntr-200);
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  height: var(--cf-bottombar-height);
  box-sizing: border-box;
}
@supports (padding: max(0px)) {
  .bottom-nav {
    padding-bottom: max(8px, env(safe-area-inset-bottom, 8px));
  }
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: var(--cf-ntr-400);
  font-size: 11px;
  position: relative;
  padding: 4px 12px;
  transition: color var(--cf-transition);
}
.nav-item:hover {
  color: var(--cf-ora-500);
}
.nav-item.active {
  color: var(--cf-ora-600);
}
.nav-label {
  font-weight: 500;
  margin-top: 2px;
}
.badge {
  position: absolute;
  top: 0;
  right: 4px;
  background: var(--cf-red-600);
  color: white;
  font-size: 10px;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}
</style>
