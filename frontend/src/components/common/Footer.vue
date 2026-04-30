<template>
  <footer class="cf-footer">

    <!-- Trust strip — 4 pilares de confiança -->
    <div class="cf-trust-band">
      <div class="container">
        <div class="cf-trust-grid">
          <div class="cf-trust-item" v-for="item in trustItems" :key="item.label">
            <div class="cf-trust-icon" v-html="item.svg"></div>
            <div class="cf-trust-text">
              <span class="cf-trust-title">{{ item.label }}</span>
              <span class="cf-trust-sub">{{ item.sub }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Corpo principal do footer -->
    <div class="cf-footer-body">
      <div class="container">
        <div class="row g-4 g-lg-5">

          <!-- Coluna marca -->
          <div class="col-lg-4 col-md-6">
            <div class="cf-footer-brand">
              <img src="/images/Logotipo.svg" alt="ClickFarma" class="footer-logo-img">
            </div>
            <p class="cf-footer-desc mt-3">
              Sua farmácia online de confiança. Dermocosméticos, medicamentos e bem-estar com qualidade e agilidade.
            </p>

            <!-- Redes sociais -->
            <div class="cf-social-row mt-4">
              <a href="#" class="cf-social" aria-label="Facebook">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"/>
                </svg>
              </a>
              <a href="#" class="cf-social" aria-label="Instagram">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <rect x="2" y="2" width="20" height="20" rx="5"/>
                  <circle cx="12" cy="12" r="4"/>
                  <circle cx="17.5" cy="6.5" r="1" fill="currentColor" stroke="none"/>
                </svg>
              </a>
              <a href="#" class="cf-social" aria-label="WhatsApp">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51a10 10 0 0 0-9.985 9.985c0 1.985.552 3.836 1.509 5.421L2 22l4.728-1.497A9.943 9.943 0 0 0 12 22c5.523 0 10-4.477 10-10S17.523 2 12 2a10 10 0 0 0-10 10c0 1.762.459 3.417 1.26 4.854z"/>
                </svg>
              </a>
            </div>
          </div>

          <!-- Loja -->
          <div class="col-lg-2 col-md-3 col-6">
            <h6 class="cf-footer-col-title">Loja</h6>
            <ul class="cf-footer-links">
              <li><router-link to="/" class="cf-footer-link">Home</router-link></li>
              <li><router-link to="/products" class="cf-footer-link">Produtos</router-link></li>
              <li><router-link to="/promotions" class="cf-footer-link">Promoções</router-link></li>
              <li><router-link to="/track-order" class="cf-footer-link">Rastrear Pedido</router-link></li>
              <li class="mt-2"><router-link to="/pharmacy/login" class="cf-footer-link text-warning"><i class="fas fa-store me-1"></i>Para Farmácias</router-link></li>
              <li><router-link to="/courier/login" class="cf-footer-link text-warning"><i class="fas fa-motorcycle me-1"></i>Para Entregadores</router-link></li>
            </ul>
          </div>

          <!-- Categorias -->
          <div class="col-lg-2 col-md-3 col-6">
            <h6 class="cf-footer-col-title">Categorias</h6>
            <ul class="cf-footer-links">
              <li><a href="#" class="cf-footer-link">Medicamentos</a></li>
              <li><a href="#" class="cf-footer-link">Dermocosméticos</a></li>
              <li><a href="#" class="cf-footer-link">Vitaminas</a></li>
              <li><a href="#" class="cf-footer-link">Maternidade</a></li>
            </ul>
          </div>

          <!-- Atendimento + Newsletter -->
          <div class="col-lg-4 col-md-6">
            <h6 class="cf-footer-col-title">Atendimento</h6>
            <div class="cf-contact-list">
              <div class="cf-contact-item">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.1 11.91a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
                <span>(81) 99818-9999</span>
              </div>
              <div class="cf-contact-item">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                <span>gustavson.adm@gmail.com</span>
              </div>
              <div class="cf-contact-item">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                <span>Seg–Sex 8h–20h · Sáb 9h–14h</span>
              </div>
            </div>

            <!-- Newsletter -->
            <div class="cf-newsletter mt-3">
              <p class="cf-newsletter-label">Receba ofertas exclusivas</p>
              <div class="cf-newsletter-form">
                <input type="email" class="cf-newsletter-input" placeholder="Seu e-mail" />
                <button class="cf-newsletter-btn">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="5" y1="12" x2="19" y2="12"/>
                    <polyline points="12 5 19 12 12 19"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- Barra inferior -->
    <div class="cf-footer-bottom">
      <div class="container d-flex flex-wrap justify-content-between align-items-center gap-2">
        <p class="cf-copyright">© 2026 <strong>ClickFarma</strong>. Todos os direitos reservados.</p>
        <div class="cf-legal">
          <a href="#" class="cf-legal-link">Privacidade</a>
          <span class="cf-sep">·</span>
          <a href="#" class="cf-legal-link">Termos</a>
          <span class="cf-sep">·</span>
          <a href="#" class="cf-legal-link">Cookies</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'Footer',
  data() {
    return {
      trustItems: [
        {
          label: 'Frete Grátis',
          sub: 'Acima de R$ 150',
          svg: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="1" y="3" width="15" height="13"/><path d="m16 8 6 2v8H16"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>`
        },
        {
          label: 'Produto Original',
          sub: 'Garantia de autenticidade',
          svg: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>`
        },
        {
          label: 'Devolução Grátis',
          sub: 'Prazo de 30 dias',
          svg: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 .49-4.07"/></svg>`
        },
        {
          label: 'Pagamento Seguro',
          sub: 'SSL e criptografia',
          svg: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M2 9h20"/><circle cx="7" cy="14" r="1" fill="currentColor" stroke="none"/></svg>`
        }
      ]
    }
  }
}
</script>

