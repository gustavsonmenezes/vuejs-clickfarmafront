import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api', // URL COMPLETA do backend (SEM PROXY)
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

// Interceptor para adicionar token (se tiver autenticação)
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// Interceptor para logs em desenvolvimento
if (process.env.NODE_ENV === 'development') {
  api.interceptors.request.use(request => {
    console.log(`🌐 Requisição: ${request.method.toUpperCase()} ${request.url}`, request.data || '');
    return request;
  });

  api.interceptors.response.use(
    response => {
      console.log(`✅ Resposta: ${response.status}`, response.data);
      return response;
    },
    error => {
      console.error('❌ Erro na requisição:', error.response || error);
      return Promise.reject(error);
    }
  );
}

export default api;