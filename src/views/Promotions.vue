<template>
  <div class="promotions-page container mt-4">
    <div class="row">
      <div class="col-12">
        <h1>🎯 Promoções Especiais</h1>
        <p class="lead">Confira nossas ofertas exclusivas!</p>
        
        <div class="alert alert-info">
          <i class="fas fa-tags me-2"></i>
          Página de promoções em desenvolvimento...
        </div>

        <div class="row mt-4">
          <div class="col-md-4 mb-4">
            <div class="card promotion-card">
              <div class="card-body text-center">
                <i class="fas fa-pills fa-2x text-primary mb-3"></i>
                <h5>Medicamentos</h5>
                <p class="text-muted">Até 30% off</p>
              </div>
            </div>
          </div>
          <div class="col-md-4 mb-4">
            <div class="card promotion-card">
              <div class="card-body text-center">
                <i class="fas fa-baby fa-2x text-success mb-3"></i>
                <h5>Produtos Infantis</h5>
                <p class="text-muted">Até 25% off</p>
              </div>
            </div>
          </div>
          <div class="col-md-4 mb-4">
            <div class="card promotion-card">
              <div class="card-body text-center">
                <i class="fas fa-heart fa-2x text-danger mb-3"></i>
                <h5>Cosméticos</h5>
                <p class="text-muted">Até 40% off</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Promotions'
}
</script>

<style scoped>
.promotions-page {
  min-height: 60vh;
}

.promotion-card {
  transition: transform 0.3s ease;
  border: none;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.promotion-card:hover {
  transform: translateY(-5px);
}
</style>
