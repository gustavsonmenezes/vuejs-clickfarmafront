<!-- src/components/admin/AdminSidebar.vue -->
<template>
  <aside class="admin-sidebar">
    <div class="sidebar-header">
      <h3>⚕️ ClickFarma Admin</h3>
    </div>
    <nav class="sidebar-nav">
      <router-link to="/admin/products" class="nav-item">
        📦 Gerenciar Produtos
      </router-link>
      <router-link to="/admin/inventory" class="nav-item">
        📊 Gerenciar Estoque
      </router-link>
      <router-link to="/admin/orders" class="nav-item">
        📋 Visualizar Pedidos
      </router-link>
      <router-link to="/admin/prescriptions" class="nav-item">
        🏥 Validar Receitas
      </router-link>
      <router-link to="/admin/users" class="nav-item">
        👥 Gerenciar Usuários
      </router-link>
    </nav>
  </aside>
</template>

<script>
export default {
  name: 'AdminSidebar'
}
</script>

<style scoped>
.admin-sidebar {
  width: 250px;
  background: #2c3e50;
  color: white;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #34495e;
  text-align: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.2rem;
}

.sidebar-nav {
  padding: 20px 0;
}

.nav-item {
  display: block;
  padding: 12px 20px;
  color: #bdc3c7;
  text-decoration: none;
  transition: all 0.3s;
  border-left: 4px solid transparent;
}

.nav-item:hover {
  background: #34495e;
  color: white;
  border-left-color: #3498db;
}

.nav-item.router-link-active {
  background: #34495e;
  color: white;
  border-left-color: #3498db;
}
</style>