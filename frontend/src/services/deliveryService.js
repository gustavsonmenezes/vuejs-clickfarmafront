// src/services/deliveryService.js
import api from './api';

export class DeliveryService {
  static async calculateDelivery(cep, cartTotal) {
    try {
      const response = await api.post('/delivery/calculate', {
        cep: cep,
        valorTotalCarrinho: cartTotal
      });
      return response.data;
    } catch (error) {
      console.error('Erro ao calcular frete na API:', error);
      throw error;
    }
  }

  static async trackOrder(orderId) {
    try {
      // Simulação de API de rastreamento
      const response = await new Promise(resolve => {
        setTimeout(() => {
          const statuses = [
            'confirmed', 'processing', 'shipped', 'out_for_delivery', 'delivered'
          ];
          const currentStatus = statuses[Math.floor(Math.random() * statuses.length)];
          
          resolve({
            orderId,
            status: currentStatus,
            trackingCode: `TRK${orderId}`,
            estimatedDelivery: this.calculateDeliveryDate(),
            currentLocation: this.getRandomLocation(),
            updates: this.generateTrackingUpdates(orderId, currentStatus),
            carrier: 'ClickFarma Express',
            contact: '(81) 99818-9999'
          });
        }, 1000);
      });
      
      return response;
    } catch (error) {
      console.error('Erro no rastreamento:', error);
      throw error;
    }
  }

  static calculateDeliveryDate() {
    const date = new Date();
    date.setDate(date.getDate() + 3);
    return date.toISOString();
  }

  static getRandomLocation() {
    const locations = [
      'Centro de Distribuição - Recife',
      'Unidade Paulista',
      'A caminho da entrega',
      'Em separação'
    ];
    return locations[Math.floor(Math.random() * locations.length)];
  }

  static generateTrackingUpdates(orderId, currentStatus) {
    const updates = [];
    const now = new Date();
    
    // Status confirmado
    updates.push({
      status: 'confirmed',
      description: 'Pedido confirmado e em processamento',
      timestamp: new Date(now - 3600000).toISOString(),
      location: 'ClickFarma - Loja Virtual'
    });

    // Status processando
    if (['processing', 'shipped', 'out_for_delivery', 'delivered'].includes(currentStatus)) {
      updates.push({
        status: 'processing',
        description: 'Produtos sendo separados',
        timestamp: new Date(now - 1800000).toISOString(),
        location: 'Centro de Distribuição'
      });
    }

    // Status enviado
    if (['shipped', 'out_for_delivery', 'delivered'].includes(currentStatus)) {
      updates.push({
        status: 'shipped',
        description: 'Pedido enviado para transporte',
        timestamp: new Date(now - 900000).toISOString(),
        location: 'Unidade de Logística'
      });
    }

    // Status em entrega
    if (['out_for_delivery', 'delivered'].includes(currentStatus)) {
      updates.push({
        status: 'out_for_delivery',
        description: 'Pedido saiu para entrega',
        timestamp: new Date(now - 300000).toISOString(),
        location: 'A caminho do destino'
      });
    }

    // Status entregue
    if (currentStatus === 'delivered') {
      updates.push({
        status: 'delivered',
        description: 'Pedido entregue com sucesso',
        timestamp: now.toISOString(),
        location: 'Local de entrega'
      });
    }

    return updates;
  }
}
