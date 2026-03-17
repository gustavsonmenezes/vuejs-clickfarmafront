import axios from 'axios';

// Utiliza a instância global configurada no main.js, ou você pode criar uma aqui
// O axios.defaults.baseURL já deve estar apontando para http://localhost:8080/api

const getAuthHeaders = () => {
    const token = localStorage.getItem('authToken');
    return {
        headers: {
            Authorization: `Bearer ${token}`
        }
    };
};

export default {
    getCart() {
        return axios.get('/cart', getAuthHeaders());
    },

    addItem(produtoId, quantidade) {
        return axios.post('/cart/items', {
            produtoId,
            quantidade
        }, getAuthHeaders());
    },

    updateItem(produtoId, quantidade) {
        return axios.put(`/cart/items/${produtoId}`, {
            quantidade
        }, getAuthHeaders());
    },

    removeItem(produtoId) {
        return axios.delete(`/cart/items/${produtoId}`, getAuthHeaders());
    },

    clearCart() {
        return axios.delete('/cart', getAuthHeaders());
    }
};