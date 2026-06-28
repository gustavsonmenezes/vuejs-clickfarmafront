<template>
  <div class="perfil-page">
    <h2 class="page-title">
      <IconEntregador name="user" size="20" />
      Meu perfil
    </h2>

    <div v-if="carregando" class="skeleton-perfil">
      <div class="skeleton-avatar"></div>
      <div class="skeleton-line w-40"></div>
      <div class="skeleton-line w-60"></div>
      <div class="skeleton-line w-80"></div>
      <div class="skeleton-line w-50"></div>
    </div>

    <div v-if="entregador" class="perfil-card">
      <div class="avatar">
        <div class="avatar-circle">{{ initials }}</div>
      </div>
      <div class="status-badge" :class="entregador.status">
        {{ statusLabel }}
      </div>

      <div class="info-group">
        <div class="info-item">
          <span class="info-label">
            <IconEntregador name="user" size="14" />
            Nome
          </span>
          <span class="info-value">{{ entregador.nome }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">
            <IconEntregador name="credit-card" size="14" />
            CPF
          </span>
          <span class="info-value">{{ formatCPF(entregador.cpf) }}</span>
        </div>
        <div class="info-item" v-if="entregador.telefone">
          <span class="info-label">
            <IconEntregador name="phone" size="14" />
            Telefone
          </span>
          <span class="info-value">{{ entregador.telefone }}</span>
        </div>
        <div class="info-item" v-if="entregador.placaVeiculo">
          <span class="info-label">
            <IconEntregador name="truck" size="14" />
            Veículo
          </span>
          <span class="info-value">{{ entregador.modeloVeiculo || '—' }} ({{ entregador.placaVeiculo }})</span>
        </div>
        <div class="info-item" v-if="entregador.chavePix">
          <span class="info-label">
            <IconEntregador name="dollar" size="14" />
            Chave PIX
          </span>
          <span class="info-value">{{ entregador.chavePix }}</span>
        </div>
      </div>
    </div>

    <button class="btn-editar" @click="iniciarEdicao" v-if="!editando">
      <IconEntregador name="user" size="16" />
      Editar perfil
    </button>

    <form v-if="editando" @submit.prevent="salvar" class="edit-form">
      <div class="form-group">
        <label>Nome</label>
        <input v-model="form.nome" class="form-control" required />
      </div>
      <div class="form-group">
        <label>Telefone</label>
        <input v-model="form.telefone" class="form-control" @input="formatPhone" maxlength="15" />
      </div>
      <div class="form-group">
        <label>Modelo do veículo</label>
        <input v-model="form.modeloVeiculo" class="form-control" />
      </div>
      <div class="form-group">
        <label>Placa</label>
        <input v-model="form.placaVeiculo" class="form-control" @input="formatPlate" maxlength="8" />
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
          <IconEntregador v-if="salvando" name="clock" size="16" />
          {{ salvando ? 'Salvando...' : 'Salvar' }}
        </button>
        <button type="button" class="btn-cancelar" @click="editando = false">Cancelar</button>
      </div>
    </form>
  </div>
</template>

<script>
import entregadorService from '@/services/entregadorService'
import IconEntregador from '@/components/entregador/IconEntregador.vue'

export default {
  name: 'EntregadorPerfil',
  components: { IconEntregador },
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
      } catch (err) {
        this.$toast.error('Erro ao carregar perfil')
      }
      finally { this.carregando = false }
    },
    formatCPF(cpf) {
      if (!cpf) return ''
      return cpf.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4')
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
    iniciarEdicao() {
      this.form = {
        nome: this.entregador?.nome || '',
        telefone: this.entregador?.telefone || '',
        modeloVeiculo: this.entregador?.modeloVeiculo || '',
        placaVeiculo: this.entregador?.placaVeiculo || '',
        chavePix: this.entregador?.chavePix || '',
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
        this.$toast.success('Perfil atualizado com sucesso!')
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
.page-title {
  font-size: 18px;
  color: var(--cf-ntr-800);
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ─── Skeleton ─── */
.skeleton-perfil {
  background: var(--cf-surface);
  border-radius: 14px;
  padding: 32px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.skeleton-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(90deg, var(--cf-ntr-100) 25%, var(--cf-ntr-200) 50%, var(--cf-ntr-100) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
.skeleton-line {
  height: 12px;
  background: linear-gradient(90deg, var(--cf-ntr-100) 25%, var(--cf-ntr-200) 50%, var(--cf-ntr-100) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 6px;
}
.skeleton-line.w-40 { width: 40%; }
.skeleton-line.w-50 { width: 50%; }
.skeleton-line.w-60 { width: 60%; }
.skeleton-line.w-80 { width: 80%; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.perfil-card {
  background: var(--cf-surface);
  border-radius: 14px;
  padding: 24px 16px;
  text-align: center;
  margin-bottom: 16px;
}
.avatar { margin-bottom: 12px; }
.avatar-circle {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--cf-ora-600);
  color: white;
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}
.status-badge {
  display: inline-block;
  padding: 4px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 16px;
}
.status-badge.ATIVO { background: var(--cf-grn-50); color: var(--cf-grn-700); }
.status-badge.PENDENTE { background: var(--cf-amb-50); color: var(--cf-amb-600); }
.status-badge.BLOQUEADO { background: var(--cf-red-50); color: var(--cf-red-600); }
.info-group { text-align: left; }
.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--cf-ntr-100);
  gap: 8px;
}
.info-item:last-child { border: none; }
.info-label {
  font-size: 13px;
  color: var(--cf-ntr-400);
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.info-value {
  font-size: 13px;
  color: var(--cf-ntr-700);
  font-weight: 500;
  text-align: right;
}
.btn-editar {
  width: 100%;
  padding: 14px;
  border: 2px dashed var(--cf-ora-300);
  border-radius: var(--cf-radius-lg);
  background: white;
  color: var(--cf-ora-600);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all var(--cf-transition);
  font-family: var(--cf-font);
}
.btn-editar:hover {
  background: var(--cf-ora-50);
  border-color: var(--cf-ora-600);
  border-style: solid;
}
.btn-editar:focus-visible {
  outline: 2px solid var(--cf-ora-500);
  outline-offset: 2px;
}
.edit-form {
  background: var(--cf-surface);
  border-radius: 14px;
  padding: 16px;
}
.form-group { margin-bottom: 14px; }
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
.error-msg {
  color: var(--cf-red-600);
  font-size: 13px;
  margin: 10px 0;
  background: var(--cf-red-50);
  padding: 8px 12px;
  border-radius: var(--cf-radius-md);
}
.edit-actions { display: flex; gap: 8px; }
.btn-salvar {
  flex: 1;
  padding: 12px;
  background: var(--cf-ora-600);
  color: white;
  border: none;
  border-radius: var(--cf-radius-lg);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: background var(--cf-transition);
  font-family: var(--cf-font);
}
.btn-salvar:hover:not(:disabled) {
  background: var(--cf-ora-700);
}
.btn-salvar:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-salvar:focus-visible {
  outline: 2px solid var(--cf-ora-500);
  outline-offset: 2px;
}
.btn-cancelar {
  flex: 1;
  padding: 12px;
  background: white;
  color: var(--cf-ntr-500);
  border: 2px solid var(--cf-ntr-200);
  border-radius: var(--cf-radius-lg);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--cf-transition);
  font-family: var(--cf-font);
}
.btn-cancelar:hover {
  background: var(--cf-ntr-50);
  border-color: var(--cf-ntr-300);
}
.btn-cancelar:focus-visible {
  outline: 2px solid var(--cf-ntr-400);
  outline-offset: 2px;
}
</style>
