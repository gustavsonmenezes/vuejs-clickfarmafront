<template>
  <div class="admin-login container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="card">
          <div class="card-body">
            <h2 class="card-title text-center">Login Administrativo - ClickFarma</h2>
            <form @submit.prevent="login">
              <div class="form-group mb-3">
                <label for="email">Email</label>
                <input 
                  id="email" 
                  v-model="email" 
                  type="email" 
                  class="form-control" 
                  placeholder="admin@clickfarma.com" 
                  required 
                />
              </div>
              <div class="form-group mb-3">
                <label for="password">Senha</label>
                <input 
                  id="password" 
                  v-model="password" 
                  type="password" 
                  class="form-control" 
                  placeholder="senha123" 
                  required 
                />
              </div>
              <button 
                type="submit" 
                class="btn btn-primary w-100"
                :disabled="isLoading"
              >
                <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
                {{ isLoading ? 'Entrando...' : 'Entrar no Painel' }}
              </button>
            </form>
            <p v-if="error" class="alert alert-danger mt-3 text-center">{{ error }}</p>
            <div class="mt-3 p-3 bg-light rounded">
              <small class="text-muted">
                <strong>Credenciais para teste:</strong><br>
                Email: admin@clickfarma.com<br>
                Senha: senha123
              </small>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminLogin',
  data() {
    return {
      email: '',
      password: '',
      error: '',
      isLoading: false
    }
  },
  methods: {
    async login() {
      this.isLoading = true
      this.error = ''
      
      
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      
      if (this.email === 'admin@clickfarma.com' && this.password === 'senha123') {
        
        const mockUser = {
          id: 1,
          name: 'Administrador ClickFarma',
          email: 'admin@clickfarma.com',
          role: 'admin',
          avatar: null
        }
        
        const mockToken = 'mock-jwt-token-' + Date.now()
        
        
        localStorage.setItem('authToken', mockToken)
        localStorage.setItem('user', JSON.stringify(mockUser))
        
        
        this.$router.push('/admin')
      } else {
        this.error = 'Credenciais inválidas. Use: admin@clickfarma.com / senha123'
      }
      
      this.isLoading = false
    }
  },
  mounted() {
    
    localStorage.removeItem('authToken')
    localStorage.removeItem('user')
  }
}
</script>

<style scoped>
.card { 
  box-shadow: 0 4px 6px rgba(0,0,0,0.1); 
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
}
</style>