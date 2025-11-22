<template>
  <div class="order-details">
    <!-- Cabeçalho do Pedido -->
    <div class="card mb-4">
      <div class="card-header bg-primary text-white">
        <div class="row align-items-center">
          <div class="col-md-6">
            <h5 class="mb-0">
              <i class="fas fa-receipt me-2"></i>
              Pedido #{{ order.id }}
            </h5>
          </div>
          <div class="col-md-6 text-md-end">
            <span class="badge bg-light text-primary fs-6">
              {{ getStatusText(order.status) }}
            </span>
          </div>
        </div>
      </div>
      <div class="card-body">
        <div class="row">
          <div class="col-md-3">
            <strong>Data do Pedido:</strong><br>
            {{ formatDate(order.date) }}
          </div>
          <div class="col-md-3">
            <strong>Total:</strong><br>
            R$ {{ order.total.toFixed(2) }}
          </div>
          <div class="col-md-3">
            <strong>Pagamento:</strong><br>
            {{ getPaymentMethodLabel(order.paymentMethod) }}
          </div>
          <div class="col-md-3">
            <strong>Entrega:</strong><br>
            {{ order.deliveryType === 'delivery' ? 'Entrega' : 'Retirada' }}
          </div>
        </div>
      </div>
    </div>

    <!-- Rastreamento -->
    <div v-if="order.tracking" class="card mb-4">
      <div class="card-header bg-light">
        <h5 class="mb-0">
          <i class="fas fa-truck me-2"></i>Status da Entrega
        </h5>
      </div>
      <div class="card-body">
        <OrderTimeline :order="order" />
        
        <div v-if="order.tracking.trackingCode" class="mt-3 p-3 bg-light rounded">
          <div class="row">
            <div class="col-md-6">
              <strong>Código de Rastreamento:</strong><br>
              <span class="text-primary">{{ order.tracking.trackingCode }}</span>
            </div>
            <div class="col-md-6">
              <strong>Transportadora:</strong><br>
              {{ order.tracking.carrier }}
            </div>
          </div>
          <div class="row mt-2">
            <div class="col-12">
              <strong>Localização Atual:</strong><br>
              {{ order.tracking.currentLocation }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Itens do Pedido -->
    <div class="card mb-4">
      <div class="card-header bg-light">
        <h5 class="mb-0">
          <i class="fas fa-boxes me-2"></i>Itens do Pedido
        </h5>
      </div>
      <div class="card-body">
        <div v-for="item in order.items" :key="item.id" class="d-flex justify-content-between align-items-center mb-3 pb-3 border-bottom">
          <div class="d-flex align-items-center">
            <img :src="getItemImage(item)" :alt="item.name" class="rounded me-3" width="60" height="60">
            <div>
              <h6 class="mb-1">{{ item.name }}</h6>
              <small class="text-muted">Quantidade: {{ item.quantity }}</small>
            </div>
          </div>
          <span class="fw-bold">R$ {{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
        
        <div class="row mt-3">
          <div class="col-md-6 offset-md-6">
            <div class="d-flex justify-content-between mb-2">
              <span>Subtotal:</span>
              <span>R$ {{ order.subtotal.toFixed(2) }}</span>
            </div>
            <div class="d-flex justify-content-between mb-2">
              <span>Entrega:</span>
              <span>{{ getDeliveryCostLabel() }}</span>
            </div>
            <div v-if="order.paymentMethod === 'pix'" class="d-flex justify-content-between mb-2 text-success">
              <span>Desconto PIX:</span>
              <span>- R$ {{ (order.subtotal * 0.05).toFixed(2) }}</span>
            </div>
            <hr>
            <div class="d-flex justify-content-between fw-bold fs-5">
              <span>Total:</span>
              <span>R$ {{ order.total.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Informações de Entrega -->
    <div class="card">
      <div class="card-header bg-light">
        <h5 class="mb-0">
          <i class="fas fa-map-marker-alt me-2"></i>Informações de Entrega
        </h5>
      </div>
      <div class="card-body">
        <div v-if="order.deliveryType === 'delivery' && order.deliveryInfo">
          <p class="mb-1">
            <strong>Endereço de Entrega:</strong><br>
            {{ order.deliveryInfo.street }}, {{ order.deliveryInfo.number }}<br>
            {{ order.deliveryInfo.neighborhood }}<br>
            {{ order.deliveryInfo.city }} - {{ order.deliveryInfo.state }}<br>
            CEP: {{ order.deliveryInfo.zipcode || order.deliveryInfo.zip }}
            <span v-if="order.deliveryInfo.complement">
              <br>Complemento: {{ order.deliveryInfo.complement }}
            </span>
          </p>
        </div>
        <div v-else-if="order.deliveryType === 'pickup' && order.deliveryInfo">
          <p class="mb-1">
            <strong>Local de Retirada:</strong><br>
            {{ order.deliveryInfo.name || 'ClickFarma Centro' }}<br>
            {{ order.deliveryInfo.address || order.deliveryInfo.street }}<br>
            Telefone: {{ order.deliveryInfo.phone || '(11) 3333-3333' }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import OrderTimeline from './OrderTimeline.vue'

export default {
  name: 'OrderDetails',
  components: {
    OrderTimeline
  },
  props: {
    order: {
      type: Object,
      required: true
    }
  },
  methods: {
    getStatusText(status) {
      const statusMap = {
        'confirmed': 'Confirmado',
        'processing': 'Processando',
        'shipped': 'Enviado',
        'out_for_delivery': 'Saiu para entrega',
        'delivered': 'Entregue'
      }
      return statusMap[status] || status
    },
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('pt-BR')
    },
    getPaymentMethodLabel(method) {
      const methods = {
        'credit_card': 'Cartão de Crédito',
        'debit_card': 'Cartão de Débito',
        'pix': 'PIX'
      }
      return methods[method] || method
    },
    getItemImage(item) {
      return item.image || 'https://via.placeholder.com/60x60?text=Produto'
    },
    getDeliveryCostLabel() {
      if (this.order.deliveryType === 'pickup') return 'Grátis'
      const deliveryCost = this.order.total - this.order.subtotal
      return deliveryCost > 0 ? `R$ ${deliveryCost.toFixed(2)}` : 'Grátis'
    }
  }
}
</script>