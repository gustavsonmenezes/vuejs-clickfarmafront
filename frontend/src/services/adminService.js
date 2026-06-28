import api from './api';

export const adminService = {
    // Dashboard
    async getResumo() {
        return api.get('/dashboard/resumo');
    },
    async getVendasPorPeriodo(inicio, fim) {
        const params = {};
        if (inicio) params.inicio = inicio;
        if (fim) params.fim = fim;
        return api.get('/dashboard/vendas', { params });
    },
    async getProdutosPopulares(limite = 5) {
        return api.get('/dashboard/produtos-populares', { params: { limite } });
    },
    async getPedidosPorStatus() {
        return api.get('/dashboard/pedidos-por-status');
    },

    // Categories
    async getCategorias() {
        return api.get('/categorias');
    },

    // Products
    async getProducts() {
        return api.get('/produtos');
    },
    async createProduct(productData) {
        return api.post('/produtos', productData);
    },
    async updateProduct(id, productData) {
        return api.put(`/produtos/${id}`, productData);
    },
    async deleteProduct(id) {
        return api.delete(`/produtos/${id}`);
    },

    // Orders
    async getOrders() {
        return api.get('/pedidos');
    },
    async getOrderById(id) {
        return api.get(`/pedidos/${id}`);
    },
    async updateOrderStatus(id, status) {
        return api.patch(`/pedidos/${id}/status?status=${status}`);
    },

    // Users
    async getUsers() {
        return api.get('/usuarios');
    },
    async updateUserRole(id, role) {
        return api.patch(`/usuarios/${id}/role?role=${role}`);
    },
    async deleteUser(id) {
        return api.delete(`/usuarios/${id}`);
    },

    // Entregadores
    async getEntregadoresPendentes() {
        return api.get('/entregadores/admin/pendentes');
    },
    async getEntregadoresAprovados() {
        return api.get('/entregadores/admin/aprovados');
    },
    async getEntregadoresRejeitados() {
        return api.get('/entregadores/admin/rejeitados');
    },
    async aprovarEntregador(id) {
        return api.put(`/entregadores/admin/${id}/aprovar`);
    },
    async rejeitarEntregador(id) {
        return api.put(`/entregadores/admin/${id}/rejeitar`);
    },

    // Prescriptions (admin)
    async getPrescriptions() {
        return api.get('/admin/receitas');
    },
    async approvePrescription(id) {
        return api.put(`/admin/receitas/${id}/aprovar`);
    },
    async rejectPrescription(id) {
        return api.put(`/admin/receitas/${id}/rejeitar`);
    },

    // Cupons (admin)
    async getCupons() {
        return api.get('/admin/cupons');
    },
    async createCupom(data) {
        return api.post('/admin/cupons', data);
    },
    async updateCupom(id, data) {
        return api.put(`/admin/cupons/${id}`, data);
    },
    async deleteCupom(id) {
        return api.delete(`/admin/cupons/${id}`);
    },

    // Corridas (admin)
    async getCorridasAtivas() {
        return api.get('/admin/corridas/ativas');
    },
    async getCorridasEntregador(entregadorId) {
        return api.get(`/admin/corridas/entregador/${entregadorId}`);
    },
    async getCorridaAdmin(id) {
        return api.get(`/admin/corridas/${id}`);
    },
    async cancelarCorridaAdmin(corridaId) {
        return api.post(`/admin/corridas/${corridaId}/cancelar`);
    }
};

export default adminService;
