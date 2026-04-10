// Este arquivo foi intencionalmente limpo para evitar conflitos.
// A configuração de desenvolvimento está em webpack.config.js.
module.exports = {};
const { defineConfig } = require('@vue/cli-service')
const path = require('path')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    host: '0.0.0.0',
    port: 80,
    allowedHosts: 'all',
    proxy: {
      '/api': {
        target: 'http://backend:8080',
        changeOrigin: true,
        ws: true
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
