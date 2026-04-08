<template>
  <footer class="footer-custom">
    <div class="container">
      <div class="footer-content">
        <!-- Main Footer Sections -->
        <div class="row g-4">
          <!-- Company Info -->
          <div class="col-lg-4 col-md-6">
            <div class="footer-section">
              <div class="brand-footer mb-3">
                <span class="footer-logo-icon">💊</span>
                <span class="footer-brand-text">ClickFarma</span>
              </div>
              <p class="footer-description">
                Sua farmácia online de confiança, levando saúde e bem-estar 
                até você com qualidade e agilidade.
              </p>
              <div class="social-links">
                <a href="#" class="social-link" title="Facebook">
                  <i class="fab fa-facebook-f"></i>
                </a>
                <a href="#" class="social-link" title="Instagram">
                  <i class="fab fa-instagram"></i>
                </a>
                <a href="#" class="social-link" title="WhatsApp">
                  <i class="fab fa-whatsapp"></i>
                </a>
                <a href="#" class="social-link" title="Twitter">
                  <i class="fab fa-twitter"></i>
                </a>
              </div>
            </div>
          </div>

          <!-- Quick Links -->
          <div class="col-lg-2 col-md-6">
            <div class="footer-section">
              <h5 class="footer-title">Navegação</h5>
              <ul class="footer-links">
                <li><router-link to="/" class="footer-link">🏠 Home</router-link></li>
                <li><router-link to="/products" class="footer-link">💊 Produtos</router-link></li>
                <li><router-link to="/promotions" class="footer-link">💰 Promoções</router-link></li>
                <li><router-link to="/about" class="footer-link">ℹ️ Sobre Nós</router-link></li>
                <li><router-link to="/contact" class="footer-link">📞 Contato</router-link></li>
              </ul>
            </div>
          </div>

          <!-- Categories -->
          <div class="col-lg-3 col-md-6">
            <div class="footer-section">
              <h5 class="footer-title">Categorias</h5>
              <ul class="footer-links">
                <li><router-link to="/products?category=Medicamentos" class="footer-link">💊 Medicamentos</router-link></li>
                <li><router-link to="/products?category=Vitaminas" class="footer-link">🌿 Vitaminas</router-link></li>
                <li><router-link to="/products?category=Higiene" class="footer-link">🚿 Higiene Pessoal</router-link></li>
                <li><router-link to="/products?category=Cosméticos" class="footer-link">🧴 Cosméticos</router-link></li>
                <li><router-link to="/products?category=Maternidade" class="footer-link">👶 Maternidade</router-link></li>
              </ul>
            </div>
          </div>

          <!-- Contact & Support -->
          <div class="col-lg-3 col-md-6">
            <div class="footer-section">
              <h5 class="footer-title">Atendimento</h5>
              <div class="contact-info">
                <div class="contact-item">
                  <i class="fas fa-phone contact-icon"></i>
                  <div>
                    <div class="contact-label">Telefone</div>
                    <div class="contact-value">(81) 99818-9999</div>
                  </div>
                </div>
                <div class="contact-item">
                  <i class="fas fa-envelope contact-icon"></i>
                  <div>
                    <div class="contact-label">E-mail</div>
                    <div class="contact-value">gustavson.adm@gmail.com</div>
                  </div>
                </div>
                <div class="contact-item">
                  <i class="fas fa-clock contact-icon"></i>
                  <div>
                    <div class="contact-label">Horário</div>
                    <div class="contact-value">24 horas</div>
                  </div>
                </div>
                <div class="contact-item">
                  <i class="fas fa-map-marker-alt contact-icon"></i>
                  <div>
                    <div class="contact-label">Localização</div>
                    <div class="contact-value">Recife - PE</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Divider -->
        <hr class="footer-divider">

        <!-- Bottom Footer -->
        <div class="footer-bottom">
          <div class="row align-items-center">
            <div class="col-md-6">
              <p class="copyright">
                &copy; 2024 <strong>ClickFarma</strong>. Todos os direitos reservados.
              </p>
            </div>
            <div class="col-md-6 text-md-end">
              <div class="developer-credit">
                <span class="credit-text">Desenvolvido com</span>
                <i class="fas fa-heart text-danger mx-1"></i>
                <span class="credit-text">por <strong>Gustavson Barros</strong></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'Footer'
}
</script>

<style scoped>
.footer-custom {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  color: #ecf0f1;
  margin-top: auto;
}

.footer-content {
  padding: 3rem 0 1.5rem;
}

.footer-section {
  margin-bottom: 1.5rem;
}

.brand-footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.footer-logo-icon {
  font-size: 2rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.footer-brand-text {
  font-family: 'Cormorant', serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
  letter-spacing: -0.02em;
}

.footer-description {
  color: #bdc3c7;
  line-height: 1.8;
  margin-bottom: 1.5rem;
  font-size: 0.95rem;
}

.social-links {
  display: flex;
  gap: 0.8rem;
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: rgba(212, 175, 55, 0.15);
  color: #d4af37;
  border-radius: 8px;
  text-decoration: none;
  transition: all 250ms ease-in-out;
  font-size: 1rem;
}

.social-link:hover {
  background: #d4af37;
  color: #1a1a1a;
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(212, 175, 55, 0.3);
}

.footer-title {
  font-family: 'Cormorant', serif;
  color: white;
  font-weight: 600;
  margin-bottom: 1.2rem;
  font-size: 1.1rem;
  position: relative;
  padding-bottom: 0.5rem;
  letter-spacing: -0.01em;
}

.footer-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 2px;
  background: #d4af37;
  border-radius: 2px;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin-bottom: 0.7rem;
}

.footer-link {
  color: #bdc3c7;
  text-decoration: none;
  transition: all 250ms ease-in-out;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
}

.footer-link:hover {
  color: #d4af37;
  transform: translateX(5px);
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.contact-item {
  display: flex;
  align-items: flex-start;
  gap: 0.8rem;
}

.contact-icon {
  color: #d4af37;
  margin-top: 0.2rem;
  min-width: 16px;
}

.contact-label {
  font-size: 0.8rem;
  color: #95a5a6;
  margin-bottom: 0.2rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: 600;
}

.contact-value {
  color: #ecf0f1;
  font-weight: 500;
  font-size: 0.95rem;
}

.footer-divider {
  border-color: rgba(255, 255, 255, 0.1);
  margin: 2rem 0 1.5rem;
}

.footer-bottom {
  padding-top: 1rem;
}

.copyright {
  color: #95a5a6;
  margin: 0;
  font-size: 0.9rem;
}

.developer-credit {
  color: #95a5a6;
  font-size: 0.9rem;
}

.credit-text {
  color: #bdc3c7;
}

/* Responsividade */
@media (max-width: 768px) {
  .footer-content {
    padding: 2rem 0 1rem;
  }
  
  .footer-section {
    text-align: center;
  }
  
  .footer-title::after {
    left: 50%;
    transform: translateX(-50%);
  }
  
  .social-links {
    justify-content: center;
  }
  
  .contact-item {
    justify-content: center;
    text-align: center;
  }
  
  .developer-credit {
    text-align: center !important;
    margin-top: 1rem;
  }
}
</style>
