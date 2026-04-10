import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import axios from 'axios'

// Configuração do axios
axios.defaults.baseURL = 'http://backend:8080/api'
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

// Importar Tema Global
import './theme.css'

// Importar Bootstrap
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import Toast from 'vue-toastification'
import 'vue-toastification/dist/index.css'

// Import Font Awesome
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'

// Add all solid icons to the library
library.add(fas)

// Configurar axios para usar variável de ambiente ou a porta correta do backend
axios.defaults.baseURL = process.env.VUE_APP_API_URL || 'http://localhost:8080/api'

const app = createApp(App)

app.component('font-awesome-icon', FontAwesomeIcon)

app.use(store)
app.use(router)
app.use(Toast)

app.mount('#app')

const app = createApp(App)
app.use(router)
app.use(store)
app.mount('#app')
