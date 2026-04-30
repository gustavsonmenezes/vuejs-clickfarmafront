import axios from 'axios';

// Usa o proxy do vue.config.js em dev, e o mesmo origin em produção
const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json'
    }
});

// Interceptor para adicionar token JWT (se existir)
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Interceptor para tratar erros de autenticação
api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('authToken');
            localStorage.removeItem('user');
            // Só redireciona se não estiver na página de login
            const publicPages = ['/login', '/admin/login', '/pharmacy/login'];
            if (!publicPages.includes(window.location.pathname)) {
                // window.location.href = '/login'; // Desativado temporariamente para não atrapalhar o debug
            }
        }
        return Promise.reject(error);
    }
);

export default api;
