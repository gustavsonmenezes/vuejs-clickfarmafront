import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import axios from 'axios'
import './theme.css'
import './assets/admin.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Configuração do axios - URL relativa funciona em qualquer ambiente
axios.defaults.baseURL = '/api'
axios.defaults.headers.common['Content-Type'] = 'application/json'

// Interceptor para token
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// Importar Bootstrap
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

const app = createApp(App)
app.use(router)
app.use(store)

// Restaura sessão do usuário ao iniciar
store.dispatch('checkAuthStatus').then(() => {
  app.mount('#app')
})

// Registra Service Worker do PWA (desativado em dev para evitar loop de refresh)
if ('serviceWorker' in navigator && process.env.NODE_ENV !== 'development') {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').then(reg => {
      console.log('✅ PWA: Service Worker registrado', reg.scope)
    }).catch(err => {
      console.warn('⚠️ PWA: Falha ao registrar SW', err)
    })
  })
}