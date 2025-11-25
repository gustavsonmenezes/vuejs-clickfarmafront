<template>
  <div class="farmacias-proximas">
    <div class="card">
      <div class="card-header bg-primary text-white">
        <h3 class="card-title mb-0">
          <i class="fas fa-map-marker-alt me-2"></i>
          Farmácias Próximas
        </h3>
      </div>
      
      <div class="card-body">
        <button 
          class="btn btn-outline-primary w-100"
          @click="buscarFarmaciasProximas"
          :disabled="carregando"
        >
          <i class="fas fa-search me-2"></i>
          CLIQUE AQUI
        </button>

        <div v-if="carregando" class="mt-3 text-center">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Carregando...</span>
          </div>
          <p class="mt-2 text-muted">Buscando farmácias próximas...</p>
        </div>

        <div v-if="erro" class="alert alert-warning mt-3 mb-0">
          <i class="fas fa-exclamation-triangle me-2"></i>
          {{ erro }}
        </div>

        <div v-if="farmacias.length > 0" class="mt-4">
          <h5 class="mb-3">Farmácias encontradas:</h5>
          <div class="list-group">
            <div 
              v-for="farmacia in farmacias" 
              :key="farmacia.id"
              class="list-group-item list-group-item-action"
            >
              <div class="d-flex justify-content-between align-items-start">
                <div class="flex-grow-1">
                  <h6 class="mb-1 fw-bold">{{ farmacia.nome }}</h6>
                  <p class="mb-1 text-muted small">
                    <i class="fas fa-map-marker-alt me-1"></i>
                    {{ farmacia.endereco }}
                  </p>
                  <div class="d-flex align-items-center mt-2">
                    <span class="badge bg-primary me-2">
                      {{ farmacia.distancia }} km
                    </span>
                    <small class="text-muted">
                      <i class="fas fa-clock me-1"></i>
                      {{ farmacia.horario }}
                    </small>
                  </div>
                </div>
                <button 
                  class="btn btn-sm btn-outline-primary ms-3"
                  @click="verRota(farmacia)"
                >
                  Ver Rota
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FarmaciasProximas',
  data() {
    return {
      carregando: false,
      erro: '',
      farmacias: []
    }
  },
  methods: {
    async buscarFarmaciasProximas() {
      this.carregando = true
      this.erro = ''
      this.farmacias = []
      
      try {
        if (!navigator.geolocation) {
          throw new Error('Geolocalização não suportada pelo navegador')
        }

        // Simulação de dados - depois substitua por API real
        this.farmacias = await this.simularBuscaFarmacias()
      } catch (error) {
        this.erro = error.message || 'Erro ao buscar farmácias próximas'
      } finally {
        this.carregando = false
      }
    },

    simularBuscaFarmacias() {
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve([
            {
              id: 1,
              nome: 'Farmácia Central',
              endereco: 'Rua Principal, 123 - Centro',
              distancia: '0.8',
              horario: '24h'
            },
            {
              id: 2,
              nome: 'Drogaria Saúde',
              endereco: 'Av. Comercial, 456 - Jardim',
              distancia: '1.2',
              horario: '08h-22h'
            },
            {
              id: 3,
              nome: 'Farmácia Popular',
              endereco: 'Praça da Matriz, 789 - Centro',
              distancia: '1.5',
              horario: '07h-23h'
            }
          ])
        }, 1500)
      })
    },

    verRota(farmacia) {
      // Abre no Google Maps com o endereço
      const endereco = encodeURIComponent(farmacia.endereco)
      window.open(`https://www.google.com/maps/dir/?api=1&destination=${endereco}`, '_blank')
    }
  }
}
</script>

<style scoped>
.farmacias-proximas {
  min-height: 200px;
}
.list-group-item {
  border-left: none;
  border-right: none;
}
.list-group-item:first-child {
  border-top: none;
}
</style>