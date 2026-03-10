<template>
  <div class="container mt-4">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="card">
          <div class="card-header text-center">
            <h4>🔒 Alterar Senha</h4>
          </div>
          <div class="card-body">
            <form @submit.prevent="changePassword">
              <div class="mb-3">
                <label class="form-label">Senha Atual</label>
                <input 
                  v-model="passwordForm.currentPassword" 
                  type="password" 
                  class="form-control" 
                  required
                >
              </div>
              <div class="mb-3">
                <label class="form-label">Nova Senha</label>
                <input 
                  v-model="passwordForm.newPassword" 
                  type="password" 
                  class="form-control" 
                  minlength="6"
                  required
                >
                <div class="form-text">A senha deve ter pelo menos 6 caracteres.</div>
              </div>
              <div class="mb-3">
                <label class="form-label">Confirmar Nova Senha</label>
                <input 
                  v-model="passwordForm.confirmPassword" 
                  type="password" 
                  class="form-control" 
                  required
                >
                <div v-if="passwordForm.newPassword && passwordForm.confirmPassword && passwordForm.newPassword !== passwordForm.confirmPassword" 
                     class="text-danger small">
                  As senhas não coincidem.
                </div>
              </div>
              <button 
                type="submit" 
                class="btn btn-primary w-100"
                :disabled="!isFormValid"
              >
                Alterar Senha
              </button>
            </form>
            <div class="text-center mt-3">
              <router-link to="/profile" class="btn btn-link">
                Voltar ao Perfil
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ResetPassword',
  data() {
    return {
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  computed: {
    isFormValid() {
      return this.passwordForm.currentPassword &&
             this.passwordForm.newPassword &&
             this.passwordForm.confirmPassword &&
             this.passwordForm.newPassword === this.passwordForm.confirmPassword &&
             this.passwordForm.newPassword.length >= 6;
    }
  },
  methods: {
    changePassword() {
      if (!this.isFormValid) {
        alert('Por favor, preencha todos os campos corretamente.');
        return;
      }
      
      // Lógica para alterar a senha (simulada)
      alert('Senha alterada com sucesso!');
      this.passwordForm = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      };
      this.$router.push('/profile');
    }
  }
}
</script>
