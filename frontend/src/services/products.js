import api from './api';

export default {
    getProducts() {
        return api.get('/produtos');
    },

    getProductById(id) {
        return api.get(`/produtos/${id}`);
    }
};