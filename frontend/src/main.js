import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import axios from 'axios'
import './theme.css'
import './assets/admin.css'
import './assets/entregador.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

import { createToastInterface } from 'vue-toastification'
import 'vue-toastification/dist/index.css'

const app = createApp(App)
app.use(router)
app.use(store)

const toast = createToastInterface({
  position: 'top-center',
  timeout: 3000,
  closeOnClick: true,
  pauseOnHover: false,
  draggable: true,
  draggablePercent: 0.3,
  hideProgressBar: true,
  icon: true,
  rtl: false,
})
app.config.globalProperties.$toast = toast

// Restaura sessão do usuário ao iniciar
store.dispatch('checkAuthStatus').then(() => {
  app.mount('#app')
})

// Registra Service Worker do PWA (desativado em dev para evitar loop de refresh)
async function registerSW() {
  if (!('serviceWorker' in navigator) || process.env.NODE_ENV === 'development') return
  try {
    const reg = await navigator.serviceWorker.register('/sw.js')
    console.log('✅ PWA: Service Worker registrado', reg.scope)

    // Tenta sincronizar push subscription (falha silenciosa se sem VAPID)
    try {
      const pushService = (await import('@/services/pushService')).default
      const supported = await pushService.isSupported()
      if (supported && Notification.permission === 'granted') {
        await pushService.syncSubscription()
        console.log('✅ Push: inscrição sincronizada')
      }
    } catch (e) {
      console.log('ℹ️ Push: não disponível ou sem permissão')
    }
  } catch (err) {
    console.warn('⚠️ PWA: Falha ao registrar SW', err)
  }
}
window.addEventListener('load', registerSW)