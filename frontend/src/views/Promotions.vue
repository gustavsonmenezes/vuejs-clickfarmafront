<template>
  <div class="promotions-page">
    <div class="container py-5">
      <div class="text-center mb-5">
        <i class="fas fa-tags text-primary" style="font-size: 2.5rem;"></i>
        <h1 class="mt-3 fw-bold">Promoções Especiais</h1>
        <p class="lead text-muted">Aproveite ofertas imperdíveis em toda a loja</p>
      </div>

      <div class="row g-4">
        <div v-for="promo in promocoes" :key="promo.titulo" class="col-md-6 col-lg-4">
          <div class="card border-0 shadow-sm h-100 promo-card">
            <div class="card-body text-center p-4">
              <div class="promo-icon mb-3" :style="{ background: promo.bg }">
                <i :class="promo.icon" style="font-size: 1.5rem; color: white;"></i>
              </div>
              <span class="badge bg-danger mb-2">{{ promo.desconto }}</span>
              <h5 class="fw-bold mb-2">{{ promo.titulo }}</h5>
              <p class="text-muted small mb-3">{{ promo.descricao }}</p>
              <router-link to="/products" class="btn btn-outline-primary btn-sm">Ver Ofertas</router-link>
            </div>
          </div>
        </div>
      </div>

      <div class="alert alert-info mt-5 text-center">
        <i class="fas fa-info-circle me-2"></i>
        Promoções válidas enquanto durarem os estoques. Consulte termos e condições.
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Promotions',
  data() {
    return {
      promocoes: [
        { titulo: 'Medicamentos', descricao: 'Descontos especiais em medicamentos selecionados', desconto: 'Até 30% OFF', icon: 'fas fa-pills', bg: 'linear-gradient(135deg, #15803D, #22C55E)' },
        { titulo: 'Higiene Pessoal', descricao: 'Shampoos, sabonetes e muito mais', desconto: 'Até 25% OFF', icon: 'fas fa-soap', bg: 'linear-gradient(135deg, #0EA5E9, #38BDF8)' },
        { titulo: 'Vitaminas', descricao: 'Suplementos para sua saúde', desconto: 'Até 20% OFF', icon: 'fas fa-leaf', bg: 'linear-gradient(135deg, #D97706, #F59E0B)' },
        { titulo: 'Cosméticos', descricao: 'Protetores, hidratantes e dermocosméticos', desconto: 'Até 40% OFF', icon: 'fas fa-heart', bg: 'linear-gradient(135deg, #DC2626, #EF4444)' },
        { titulo: 'Maternidade', descricao: 'Fraldas, lenços e cuidados para o bebê', desconto: 'Até 15% OFF', icon: 'fas fa-baby', bg: 'linear-gradient(135deg, #7C3AED, #A78BFA)' },
        { titulo: 'Frete Grátis', descricao: 'Em pedidos acima de R$ 200,00', desconto: 'FRETE GRÁTIS', icon: 'fas fa-truck', bg: 'linear-gradient(135deg, #047857, #10B981)' }
      ]
    }
  }
}
</script>

<style scoped>
.promotions-page { min-height: 60vh; }
.promo-card { transition: transform 0.2s, box-shadow 0.2s; }
.promo-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.promo-icon {
  width: 56px; height: 56px; border-radius: 14px;
  display: inline-flex; align-items: center; justify-content: center;
}
</style>
