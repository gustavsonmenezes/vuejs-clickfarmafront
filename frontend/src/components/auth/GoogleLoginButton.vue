<template>
  <div class="google-login-wrapper">
    <div class="divider">
      <span class="divider-text">ou</span>
    </div>
    <div id="googleSignInDiv" class="google-btn-container"></div>
    <p v-if="error" class="text-danger mt-2 mb-0 small">{{ error }}</p>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'GoogleLoginButton',
  props: {
    clientId: {
      type: String,
      default: 'SEU_GOOGLE_CLIENT_ID.apps.googleusercontent.com'
    }
  },
  data() {
    return {
      error: '',
      loading: false
    }
  },
  methods: {
    ...mapActions(['loginWithGoogle']),

    async handleGoogleCredential(response) {
      if (this.loading) return
      this.loading = true
      this.error = ''

      try {
        const user = await this.loginWithGoogle(response.credential)
        const userName = user.name?.split(' ')[0] || ''
        alert(`Olá, ${userName}! Bem-vindo ao ClickFarma! 🎉`)
        this.$router.push('/')
      } catch (err) {
        this.error = err.message || 'Erro ao fazer login com Google'
      } finally {
        this.loading = false
      }
    },

    initGoogleButton() {
      if (typeof window.google === 'undefined' || !window.google.accounts) {
        setTimeout(() => this.initGoogleButton(), 500)
        return
      }

      window.google.accounts.id.initialize({
        client_id: this.clientId,
        callback: (resp) => this.handleGoogleCredential(resp),
        cancel_on_tap_outside: false
      })

      window.google.accounts.id.renderButton(
        document.getElementById('googleSignInDiv'),
        {
          theme: 'outline',
          size: 'large',
          text: 'signin_with',
          shape: 'rect',
          width: 300
        }
      )
    }
  },
  mounted() {
    this.initGoogleButton()
  }
}
</script>

<style scoped>
.google-login-wrapper {
  margin-top: 1.5rem;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: 1.25rem;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  border-top: 1px solid #e9ecef;
}

.divider-text {
  padding: 0 1rem;
  color: #6c757d;
  font-size: 0.875rem;
}

.google-btn-container {
  display: flex;
  justify-content: center;
}
</style>
