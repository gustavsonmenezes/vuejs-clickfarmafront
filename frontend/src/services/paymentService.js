import api from './api';

export default {
    listarTodos() {
        return api.get('/pagamentos');
    },
    listarPendentes() {
        return api.get('/pagamentos/pendentes');
    },
    listarPorFarmacia(id) {
        return api.get(`/pagamentos/farmacia/${id}`);
    },
    listarPorMotoboy(id) {
        return api.get(`/pagamentos/motoboy/${id}`);
    },
    gerarPagamentoFarmacia(farmaciaId, periodo) {
        return api.post(`/pagamentos/gerar/farmacia/${farmaciaId}`, { periodo });
    },
    gerarPagamentoMotoboy(motoboyId, periodo) {
        return api.post(`/pagamentos/gerar/motoboy/${motoboyId}`, { periodo });
    },
    marcarComoPago(id, observacoes) {
        return api.patch(`/pagamentos/${id}/pagar`, { observacoes });
    }
};
