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
    // 禁用 gzip 压缩，否则 SSE 流式响应会被缓冲直到结束才一次性返回
    compress: false,
    proxy: {
      "/api": {
        target: process.env.VUE_APP_BASE_API,
        ws: false,
        changeOrigin: true,
        timeout: 600000,
        proxyTimeout: 600000,
        // 禁用代理缓冲，确保 SSE 流式即时推送
        onProxyRes: function(proxyRes) {
          proxyRes.headers['X-Accel-Buffering'] = 'no';
          proxyRes.headers['Cache-Control'] = 'no-cache';
        },
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
