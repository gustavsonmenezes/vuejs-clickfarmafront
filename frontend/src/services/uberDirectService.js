import api from './api';

export class UberDirectService {

  static async getQuote(dropoffAddress) {
    const response = await api.post('/uber-direct/quote', {
      dropoffAddress
    });
    return response.data;
  }

  static async createDelivery(quoteId, dropoffInfo, manifestItems, externalOrderId) {
    const response = await api.post('/uber-direct/delivery', {
      quoteId,
      dropoff: {
        name: dropoffInfo.name,
        address: dropoffInfo.address,
        phone: dropoffInfo.phone
      },
      manifestItems,
      externalOrderId
    });
    return response.data;
  }

  static async getDeliveryStatus(deliveryId) {
    const response = await api.get(`/uber-direct/delivery/${deliveryId}`);
    return response.data;
  }

  static async cancelDelivery(deliveryId) {
    const response = await api.post(`/uber-direct/delivery/${deliveryId}/cancel`);
    return response.data;
  }
}
