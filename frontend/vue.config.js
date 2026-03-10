const { defineConfig } = require('@vue/cli-service')
const path = require('path')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    host: '0.0.0.0',
    port: 8082,
    allowedHosts: 'all',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // URL do backend
        changeOrigin: true,
        pathRewrite: { '^/api': '/api' }  // Mantém /api
      }
    }
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    }
  }
})