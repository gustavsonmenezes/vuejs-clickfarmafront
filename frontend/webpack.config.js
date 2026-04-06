const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader');
const webpack = require('webpack');

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
    host: '0.0.0.0',
    port: 8080,
    hot: true,
    open: false,
    historyApiFallback: true,
    allowedHosts: 'all',
    static: {
      directory: path.join(__dirname, 'public'),
    },
    client: {
      webSocketURL: 'auto://0.0.0.0:0/ws',
    }
    historyApiFallback: true
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
      },
      {
        test: /\.(png|jpe?g|gif|svg)$/i,
        type: 'asset/resource'
      }
    ]
  },
  plugins: [
    new VueLoaderPlugin(),
    new HtmlWebpackPlugin({
      template: './public/index.html',
      templateParameters: {
        BASE_URL: '/'
      }
    }),
    new webpack.DefinePlugin({
      'process.env.VUE_APP_API_URL': JSON.stringify(process.env.VUE_APP_API_URL),
      'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV)
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
