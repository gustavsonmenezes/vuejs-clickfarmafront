[file name]: DashboardOverview.vue
[file content begin]
<template>
  <div class="dashboard-overview">
    <div class="row">
      <!-- Estatísticas -->
      <div class="col-md-3 mb-4">
        <div class="stat-card">
          <div class="stat-icon sales">
            <i class="fas fa-shopping-bag"></i>
          </div>
          <div class="stat-info">
            <h3>R$ {{ stats.totalSales.toFixed(2) }}</h3>
            <p>Vendas Totais</p>
          </div>
        </div>
      </div>
      
      <div class="col-md-3 mb-4">
        <div class="stat-card">
          <div class="stat-icon orders">
            <i class="fas fa-clipboard-list"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.totalOrders }}</h3>
            <p>Pedidos Hoje</p>
          </div>
        </div>
      </div>
      
      <div class="col-md-3 mb-4">
        <div class="stat-card">
          <div class="stat-icon products">
            <i class="fas fa-pills"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.totalProducts }}</h3>
            <p>Produtos</p>
          </div>
        </div>
      </div>
      
      <div class="col-md-3 mb-4">
        <div class="stat-card">
          <div class="stat-icon users">
            <i class="fas fa-users"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.totalUsers }}</h3>
            <p>Usuários</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Gráficos e Tabelas -->
    <div class="row">
      <div class="col-md-8 mb-4">
        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">Vendas Recentes</h5>
          </div>
          <div class="card-body">
            <div class="table-responsive">
              <table class="table">
                <thead>
                  <tr>
                    <th>Pedido</th>
                    <th>Cliente</th>
                    <th>Valor</th>
                    <th>Status</th>
                    <th>Data</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="order in recentOrders" :key="order.id">
                    <td>#{{ order.id }}</td>
                    <td>{{ order.customer }}</td>
                    <td>R$ {{ order.amount.toFixed(2) }}</td>
                    <td>
                      <span :class="`status-badge ${order.status}`">
                        {{ order.status }}
                      </span>
                    </td>
                    <td>{{ order.date }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-md-4 mb-4">
        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">Status dos Pedidos</h5>
          </div>
          <div class="card-body">
            <div class="status-chart">
              <div v-for="status in orderStatus" :key="status.type" class="status-item">
                <div class="status-info">
                  <span class="status-dot" :class="status.type"></span>
                  <span>{{ status.label }}</span>
                </div>
                <span class="status-count">{{ status.count }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Produtos com Baixo Estoque -->
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">Produtos com Baixo Estoque</h5>
          </div>
          <div class="card-body">
            <div class="table-responsive">
              <table class="table">
                <thead>
                  <tr>
                    <th>Produto</th>
                    <th>Categoria</th>
                    <th>Estoque Atual</th>
                    <th>Estoque Mínimo</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="product in lowStockProducts" :key="product.id">
                    <td>
                      <strong>{{ product.name }}</strong>
                    </td>
                    <td>{{ product.category }}</td>
                    <td>{{ product.stock }}</td>
                    <td>{{ product.minStock }}</td>
                    <td>
                      <span class="badge bg-warning">Baixo Estoque</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DashboardOverview',
  data() {
    return {
      stats: {
        totalSales: 15250.00,
        totalOrders: 24,
        totalProducts: 156,
        totalUsers: 89
      },
      recentOrders: [
        {
          id: 'ORD-001',
          customer: 'João Silva',
          amount: 125.90,
          status: 'entregue',
          date: '2024-01-15'
        },
        {
          id: 'ORD-002',
          customer: 'Maria Santos',
          amount: 89.50,
          status: 'processando',
          date: '2024-01-15'
        },
        {
          id: 'ORD-003',
          customer: 'Pedro Oliveira',
          amount: 45.00,
          status: 'pendente',
          date: '2024-01-14'
        }
      ],
      orderStatus: [
        { type: 'pendente', label: 'Pendentes', count: 5 },
        { type: 'processando', label: 'Processando', count: 8 },
        { type: 'enviado', label: 'Enviados', count: 6 },
        { type: 'entregue', label: 'Entregues', count: 15 }
      ],
      lowStockProducts: [
        {
          id: 1,
          name: 'Paracetamol 500mg',
          category: 'Medicamentos',
          stock: 8,
          minStock: 20
        },
        {
          id: 2,
          name: 'Vitamina C 1000mg',
          category: 'Vitaminas',
          stock: 12,
          minStock: 25
        }
      ]
    }
  }
}
</script>

<style scoped>
.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.stat-icon.sales { background: #3498db; }
.stat-icon.orders { background: #2ecc71; }
.stat-icon.products { background: #9b59b6; }
.stat-icon.users { background: #e74c3c; }

.stat-info h3 {
  margin: 0;
  font-weight: 700;
  color: #2c3e50;
}

.stat-info p {
  margin: 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-badge.pendente { background: #fff3cd; color: #856404; }
.status-badge.processando { background: #cce7ff; color: #004085; }
.status-badge.enviado { background: #fff3cd; color: #856404; }
.status-badge.entregue { background: #d4edda; color: #155724; }

.status-chart {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.status-dot.pendente { background: #ffc107; }
.status-dot.processando { background: #17a2b8; }
.status-dot.enviado { background: #ffc107; }
.status-dot.entregue { background: #28a745; }

.status-count {
  font-weight: 600;
  color: #2c3e50;
}

.table th {
  border-top: none;
  font-weight: 600;
  color: #495057;
  background: #f8f9fa;
}
</style>
[file content end]