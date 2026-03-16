import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import axios from 'axios'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import Toast from 'vue-toastification'
import 'vue-toastification/dist/index.css'

// Configurar axios para usar variável de ambiente ou a porta correta do backend
axios.defaults.baseURL = process.env.VUE_APP_API_URL || 'http://localhost:8080/api'

const app = createApp(App)

app.use(store)
app.use(router)
app.use(Toast)

app.mount('#app')
