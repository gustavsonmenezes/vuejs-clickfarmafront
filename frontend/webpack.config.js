const path = require('path');
const fs = require('fs');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader');
const CopyWebpackPlugin = require('copy-webpack-plugin');

function loadEnvVars(prefix = 'VUE_APP_') {
  const envFile = path.resolve(__dirname, '.env');
  if (!fs.existsSync(envFile)) return {};
  const vars = {};
  fs.readFileSync(envFile, 'utf-8').split('\n').forEach(line => {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) return;
    const eqIdx = trimmed.indexOf('=');
    if (eqIdx === -1) return;
    const key = trimmed.substring(0, eqIdx).trim();
    const value = trimmed.substring(eqIdx + 1).trim();
    if (key.startsWith(prefix)) {
      vars[`process.env.${key}`] = JSON.stringify(value);
    }
  });
  return vars;
}

module.exports = {
  mode: 'development',
  entry: './src/main.js',
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: 'bundle.js',
    clean: true,
    publicPath: '/'
  },
  devServer: {
    static: './public',
    host: '0.0.0.0',
    allowedHosts: 'all',
    port: 8081,
    hot: true,
    open: true,
    historyApiFallback: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        logLevel: 'debug'
      }
    }
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader'
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader'
        }
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      }
    ]
  },
  plugins: [
    new webpack.DefinePlugin({
      ...loadEnvVars(),
      'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'development'),
    }),
    new VueLoaderPlugin(),
    new HtmlWebpackPlugin({
      template: './public/index.html',
      templateParameters: {
        BASE_URL: '/'
      }
    }),
    new CopyWebpackPlugin({
      patterns: [
        {
          from: 'public',
          to: '',
          globOptions: {
            ignore: ['**/index.html']
          }
        }
      ]
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
      'vue': 'vue/dist/vue.esm-bundler.js'
    },
    extensions: ['.js', '.vue', '.json']
  }
};