<style scoped>
/* ---- TRUST BAND ---- */
.cf-trust-band {
  background: var(--cf-green-xlight);
  border-top: 1px solid var(--cf-green-light);
  border-bottom: 1px solid var(--cf-green-light);
  padding: 1.4rem 0;
}

.cf-trust-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

.cf-trust-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cf-trust-icon {
  color: var(--cf-green);
  flex-shrink: 0;
  opacity: 0.85;
}

.cf-trust-title {
  display: block;
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--cf-text-dark);
}
.cf-trust-sub {
  display: block;
  font-size: 0.7rem;
  color: var(--cf-text-muted);
  margin-top: 1px;
}

/* ---- CORPO ---- */
.cf-footer-body {
  background: var(--cf-cream);
  border-top: 1px solid var(--cf-border);
  padding: 3.5rem 0 2.5rem;
}

/* ---- LOGO ---- */
.cf-footer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.footer-logo-img {
  max-height: 42px;
  width: auto;
  display: block;
}

.cf-footer-desc {
  font-size: 0.85rem;
  line-height: 1.75;
  color: var(--cf-text-muted);
  margin: 0;
  font-weight: 300;
  max-width: 300px;
}

/* ---- SOCIAL ---- */
.cf-social-row {
  display: flex;
  gap: 8px;
}
.cf-social {
  width: 34px;
  height: 34px;
  border: 1px solid var(--cf-border-mid);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cf-text-muted);
  text-decoration: none;
  background: var(--cf-white);
  transition: all 200ms var(--cf-ease);
}
.cf-social:hover {
  border-color: var(--cf-green);
  color: var(--cf-green);
  background: var(--cf-green-xlight);
  transform: translateY(-2px);
}

/* ---- COLUNAS ---- */
.cf-footer-col-title {
  font-family: var(--cf-sans);
  font-size: 0.68rem;
  font-weight: 500;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--cf-text-mid);
  margin-bottom: 1rem;
  position: relative;
  padding-bottom: 0.6rem;
}
.cf-footer-col-title::after {
  content: '';
  position: absolute;
  bottom: 0; left: 0;
  width: 18px; height: 1.5px;
  background: var(--cf-gold);
  border-radius: 1px;
}

