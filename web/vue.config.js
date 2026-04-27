const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  css: {
    loaderOptions: {
      scss: {
        additionalData: `@import "~@/styles/variables.scss";`
      }
    }
  },
  devServer: {
    port: 8080,
    open: false,
    https: false,
    proxy: {
      "/api": {
        target: process.env.VUE_APP_BASE_API,
        ws: false,
        changeOrigin: true,
        router: function(req) {
          delete req.headers.origin
        },
        pathRewrite: {
          "^/api": ""
        }
      }
    }
  },
})
