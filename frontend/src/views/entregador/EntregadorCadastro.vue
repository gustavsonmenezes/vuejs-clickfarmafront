<template>
  <div class="cadastro-page">
    <div class="cadastro-container">
      <div class="logo-area">
        <div class="moto-icon">🏍️</div>
        <h1>Cadastro Entregador</h1>
        <p class="subtitle">Preencha os dados para se cadastrar</p>
      </div>

      <form @submit.prevent="handleCadastro" class="cadastro-form">
        <div class="form-group">
          <label>Nome completo</label>
          <input v-model="form.nome" type="text" class="form-control" placeholder="Seu nome" required />
        </div>
        <div class="form-group">
          <label>CPF</label>
          <input v-model="form.cpf" type="text" class="form-control" placeholder="000.000.000-00" maxlength="14" @input="formatCPF" required />
        </div>
        <div class="form-group">
          <label>Senha</label>
          <input v-model="form.senha" type="password" class="form-control" placeholder="Mínimo 6 caracteres" minlength="6" required />
        </div>
        <div class="form-group">
          <label>Telefone</label>
          <input v-model="form.telefone" type="text" class="form-control" placeholder="(00) 00000-0000" />
        </div>
        <div class="form-group">
          <label>CNH</label>
          <input v-model="form.cnh" type="text" class="form-control" placeholder="Número da CNH" />
        </div>
        <div class="row">
          <div class="col-6">
            <div class="form-group">
              <label>Placa do veículo</label>
              <input v-model="form.placaVeiculo" type="text" class="form-control" placeholder="ABC-1234" />
            </div>
          </div>
          <div class="col-6">
            <div class="form-group">
              <label>Modelo</label>
              <input v-model="form.modeloVeiculo" type="text" class="form-control" placeholder="Ex: Honda CG 160" />
            </div>
          </div>
        </div>
        <div class="form-group">
          <label>Chave PIX</label>
          <input v-model="form.chavePix" type="text" class="form-control" placeholder="CPF, telefone ou e-mail" />
        </div>

        <p v-if="erro" class="error-msg">{{ erro }}</p>
        <p v-if="sucessoMsg" class="success-msg">{{ sucessoMsg }}</p>

        <button type="submit" class="btn-cadastrar" :disabled="carregando">
          {{ carregando ? 'Cadastrando...' : 'Cadastrar' }}
        </button>
      </form>

      <p class="login-link">
        Já tem conta?
        <router-link to="/entregador/login">Faça login</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'

export default {
  name: 'EntregadorCadastro',
  data() {
    return {
      form: { nome: '', cpf: '', senha: '', telefone: '', cnh: '', placaVeiculo: '', modeloVeiculo: '', chavePix: '' },
      erro: '',
      sucessoMsg: '',
      carregando: false
    }
  },
  methods: {
    formatCPF() {
      let v = this.form.cpf.replace(/\D/g, '').substring(0, 11)
      if (v.length > 9) v = v.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4')
      else if (v.length > 6) v = v.replace(/^(\d{3})(\d{3})(\d{1,3})$/, '$1.$2.$3')
      else if (v.length > 3) v = v.replace(/^(\d{3})(\d{1,3})$/, '$1.$2')
      this.form.cpf = v
    },
    async handleCadastro() {
      this.erro = ''
      this.sucessoMsg = ''
      this.carregando = true
      try {
        const dados = { ...this.form, cpf: this.form.cpf.replace(/\D/g, '') }
        await entregadorService.cadastro(dados)
        this.sucessoMsg = 'Cadastro realizado! Aguarde aprovação.'
        setTimeout(() => this.$router.push('/entregador/login'), 3000)
      } catch (err) {
        this.erro = err.response?.data || 'Erro ao cadastrar'
      } finally {
        this.carregando = false
      }
    }
  }
}
</script>

<style scoped>
.cadastro-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}
.cadastro-container {
  background: white;
  border-radius: 20px;
  padding: 40px 30px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.logo-area { text-align: center; margin-bottom: 30px; }
.moto-icon { font-size: 48px; margin-bottom: 10px; }
.logo-area h1 { font-size: 22px; font-weight: 700; color: #333; margin: 0; }
.subtitle { color: #666; font-size: 14px; margin-top: 5px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }
.form-control {
  width: 100%; padding: 11px 14px; border: 2px solid #e0e0e0; border-radius: 10px;
  font-size: 14px; transition: border-color 0.2s; box-sizing: border-box;
}
.form-control:focus { outline: none; border-color: #667eea; }
.row { display: flex; gap: 12px; }
.col-6 { flex: 1; }
.btn-cadastrar {
  width: 100%; padding: 14px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; border: none; border-radius: 12px; font-size: 16px; font-weight: 600;
  cursor: pointer; transition: opacity 0.2s;
}
.btn-cadastrar:hover { opacity: 0.9; }
.btn-cadastrar:disabled { opacity: 0.6; cursor: not-allowed; }
.error-msg { color: #e74c3c; font-size: 13px; text-align: center; margin: 10px 0; }
.success-msg { color: #27ae60; font-size: 13px; text-align: center; margin: 10px 0; }
.login-link { text-align: center; margin-top: 20px; font-size: 14px; color: #666; }
.login-link a { color: #667eea; text-decoration: none; font-weight: 600; }
</style>
