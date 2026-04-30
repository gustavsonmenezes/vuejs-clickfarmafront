import api from './api';

export default {
    listarTodos() {
        return api.get('/pedidos');
    },
    listarPorUsuario(usuarioId) {
        return api.get(`/pedidos/usuario/${usuarioId}`);
    },
    listarPorFarmacia(farmaciaId) {
        return api.get(`/pedidos/farmacia/${farmaciaId}`);
    },
    buscarPorId(id) {
        return api.get(`/pedidos/${id}`);
    },
    atualizarStatus(id, novoStatus) {
        return api.patch(`/pedidos/${id}/status?status=${novoStatus}`);
    },
    cancelar(id) {
        return api.delete(`/pedidos/${id}`);
    }
};
