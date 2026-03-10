<template>
  <div class="password-reset-form">
    <form @submit.prevent="$emit('submit', formData)">
      <div class="mb-3">
        <label class="form-label">Nova Senha:</label>
        <input 
          type="password" 
          class="form-control" 
          v-model="formData.password"
          required
          minlength="6"
        >
      </div>
      
      <div class="mb-3">
        <label class="form-label">Confirmar Senha:</label>
        <input 
          type="password" 
          class="form-control" 
          v-model="formData.confirmPassword"
          required
        >
      </div>
      
      <button 
        type="submit" 
        class="btn btn-primary w-100"
        :disabled="loading"
      >
        <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
        {{ loading ? 'Redefinindo...' : 'Redefinir Senha' }}
      </button>
    </form>
  </div>
</template>

<script>
export default {
  name: 'PasswordResetForm',
  props: {
    loading: Boolean
  },
  data() {
    return {
      formData: {
        password: '',
        confirmPassword: ''
      }
    }
  },
  watch: {
    formData: {
      deep: true,
      handler(newVal) {
        this.$emit('update', newVal)
      }
    }
  }
}
</script>
