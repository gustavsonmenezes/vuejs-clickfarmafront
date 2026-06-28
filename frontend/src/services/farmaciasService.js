import api from './api'

const farmaciasService = {
  listar() {
    return api.get('/admin/farmacias')
  },
  buscar(id) {
    return api.get(`/admin/farmacias/${id}`)
  },
  criar(data) {
    return api.post('/admin/farmacias', data)
  },
  atualizar(id, data) {
    return api.put(`/admin/farmacias/${id}`, data)
  },
  listarAtivas() {
    return api.get('/farmacias')
  },
  listarProdutos(farmaciaId) {
    return api.get(`/farmacias/${farmaciaId}/produtos`)
  },
  adicionarProduto(data) {
    return api.post('/farmacias/produtos', data)
  },
  removerProduto(id) {
    return api.delete(`/farmacias/produtos/${id}`)
  },
  atualizarEstoque(id, quantidade) {
    return api.patch(`/farmacias/produtos/${id}/estoque`, null, { params: { quantidade } })
  },
  buscarProdutos(nome, lat, lng) {
    let params = {}
    if (nome) params.nome = nome
    if (lat != null) params.lat = lat
    if (lng != null) params.lng = lng
    return api.get('/farmacias/busca', { params })
  }
}

export default farmaciasService
