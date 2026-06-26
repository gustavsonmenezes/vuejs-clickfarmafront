import api from './api';

export const paymentService = {
  checkPaymentStatus(pedidoId) {
    return api.get(`/pagamentos/status/${pedidoId}`);
  }
};

export default paymentService;
