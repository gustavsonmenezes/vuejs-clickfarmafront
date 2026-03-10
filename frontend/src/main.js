import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import axios from 'axios'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import Toast from 'vue-toastification'
import 'vue-toastification/dist/index.css'

// Configurar axios
axios.defaults.baseURL = 'http://localhost:3000/api'

const app = createApp(App)

app.use(store)
app.use(router)
app.use(Toast)

app.mount('#app')