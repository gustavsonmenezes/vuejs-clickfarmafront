<template>
  <div class="entregador-cadastro-page">
    <div class="cadastro-container">
      <div class="logo-area">
        <IconEntregador name="motorcycle" size="56" class="moto-icon" />
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
          <input v-model="form.telefone" type="tel" class="form-control" placeholder="(00) 00000-0000" @input="formatPhone" maxlength="15" />
        </div>
        <div class="form-group">
          <label>CNH</label>
          <input v-model="form.cnh" type="text" class="form-control" placeholder="Número da CNH" inputmode="numeric" />
        </div>
        <div class="row">
          <div class="col-6">
            <div class="form-group">
              <label>Placa</label>
              <input v-model="form.placaVeiculo" type="text" class="form-control" placeholder="ABC-1234" @input="formatPlate" maxlength="8" />
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
          <IconEntregador v-if="carregando" name="clock" size="18" />
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
import IconEntregador from '@/components/entregador/IconEntregador.vue'

export default {
  name: 'EntregadorCadastro',
  components: { IconEntregador },
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
    formatPhone() {
      let v = this.form.telefone.replace(/\D/g, '').substring(0, 11)
      if (v.length > 10) {
        v = v.replace(/^(\d{2})(\d{5})(\d{4})$/, '($1) $2-$3')
      } else if (v.length > 6) {
        v = v.replace(/^(\d{2})(\d{4})(\d{0,4})$/, '($1) $2-$3')
      } else if (v.length > 2) {
        v = v.replace(/^(\d{2})(\d{0,5})$/, '($1) $2')
      }
      this.form.telefone = v
    },
    formatPlate() {
      let v = this.form.placaVeiculo.toUpperCase().replace(/[^A-Z0-9]/g, '').substring(0, 7)
      if (v.length > 3) {
        v = v.replace(/^([A-Z]{3})([A-Z0-9]{0,4})$/, '$1-$2')
      }
      this.form.placaVeiculo = v
    },
    async handleCadastro() {
      this.erro = ''
      this.sucessoMsg = ''
      this.carregando = true
      try {
        const dados = { ...this.form, cpf: this.form.cpf.replace(/\D/g, '') }
        await entregadorService.cadastro(dados)
        this.sucessoMsg = 'Cadastro realizado! Aguarde aprovação.'
        this.$toast.success('Cadastro realizado com sucesso!')
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
.entregador-cadastro-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--cf-ora-600) 0%, var(--cf-ora-800) 100%);
  padding: 20px;
}
.cadastro-container {
  background: var(--cf-surface);
  border-radius: 20px;
  padding: 40px 30px;
  width: 100%;
  max-width: 480px;
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
.form-group { margin-bottom: 16px; }
.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--cf-ntr-600);
  margin-bottom: 6px;
}
.form-control {
  width: 100%;
  padding: 11px 14px;
  border: 2px solid var(--cf-ntr-200);
  border-radius: var(--cf-radius-lg);
  font-size: 14px;
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
.row { display: flex; gap: 12px; }
.col-6 { flex: 1; }
.btn-cadastrar {
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
.btn-cadastrar:hover:not(:disabled) {
  background: var(--cf-ora-700);
  transform: translateY(-1px);
}
.btn-cadastrar:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-cadastrar:focus-visible {
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
.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--cf-ntr-500);
}
.login-link a {
  color: var(--cf-ora-600);
  text-decoration: none;
  font-weight: 600;
}
.login-link a:hover {
  text-decoration: underline;
}
</style>
