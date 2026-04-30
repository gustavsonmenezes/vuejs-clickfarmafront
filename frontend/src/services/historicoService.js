import api from './api';

export default {
    async registrarVisualizacao(produtoId) {
        try {
            const userRaw = localStorage.getItem('user');
            if (!userRaw) return;
            const user = JSON.parse(userRaw);
            if (user && user.id) {
                await api.post('/historico/visualizacao', {
                    usuarioId: user.id,
                    produtoId: produtoId
                });
            }
        } catch (err) {
            console.warn('Erro ao registrar histórico:', err);
        }
    },

    async getRecomendacoes() {
        try {
            const userRaw = localStorage.getItem('user');
            if (!userRaw) return null;
            const user = JSON.parse(userRaw);
            const res = await api.get(`/historico/usuario/${user.id}`);
            return res.data;
        } catch (err) {
            console.error(err);
            return null;
        }
    }
}
