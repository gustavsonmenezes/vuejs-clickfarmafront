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

// Configuração do axios
axios.defaults.baseURL = '/api'
axios.defaults.headers.common['Content-Type'] = 'application/json'

// Interceptor para token
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

console.log('🚀 Iniciando ClickFarma Frontend...')

// Emergency debug
const debugDiv = document.createElement('div')
debugDiv.style.position = 'fixed'
debugDiv.style.top = '0'
debugDiv.style.left = '0'
debugDiv.style.background = 'red'
debugDiv.style.color = 'white'
debugDiv.style.zIndex = '100000'
debugDiv.innerText = 'JS RUNNING'
document.body.appendChild(debugDiv)
setTimeout(() => debugDiv.remove(), 2000)

const app = createApp(App)
app.use(router)
app.use(store)

console.log('📦 Montando aplicativo no #app...')
try {
  app.mount('#app')
  console.log('✨ Aplicativo montado com sucesso!')
} catch (e) {
  console.error('❌ CRASH NO MOUNT:', e)
  const errDiv = document.createElement('div')
  errDiv.style.color = 'red'
  errDiv.style.padding = '20px'
  errDiv.innerText = 'CRASH: ' + e.message
  document.body.appendChild(errDiv)
}