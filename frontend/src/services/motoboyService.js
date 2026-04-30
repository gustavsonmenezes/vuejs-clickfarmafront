import api from './api';

export default {
    registrar(dados) {
        return api.post('/motoboys/register', dados);
    },
    listarTodos() {
        return api.get('/motoboys');
    },
    deletar(id) {
        return api.delete(`/motoboys/${id}`);
    }
};
