import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import axios from 'axios'

// 1. CARREGAR BOOTSTRAP PRIMEIRO
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// 2. CARREGAR DESIGN SYSTEM DEPOIS (PARA SOBREPOR)
import './theme.css'

// Configuração do axios global (axios padrão, para compatibilidade)
axios.defaults.baseURL = '/api'
axios.defaults.headers.common['Content-Type'] = 'application/json'

// Interceptor para token (chave unificada: 'authToken')
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

console.log('🚀 Iniciando ClickFarma Frontend...')

const app = createApp(App)
app.use(router)
app.use(store)

console.log('📦 Montando aplicativo no #app...')
try {
  app.mount('#app')
  console.log('✨ Aplicativo montado com sucesso!')
} catch (e) {
  console.error('❌ CRASH NO MOUNT:', e)
}