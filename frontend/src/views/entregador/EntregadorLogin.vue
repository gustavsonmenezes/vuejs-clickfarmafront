<template>
  <div class="entregador-login-page">
    <div class="login-container">
      <div class="logo-area">
        <IconEntregador name="motorcycle" size="56" class="moto-icon" />
        <h1>ClickFarma Entregas</h1>
        <p class="subtitle">Faça login para começar</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>CPF</label>
          <input
            v-model="cpf"
            type="text"
            class="form-control"
            placeholder="000.000.000-00"
            maxlength="14"
            @input="formatCPF"
            required
          />
        </div>
        <div class="form-group">
          <label>Senha</label>
          <input
            v-model="senha"
            type="password"
            class="form-control"
            placeholder="Sua senha"
            required
          />
        </div>

        <p v-if="erro" class="error-msg">{{ erro }}</p>
        <p v-if="sucessoMsg" class="success-msg">{{ sucessoMsg }}</p>

        <button type="submit" class="btn-login" :disabled="carregando">
          <IconEntregador v-if="carregando" name="clock" size="18" />
          {{ carregando ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>

      <p class="cadastro-link">
        Novo por aqui?
        <router-link to="/entregador/cadastro">Cadastre-se</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'
import IconEntregador from '@/components/entregador/IconEntregador.vue'

export default {
  name: 'EntregadorLogin',
  components: { IconEntregador },
  data() {
    return {
      cpf: '',
      senha: '',
      erro: '',
      sucessoMsg: '',
      carregando: false
    }
  },
  methods: {
    formatCPF() {
      let v = this.cpf.replace(/\D/g, '').substring(0, 11)
      if (v.length > 9) v = v.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4')
      else if (v.length > 6) v = v.replace(/^(\d{3})(\d{3})(\d{1,3})$/, '$1.$2.$3')
      else if (v.length > 3) v = v.replace(/^(\d{3})(\d{1,3})$/, '$1.$2')
      this.cpf = v
    },
    async handleLogin() {
      this.erro = ''
      this.sucessoMsg = ''
      this.carregando = true
      try {
        const cpfLimpo = this.cpf.replace(/\D/g, '')
        await entregadorService.login(cpfLimpo, this.senha)
        this.$toast.success('Login realizado com sucesso!')
        this.$router.push('/entregador/corridas')
      } catch (err) {
        this.erro = err.response?.data || 'Erro ao fazer login'
      } finally {
        this.carregando = false
      }
    }
  }
}
</script>

<style scoped>
.entregador-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--cf-ora-600) 0%, var(--cf-ora-800) 100%);
  padding: 20px;
}
.login-container {
  background: var(--cf-surface);
  border-radius: 20px;
  padding: 40px 30px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.logo-area { text-align: center; margin-bottom: 30px; }
.moto-icon {
  color: var(--cf-ora-600);
  margin-bottom: 10px;
}
.logo-area h1 {
  font-size: 22px;
  font-weight: 700;
  color: var(--cf-ntr-800);
  margin: 0;
}
.subtitle {
  color: var(--cf-ntr-500);
  font-size: 14px;
  margin-top: 5px;
}
.form-group { margin-bottom: 20px; }
.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--cf-ntr-600);
  margin-bottom: 6px;
}
.form-control {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid var(--cf-ntr-200);
  border-radius: var(--cf-radius-lg);
  font-size: 15px;
  transition: border-color var(--cf-transition), box-shadow var(--cf-transition);
  box-sizing: border-box;
  font-family: var(--cf-font);
}
.form-control:focus {
  outline: none;
  border-color: var(--cf-ora-500);
  box-shadow: 0 0 0 3px var(--cf-ora-100);
}
.form-control::placeholder {
  color: var(--cf-ntr-300);
}
.btn-login {
  width: 100%;
  padding: 14px;
  background: var(--cf-ora-600);
  color: white;
  border: none;
  border-radius: var(--cf-radius-lg);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background var(--cf-transition), transform var(--cf-transition);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: var(--cf-font);
}
.btn-login:hover:not(:disabled) {
  background: var(--cf-ora-700);
  transform: translateY(-1px);
}
.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-login:focus-visible {
  outline: 2px solid white;
  outline-offset: 2px;
}
.error-msg {
  color: var(--cf-red-600);
  font-size: 13px;
  text-align: center;
  margin: 10px 0;
  background: var(--cf-red-50);
  padding: 8px 12px;
  border-radius: var(--cf-radius-md);
}
.success-msg {
  color: var(--cf-grn-700);
  font-size: 13px;
  text-align: center;
  margin: 10px 0;
  background: var(--cf-grn-50);
  padding: 8px 12px;
  border-radius: var(--cf-radius-md);
}
.cadastro-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--cf-ntr-500);
}
.cadastro-link a {
  color: var(--cf-ora-600);
  text-decoration: none;
  font-weight: 600;
}
.cadastro-link a:hover {
  text-decoration: underline;
}
</style>
