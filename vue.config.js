const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    host: '0.0.0.0',
    port: 8081,
    allowedHosts: 'all'
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': require('path').resolve(__dirname, 'src')
      }
    },
    plugins: [
      new (require('webpack').DefinePlugin)({
        __VUE_OPTIONS_API__: JSON.stringify(true),
        __VUE_PROD_DEVTOOLS__: JSON.stringify(false),
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: JSON.stringify(false)
      })
    ]
  },
  // Configuração do Jest para Vue CLI
  pluginOptions: {
    jest: {
      setupFiles: ['<rootDir>/src/tests/setup.js']
    }
  }
})