import api from './api';

export const cepService = {
  buscar(cep) {
    const numeros = cep.replace(/\D/g, '');
    if (numeros.length !== 8) return Promise.reject('CEP invalido');
    return api.get(`/cep/${numeros}`);
  }
};

export default cepService;
