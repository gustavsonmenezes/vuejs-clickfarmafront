<template>
  <div class="perfil-page">
    <h2 class="page-title">👤 Meu perfil</h2>

    <div v-if="carregando" class="loading">Carregando...</div>

    <div v-if="entregador" class="perfil-card">
      <div class="avatar">
        <div class="avatar-circle">{{ initials }}</div>
      </div>
      <div class="status-badge" :class="entregador.status">
        {{ statusLabel }}
      </div>

      <div class="info-group">
        <div class="info-item">
          <span class="info-label">Nome</span>
          <span class="info-value">{{ entregador.nome }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">CPF</span>
          <span class="info-value">{{ formatCPF(entregador.cpf) }}</span>
        </div>
        <div class="info-item" v-if="entregador.telefone">
          <span class="info-label">Telefone</span>
          <span class="info-value">{{ entregador.telefone }}</span>
        </div>
        <div class="info-item" v-if="entregador.placaVeiculo">
          <span class="info-label">Veículo</span>
          <span class="info-value">{{ entregador.modeloVeiculo || '—' }} ({{ entregador.placaVeiculo }})</span>
        </div>
        <div class="info-item" v-if="entregador.chavePix">
          <span class="info-label">Chave PIX</span>
          <span class="info-value">{{ entregador.chavePix }}</span>
        </div>
      </div>
    </div>

    <button class="btn-editar" @click="editando = true" v-if="!editando">✏️ Editar perfil</button>

    <form v-if="editando" @submit.prevent="salvar" class="edit-form">
      <div class="form-group">
        <label>Nome</label>
        <input v-model="form.nome" class="form-control" required />
      </div>
      <div class="form-group">
        <label>Telefone</label>
        <input v-model="form.telefone" class="form-control" />
      </div>
      <div class="form-group">
        <label>Modelo do veículo</label>
        <input v-model="form.modeloVeiculo" class="form-control" />
      </div>
      <div class="form-group">
        <label>Placa</label>
        <input v-model="form.placaVeiculo" class="form-control" />
      </div>
      <div class="form-group">
        <label>Chave PIX</label>
        <input v-model="form.chavePix" class="form-control" />
      </div>
      <div class="form-group">
        <label>Nova senha (deixe em branco para manter)</label>
        <input v-model="form.senha" type="password" class="form-control" minlength="6" />
      </div>

      <p v-if="erro" class="error-msg">{{ erro }}</p>

      <div class="edit-actions">
        <button type="submit" class="btn-salvar" :disabled="salvando">
          {{ salvando ? 'Salvando...' : 'Salvar' }}
        </button>
        <button type="button" class="btn-cancelar" @click="editando = false">Cancelar</button>
      </div>
    </form>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'

export default {
  name: 'EntregadorPerfil',
  data() {
    return {
      entregador: null,
      carregando: true,
      editando: false,
      salvando: false,
      erro: '',
      form: { nome: '', telefone: '', modeloVeiculo: '', placaVeiculo: '', chavePix: '', senha: '' }
    }
  },
  async created() {
    await this.carregar()
  },
  computed: {
    initials() {
      if (!this.entregador?.nome) return '?'
      return this.entregador.nome.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase()
    },
    statusLabel() {
      const labels = { PENDENTE: 'Aguardando aprovação', ATIVO: 'Ativo', BLOQUEADO: 'Bloqueado' }
      return labels[this.entregador?.status] || this.entregador?.status
    }
  },
  methods: {
    async carregar() {
      try {
        const e = entregadorService.getEntregador()
        if (!e) { this.$router.push('/entregador/login'); return }
        this.entregador = await entregadorService.getPerfil(e.id)
      } catch (err) { console.error(err) }
      finally { this.carregando = false }
    },
    formatCPF(cpf) {
      if (!cpf) return ''
      return cpf.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4')
    },
    iniciarEdicao() {
      this.form = {
        nome: this.entregador.nome,
        telefone: this.entregador.telefone || '',
        modeloVeiculo: this.entregador.modeloVeiculo || '',
        placaVeiculo: this.entregador.placaVeiculo || '',
        chavePix: this.entregador.chavePix || '',
        senha: ''
      }
      this.editando = true
    },
    async salvar() {
      this.salvando = true
      this.erro = ''
      try {
        const dados = { ...this.form }
        if (!dados.senha) delete dados.senha
        this.entregador = await entregadorService.atualizarPerfil(this.entregador.id, dados)
        entregadorService.setEntregador(this.entregador)
        this.editando = false
      } catch (err) {
        this.erro = err.response?.data || 'Erro ao salvar'
      } finally {
        this.salvando = false
      }
    }
  }
}
</script>

<style scoped>
.perfil-page { padding-top: 4px; }
.page-title { font-size: 18px; color: #333; margin: 0 0 16px; }
.loading { text-align: center; padding: 40px; color: #999; }
.perfil-card { background: white; border-radius: 14px; padding: 24px 16px; text-align: center; margin-bottom: 16px; }
.avatar { margin-bottom: 12px; }
.avatar-circle {
  width: 72px; height: 72px; border-radius: 50%; background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; font-size: 28px; font-weight: 700; display: flex; align-items: center;
  justify-content: center; margin: 0 auto;
}
.status-badge {
  display: inline-block; padding: 4px 16px; border-radius: 20px; font-size: 12px;
  font-weight: 600; margin-bottom: 16px;
}
.status-badge.ATIVO { background: #d4edda; color: #155724; }
.status-badge.PENDENTE { background: #fff3cd; color: #856404; }
.status-badge.BLOQUEADO { background: #f8d7da; color: #721c24; }
.info-group { text-align: left; }
.info-item { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.info-item:last-child { border: none; }
.info-label { font-size: 13px; color: #888; }
.info-value { font-size: 13px; color: #333; font-weight: 500; }
.btn-editar {
  width: 100%; padding: 14px; border: 2px dashed #667eea; border-radius: 12px;
  background: white; color: #667eea; font-size: 14px; font-weight: 600;
  cursor: pointer; margin-bottom: 16px;
}
.edit-form { background: white; border-radius: 14px; padding: 16px; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }
.form-control {
  width: 100%; padding: 11px 14px; border: 2px solid #e0e0e0; border-radius: 10px;
  font-size: 14px; box-sizing: border-box;
}
.form-control:focus { outline: none; border-color: #667eea; }
.error-msg { color: #e74c3c; font-size: 13px; margin: 10px 0; }
.edit-actions { display: flex; gap: 8px; }
.btn-salvar {
  flex: 1; padding: 12px; background: #667eea; color: white; border: none;
  border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer;
}
.btn-cancelar {
  flex: 1; padding: 12px; background: white; color: #666; border: 2px solid #e0e0e0;
  border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer;
}
</style>
