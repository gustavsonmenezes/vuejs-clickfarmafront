<template>
  <div class="about-page">
    <!-- Hero Section -->
    <section class="hero-section bg-primary text-white py-5">
      <div class="container">
        <div class="row align-items-center">
          <div class="col-lg-6">
            <h1 class="display-4 fw-bold mb-4">Sobre a ClickFarma</h1>
            <p class="lead mb-4">Mais de 20 anos cuidando da sua saúde e bem-estar com qualidade e confiança</p>
            <div class="d-flex gap-3">
              <router-link to="/products" class="btn btn-light btn-lg">
                <i class="fas fa-shopping-bag me-2"></i>Ver Produtos
              </router-link>
              <router-link to="/contact" class="btn btn-outline-light btn-lg">
                <i class="fas fa-phone me-2"></i>Fale Conosco
              </router-link>
            </div>
          </div>
          
        </div>
      </div>
    </section>

    <!-- Nossa História -->
    <section class="py-5">
      <div class="container">
        <div class="row">
          <div class="col-lg-10 mx-auto">
            <div class="row align-items-center">
              <div class="col-lg-6">
                <img src="https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80" 
                     alt="Farmácia moderna" class="img-fluid rounded shadow">
              </div>
              <div class="col-lg-6">
                <h2 class="fw-bold mb-4">Nossa História</h2>
                <p class="text-muted fs-5 mb-4">
                  Fundada em 2024, nossa farmácia nasceu com a missão de oferecer não apenas medicamentos, 
                  mas cuidado integral à saúde. Ao longo desses anos, crescemos e nos modernizamos, 
                  mas mantemos o mesmo compromisso com a qualidade e o atendimento humanizado.
                </p>
                <p class="text-muted fs-5">
                  Em 2025, lançamos nossa plataforma digital ClickFarma, unindo a tradição do atendimento 
                  farmacêutico com a tecnologia para oferecer mais comodidade e agilidade aos nossos clientes.
                </p>
              </div>
            </div>
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
                  <i class="fas fa-bolt fa-3x text-warning"></i>
                </div>
                <h5 class="fw-bold">Inovação Tecnológica</h5>
                <p class="text-muted">Plataforma moderna para oferecer a melhor experiência em compras online</p>
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
              <p class="small text-primary">CRF/PE 12345</p>
              <div class="social-links">
                <a href="#" class="text-muted me-2"><i class="fab fa-linkedin"></i></a>
                <a href="#" class="text-muted"><i class="fas fa-envelope"></i></a>
              </div>
            </div>
          </div>
          <div class="col-md-6 col-lg-3">
            <div class="team-card text-center">
              <img src="https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80" 
                   alt="Farmacêutico" class="img-fluid rounded-circle mb-3">
              <h5 class="fw-bold">Dr. João Santos</h5>
              <p class="text-muted">Farmacêutico Clínico</p>
              <p class="small text-primary">CRF/PE 67890</p>
              <div class="social-links">
                <a href="#" class="text-muted me-2"><i class="fab fa-linkedin"></i></a>
                <a href="#" class="text-muted"><i class="fas fa-envelope"></i></a>
              </div>
            </div>
          </div>
          <div class="col-md-6 col-lg-3">
            <div class="team-card text-center">
              <img src="https://images.unsplash.com/photo-1582750433449-648ed127bb54?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80" 
                   alt="Atendente" class="img-fluid rounded-circle mb-3">
              <h5 class="fw-bold">Ana Oliveira</h5>
              <p class="text-muted">Gerente de Atendimento</p>
              <p class="small text-success">10 anos de experiência</p>
              <div class="social-links">
                <a href="#" class="text-muted me-2"><i class="fab fa-linkedin"></i></a>
                <a href="#" class="text-muted"><i class="fas fa-envelope"></i></a>
              </div>
            </div>
          </div>
          <div class="col-md-6 col-lg-3">
            <div class="team-card text-center">
              <div class="developer-photo mb-3">
                <img src="/images/gustavsonbarros.jpg" 
                     alt="Desenvolvedor do Sistema" class="img-fluid rounded-circle">
                <span class="badge bg-primary developer-badge">DEV</span>
              </div>
              <h5 class="fw-bold">Gustavson Barros</h5>
              <p class="text-muted">Desenvolvedor Full Stack</p>
              <p class="small text-info">Sistema ClickFarma</p>
              <div class="social-links">
                <a href="https://github.com/gustavsonbarros" class="text-muted me-2" target="_blank">
                  <i class="fab fa-github"></i>
                </a>
                <a href="https://linkedin.com/in/gustavsonbarros" class="text-muted me-2" target="_blank">
                  <i class="fab fa-linkedin"></i>
                </a>
                <a href="mailto:gustavson.adm@gmail.com" class="text-muted">
                  <i class="fas fa-envelope"></i>
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Tecnologia -->
    <section class="py-5 bg-dark text-white">
      <div class="container">
        <div class="row align-items-center">
          <div class="col-lg-6">
            <h2 class="fw-bold mb-4">Tecnologia e Inovação</h2>
            <p class="fs-5 mb-4">
              Nossa plataforma ClickFarma foi desenvolvida com as mais modernas tecnologias para 
              oferecer uma experiência de compra segura, rápida e intuitiva.
            </p>
            <div class="tech-stack">
              <span class="badge bg-primary me-2 mb-2">Vue.js</span>
              <span class="badge bg-success me-2 mb-2">Node.js</span>
              <span class="badge bg-info me-2 mb-2">Bootstrap 5</span>
              <span class="badge bg-warning me-2 mb-2">JavaScript</span>
              <span class="badge bg-secondary me-2 mb-2">Vuex</span>
              <span class="badge bg-danger me-2 mb-2">Vue Router</span>
            </div>
          </div>
          <div class="col-lg-6 text-center">
            <i class="fas fa-laptop-code fa-8x text-primary mb-4"></i>
            <h4 class="fw-bold">Sistema 100% Digital</h4>
            <p class="text-light">Desenvolvido para sua comodidade e segurança</p>
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
            <div class="info-item mb-4">
              <i class="fas fa-envelope me-3"></i>
              <span>contato@clickfarma.com.br</span>
            </div>
            <div class="d-flex gap-3">
              <a href="#" class="btn btn-light">
                <i class="fab fa-whatsapp me-2"></i>WhatsApp
              </a>
              <a href="#" class="btn btn-outline-light">
                <i class="fas fa-directions me-2"></i>Como Chegar
              </a>
            </div>
          </div>
          <div class="col-lg-6 mt-4 mt-lg-0">
            <div class="map-container rounded shadow">
              <div class="bg-dark text-white rounded d-flex align-items-center justify-content-center" style="height: 300px;">
                <div class="text-center">
                  <i class="fas fa-map-marked-alt fa-3x mb-3"></i>
                  <p>Localização ClickFarma</p>
                  <small class="text-muted">Centro - Recife/PE</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Final -->
    <section class="py-5 bg-light">
      <div class="container text-center">
        <h2 class="fw-bold mb-3">Pronto para Experimentar?</h2>
        <p class="text-muted mb-4">Faça seu primeiro pedido e descubra a facilidade da ClickFarma</p>
        <router-link to="/products" class="btn btn-primary btn-lg">
          <i class="fas fa-shopping-bag me-2"></i>Começar a Comprar
        </router-link>
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
  background: linear-gradient(135deg, #198754 0%, #146c43 100%);
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

.developer-photo {
  position: relative;
  display: inline-block;
}

.developer-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 0.7rem;
  padding: 0.25rem 0.5rem;
}

.social-links {
  margin-top: 1rem;
}

.social-links a {
  transition: color 0.3s ease;
}

.social-links a:hover {
  color: #198754 !important;
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

.tech-stack {
  margin-top: 1.5rem;
}

.badge {
  font-size: 0.9rem;
  padding: 0.5rem 0.75rem;
}

.team-card {
  transition: all 0.3s ease;
}

.team-card:hover {
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .hero-section .btn {
    width: 100%;
    margin-bottom: 0.5rem;
  }
  
  .team-card {
    margin-bottom: 2rem;
  }
}
</style>