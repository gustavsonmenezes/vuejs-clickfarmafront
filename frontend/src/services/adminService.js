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
    }
};

export default adminService;
