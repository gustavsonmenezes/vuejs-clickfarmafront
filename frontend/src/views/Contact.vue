<template>
  <div class="contact-page">
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-lg-8">
          <div class="text-center mb-5">
            <i class="fas fa-headset text-primary" style="font-size: 3rem;"></i>
            <h1 class="mt-3 fw-bold">Fale Conosco</h1>
            <p class="lead text-muted">Estamos aqui para ajudar</p>
          </div>

          <div class="row g-4 mb-5">
            <div class="col-md-4">
              <div class="card border-0 shadow-sm h-100 text-center p-4">
                <i class="fas fa-phone text-primary mb-3" style="font-size: 1.5rem;"></i>
                <h6 class="fw-semibold">Telefone</h6>
                <p class="text-muted mb-0 small">(81) 3000-0000</p>
                <p class="text-muted small">Seg-Sex 8h-18h</p>
              </div>
            </div>
            <div class="col-md-4">
              <div class="card border-0 shadow-sm h-100 text-center p-4">
                <i class="fab fa-whatsapp text-success mb-3" style="font-size: 1.5rem;"></i>
                <h6 class="fw-semibold">WhatsApp</h6>
                <p class="text-muted mb-0 small">(81) 9 9999-0000</p>
                <p class="text-muted small">Atendimento 24h</p>
              </div>
            </div>
            <div class="col-md-4">
              <div class="card border-0 shadow-sm h-100 text-center p-4">
                <i class="fas fa-envelope text-info mb-3" style="font-size: 1.5rem;"></i>
                <h6 class="fw-semibold">Email</h6>
                <p class="text-muted mb-0 small">contato@clickfarma.com.br</p>
                <p class="text-muted small">Respondemos em até 24h</p>
              </div>
            </div>
          </div>

          <div class="card border-0 shadow-sm">
            <div class="card-body p-4">
              <h5 class="fw-semibold mb-4">Envie uma mensagem</h5>
              <form @submit.prevent="enviar">
                <div class="row g-3">
                  <div class="col-sm-6">
                    <input v-model="form.nome" class="form-control" placeholder="Seu nome" required />
                  </div>
                  <div class="col-sm-6">
                    <input v-model="form.email" type="email" class="form-control" placeholder="Seu email" required />
                  </div>
                  <div class="col-12">
                    <select v-model="form.assunto" class="form-select" required>
                      <option value="">Selecione um assunto</option>
                      <option value="duvida">Dúvida sobre produto</option>
                      <option value="pedido">Problema com pedido</option>
                      <option value="entrega">Problema com entrega</option>
                      <option value="sugestao">Sugestão</option>
                      <option value="outro">Outro</option>
                    </select>
                  </div>
                  <div class="col-12">
                    <textarea v-model="form.mensagem" class="form-control" rows="4" placeholder="Sua mensagem" required></textarea>
                  </div>
                  <div class="col-12">
                    <button type="submit" class="btn btn-primary px-4" :disabled="enviando">
                      <span v-if="enviando" class="spinner-border spinner-border-sm me-2"></span>
                      {{ enviando ? 'Enviando...' : 'Enviar mensagem' }}
                    </button>
                  </div>
                  <div v-if="enviado" class="col-12">
                    <div class="alert alert-success mb-0">Mensagem enviada com sucesso! Entraremos em contato em breve.</div>
                  </div>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Contact',
  data() {
    return {
      form: { nome: '', email: '', assunto: '', mensagem: '' },
      enviando: false,
      enviado: false
    }
  },
  methods: {
    async enviar() {
      this.enviando = true;
      await new Promise(r => setTimeout(r, 1000));
      this.enviado = true;
      this.enviando = false;
      this.form = { nome: '', email: '', assunto: '', mensagem: '' };
    }
  }
}
</script>

<style scoped>
.contact-page { min-height: 60vh; }
.form-control, .form-select {
  border-radius: 8px; padding: 10px 14px;
  border: 1px solid #e2e8f0; font-size: 0.9rem;
}
.form-control:focus, .form-select:focus {
  border-color: var(--cf-primary-500);
  box-shadow: 0 0 0 3px rgba(21,128,61,0.1);
}
</style>
