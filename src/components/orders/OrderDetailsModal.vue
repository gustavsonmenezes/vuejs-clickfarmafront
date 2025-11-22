<template>
  <div class="modal fade show d-block" style="background-color: rgba(0,0,0,0.5)">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Detalhes do Pedido #{{ order.id }}</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <OrderDetails :order="order" />
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="$emit('track-order', order)">
            <i class="fas fa-shipping-fast me-2"></i>Rastrear Pedido
          </button>
          <button class="btn btn-secondary" @click="$emit('close')">Fechar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import OrderDetails from './OrderDetails.vue'

export default {
  name: 'OrderDetailsModal',
  components: {
    OrderDetails
  },
  props: {
    order: {
      type: Object,
      required: true
    }
  },
  emits: ['close', 'track-order']
}
</script>