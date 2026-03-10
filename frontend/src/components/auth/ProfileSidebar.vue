<template>
  <div class="profile-sidebar">
    <div class="card">
      <div class="card-body text-center">
        <div class="profile-avatar mb-3">
          <i class="fas fa-user-circle fa-3x text-primary"></i>
        </div>
        <h5>{{ user.name }}</h5>
        <p class="text-muted small">{{ user.email }}</p>
      </div>
      <div class="list-group list-group-flush">
        <router-link to="/profile" class="list-group-item list-group-item-action" :class="{ active: $route.path === '/profile' }">
          <i class="fas fa-user me-2"></i>Dados Pessoais
        </router-link>
        <router-link to="/addresses" class="list-group-item list-group-item-action" :class="{ active: $route.path === '/addresses' }">
          <i class="fas fa-map-marker-alt me-2"></i>Endereços
        </router-link>
        <router-link to="/orders" class="list-group-item list-group-item-action" :class="{ active: $route.path === '/orders' }">
          <i class="fas fa-shopping-bag me-2"></i>Meus Pedidos
        </router-link>
        <router-link to="/prescriptions" class="list-group-item list-group-item-action" :class="{ active: $route.path === '/prescriptions' }">
          <i class="fas fa-file-medical me-2"></i>Receitas
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'ProfileSidebar',
  computed: {
    ...mapGetters(['user'])
  }
}
</script>

<style scoped>
.profile-sidebar .card {
  border: none;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.list-group-item {
  border: none;
  padding: 0.75rem 1.25rem;
  color: #495057;
  font-weight: 500;
}

.list-group-item:hover,
.list-group-item.active {
  background-color: #f8f9fa;
  color: #0d6efd;
}

.list-group-item.active {
  border-left: 3px solid #0d6efd;
}
</style>