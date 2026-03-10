import api from './api';

export const paymentService = {
  // Processar pagamento com cartão
  processCardPayment(paymentData) {
    return api.post('/payments/process', paymentData);
  },
  
  // Gerar código PIX
  generatePixCode(orderData) {
    return api.post('/payments/pix', orderData);
  },
  
  // Verificar status de pagamento
  checkPaymentStatus(orderId) {
    return api.get(`/payments/status/${orderId}`);
  }
};

export default paymentService;