.cf-footer-links {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}
.cf-footer-link {
  font-size: 0.85rem;
  color: var(--cf-text-muted);
  text-decoration: none;
  transition: color 160ms var(--cf-ease);
  font-weight: 300;
}
.cf-footer-link:hover {
  color: var(--cf-green);
}

/* ---- CONTATO ---- */
.cf-contact-list {
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}
.cf-contact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.83rem;
  color: var(--cf-text-muted);
  font-weight: 300;
}
.cf-contact-item svg {
  color: var(--cf-green);
  flex-shrink: 0;
  opacity: 0.8;
}

/* ---- NEWSLETTER ---- */
.cf-newsletter-label {
  font-size: 0.68rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--cf-text-muted);
  margin-bottom: 0.55rem;
  font-weight: 500;
}
.cf-newsletter-form {
  display: flex;
  border: 1px solid var(--cf-border-mid);
  border-radius: var(--cf-r-md);
  overflow: hidden;
  background: var(--cf-white);
  transition: border-color 180ms var(--cf-ease);
}
.cf-newsletter-form:focus-within {
  border-color: var(--cf-green);
  box-shadow: 0 0 0 3px rgba(42,92,69,0.07);
}
.cf-newsletter-input {
  flex: 1;
  background: transparent;
  border: none;
  padding: 0.58rem 0.85rem;
  color: var(--cf-text-dark);
  font-size: 0.83rem;
  font-family: var(--cf-sans);
  font-weight: 300;
  outline: none;
  min-width: 0;
}
.cf-newsletter-input::placeholder { color: var(--cf-text-faint); }

.cf-newsletter-btn {
  background: var(--cf-green);
  border: none;
  padding: 0 1rem;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 160ms var(--cf-ease);
}
.cf-newsletter-btn:hover { background: var(--cf-green-dark); }

/* ---- BOTTOM BAR ---- */
.cf-footer-bottom {
  background: var(--cf-white);
  border-top: 1px solid var(--cf-border);
  padding: 0.9rem 0;
}
.cf-copyright {
  font-size: 0.76rem;
  color: var(--cf-text-faint);
  margin: 0;
  font-weight: 300;
}
.cf-copyright strong {
  color: var(--cf-text-muted);
  font-weight: 500;
}

.cf-legal {
  display: flex;
  align-items: center;
  gap: 8px;
}
.cf-legal-link {
  font-size: 0.73rem;
  color: var(--cf-text-faint);
  text-decoration: none;
  transition: color 150ms var(--cf-ease);
}
.cf-legal-link:hover { color: var(--cf-green); }
.cf-sep { color: var(--cf-border-mid); font-size: 0.7rem; }

/* ---- RESPONSIVO ---- */
@media (max-width: 992px) {
  .cf-trust-grid { grid-template-columns: repeat(2, 1fr); gap: 1rem 1.5rem; }
}
@media (max-width: 768px) {
  .cf-footer-body { padding: 2.5rem 0 2rem; }
  .cf-footer-links { margin-bottom: 1.5rem; }
}
@media (max-width: 576px) {
  .cf-trust-grid { grid-template-columns: 1fr; gap: 0.75rem; }
  .cf-footer-body { padding: 2rem 0 1.5rem; }
  .cf-footer-desc { max-width: 100%; text-align: center; }
  .cf-footer-brand { justify-content: center; }
  .cf-social-row { justify-content: center; }
  .cf-footer-col-title { text-align: center; }
  .cf-footer-col-title::after { left: 50%; transform: translateX(-50%); }
  .cf-footer-links { align-items: center; }
  .cf-contact-item { justify-content: center; }
  .cf-newsletter-label { text-align: center; }
  .cf-copyright { text-align: center; width: 100%; }
  .cf-legal { width: 100%; justify-content: center; }
}
</style>