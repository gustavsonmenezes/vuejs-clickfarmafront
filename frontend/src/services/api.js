import axios from 'axios';

// Base default: relative `/api` para funcionar com:
// - nginx proxy em producao/docker
// - webpack-dev-server proxy em dev
// Opcionalmente pode ser sobrescrito por env var.
const resolvedBaseURL =
    (typeof process !== 'undefined' && process.env && (process.env.VUE_APP_API_BASE_URL || process.env.VUE_APP_API_URL)) ||
    '/api';

const api = axios.create({
    baseURL: resolvedBaseURL,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Interceptor para adicionar token JWT (se existir)
api.interceptors.request.use(
    (config) => {
        // O projeto usa 'authToken' em varios pontos (store/login). Mantemos compatibilidade com 'access_token'.
        const token = localStorage.getItem('authToken') || localStorage.getItem('access_token');
        // Só anexa se parecer um JWT real (3 partes separadas por '.').
        if (token && token.split('.').length === 3) {
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
            localStorage.removeItem('access_token');
            localStorage.removeItem('user');
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;
