import api from './api';

export default {
    registrar(dados) {
        return api.post('/farmacias/register', dados);
    },
    listarTodas() {
        return api.get('/farmacias');
    },
    buscarPorId(id) {
        return api.get(`/farmacias/${id}`);
    },
    getDashboard(id) {
        return api.get(`/dashboard/farmacia/${id}`);
    }
};
