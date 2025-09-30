<template>
  <div class="order-management container-fluid">
    <h2>Visualizar Pedidos</h2>
    <input v-model="search" class="form-control mb-3" placeholder="Buscar por ID ou Cliente" />
    <div class="row">
      <div v-for="order in filteredOrders" :key="order.id" class="col-md-6 mb-3">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Pedido #{{ order.id }}</h5>
            <p class="card-text">Cliente: {{ order.customerName }}</p>
            <p class="card-text">Data: {{ order.date }}</p>
            <p class="card-text">Status: <span :class="getStatusClass(order.status)">{{ order.status }}</span></p>
            <p class="card-text">Total: R$ {{ order.total.toFixed(2) }}</p>
            <div class="btn-group">
              <select v-model="newStatus" class="form-control form-control-sm d-inline-block w-auto">
                <option value="Pendente">Pendente</option>
                <option value="Enviado">Enviado</option>
                <option value="Entregue">Entregue</option>
                <option value="Cancelado">Cancelado</option>
              </select>
              <button @click="updateStatus(order.id)" class="btn btn-primary btn-sm">Atualizar Status</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderManagement',
  data() {
    return {
      orders: [],
      filteredOrders: [],
      search: '',
      newStatus: 'Pendente'
    }
  },
  mounted() {
    this.fetchOrders()
  },
  methods: {
    async fetchOrders() {
      // Dados mockados
      this.orders = [
        { id: 1001, customerName: 'João Silva', date: '2023-10-01', status: 'Pendente', total: 45.50 },
        { id: 1002, customerName: 'Maria Oliveira', date: '2023-10-02', status: 'Enviado', total: 120.00 },
        { id: 1003, customerName: 'Carlos Santos', date: '2023-10-03', status: 'Entregue', total: 89.90 },
        { id: 1004, customerName: 'Ana Costa', date: '2023-10-04', status: 'Pendente', total: 34.75 }
      ]
      this.filteredOrders = this.orders
    },

    updateFilteredOrders() {
      this.filteredOrders = this.orders.filter(order =>
        order.id.toString().includes(this.search) ||
        order.customerName.toLowerCase().includes(this.search.toLowerCase())
      )
    },

    getStatusClass(status) {
      return {
        'text-warning': status === 'Pendente',
        'text-info': status === 'Enviado',
        'text-success': status === 'Entregue',
        'text-danger': status === 'Cancelado'
      }
    },

    async updateStatus(id) {
      const order = this.orders.find(o => o.id === id)
      if (order) {
        order.status = this.newStatus
        this.updateFilteredOrders()
        alert(`Status do pedido #${id} atualizado para "${this.newStatus}"!`)
      }
    }
  },
  watch: {
    search() {
      this.updateFilteredOrders()
    }
  }
}
</script>

<style scoped>
.card { box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
</style>