<template>
  <div class="entregador-login-page">
    <div class="login-container">
      <div class="logo-area">
        <div class="moto-icon">🏍️</div>
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

export default {
  name: 'EntregadorLogin',
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}
.login-container {
  background: white;
  border-radius: 20px;
  padding: 40px 30px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.logo-area { text-align: center; margin-bottom: 30px; }
.moto-icon { font-size: 48px; margin-bottom: 10px; }
.logo-area h1 { font-size: 22px; font-weight: 700; color: #333; margin: 0; }
.subtitle { color: #666; font-size: 14px; margin-top: 5px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }
.form-control {
  width: 100%; padding: 12px 16px; border: 2px solid #e0e0e0; border-radius: 12px;
  font-size: 15px; transition: border-color 0.2s; box-sizing: border-box;
}
.form-control:focus { outline: none; border-color: #667eea; }
.btn-login {
  width: 100%; padding: 14px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; border: none; border-radius: 12px; font-size: 16px; font-weight: 600;
  cursor: pointer; transition: opacity 0.2s;
}
.btn-login:hover { opacity: 0.9; }
.btn-login:disabled { opacity: 0.6; cursor: not-allowed; }
.error-msg { color: #e74c3c; font-size: 13px; text-align: center; margin: 10px 0; }
.success-msg { color: #27ae60; font-size: 13px; text-align: center; margin: 10px 0; }
.cadastro-link { text-align: center; margin-top: 20px; font-size: 14px; color: #666; }
.cadastro-link a { color: #667eea; text-decoration: none; font-weight: 600; }
</style>
