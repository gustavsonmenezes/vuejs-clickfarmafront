<template>
  <div class="entregador-app">
    <header class="top-bar">
      <div class="top-bar-content">
        <span class="top-title">ClickFarma Entregas</span>
        <div class="top-right">
          <span v-if="entregador" class="online-indicator" :class="{ pulse: gpsAtivo }"></span>
          <span class="entregador-nome">{{ entregador?.nome || 'Entregador' }}</span>
          <button @click="sair" class="btn-sair" title="Sair">⏻</button>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>

    <nav class="bottom-nav">
      <router-link to="/entregador/corridas" class="nav-item" active-class="active">
        <span class="nav-icon">📋</span>
        <span class="nav-label">Corridas</span>
        <span v-if="pendentesCount > 0" class="badge">{{ pendentesCount }}</span>
      </router-link>
      <router-link to="/entregador/historico" class="nav-item" active-class="active">
        <span class="nav-icon">📜</span>
        <span class="nav-label">Histórico</span>
      </router-link>
      <router-link to="/entregador/saldo" class="nav-item" active-class="active">
        <span class="nav-icon">💰</span>
        <span class="nav-label">Saldo</span>
      </router-link>
      <router-link to="/entregador/perfil" class="nav-item" active-class="active">
        <span class="nav-icon">👤</span>
        <span class="nav-label">Perfil</span>
      </router-link>
    </nav>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'

export default {
  name: 'EntregadorLayout',
  data() {
    return {
      entregador: null,
      pendentesCount: 0,
      pollTimer: null,
      gpsTimer: null,
      gpsAtivo: false
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
  },
  beforeUnmount() {
    if (this.pollTimer) clearInterval(this.pollTimer)
    if (this.gpsTimer) clearInterval(this.gpsTimer)
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
      if (!navigator.geolocation) return
      this.gpsTimer = setInterval(() => {
        navigator.geolocation.getCurrentPosition(
          (pos) => this.enviarGPS(pos.coords.latitude, pos.coords.longitude),
          () => {},
          { enableHighAccuracy: true, timeout: 10000, maximumAge: 5000 }
        )
      }, 10000)
    },
    async enviarGPS(lat, lng) {
      if (!this.entregador) return
      try {
        await entregadorService.atualizarLocalizacao(this.entregador.id, lat, lng)
        this.gpsAtivo = true
      } catch { this.gpsAtivo = false }
    }
  }
}
</script>

<style scoped>
.entregador-app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f5f5;
}
.top-bar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 12px 16px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}
.top-bar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 800px;
  margin: 0 auto;
}
.top-title { font-weight: 700; font-size: 16px; }
.top-right { display: flex; align-items: center; gap: 10px; }
.online-indicator {
  width: 8px; height: 8px; background: #2ecc71; border-radius: 50%;
  display: inline-block;
}
.online-indicator.pulse { animation: pulse 2s infinite; }
@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }
.entregador-nome { font-size: 13px; opacity: 0.9; }
.btn-sair {
  background: rgba(255,255,255,0.2); border: none; color: white;
  width: 32px; height: 32px; border-radius: 50%; cursor: pointer;
  font-size: 14px; display: flex; align-items: center; justify-content: center;
}
.main-content {
  flex: 1;
  padding: 70px 16px 80px;
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
  background: white;
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
  border-top: 1px solid #e0e0e0;
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #999;
  font-size: 11px;
  position: relative;
  padding: 4px 12px;
}
.nav-item.active { color: #667eea; }
.nav-icon { font-size: 22px; margin-bottom: 2px; }
.nav-label { font-weight: 500; }
.badge {
  position: absolute;
  top: 0;
  right: 4px;
  background: #e74c3c;
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
