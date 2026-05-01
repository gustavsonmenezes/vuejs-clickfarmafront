import api from './api';

export class OrderService {
  /**
   * Cria um novo pedido no backend.
   * @param {object} pedidoRequest - O objeto do pedido a ser enviado.
   * @returns {Promise<object>} A resposta da API, contendo o link de pagamento.
   */
  static async createOrder(pedidoRequest) {
    try {
      // Corrigido: O prefixo /api já está na baseURL do axios.
      const response = await api.post('/pedidos', pedidoRequest);
      return response.data;
    } catch (error) {
      console.error('Erro ao criar o pedido:', error.response?.data || error.message);
      // Lança o erro para que o componente possa tratá-lo
      throw error;
    }
  }

  static async getOrderDetails(orderId) {
    try {
      // Simulação de API
      const response = await new Promise(resolve => {
        setTimeout(() => {
          resolve({
            id: orderId,
            status: 'processing',
            date: new Date().toISOString(),
            items: [],
            total: 0,
            deliveryInfo: {},
            paymentMethod: 'credit_card'
          })
        }, 500)
      })
      return response
    } catch (error) {
      console.error('Erro ao buscar detalhes do pedido:', error)
      throw error
    }
  }

  static async updateOrderStatus(orderId, status) {
    console.log(`Atualizando pedido ${orderId} para status: ${status}`);
  }

  static async trackOrder(codigoPedido) {
    try {
      const response = await api.get(`/pedidos/${codigoPedido}/rastreio`);
      return response.data;
    } catch (error) {
      console.error('Erro ao rastrear pedido:', error.response?.data || error.message);
      throw error;
    }
  }
}