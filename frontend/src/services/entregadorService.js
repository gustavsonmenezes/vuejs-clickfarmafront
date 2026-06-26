import api from './api'

const TOKEN_KEY = 'entregador_token'
const ENTREGADOR_KEY = 'entregador_data'

export default {
  setToken(token) {
    localStorage.setItem(TOKEN_KEY, token)
  },
  getToken() {
    return localStorage.getItem(TOKEN_KEY)
  },
  setEntregador(data) {
    localStorage.setItem(ENTREGADOR_KEY, JSON.stringify(data))
  },
  getEntregador() {
    const raw = localStorage.getItem(ENTREGADOR_KEY)
    return raw ? JSON.parse(raw) : null
  },
  logout() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(ENTREGADOR_KEY)
  },
  isLoggedIn() {
    const token = this.getToken()
    return token && token.split('.').length === 3
  },

  async login(cpf, senha) {
    const res = await api.post('/entregadores/login', { cpf, senha })
    this.setToken(res.data.token)
    this.setEntregador(res.data.entregador)
    return res.data
  },

  async cadastro(dados) {
    const res = await api.post('/entregadores/cadastro', dados)
    return res.data
  },

  async getPerfil(id) {
    const res = await api.get(`/entregadores/${id}`)
    return res.data
  },

  async atualizarPerfil(id, dados) {
    const res = await api.put(`/entregadores/${id}`, dados)
    return res.data
  },

  async atualizarLocalizacao(id, latitude, longitude) {
    const res = await api.put(`/entregadores/${id}/localizacao`, { latitude, longitude })
    return res.data
  },

  async getPendentes() {
    const res = await api.get('/entregas-entregador/pendentes')
    return res.data
  },

  async getEntregasAtivas(entregadorId) {
    const res = await api.get(`/entregas-entregador/entregador/${entregadorId}/ativas`)
    return res.data
  },

  async getHistorico(entregadorId) {
    const res = await api.get(`/entregas-entregador/entregador/${entregadorId}/historico`)
    return res.data
  },

  async getTodasEntregas(entregadorId) {
    const res = await api.get(`/entregas-entregador/entregador/${entregadorId}/todas`)
    return res.data
  },

  async aceitarEntrega(entregaId, entregadorId) {
    const res = await api.post(`/entregas-entregador/${entregaId}/aceitar/${entregadorId}`)
    return res.data
  },

  async recusarEntrega(entregaId, entregadorId) {
    const res = await api.post(`/entregas-entregador/${entregaId}/recusar/${entregadorId}`)
    return res.data
  },

  async marcarRetirada(entregaId) {
    const res = await api.post(`/entregas-entregador/${entregaId}/retirar`)
    return res.data
  },

  async iniciarRota(entregaId) {
    const res = await api.post(`/entregas-entregador/${entregaId}/iniciar-rota`)
    return res.data
  },

  async finalizarEntrega(entregaId) {
    const res = await api.post(`/entregas-entregador/${entregaId}/finalizar`)
    return res.data
  },

  async getEntrega(id) {
    const res = await api.get(`/entregas-entregador/${id}`)
    return res.data
  }
}
