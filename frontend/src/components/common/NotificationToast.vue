<template>
  <div class="toast-container">
    <div 
      v-for="toast in toasts" 
      :key="toast.id"
      :class="['toast', 'show', `toast-${toast.type}`]"
      role="alert"
    >
      <div class="toast-header">
        <i :class="getIcon(toast.type)" class="me-2"></i>
        <strong class="me-auto">{{ getTitle(toast.type) }}</strong>
        <small class="text-muted">{{ formatTime(toast.timestamp) }}</small>
        <button 
          type="button" 
          class="btn-close" 
          @click="removeToast(toast.id)"
          aria-label="Close"
        ></button>
      </div>
      <div class="toast-body">
        {{ toast.message }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotificationToast',
  data() {
    return {
      toasts: []
    }
  },
  methods: {
    addToast(message, type = 'info', duration = 5000) {
      const toast = {
        id: Date.now() + Math.random(),
        message,
        type,
        timestamp: new Date(),
        duration
      }
      
      this.toasts.push(toast)
      
      if (duration > 0) {
        setTimeout(() => {
          this.removeToast(toast.id)
        }, duration)
      }
      
      return toast.id
    },
    removeToast(id) {
      const index = this.toasts.findIndex(toast => toast.id === id)
      if (index > -1) {
        this.toasts.splice(index, 1)
      }
    },
    clearAll() {
      this.toasts = []
    },
    getIcon(type) {
      const icons = {
        success: 'fas fa-check-circle text-success',
        error: 'fas fa-exclamation-circle text-danger',
        warning: 'fas fa-exclamation-triangle text-warning',
        info: 'fas fa-info-circle text-info'
      }
      return icons[type] || icons.info
    },
    getTitle(type) {
      const titles = {
        success: 'Sucesso',
        error: 'Erro',
        warning: 'Atenção',
        info: 'Informação'
      }
      return titles[type] || titles.info
    },
    formatTime(timestamp) {
      const now = new Date()
      const diff = Math.floor((now - timestamp) / 1000)
      
      if (diff < 60) return 'agora'
      if (diff < 3600) return `${Math.floor(diff / 60)}m atrás`
      return timestamp.toLocaleTimeString('pt-BR', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }
  }
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1060;
  max-width: 350px;
}

.toast {
  margin-bottom: 10px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: slideInRight 0.3s ease-out;
}

.toast-success {
  border-left: 4px solid #28a745;
}

.toast-error {
  border-left: 4px solid #dc3545;
}

.toast-warning {
  border-left: 4px solid #ffc107;
}

.toast-info {
  border-left: 4px solid #17a2b8;
}

.toast-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}

.toast-body {
  background-color: white;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Responsividade */
@media (max-width: 576px) {
  .toast-container {
    left: 20px;
    right: 20px;
    max-width: none;
  }
}
</style>