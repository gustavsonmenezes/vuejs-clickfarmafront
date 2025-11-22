<template>
  <div class="about-page">
    <!-- Hero Section -->
    <section class="hero-section bg-primary text-white py-5">
      <div class="container">
        <div class="row align-items-center">
          <div class="col-lg-6">
            <h1 class="display-4 fw-bold mb-4">Sobre Nossa Farmácia</h1>
            <p class="lead mb-4">Mais de 20 anos cuidando da sua saúde e bem-estar com qualidade e confiança</p>
          </div>
          <div class="col-lg-6">
            <img src="https://images.unsplash.com/photo-1585435557343-3b092031d5ad?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80" 
                 alt="Nossa Farmácia" class="img-fluid rounded shadow">
          </div>
        </div>
      </div>
    </section>

    <!-- Nossa História -->
    <section class="py-5">
      <div class="container">
        <div class="row">
          <div class="col-lg-8 mx-auto text-center">
            <h2 class="fw-bold mb-4">Nossa História</h2>
            <p class="text-muted fs-5">
              Fundada em 2003, nossa farmácia nasceu com a missão de oferecer não apenas medicamentos, 
              mas cuidado integral à saúde. Ao longo desses anos, crescemos e nos modernizamos, 
              mas mantemos o mesmo compromisso com a qualidade e o atendimento humanizado.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Valores -->
    <section class="py-5 bg-light">
      <div class="container">
        <h2 class="text-center fw-bold mb-5">Nossos Valores</h2>
        <div class="row g-4">
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm">
              <div class="card-body text-center p-4">
                <div class="icon-wrapper mb-3">
                  <i class="fas fa-shield-alt fa-3x text-primary"></i>
                </div>
                <h5 class="fw-bold">Qualidade Garantida</h5>
                <p class="text-muted">Trabalhamos apenas com produtos de laboratórios reconhecidos e certificados pela ANVISA</p>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm">
              <div class="card-body text-center p-4">
                <div class="icon-wrapper mb-3">
                  <i class="fas fa-heart fa-3x text-danger"></i>
                </div>
                <h5 class="fw-bold">Atendimento Humanizado</h5>
                <p class="text-muted">Nossa equipe é treinada para oferecer orientação personalizada e acolhedora</p>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm">
              <div class="card-body text-center p-4">
                <div class="icon-wrapper mb-3">
                  <i class="fas fa-truck fa-3x text-success"></i>
                </div>
                <h5 class="fw-bold">Comodidade</h5>
                <p class="text-muted">Entregamos em toda a cidade e oferecemos retirada rápida na loja</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Equipe -->
    <section class="py-5">
      <div class="container">
        <h2 class="text-center fw-bold mb-5">Nossa Equipe</h2>
        <div class="row g-4">
          <div class="col-md-6 col-lg-3">
            <div class="team-card text-center">
              <img src="https://images.unsplash.com/photo-1559839734-2b71ea197ec2?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80" 
                   alt="Farmacêutica" class="img-fluid rounded-circle mb-3">
              <h5 class="fw-bold">Dra. Maria Silva</h5>
              <p class="text-muted">Farmacêutica Responsável</p>
              <p class="small">CRF/PE 12345</p>
            </div>
          </div>
          <div class="col-md-6 col-lg-3">
            <div class="team-card text-center">
              <img src="https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80" 
                   alt="Farmacêutico" class="img-fluid rounded-circle mb-3">
              <h5 class="fw-bold">Dr. João Santos</h5>
              <p class="text-muted">Farmacêutico Clínico</p>
              <p class="small">CRF/PE 67890</p>
            </div>
          </div>
          <div class="col-md-6 col-lg-3">
            <div class="team-card text-center">
              <img src="https://images.unsplash.com/photo-1582750433449-648ed127bb54?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80" 
                   alt="Atendente" class="img-fluid rounded-circle mb-3">
              <h5 class="fw-bold">Ana Oliveira</h5>
              <p class="text-muted">Gerente de Atendimento</p>
              <p class="small">10 anos de experiência</p>
            </div>
          </div>
          <div class="col-md-6 col-lg-3">
            <div class="team-card text-center">
              <img src="https://images.unsplash.com/photo-1576091160399-112ba8d25d1f?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80" 
                   alt="Entregador" class="img-fluid rounded-circle mb-3">
              <h5 class="fw-bold">Carlos Mendes</h5>
              <p class="text-muted">Coordenador de Entregas</p>
              <p class="small">Entrega rápida e segura</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Localização -->
    <section class="py-5 bg-primary text-white">
      <div class="container">
        <div class="row align-items-center">
          <div class="col-lg-6">
            <h2 class="fw-bold mb-4">Venha nos Visitar</h2>
            <div class="info-item mb-3">
              <i class="fas fa-map-marker-alt me-3"></i>
              <span>Rua das Flores, 123 - Centro, Recife - PE</span>
            </div>
            <div class="info-item mb-3">
              <i class="fas fa-clock me-3"></i>
              <span>Segunda a Sábado: 7h às 22h | Domingos: 8h às 20h</span>
            </div>
            <div class="info-item mb-3">
              <i class="fas fa-phone me-3"></i>
              <span>(81) 3333-4444</span>
            </div>
            <div class="info-item">
              <i class="fas fa-envelope me-3"></i>
              <span>contato@farmaciaexemplo.com.br</span>
            </div>
          </div>
          <div class="col-lg-6 mt-4 mt-lg-0">
            <div class="map-container rounded shadow">
              <!-- Mapa placeholder - você pode integrar Google Maps aqui -->
              <div class="bg-dark text-white rounded d-flex align-items-center justify-content-center" style="height: 300px;">
                <div class="text-center">
                  <i class="fas fa-map fa-3x mb-3"></i>
                  <p>Mapa da Localização</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'About'
}
</script>

<style scoped>
.hero-section {
  background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%);
}

.icon-wrapper {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.team-card img {
  width: 150px;
  height: 150px;
  object-fit: cover;
}

.info-item {
  font-size: 1.1rem;
}

.card {
  transition: transform 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
}

.map-container {
  overflow: hidden;
}
</style>